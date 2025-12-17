package com.ctonew.taskmanagement.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "task_management_sync_state",
)

@Singleton
class DataStoreSyncStateStore @Inject constructor(
  @ApplicationContext private val context: Context,
) : SyncStateStore {
  private val dataStore = context.syncDataStore

  private val lastTasksSyncKey = longPreferencesKey("last_tasks_sync_millis")
  private val lastCategoriesSyncKey = longPreferencesKey("last_categories_sync_millis")
  private val lastTimeBlocksSyncKey = longPreferencesKey("last_time_blocks_sync_millis")

  override val lastTasksSyncMillis: Flow<Long> =
    dataStore.data.map { it[lastTasksSyncKey] ?: 0L }

  override val lastCategoriesSyncMillis: Flow<Long> =
    dataStore.data.map { it[lastCategoriesSyncKey] ?: 0L }

  override val lastTimeBlocksSyncMillis: Flow<Long> =
    dataStore.data.map { it[lastTimeBlocksSyncKey] ?: 0L }

  override suspend fun setLastTasksSyncMillis(value: Long) {
    dataStore.edit { it[lastTasksSyncKey] = value }
  }

  override suspend fun setLastCategoriesSyncMillis(value: Long) {
    dataStore.edit { it[lastCategoriesSyncKey] = value }
  }

  override suspend fun setLastTimeBlocksSyncMillis(value: Long) {
    dataStore.edit { it[lastTimeBlocksSyncKey] = value }
  }

  override suspend fun clear() {
    dataStore.edit {
      it.remove(lastTasksSyncKey)
      it.remove(lastCategoriesSyncKey)
      it.remove(lastTimeBlocksSyncKey)
    }
  }
}
