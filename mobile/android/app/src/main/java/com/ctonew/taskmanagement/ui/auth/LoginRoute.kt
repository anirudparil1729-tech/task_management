package com.ctonew.taskmanagement.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginRoute(
  viewModel: AuthViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LoginScreen(
    uiState = uiState,
    onPasswordChanged = viewModel::onPasswordChanged,
    onLogin = viewModel::login,
  )
}
