package com.ctonew.taskmanagement.core.datastore

import kotlinx.coroutines.flow.Flow

interface NotificationTokenStore {
  val fcmToken: Flow<String?>
  suspend fun setFcmToken(token: String)
  suspend fun clearFcmToken()
}
