package com.ctonew.taskmanagement.core.database.repository

import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import com.ctonew.taskmanagement.core.network.dto.TaskUpdateDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface TasksRepository {
  val tasks: Flow<List<TaskEntity>>

  suspend fun createTask(
    title: String,
    description: String? = null,
  ): String

  suspend fun updateTask(
    localId: String,
    update: TaskUpdateDto,
  )

  suspend fun markTaskCompleted(localId: String, completed: Boolean)

  suspend fun deleteTask(localId: String)
}

@Singleton
class DefaultTasksRepository @Inject constructor(
  private val taskDao: TaskDao,
  private val outboxDao: OutboxDao,
  private val moshi: com.squareup.moshi.Moshi,
  private val syncEngine: SyncEngine,
) : TasksRepository {
  override val tasks: Flow<List<TaskEntity>> = taskDao.observeTasks()

  override suspend fun createTask(title: String, description: String?): String {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    val entity = TaskEntity(
      localId = localId,
      remoteId = null,
      title = title,
      description = description,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    taskDao.upsert(entity)

    val payload = moshi
      .adapter(com.ctonew.taskmanagement.core.network.dto.TaskCreateDto::class.java)
      .toJson(entity.toCreateDto())

    outboxDao.insert(
      OutboxEntity(
        entityType = OutboxEntityType.TASK,
        operation = OutboxOperation.CREATE,
        localId = localId,
        remoteId = null,
        payloadJson = payload,
        createdAtMillis = now,
      ),
    )

    syncEngine.requestSyncNow()
    return localId
  }

  override suspend fun updateTask(localId: String, update: TaskUpdateDto) {
    val existing = taskDao.getByLocalId(localId) ?: return
    val now = Instant.now().toEpochMilli()

    val updated = existing.copy(
      title = update.title ?: existing.title,
      description = update.description ?: existing.description,
      notes = update.notes ?: existing.notes,
      dueDateMillis = update.dueDate?.toEpochMilli() ?: existing.dueDateMillis,
      priority = update.priority ?: existing.priority,
      recurrenceRule = update.recurrenceRule ?: existing.recurrenceRule,
      categoryRemoteId = update.categoryId ?: existing.categoryRemoteId,
      reminderTimeMillis =
        update.reminderTime?.toEpochMilli() ?: existing.reminderTimeMillis,
      isCompleted = update.isCompleted ?: existing.isCompleted,
      modifiedAtMillis = now,
      updatedAtMillis = now,
      completedAtMillis = when (update.isCompleted) {
        true -> now
        false -> null
        null -> existing.completedAtMillis
      },
    )

    taskDao.upsert(updated)

    val existingCreate = outboxDao.findFirstForEntityAndOperation(
      entityType = OutboxEntityType.TASK,
      localId = localId,
      operation = OutboxOperation.CREATE,
    )

    if (existingCreate != null) {
      val createAdapter = moshi
        .adapter(com.ctonew.taskmanagement.core.network.dto.TaskCreateDto::class.java)
      val currentCreate = existingCreate.payloadJson?.let(createAdapter::fromJson)

      val mergedCreate = (currentCreate ?: updated.toCreateDto()).copy(
        title = update.title ?: (currentCreate?.title ?: updated.title),
        description = update.description ?: currentCreate?.description,
        notes = update.notes ?: currentCreate?.notes,
        dueDate = update.dueDate ?: currentCreate?.dueDate,
        priority = update.priority ?: currentCreate?.priority ?: updated.priority,
        recurrenceRule = update.recurrenceRule ?: currentCreate?.recurrenceRule,
        categoryId = update.categoryId ?: currentCreate?.categoryId,
        reminderTime = update.reminderTime ?: currentCreate?.reminderTime,
      )

      outboxDao.update(
        existingCreate.copy(
          payloadJson = createAdapter.toJson(mergedCreate),
        ),
      )
    } else {
      val remoteId = existing.remoteId ?: return
      val updateJson = moshi.adapter(TaskUpdateDto::class.java).toJson(update)
      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.TASK,
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

  override suspend fun markTaskCompleted(localId: String, completed: Boolean) {
    updateTask(
      localId = localId,
      update = TaskUpdateDto(isCompleted = completed),
    )
  }

  override suspend fun deleteTask(localId: String) {
    val existing = taskDao.getByLocalId(localId) ?: return
    taskDao.deleteByLocalId(localId)

    outboxDao.deleteForEntity(OutboxEntityType.TASK, localId)

    val now = Instant.now().toEpochMilli()
    val remoteId = existing.remoteId

    if (remoteId != null) {
      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.TASK,
          operation = OutboxOperation.DELETE,
          localId = localId,
          remoteId = remoteId,
          payloadJson = null,
          createdAtMillis = now,
        ),
      )
    }

    syncEngine.requestSyncNow()
  }
}

private fun TaskEntity.toCreateDto() =
  com.ctonew.taskmanagement.core.database.mapper.toCreateDto(this)
