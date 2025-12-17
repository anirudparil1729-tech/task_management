package com.ctonew.taskmanagement.core.datastore

import kotlinx.coroutines.flow.Flow

interface SyncStateStore {
  val lastTasksSyncMillis: Flow<Long>
  val lastCategoriesSyncMillis: Flow<Long>
  val lastTimeBlocksSyncMillis: Flow<Long>

  suspend fun setLastTasksSyncMillis(value: Long)
  suspend fun setLastCategoriesSyncMillis(value: Long)
  suspend fun setLastTimeBlocksSyncMillis(value: Long)

  suspend fun clear()
}
