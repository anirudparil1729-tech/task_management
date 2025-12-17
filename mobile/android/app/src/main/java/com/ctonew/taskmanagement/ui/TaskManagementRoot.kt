package com.ctonew.taskmanagement.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ctonew.taskmanagement.ui.auth.LoginRoute
import com.ctonew.taskmanagement.ui.main.TaskManagementApp
import com.ctonew.taskmanagement.ui.splash.SplashScreen

@Composable
fun TaskManagementRoot(
  viewModel: MainViewModel = hiltViewModel(),
) {
  val sessionUiState by viewModel.sessionUiState.collectAsStateWithLifecycle()

  when (sessionUiState) {
    SessionUiState.Loading -> SplashScreen()
    SessionUiState.LoggedOut -> LoginRoute()
    SessionUiState.LoggedIn -> TaskManagementApp()
  }
}
