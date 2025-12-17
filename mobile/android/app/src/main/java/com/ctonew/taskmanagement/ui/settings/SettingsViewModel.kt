package com.ctonew.taskmanagement.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.common.session.SessionStore
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.datastore.SyncStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUIState(
  val pendingChangesCount: Int = 0,
  val lastSyncedAt: Long? = null,
  val isSyncing: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val sessionStore: SessionStore,
  private val outboxDao: OutboxDao,
  private val syncStateStore: SyncStateStore,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SettingsUIState())
  val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      syncStateStore.lastTasksSyncMillis.collect { lastSync ->
        _uiState.value = _uiState.value.copy(
          lastSyncedAt = lastSync.takeIf { it > 0 },
        )
      }
    }

    viewModelScope.launch {
      while (true) {
        val pending = outboxDao.getPending()
        _uiState.value = _uiState.value.copy(pendingChangesCount = pending.size)
        kotlinx.coroutines.delay(500)
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      sessionStore.clear()
    }
  }
}
