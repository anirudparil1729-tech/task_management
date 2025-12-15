package com.ctonew.taskmanagement.ui.settings

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsRoute(
  onOpenTaskDetail: () -> Unit,
  viewModel: SettingsViewModel = hiltViewModel(),
) {
  SettingsScreen(
    onLogout = viewModel::logout,
    onOpenTaskDetail = onOpenTaskDetail,
  )
}
