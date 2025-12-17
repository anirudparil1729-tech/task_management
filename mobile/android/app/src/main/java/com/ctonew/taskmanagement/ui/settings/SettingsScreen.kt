package com.ctonew.taskmanagement.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ctonew.taskmanagement.R

@Composable
fun SettingsScreen(
  onLogout: () -> Unit,
  onOpenTaskDetail: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(WindowInsets.safeDrawing.asPaddingValues())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(id = R.string.settings),
      style = MaterialTheme.typography.displayMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Text(
      text = stringResource(id = R.string.settings_description),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onBackground,
    )

    OutlinedButton(onClick = onOpenTaskDetail) {
      Text(text = stringResource(id = R.string.open_task_detail_sheet))
    }

    Button(onClick = onLogout) {
      Text(text = stringResource(id = R.string.log_out))
    }
  }
}
