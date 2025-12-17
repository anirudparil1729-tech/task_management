package com.ctonew.taskmanagement.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ctonew.taskmanagement.R
import java.time.Instant
import java.time.ZoneId

@Composable
fun SettingsScreen(
  uiState: SettingsUIState,
  onLogout: () -> Unit,
  onOpenTaskDetail: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(WindowInsets.safeDrawing.asPaddingValues())
      .padding(horizontal = 20.dp, vertical = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(id = R.string.settings),
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Sync Status",
          style = MaterialTheme.typography.titleMedium,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Pending Changes",
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            text = uiState.pendingChangesCount.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        if (uiState.lastSyncedAt != null) {
          Divider()
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Last Synced",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              text = formatTime(uiState.lastSyncedAt),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    OutlinedButton(
      onClick = onOpenTaskDetail,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = stringResource(id = R.string.open_task_detail_sheet))
    }

    Button(
      onClick = onLogout,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = stringResource(id = R.string.log_out))
    }
  }
}

private fun formatTime(millis: Long): String {
  val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
  return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a"))
}
