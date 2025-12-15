package com.ctonew.taskmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.ctonew.taskmanagement.core.designsystem.theme.TaskManagementTheme
import com.ctonew.taskmanagement.ui.TaskManagementRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      TaskManagementTheme {
        TaskManagementRoot()
      }
    }
  }
}
