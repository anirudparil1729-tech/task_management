package com.ctonew.taskmanagement.ui.features

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
  modifier: Modifier = Modifier,
  viewModel: ProductivityViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(modifier = modifier.fillMaxSize()) {
    // Header
    ProductivityHeader()

    // Error handling
    uiState.error?.let { error ->
      Snackbar(
        modifier = Modifier.padding(16.dp),
        onDismissAction = { viewModel.clearError() }
      ) {
        Text(error)
      }
    }

    // Content
    Box(modifier = Modifier.fillMaxSize()) {
      when (uiState.isLoading) {
        true -> LoadingContent()
        false -> ProductivityContent(
          uiState = uiState,
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun ProductivityHeader(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primary
    )
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(
          Icons.Default.TrendingUp,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
          tint = Color.White
        )
        Column {
          Text(
            text = "Productivity Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Track your progress and stay motivated",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
    }
  }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
fun ProductivityContent(
  uiState: ProductivityUiState,
  viewModel: ProductivityViewModel,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Daily Score Card
    item {
      DailyScoreCard(
        productivityScore = uiState.productivityScore,
        tasksCompleted = uiState.tasksCompleted,
        tasksPlanned = uiState.tasksPlanned
      )
    }

    // Stats Grid
    item {
      StatsGrid(
        tasksCompleted = uiState.tasksCompleted,
        tasksPlanned = uiState.tasksPlanned,
        focusSessions = uiState.focusSessions.filter { session ->
          val sessionDate = java.time.Instant.ofEpochMilli(session.startedAtMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
          sessionDate == uiState.selectedDate
        }.size,
        streak = 3, // Placeholder
      )
    }

    // Focus Timer
    item {
      FocusTimerCard(
        focusTimerUi = viewModel.getFocusTimerUi(),
        onStartSession = viewModel::startFocusSession,
        onStopSession = viewModel::stopFocusSession,
        onResetTimer = viewModel::resetFocusTimer,
      )
    }

    // Reminders
    item {
      RemindersCard(
        reminders = uiState.reminders,
        onCreateReminder = viewModel::createReminder,
        onDeleteReminder = viewModel::deleteReminder,
        selectedDate = uiState.selectedDate,
      )
    }

    // Category Progress
    item {
      CategoryProgressCard(
        categoryProgress = viewModel.getCategoryProgress(),
      )
    }

    // End of Day Summary
    item {
      EndOfDaySummaryCard(
        summary = viewModel.getEndOfDaySummary(),
      )
    }
  }
}

@Composable
fun DailyScoreCard(
  productivityScore: Double,
  tasksCompleted: Int,
  tasksPlanned: Int,
  modifier: Modifier = Modifier,
) {
  val backgroundBrush = Brush.horizontalGradient(
    colors = listOf(
      MaterialTheme.colorScheme.primary,
      MaterialTheme.colorScheme.primaryContainer
    )
  )

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundBrush)
        .padding(24.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Icon(
          Icons.Default.EmojiEvents,
          contentDescription = null,
          modifier = Modifier.size(48.dp),
          tint = Color.White
        )
        Column {
          Text(
            text = "Daily Score: ${productivityScore.toInt()}/100",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "You've completed $tasksCompleted task${if (tasksCompleted != 1) "s" else ""} today",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
          )
        }
      }

      LinearProgressIndicator(
        progress = productivityScore.toFloat() / 100f,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
        color = Color.White
      )
    }
  }
}

@Composable
fun StatsGrid(
  tasksCompleted: Int,
  tasksPlanned: Int,
  focusSessions: Int,
  streak: Int,
  modifier: Modifier = Modifier,
) {
  LazyVerticalGrid(
    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = modifier.height(120.dp)
  ) {
    item {
      StatCard(
        title = "Tasks Today",
        value = "$tasksCompleted/$tasksPlanned",
        icon = Icons.Default.CheckCircle,
        iconColor = MaterialTheme.colorScheme.primary
      )
    }
    
    item {
      StatCard(
        title = "Focus Sessions",
        value = "$focusSessions",
        icon = Icons.Default.FlashOn,
        iconColor = MaterialTheme.colorScheme.tertiary
      )
    }
    
    item {
      StatCard(
        title = "Streak",
        value = "$streak",
        icon = Icons.Default.TrendingUp,
        iconColor = MaterialTheme.colorScheme.secondary
      )
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = iconColor
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun FocusTimerCard(
  focusTimerUi: FocusTimerUi,
  onStartSession: (Int) -> Unit,
  onStopSession: () -> Unit,
  onResetTimer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val (durationInput, setDurationInput) = remember { mutableStateOf("25") }

  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Timer, contentDescription = null)
          Text(
            text = "Focus Timer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Timer Display
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (focusTimerUi.isRunning) {
            "${focusTimerUi.remainingTime / 60000}:${((focusTimerUi.remainingTime % 60000) / 1000).toString().padStart(2, '0')}"
          } else {
            "—"
          },
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Duration Input and Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
      ) {
        OutlinedTextField(
          value = durationInput,
          onValueChange = setDurationInput,
          label = { Text("Minutes") },
          modifier = Modifier.weight(1f),
          enabled = !focusTimerUi.isRunning
        )

        if (focusTimerUi.isRunning) {
          OutlinedButton(
            onClick = onStopSession,
            modifier = Modifier.height(56.dp)
          ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop")
          }
        } else {
          Button(
            onClick = {
              val minutes = durationInput.toIntOrNull() ?: 25
              onStartSession(minutes)
            },
            modifier = Modifier.height(56.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start")
          }
        }
      }

      if (focusTimerUi.isRunning) {
        LinearProgressIndicator(
          progress = if (focusTimerUi.targetDuration > 0) {
            (focusTimerUi.targetDuration - focusTimerUi.remainingTime).toFloat() / focusTimerUi.targetDuration.toFloat()
          } else 0f,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun RemindersCard(
  reminders: List<ReminderEntity>,
  onCreateReminder: (ReminderRequest) -> Unit,
  onDeleteReminder: (String) -> Unit,
  selectedDate: LocalDate,
  modifier: Modifier = Modifier,
) {
  val showAddDialog = remember { mutableStateOf(false) }
  val (title, setTitle) = remember { mutableStateOf("Take a break") }
  val (minutes, setMinutes) = remember { mutableStateOf("10") }

  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Notifications, contentDescription = null)
          Text(
            text = "Reminders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        }
        Button(
          onClick = { showAddDialog.value = true },
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Schedule")
        }
      }

      Text(
        text = "Schedule a one-off reminder.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (reminders.isEmpty()) {
        Text(
          text = "No reminders scheduled",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.heightIn(max = 120.dp)
        ) {
          items(reminders.take(6)) { reminder ->
            ReminderItem(
              reminder = reminder,
              onDelete = { onDeleteReminder(reminder.localId) }
            )
          }
        }
      }
    }
  }

  if (showAddDialog.value) {
    AlertDialog(
      onDismissRequest = { showAddDialog.value = false },
      title = { Text("Schedule Reminder") },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = title,
            onValueChange = setTitle,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = minutes,
            onValueChange = setMinutes,
            label = { Text("In (minutes)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val minutesInt = minutes.toIntOrNull() ?: 10
            val reminderTime = LocalTime.ofSecondOfDay((minutesInt * 60).toLong())
            
            onCreateReminder(
              ReminderRequest(
                title = title.trim().ifEmpty { "Reminder" },
                description = "Scheduled reminder",
                reminderTime = reminderTime,
                taskLocalId = null
              )
            )
            
            showAddDialog.value = false
            setTitle("Take a break")
            setMinutes("10")
          }
        ) {
          Text("Schedule")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog.value = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ReminderItem(
  reminder: ReminderEntity,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "Reminder", // Placeholder
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = reminder.reminderTimeMillis?.let { millis ->
          java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
        } ?: "No time set",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    IconButton(onClick = onDelete) {
      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
    }
  }
}

@Composable
fun CategoryProgressCard(
  categoryProgress: List<CategoryProgressUi>,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.PieChart, contentDescription = null)
        Text(
          text = "Progress by Category",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = "See how you're doing across different areas",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (categoryProgress.isEmpty()) {
        Text(
          text = "No tasks with categories yet",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          categoryProgress.forEach { progress ->
            CategoryProgressItem(progress = progress)
          }
        }
      }
    }
  }
}

@Composable
fun CategoryProgressItem(
  progress: CategoryProgressUi,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(android.graphics.Color.parseColor(progress.color)))
        )
        Text(
          text = progress.categoryName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium
        )
      }
      
      Text(
        text = "${progress.tasksCompleted}/${progress.tasksTotal} tasks",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    LinearProgressIndicator(
      progress = progress.percentage / 100f,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun EndOfDaySummaryCard(
  summary: Map<String, Any>,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Summary, contentDescription = null)
        Text(
          text = "End of Day Summary",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }

      Text(
        text = "Your productivity at a glance",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        SummaryItem(
          title = "Tasks Completed",
          value = "${summary["tasksCompleted"]}"
        )
        SummaryItem(
          title = "Focus Sessions",
          value = "${summary["focusSessions"]}"
        )
        SummaryItem(
          title = "Productivity Score",
          value = "${summary["productivityScore"]}/100"
        )
      }

      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      ) {
        Text(
          text = summary["motivationalMessage"] as? String ?: "Great job!",
          modifier = Modifier.padding(16.dp),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

@Composable
fun SummaryItem(
  title: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = value,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )
  }
}