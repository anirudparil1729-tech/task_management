package com.ctonew.taskmanagement.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.database.repository.PlannerRepository
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class PlannerUiState(
  val viewMode: ViewMode = ViewMode.DAILY,
  val selectedDate: LocalDate = LocalDate.now(),
  val timeBlocks: List<TimeBlockEntity> = emptyList(),
  val categories: List<CategoryEntity> = emptyList(),
  val tasks: List<TaskEntity> = emptyList(),
  val isLoading: Boolean = false,
  val error: String? = null,
)

enum class ViewMode {
  DAILY, WEEKLY
}

data class TimeBlockUi(
  val localId: String,
  val title: String,
  val startTime: String,
  val endTime: String,
  val estimatedDuration: String,
  val actualDuration: String?,
  val taskTitle: String?,
  val categoryColor: String?,
  val description: String?,
)

data class CreateTimeBlockRequest(
  val title: String,
  val startTime: LocalTime,
  val endTime: LocalTime,
  val estimatedDurationMinutes: Int? = null,
  val taskLocalId: String? = null,
  val description: String? = null,
)

@HiltViewModel
class PlannerViewModel @Inject constructor(
  private val plannerRepository: PlannerRepository,
  private val tasksRepository: TasksRepository,
  private val categoriesRepository: CategoriesRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PlannerUiState())
  val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

  init {
    loadInitialData()
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      
      try {
        // Load categories
        categoriesRepository.categories.collect { categories ->
          _uiState.value = _uiState.value.copy(categories = categories)
        }

        // Load tasks
        tasksRepository.tasks.collect { tasks ->
          _uiState.value = _uiState.value.copy(tasks = tasks)
        }

        // Load time blocks for current date
        loadTimeBlocksForDate(_uiState.value.selectedDate)

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = e.message,
          isLoading = false
        )
      }
    }
  }

  fun updateViewMode(mode: ViewMode) {
    _uiState.value = _uiState.value.copy(viewMode = mode)
    if (mode == ViewMode.DAILY) {
      loadTimeBlocksForDate(_uiState.value.selectedDate)
    } else {
      loadTimeBlocksForWeek(_uiState.value.selectedDate)
    }
  }

  fun updateSelectedDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(selectedDate = date)
    if (_uiState.value.viewMode == ViewMode.DAILY) {
      loadTimeBlocksForDate(date)
    } else {
      loadTimeBlocksForWeek(date)
    }
  }

  fun navigateToPreviousPeriod() {
    val currentDate = _uiState.value.selectedDate
    val newDate = if (_uiState.value.viewMode == ViewMode.DAILY) {
      currentDate.minusDays(1)
    } else {
      currentDate.minusWeeks(1)
    }
    updateSelectedDate(newDate)
  }

  fun navigateToNextPeriod() {
    val currentDate = _uiState.value.selectedDate
    val newDate = if (_uiState.value.viewMode == ViewMode.DAILY) {
      currentDate.plusDays(1)
    } else {
      currentDate.plusWeeks(1)
    }
    updateSelectedDate(newDate)
  }

  fun navigateToToday() {
    val today = LocalDate.now()
    updateSelectedDate(today)
  }

  private fun loadTimeBlocksForDate(date: LocalDate) {
    viewModelScope.launch {
      try {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        
        val timeBlocks = plannerRepository.getTimeBlocksForDate(startOfDay, endOfDay)
        _uiState.value = _uiState.value.copy(
          timeBlocks = timeBlocks,
          isLoading = false,
          error = null
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to load time blocks: ${e.message}",
          isLoading = false
        )
      }
    }
  }

  private fun loadTimeBlocksForWeek(date: LocalDate) {
    viewModelScope.launch {
      try {
        val zoneId = ZoneId.systemDefault()
        val startOfWeek = date.with(java.time.DayOfWeek.MONDAY).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfWeek = date.with(java.time.DayOfWeek.MONDAY).plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        
        val timeBlocks = plannerRepository.getTimeBlocksForWeek(startOfWeek, endOfWeek)
        _uiState.value = _uiState.value.copy(
          timeBlocks = timeBlocks,
          isLoading = false,
          error = null
        )
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to load time blocks: ${e.message}",
          isLoading = false
        )
      }
    }
  }

  fun createTimeBlock(request: CreateTimeBlockRequest) {
    viewModelScope.launch {
      try {
        val zoneId = ZoneId.systemDefault()
        val selectedDate = _uiState.value.selectedDate
        
        val startDateTime = selectedDate.atTime(request.startTime)
          .atZone(zoneId)
          .toInstant()
          .toEpochMilli()
        
        val endDateTime = selectedDate.atTime(request.endTime)
          .atZone(zoneId)
          .toInstant()
          .toEpochMilli()

        // Find task remote ID if task is linked
        val taskRemoteId = request.taskLocalId?.let { taskLocalId ->
          _uiState.value.tasks.find { it.localId == taskLocalId }?.remoteId
        }

        // Calculate estimated duration if not provided
        val estimatedDurationMillis = request.estimatedDurationMinutes?.let { minutes ->
          minutes * 60 * 1000L
        } ?: (endDateTime - startDateTime)

        plannerRepository.createTimeBlock(
          startTimeMillis = startDateTime,
          endTimeMillis = endDateTime,
          taskRemoteId = taskRemoteId,
          estimatedDurationMillis = estimatedDurationMillis,
          title = request.title.ifBlank { "Time Block" },
          description = request.description,
        )

        // Reload time blocks
        if (_uiState.value.viewMode == ViewMode.DAILY) {
          loadTimeBlocksForDate(_uiState.value.selectedDate)
        } else {
          loadTimeBlocksForWeek(_uiState.value.selectedDate)
        }

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to create time block: ${e.message}"
        )
      }
    }
  }

  fun deleteTimeBlock(localId: String) {
    viewModelScope.launch {
      try {
        plannerRepository.deleteTimeBlock(localId)
        
        // Reload time blocks
        if (_uiState.value.viewMode == ViewMode.DAILY) {
          loadTimeBlocksForDate(_uiState.value.selectedDate)
        } else {
          loadTimeBlocksForWeek(_uiState.value.selectedDate)
        }

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to delete time block: ${e.message}"
        )
      }
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  fun getTimeBlocksUi(): List<TimeBlockUi> {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val durationFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    return _uiState.value.timeBlocks.map { timeBlock ->
      val startTime = java.time.Instant.ofEpochMilli(timeBlock.startTimeMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
      
      val endTime = java.time.Instant.ofEpochMilli(timeBlock.endTimeMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()

      val estimatedDuration = timeBlock.estimatedDurationMillis?.let { millis ->
        val duration = java.time.Duration.ofMillis(millis)
        String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
      } ?: timeBlock.endTimeMillis - timeBlock.startTimeMillis
        .let { millis ->
          val duration = java.time.Duration.ofMillis(millis)
          String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
        }

      val actualDuration = timeBlock.actualDurationMillis?.let { millis ->
        val duration = java.time.Duration.ofMillis(millis)
        String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart())
      }

      val taskTitle = timeBlock.taskRemoteId?.let { taskRemoteId ->
        _uiState.value.tasks.find { it.remoteId == taskRemoteId }?.title
      }

      val categoryColor = timeBlock.taskRemoteId?.let { taskRemoteId ->
        val task = _uiState.value.tasks.find { it.remoteId == taskRemoteId }
        task?.categoryRemoteId?.let { categoryRemoteId ->
          _uiState.value.categories.find { it.remoteId == categoryRemoteId }?.color
        }
      }

      TimeBlockUi(
        localId = timeBlock.localId,
        title = timeBlock.title ?: "Time Block",
        startTime = startTime.format(timeFormatter),
        endTime = endTime.format(timeFormatter),
        estimatedDuration = estimatedDuration,
        actualDuration = actualDuration,
        taskTitle = taskTitle,
        categoryColor = categoryColor,
        description = timeBlock.description,
      )
    }
  }

  fun getTasksForSelectedDate(): List<TaskEntity> {
    val selectedDate = _uiState.value.selectedDate
    val zoneId = ZoneId.systemDefault()
    val startOfDay = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    
    return _uiState.value.tasks.filter { task ->
      task.dueDateMillis?.let { dueDate ->
        dueDate >= startOfDay && dueDate < endOfDay
      } ?: false
    }
  }

  fun getIncompleteTasks(): List<TaskEntity> {
    return _uiState.value.tasks.filter { !it.isCompleted }
  }
}