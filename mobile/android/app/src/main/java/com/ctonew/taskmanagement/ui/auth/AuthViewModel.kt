package com.ctonew.taskmanagement.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.R
import com.ctonew.taskmanagement.core.common.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val StaticPassword = "Tikur@12345"

data class LoginUiState(
  val password: String = "",
  @StringRes val errorMessageResId: Int? = null,
  val isLoggingIn: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
  private val sessionStore: SessionStore,
) : ViewModel() {
  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  fun onPasswordChanged(value: String) {
    _uiState.update { it.copy(password = value, errorMessageResId = null) }
  }

  fun login() {
    val password = _uiState.value.password

    if (password != StaticPassword) {
      _uiState.update { it.copy(errorMessageResId = R.string.invalid_password) }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoggingIn = true, errorMessageResId = null) }
      sessionStore.setLoggedIn(true)
      _uiState.update { it.copy(password = "", isLoggingIn = false) }
    }
  }
}
