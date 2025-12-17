package com.ctonew.taskmanagement.core.database.repository

import com.ctonew.taskmanagement.core.database.dao.CategoryDao
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import com.ctonew.taskmanagement.core.network.dto.CategoryUpdateDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
  val categories: Flow<List<CategoryEntity>>

  suspend fun createCategory(
    name: String,
    color: String = "#3B82F6",
    icon: String? = null,
    isDefault: Boolean = false,
  ): String

  suspend fun updateCategory(
    localId: String,
    update: CategoryUpdateDto,
  )

  suspend fun deleteCategory(localId: String)
}

@Singleton
class DefaultCategoriesRepository @Inject constructor(
  private val categoryDao: CategoryDao,
  private val outboxDao: OutboxDao,
  private val moshi: com.squareup.moshi.Moshi,
  private val syncEngine: SyncEngine,
) : CategoriesRepository {
  override val categories: Flow<List<CategoryEntity>> = categoryDao.observeCategories()

  override suspend fun createCategory(
    name: String,
    color: String,
    icon: String?,
    isDefault: Boolean,
  ): String {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    val entity = CategoryEntity(
      localId = localId,
      remoteId = null,
      name = name,
      color = color,
      icon = icon,
      isDefault = isDefault,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    categoryDao.upsert(entity)

    val payload = moshi
      .adapter(com.ctonew.taskmanagement.core.network.dto.CategoryCreateDto::class.java)
      .toJson(entity.toCreateDto())

    outboxDao.insert(
      OutboxEntity(
        entityType = OutboxEntityType.CATEGORY,
        operation = OutboxOperation.CREATE,
        localId = localId,
        payloadJson = payload,
        createdAtMillis = now,
      ),
    )

    syncEngine.requestSyncNow()
    return localId
  }

  override suspend fun updateCategory(localId: String, update: CategoryUpdateDto) {
    val existing = categoryDao.getByLocalId(localId) ?: return
    val now = Instant.now().toEpochMilli()

    val updated = existing.copy(
      name = update.name ?: existing.name,
      color = update.color ?: existing.color,
      icon = update.icon ?: existing.icon,
      isDefault = update.isDefault ?: existing.isDefault,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    categoryDao.upsert(updated)

    val existingCreate = outboxDao.findFirstForEntityAndOperation(
      entityType = OutboxEntityType.CATEGORY,
      localId = localId,
      operation = OutboxOperation.CREATE,
    )

    if (existingCreate != null) {
      val adapter = moshi
        .adapter(com.ctonew.taskmanagement.core.network.dto.CategoryCreateDto::class.java)
      val current = existingCreate.payloadJson?.let(adapter::fromJson)

      val merged = (current ?: updated.toCreateDto()).copy(
        name = update.name ?: (current?.name ?: updated.name),
        color = update.color ?: current?.color ?: updated.color,
        icon = update.icon ?: current?.icon,
        isDefault = update.isDefault ?: current?.isDefault ?: updated.isDefault,
      )

      outboxDao.update(existingCreate.copy(payloadJson = adapter.toJson(merged)))
    } else {
      val remoteId = existing.remoteId ?: return
      val updateJson = moshi.adapter(CategoryUpdateDto::class.java).toJson(update)

      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.CATEGORY,
          operation = OutboxOperation.UPDATE,
          localId = localId,
          remoteId = remoteId,
          payloadJson = updateJson,
          createdAtMillis = now,
        ),
      )
    }

    syncEngine.requestSyncNow()
  }

  override suspend fun deleteCategory(localId: String) {
    val existing = categoryDao.getByLocalId(localId) ?: return
    categoryDao.deleteByLocalId(localId)

    outboxDao.deleteForEntity(OutboxEntityType.CATEGORY, localId)

    val now = Instant.now().toEpochMilli()
    val remoteId = existing.remoteId

    if (remoteId != null) {
      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.CATEGORY,
          operation = OutboxOperation.DELETE,
          localId = localId,
          remoteId = remoteId,
          createdAtMillis = now,
        ),
      )
    }

    syncEngine.requestSyncNow()
  }
}

private fun CategoryEntity.toCreateDto() =
  com.ctonew.taskmanagement.core.database.mapper.toCreateDto(this)
