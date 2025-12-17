package com.ctonew.taskmanagement.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clock
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ctonew.taskmanagement.R
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DashboardRoute(
  onOpenTaskDetail: () -> Unit,
  viewModel: DashboardViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()

  DashboardScreen(
    uiState = uiState,
    onOpenTaskDetail = onOpenTaskDetail,
    onTaskToggle = { viewModel.toggleTaskCompletion(it) },
  )
}

@Composable
fun DashboardScreen(
  uiState: DashboardUIState,
  onOpenTaskDetail: () -> Unit,
  onTaskToggle: (TaskEntity) -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(WindowInsets.safeDrawing.asPaddingValues())
      .padding(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = stringResource(id = R.string.dashboard),
      style = MaterialTheme.typography.displaySmall,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          StatCard(
            label = "Total Tasks",
            value = uiState.tasks.size.toString(),
            modifier = Modifier.weight(1f),
          )
          StatCard(
            label = "Completed",
            value = uiState.completedCount.toString(),
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          StatCard(
            label = "Today",
            value = uiState.todayTasks.size.toString(),
            modifier = Modifier.weight(1f),
          )
          StatCard(
            label = "Overdue",
            value = uiState.overdueTasks.size.toString(),
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Completion Rate",
          style = MaterialTheme.typography.titleMedium,
        )
        LinearProgressIndicator(
          progress = uiState.completionPercentage / 100f,
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        )
        Text(
          text = "${uiState.completedCount} of ${uiState.tasks.size} tasks completed",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (uiState.overdueTasks.isNotEmpty()) {
      TaskSection(
        title = "Overdue",
        tasks = uiState.overdueTasks,
        onTaskToggle = onTaskToggle,
      )
    }

    if (uiState.todayTasks.isNotEmpty()) {
      TaskSection(
        title = "Due Today",
        tasks = uiState.todayTasks,
        onTaskToggle = onTaskToggle,
      )
    }

    if (uiState.upcomingTasks.isNotEmpty()) {
      TaskSection(
        title = "Upcoming",
        tasks = uiState.upcomingTasks.take(5),
        onTaskToggle = onTaskToggle,
      )
    }

    if (uiState.lastSyncedAt != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Last Synced",
            style = MaterialTheme.typography.titleSmall,
          )
          Text(
            text = formatTime(uiState.lastSyncedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.primary,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun TaskSection(
  title: String,
  tasks: List<TaskEntity>,
  onTaskToggle: (TaskEntity) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onBackground,
    )

    tasks.forEach { task ->
      TaskItem(
        task = task,
        onToggle = { onTaskToggle(task) },
      )
    }
  }
}

@Composable
private fun TaskItem(
  task: TaskEntity,
  onToggle: () -> Unit,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() },
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = task.isCompleted,
        onCheckedChange = { onToggle() },
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
          else MaterialTheme.colorScheme.onSurface,
        )
        if (task.dueDateMillis != null) {
          Text(
            text = formatDate(task.dueDateMillis),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (task.isCompleted) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = "Completed",
          tint = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
  return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"))
}

private fun formatDate(millis: Long): String {
  val date = LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
  return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))
}
