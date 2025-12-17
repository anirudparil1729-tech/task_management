package com.ctonew.taskmanagement.core.database.sync

import com.ctonew.taskmanagement.core.database.dao.CategoryDao
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.dao.TimeBlockDao
import com.ctonew.taskmanagement.core.database.mapper.toEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.datastore.SyncStateStore
import com.ctonew.taskmanagement.core.network.CategoriesApi
import com.ctonew.taskmanagement.core.network.NetworkResult
import com.ctonew.taskmanagement.core.network.SyncApi
import com.ctonew.taskmanagement.core.network.TasksApi
import com.ctonew.taskmanagement.core.network.TimeBlocksApi
import com.ctonew.taskmanagement.core.network.safeApiCall
import com.ctonew.taskmanagement.core.network.safeEmptyApiCall
import com.ctonew.taskmanagement.core.network.dto.CategoryCreateDto
import com.ctonew.taskmanagement.core.network.dto.CategoryUpdateDto
import com.ctonew.taskmanagement.core.network.dto.TaskCreateDto
import com.ctonew.taskmanagement.core.network.dto.TaskUpdateDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockCreateDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockUpdateDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class SyncProcessor @Inject constructor(
  private val tasksApi: TasksApi,
  private val categoriesApi: CategoriesApi,
  private val timeBlocksApi: TimeBlocksApi,
  private val syncApi: SyncApi,
  private val taskDao: TaskDao,
  private val categoryDao: CategoryDao,
  private val timeBlockDao: TimeBlockDao,
  private val outboxDao: OutboxDao,
  private val syncStateStore: SyncStateStore,
  private val moshi: com.squareup.moshi.Moshi,
) {
  suspend fun syncOnce(): SyncOutcome {
    val pushResult = pushOutbox()
    if (pushResult != SyncOutcome.Success) return pushResult

    return pullChanges()
  }

  private suspend fun pushOutbox(): SyncOutcome {
    val pending = outboxDao.getPending()

    for (item in pending) {
      val outcome = when (item.entityType) {
        OutboxEntityType.TASK -> pushTaskMutation(item)
        OutboxEntityType.CATEGORY -> pushCategoryMutation(item)
        OutboxEntityType.TIME_BLOCK -> pushTimeBlockMutation(item)
        else -> SyncOutcome.Success
      }

      if (outcome != SyncOutcome.Success) return outcome
    }

    return SyncOutcome.Success
  }

  private suspend fun pushTaskMutation(item: OutboxEntity): SyncOutcome {
    return when (item.operation) {
      OutboxOperation.CREATE -> {
        val adapter = moshi.adapter(TaskCreateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: return SyncOutcome.Failure

        when (val result = safeApiCall { tasksApi.createTask(payload) }) {
          is NetworkResult.Success -> {
            taskDao.upsert(result.data.toEntity(existingLocalId = item.localId))
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.UPDATE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Failure
        val adapter = moshi.adapter(TaskUpdateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: TaskUpdateDto()

        when (val result = safeApiCall { tasksApi.updateTask(remoteId, payload) }) {
          is NetworkResult.Success -> {
            val local = taskDao.getByLocalId(item.localId)
            if (local != null) {
              taskDao.upsert(result.data.toEntity(existingLocalId = local.localId))
            }
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.DELETE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Success
        when (val result = safeEmptyApiCall { tasksApi.deleteTask(remoteId) }) {
          is NetworkResult.Success -> {
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }
    }
  }

  private suspend fun pushCategoryMutation(item: OutboxEntity): SyncOutcome {
    return when (item.operation) {
      OutboxOperation.CREATE -> {
        val adapter = moshi.adapter(CategoryCreateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: return SyncOutcome.Failure

        when (val result = safeApiCall { categoriesApi.createCategory(payload) }) {
          is NetworkResult.Success -> {
            categoryDao.upsert(result.data.toEntity(existingLocalId = item.localId))
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.UPDATE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Failure
        val adapter = moshi.adapter(CategoryUpdateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: CategoryUpdateDto()

        when (val result = safeApiCall { categoriesApi.updateCategory(remoteId, payload) }) {
          is NetworkResult.Success -> {
            val local = categoryDao.getByLocalId(item.localId)
            if (local != null) {
              categoryDao.upsert(result.data.toEntity(existingLocalId = local.localId))
            }
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.DELETE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Success
        when (val result = safeEmptyApiCall { categoriesApi.deleteCategory(remoteId) }) {
          is NetworkResult.Success -> {
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }
    }
  }

  private suspend fun pushTimeBlockMutation(item: OutboxEntity): SyncOutcome {
    return when (item.operation) {
      OutboxOperation.CREATE -> {
        val adapter = moshi.adapter(TimeBlockCreateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: return SyncOutcome.Failure

        when (val result = safeApiCall { timeBlocksApi.createTimeBlock(payload) }) {
          is NetworkResult.Success -> {
            timeBlockDao.upsert(result.data.toEntity(existingLocalId = item.localId))
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.UPDATE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Failure
        val adapter = moshi.adapter(TimeBlockUpdateDto::class.java)
        val payload = item.payloadJson?.let(adapter::fromJson) ?: TimeBlockUpdateDto()

        when (val result = safeApiCall { timeBlocksApi.updateTimeBlock(remoteId, payload) }) {
          is NetworkResult.Success -> {
            val local = timeBlockDao.getByLocalId(item.localId)
            if (local != null) {
              timeBlockDao.upsert(result.data.toEntity(existingLocalId = local.localId))
            }
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }

      OutboxOperation.DELETE -> {
        val remoteId = item.remoteId ?: return SyncOutcome.Success
        when (val result = safeEmptyApiCall { timeBlocksApi.deleteTimeBlock(remoteId) }) {
          is NetworkResult.Success -> {
            outboxDao.deleteById(item.id)
            SyncOutcome.Success
          }

          is NetworkResult.NetworkError -> SyncOutcome.Retry
          else -> SyncOutcome.Failure
        }
      }
    }
  }

  private suspend fun pullChanges(): SyncOutcome {
    val tasksResult = pullTasks()
    if (tasksResult != SyncOutcome.Success) return tasksResult

    val categoriesResult = pullCategories()
    if (categoriesResult != SyncOutcome.Success) return categoriesResult

    val timeBlocksResult = pullTimeBlocks()
    if (timeBlocksResult != SyncOutcome.Success) return timeBlocksResult

    return SyncOutcome.Success
  }

  private suspend fun pullTasks(): SyncOutcome {
    val lastSync = syncStateStore.lastTasksSyncMillis.first()
    val modifiedSince = lastSync.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).toString() }

    val remoteTasks = when (val result = safeApiCall { syncApi.syncTasks(modifiedSince) }) {
      is NetworkResult.Success -> result.data
      is NetworkResult.NetworkError -> return SyncOutcome.Retry
      else -> return SyncOutcome.Failure
    }

    var maxUpdatedAt: Instant? = null

    for (remote in remoteTasks) {
      maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)

      val existing = taskDao.getByRemoteId(remote.id)

      if (existing != null) {
        if (existing.modifiedAtMillis > remote.updatedAt.toEpochMilli()) {
          continue
        }

        val pending = outboxDao.countPendingForEntity(OutboxEntityType.TASK, existing.localId)
        if (pending > 0) continue

        taskDao.upsert(remote.toEntity(existingLocalId = existing.localId))
      } else {
        taskDao.upsert(remote.toEntity())
      }
    }

    if (maxUpdatedAt != null) {
      syncStateStore.setLastTasksSyncMillis(maxUpdatedAt.toEpochMilli())
    }

    return SyncOutcome.Success
  }

  private suspend fun pullCategories(): SyncOutcome {
    val lastSync = syncStateStore.lastCategoriesSyncMillis.first()
    val modifiedSince = lastSync.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).toString() }

    val remoteCategories =
      when (val result = safeApiCall { syncApi.syncCategories(modifiedSince) }) {
        is NetworkResult.Success -> result.data
        is NetworkResult.NetworkError -> return SyncOutcome.Retry
        else -> return SyncOutcome.Failure
      }

    var maxUpdatedAt: Instant? = null

    for (remote in remoteCategories) {
      maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)

      val existing = categoryDao.getByRemoteId(remote.id)

      if (existing != null) {
        if (existing.modifiedAtMillis > remote.updatedAt.toEpochMilli()) {
          continue
        }

        val pending = outboxDao.countPendingForEntity(OutboxEntityType.CATEGORY, existing.localId)
        if (pending > 0) continue

        categoryDao.upsert(remote.toEntity(existingLocalId = existing.localId))
      } else {
        categoryDao.upsert(remote.toEntity())
      }
    }

    if (maxUpdatedAt != null) {
      syncStateStore.setLastCategoriesSyncMillis(maxUpdatedAt.toEpochMilli())
    }

    return SyncOutcome.Success
  }

  private suspend fun pullTimeBlocks(): SyncOutcome {
    val lastSync = syncStateStore.lastTimeBlocksSyncMillis.first()
    val modifiedSince = lastSync.takeIf { it > 0 }?.let { Instant.ofEpochMilli(it).toString() }

    val remoteTimeBlocks =
      when (val result = safeApiCall { syncApi.syncTimeBlocks(modifiedSince) }) {
        is NetworkResult.Success -> result.data
        is NetworkResult.NetworkError -> return SyncOutcome.Retry
        else -> return SyncOutcome.Failure
      }

    var maxUpdatedAt: Instant? = null

    for (remote in remoteTimeBlocks) {
      maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)

      val existing = timeBlockDao.getByRemoteId(remote.id)

      if (existing != null) {
        if (existing.modifiedAtMillis > remote.updatedAt.toEpochMilli()) {
          continue
        }

        val pending = outboxDao.countPendingForEntity(OutboxEntityType.TIME_BLOCK, existing.localId)
        if (pending > 0) continue

        timeBlockDao.upsert(remote.toEntity(existingLocalId = existing.localId))
      } else {
        timeBlockDao.upsert(remote.toEntity())
      }
    }

    if (maxUpdatedAt != null) {
      syncStateStore.setLastTimeBlocksSyncMillis(maxUpdatedAt.toEpochMilli())
    }

    return SyncOutcome.Success
  }

  private fun maxOf(a: Instant?, b: Instant): Instant {
    return if (a == null || b > a) b else a
  }
}

enum class SyncOutcome {
  Success,
  Retry,
  Failure,
}
