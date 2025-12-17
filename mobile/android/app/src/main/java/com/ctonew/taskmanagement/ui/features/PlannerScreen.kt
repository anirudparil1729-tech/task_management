package com.ctonew.taskmanagement.ui.features

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
  onNavigateToTask: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PlannerViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(modifier = modifier.fillMaxSize()) {
    // Header
    PlannerHeader(
      viewMode = uiState.viewMode,
      selectedDate = uiState.selectedDate,
      onViewModeChange = viewModel::updateViewMode,
      onNavigatePrevious = viewModel::navigateToPreviousPeriod,
      onNavigateNext = viewModel::navigateToNextPeriod,
      onNavigateToday = viewModel::navigateToToday,
    )

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
      when (uiState.viewMode) {
        ViewMode.DAILY -> DailyPlannerView(
          selectedDate = uiState.selectedDate,
          timeBlocks = viewModel.getTimeBlocksUi(),
          tasksForDate = viewModel.getTasksForSelectedDate(),
          availableTasks = viewModel.getIncompleteTasks(),
          categories = uiState.categories,
          onCreateTimeBlock = viewModel::createTimeBlock,
          onDeleteTimeBlock = viewModel::deleteTimeBlock,
          onTaskClick = onNavigateToTask,
          modifier = Modifier.fillMaxSize(),
        )
        ViewMode.WEEKLY -> WeeklyPlannerView(
          selectedDate = uiState.selectedDate,
          timeBlocks = uiState.timeBlocks,
          tasks = uiState.tasks,
          categories = uiState.categories,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerHeader(
  viewMode: ViewMode,
  selectedDate: LocalDate,
  onViewModeChange: (ViewMode) -> Unit,
  onNavigatePrevious: () -> Unit,
  onNavigateNext: () -> Unit,
  onNavigateToday: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Planner",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = viewMode == ViewMode.DAILY,
            onClick = { onViewModeChange(ViewMode.DAILY) },
            label = { Text("Daily") }
          )
          FilterChip(
            selected = viewMode == ViewMode.WEEKLY,
            onClick = { onViewModeChange(ViewMode.WEEKLY) },
            label = { Text("Weekly") }
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigatePrevious) {
          Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.CalendarToday, contentDescription = null)
          Text(
            text = if (viewMode == ViewMode.DAILY) {
              selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            } else {
              val weekStart = selectedDate.with(java.time.DayOfWeek.MONDAY)
              "Week of ${weekStart.format(DateTimeFormatter.ofPattern("MMM dd"))}"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onNavigateToday,
            modifier = Modifier.height(32.dp)
          ) {
            Text("Today")
          }
          IconButton(onClick = onNavigateNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
          }
        }
      }
    }
  }
}

@Composable
fun DailyPlannerView(
  selectedDate: LocalDate,
  timeBlocks: List<TimeBlockUi>,
  tasksForDate: List<com.ctonew.taskmanagement.core.database.model.TaskEntity>,
  availableTasks: List<com.ctonew.taskmanagement.core.database.model.TaskEntity>,
  categories: List<com.ctonew.taskmanagement.core.database.model.CategoryEntity>,
  onCreateTimeBlock: (CreateTimeBlockRequest) -> Unit,
  onDeleteTimeBlock: (String) -> Unit,
  onTaskClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val showAddBlockDialog = remember { mutableStateOf(false) }

  if (showAddBlockDialog.value) {
    AddTimeBlockDialog(
      availableTasks = availableTasks,
      categories = categories,
      onDismiss = { showAddBlockDialog.value = false },
      onConfirm = { request ->
        onCreateTimeBlock(request)
        showAddBlockDialog.value = false
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Time Blocks Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Time Blocks",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (timeBlocks.isEmpty()) {
          Text(
            text = "No time blocks scheduled",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 300.dp)
          ) {
            items(timeBlocks) { timeBlock ->
              TimeBlockItem(
                timeBlock = timeBlock,
                onDelete = { onDeleteTimeBlock(timeBlock.localId) }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = { showAddBlockDialog.value = true },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Add Time Block")
        }
      }
    }

    // Tasks for Today Section
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Tasks Due Today",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        
        Text(
          text = "Tasks scheduled for this day",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (tasksForDate.isEmpty()) {
          Text(
            text = "No tasks due today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 200.dp)
          ) {
            items(tasksForDate) { task ->
              TaskItem(
                task = task,
                onClick = { onTaskClick(task.localId) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun WeeklyPlannerView(
  selectedDate: LocalDate,
  timeBlocks: List<com.ctonew.taskmanagement.core.database.model.TimeBlockEntity>,
  tasks: List<com.ctonew.taskmanagement.core.database.model.TaskEntity>,
  categories: List<com.ctonew.taskmanagement.core.database.model.CategoryEntity>,
  modifier: Modifier = Modifier,
) {
  val weekDays = remember(selectedDate) {
    val startOfWeek = selectedDate.with(java.time.DayOfWeek.MONDAY)
    (0..6).map { offset ->
      startOfWeek.plusDays(offset.toLong())
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(weekDays) { date ->
      val isToday = date == LocalDate.now()
      
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isToday) {
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          )
        } else {
          CardDefaults.cardColors()
        }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = date.format(DateTimeFormatter.ofPattern("MMM dd")),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Week day content would go here (simplified for now)
          Text(
            text = "Week view content placeholder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun TimeBlockItem(
  timeBlock: TimeBlockUi,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Color indicator
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(40.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(Color(android.graphics.Color.parseColor(timeBlock.categoryColor ?: "#3B82F6")))
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${timeBlock.startTime} - ${timeBlock.endTime}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "(${timeBlock.estimatedDuration})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Text(
          text = timeBlock.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(vertical = 4.dp)
        )

        timeBlock.taskTitle?.let { taskTitle ->
          TextButton(onClick = { /* Navigate to task */ }) {
            Text(
              text = "→ $taskTitle",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        timeBlock.description?.let { description ->
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@Composable
fun TaskItem(
  task: com.ctonew.taskmanagement.core.database.model.TaskEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onClick,
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          textDecoration = if (task.isCompleted) {
            androidx.compose.ui.text.style.TextDecoration.LineThrough
          } else {
            null
          }
        )
        // Additional task details would go here
      }
      
      if (task.isCompleted) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
fun AddTimeBlockDialog(
  availableTasks: List<com.ctonew.taskmanagement.core.database.model.TaskEntity>,
  categories: List<com.ctonew.taskmanagement.core.database.model.CategoryEntity>,
  onDismiss: () -> Unit,
  onConfirm: (CreateTimeBlockRequest) -> Unit,
) {
  val (title, setTitle) = remember { mutableStateOf("") }
  val (startTime, setStartTime) = remember { mutableStateOf("09:00") }
  val (endTime, setEndTime) = remember { mutableStateOf("10:00") }
  val (selectedTaskId, setSelectedTaskId) = remember { mutableStateOf<String?>(null) }
  val (description, setDescription) = remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Time Block") },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = setTitle,
          label = { Text("Title") },
          placeholder = { Text("e.g., Team Meeting, Focus Time") },
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = startTime,
            onValueChange = setStartTime,
            label = { Text("Start Time") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = endTime,
            onValueChange = setEndTime,
            label = { Text("End Time") },
            modifier = Modifier.weight(1f)
          )
        }

        // Task selection dropdown would go here (simplified)
        OutlinedTextField(
          value = selectedTaskId ?: "",
          onValueChange = { setSelectedTaskId(it.ifBlank { null }) },
          label = { Text("Link to Task (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = description,
          onValueChange = setDescription,
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val startTimeParts = startTime.split(":")
          val endTimeParts = endTime.split(":")
          
          if (startTimeParts.size == 2 && endTimeParts.size == 2) {
            val request = CreateTimeBlockRequest(
              title = title.trim().ifEmpty { "Time Block" },
              startTime = LocalTime.of(startTimeParts[0].toInt(), startTimeParts[1].toInt()),
              endTime = LocalTime.of(endTimeParts[0].toInt(), endTimeParts[1].toInt()),
              taskLocalId = selectedTaskId,
              description = description.ifBlank { null }
            )
            onConfirm(request)
          }
        }
      ) {
        Text("Add Block")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}