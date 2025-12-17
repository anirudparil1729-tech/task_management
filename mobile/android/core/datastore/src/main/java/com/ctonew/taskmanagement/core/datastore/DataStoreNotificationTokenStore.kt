package com.ctonew.taskmanagement.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationTokenDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "notification_token",
)

@Singleton
class DataStoreNotificationTokenStore @Inject constructor(
  @ApplicationContext private val context: Context,
) : NotificationTokenStore {
  private val dataStore = context.notificationTokenDataStore

  private val fcmTokenKey = stringPreferencesKey("fcm_token")

  override val fcmToken: Flow<String?> =
    dataStore.data.map { it[fcmTokenKey] }

  override suspend fun setFcmToken(token: String) {
    dataStore.edit { it[fcmTokenKey] = token }
  }

  override suspend fun clearFcmToken() {
    dataStore.edit { it.remove(fcmTokenKey) }
  }
}
