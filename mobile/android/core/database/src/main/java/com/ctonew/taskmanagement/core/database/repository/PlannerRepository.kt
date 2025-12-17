package com.ctonew.taskmanagement.core.database.repository

import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.dao.TimeBlockDao
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import com.ctonew.taskmanagement.core.network.dto.TimeBlockUpdateDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface PlannerRepository {
  val timeBlocks: Flow<List<TimeBlockEntity>>

  suspend fun createTimeBlock(
    startTimeMillis: Long,
    endTimeMillis: Long,
    taskRemoteId: Int? = null,
    estimatedDurationMillis: Long? = null,
    actualDurationMillis: Long? = null,
    title: String? = null,
    description: String? = null,
  ): String

  suspend fun updateTimeBlock(
    localId: String,
    update: TimeBlockUpdateDto,
  )

  suspend fun deleteTimeBlock(localId: String)

  suspend fun getTimeBlocksForDate(startOfDayMillis: Long, endOfDayMillis: Long): List<TimeBlockEntity>

  suspend fun getTimeBlocksForWeek(startOfWeekMillis: Long, endOfWeekMillis: Long): List<TimeBlockEntity>
}

@Singleton
class DefaultPlannerRepository @Inject constructor(
  private val timeBlockDao: TimeBlockDao,
  private val outboxDao: OutboxDao,
  private val moshi: com.squareup.moshi.Moshi,
  private val syncEngine: SyncEngine,
) : PlannerRepository {
  override val timeBlocks: Flow<List<TimeBlockEntity>> = timeBlockDao.observeTimeBlocks()

  override suspend fun createTimeBlock(
    startTimeMillis: Long,
    endTimeMillis: Long,
    taskRemoteId: Int?,
    estimatedDurationMillis: Long?,
    actualDurationMillis: Long?,
    title: String?,
    description: String?,
  ): String {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    val entity = TimeBlockEntity(
      localId = localId,
      remoteId = null,
      taskRemoteId = taskRemoteId,
      startTimeMillis = startTimeMillis,
      endTimeMillis = endTimeMillis,
      estimatedDurationMillis = estimatedDurationMillis,
      actualDurationMillis = actualDurationMillis,
      title = title,
      description = description,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    timeBlockDao.upsert(entity)

    val payload = moshi
      .adapter(com.ctonew.taskmanagement.core.network.dto.TimeBlockCreateDto::class.java)
      .toJson(entity.toCreateDto())

    outboxDao.insert(
      OutboxEntity(
        entityType = OutboxEntityType.TIME_BLOCK,
        operation = OutboxOperation.CREATE,
        localId = localId,
        payloadJson = payload,
        createdAtMillis = now,
      ),
    )

    syncEngine.requestSyncNow()
    return localId
  }

  override suspend fun updateTimeBlock(localId: String, update: TimeBlockUpdateDto) {
    val existing = timeBlockDao.getByLocalId(localId) ?: return
    val now = Instant.now().toEpochMilli()

    val updated = existing.copy(
      taskRemoteId = update.taskId ?: existing.taskRemoteId,
      startTimeMillis = update.startTime?.toEpochMilli() ?: existing.startTimeMillis,
      endTimeMillis = update.endTime?.toEpochMilli() ?: existing.endTimeMillis,
      title = update.title ?: existing.title,
      description = update.description ?: existing.description,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    timeBlockDao.upsert(updated)

    val existingCreate = outboxDao.findFirstForEntityAndOperation(
      entityType = OutboxEntityType.TIME_BLOCK,
      localId = localId,
      operation = OutboxOperation.CREATE,
    )

    if (existingCreate != null) {
      val adapter = moshi
        .adapter(com.ctonew.taskmanagement.core.network.dto.TimeBlockCreateDto::class.java)
      val current = existingCreate.payloadJson?.let(adapter::fromJson)

      val merged = (current ?: updated.toCreateDto()).copy(
        taskId = update.taskId ?: current?.taskId,
        startTime =
          update.startTime
            ?: current?.startTime
            ?: Instant.ofEpochMilli(updated.startTimeMillis),
        endTime =
          update.endTime
            ?: current?.endTime
            ?: Instant.ofEpochMilli(updated.endTimeMillis),
        title = update.title ?: current?.title,
        description = update.description ?: current?.description,
      )

      outboxDao.update(existingCreate.copy(payloadJson = adapter.toJson(merged)))
    } else {
      val remoteId = existing.remoteId ?: return
      val updateJson = moshi.adapter(TimeBlockUpdateDto::class.java).toJson(update)

      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.TIME_BLOCK,
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

  override suspend fun deleteTimeBlock(localId: String) {
    val existing = timeBlockDao.getByLocalId(localId) ?: return
    timeBlockDao.deleteByLocalId(localId)

    outboxDao.deleteForEntity(OutboxEntityType.TIME_BLOCK, localId)

    val now = Instant.now().toEpochMilli()
    val remoteId = existing.remoteId

    if (remoteId != null) {
      outboxDao.insert(
        OutboxEntity(
          entityType = OutboxEntityType.TIME_BLOCK,
          operation = OutboxOperation.DELETE,
          localId = localId,
          remoteId = remoteId,
          createdAtMillis = now,
        ),
      )
    }

    syncEngine.requestSyncNow()
  }

  override suspend fun getTimeBlocksForDate(startOfDayMillis: Long, endOfDayMillis: Long): List<TimeBlockEntity> {
    return timeBlockDao.getTimeBlocksForDate(startOfDayMillis, endOfDayMillis)
  }

  override suspend fun getTimeBlocksForWeek(startOfWeekMillis: Long, endOfWeekMillis: Long): List<TimeBlockEntity> {
    return timeBlockDao.getTimeBlocksForWeek(startOfWeekMillis, endOfWeekMillis)
  }
}

private fun TimeBlockEntity.toCreateDto() =
  com.ctonew.taskmanagement.core.database.mapper.toCreateDto(this)
