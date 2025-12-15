package com.ctonew.taskmanagement.core.common.session

import kotlinx.coroutines.flow.Flow

interface SessionStore {
  val isLoggedIn: Flow<Boolean>

  suspend fun setLoggedIn(loggedIn: Boolean)

  suspend fun clear()
}
