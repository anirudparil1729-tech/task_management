package com.ctonew.taskmanagement.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
  onOpenTaskDetail: () -> Unit,
  viewModel: SettingsViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  SettingsScreen(
    uiState = uiState,
    onLogout = viewModel::logout,
    onOpenTaskDetail = onOpenTaskDetail,
  )
}
