package com.ctonew.taskmanagement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.common.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionUiState {
  data object Loading : SessionUiState

  data object LoggedOut : SessionUiState

  data object LoggedIn : SessionUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
  private val sessionStore: SessionStore,
) : ViewModel() {
  private val _sessionUiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
  val sessionUiState: StateFlow<SessionUiState> = _sessionUiState.asStateFlow()

  init {
    viewModelScope.launch {
      sessionStore.isLoggedIn.collect { loggedIn ->
        _sessionUiState.value = if (loggedIn) {
          SessionUiState.LoggedIn
        } else {
          SessionUiState.LoggedOut
        }
      }
    }
  }
}
