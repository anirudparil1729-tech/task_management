package com.ctonew.taskmanagement.core.common.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "task_management_session",
)

@Singleton
class DataStoreSessionStore @Inject constructor(
  @ApplicationContext private val context: Context,
) : SessionStore {
  private val dataStore = context.sessionDataStore

  private val isLoggedInKey = booleanPreferencesKey("is_logged_in")

  override val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
    prefs[isLoggedInKey] ?: false
  }

  override suspend fun setLoggedIn(loggedIn: Boolean) {
    dataStore.edit { prefs ->
      prefs[isLoggedInKey] = loggedIn
    }
  }

  override suspend fun clear() {
    dataStore.edit { prefs ->
      prefs.remove(isLoggedInKey)
    }
  }
}
