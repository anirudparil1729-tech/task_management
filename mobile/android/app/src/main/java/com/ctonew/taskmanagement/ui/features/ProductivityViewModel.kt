package com.ctonew.taskmanagement.ui.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.database.repository.ProductivityRepository
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import com.ctonew.taskmanagement.core.database.repository.CategoriesRepository
import com.ctonew.taskmanagement.core.database.model.FocusSessionEntity
import com.ctonew.taskmanagement.core.database.model.ReminderEntity
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

data class ProductivityUiState(
  val selectedDate: LocalDate = LocalDate.now(),
  val productivityScore: Double = 0.0,
  val tasksCompleted: Int = 0,
  val tasksPlanned: Int = 0,
  val focusSessions: List<FocusSessionEntity> = emptyList(),
  val reminders: List<ReminderEntity> = emptyList(),
  val categories: List<CategoryEntity> = emptyList(),
  val tasks: List<TaskEntity> = emptyList(),
  val isLoading: Boolean = false,
  val error: String? = null,
  val focusTimer: FocusTimerState = FocusTimerState.IDLE,
)

enum class FocusTimerState {
  IDLE, RUNNING, COMPLETED
}

data class FocusTimerUi(
  val isRunning: Boolean = false,
  val elapsedTime: Long = 0L,
  val targetDuration: Long = 0L,
  val remainingTime: Long = 0L,
  val sessionLocalId: String? = null,
)

data class CategoryProgressUi(
  val categoryId: String,
  val categoryName: String,
  val color: String,
  val tasksCompleted: Int,
  val tasksTotal: Int,
  val percentage: Float,
)

data class ReminderRequest(
  val title: String,
  val description: String?,
  val reminderTime: LocalTime,
  val taskLocalId: String? = null,
)

@HiltViewModel
class ProductivityViewModel @Inject constructor(
  private val productivityRepository: ProductivityRepository,
  private val tasksRepository: TasksRepository,
  private val categoriesRepository: CategoriesRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProductivityUiState())
  val uiState: StateFlow<ProductivityUiState> = _uiState.asStateFlow()

  private var focusTimerJob: kotlinx.coroutines.Job? = null

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
          calculateProductivityMetrics()
        }

        // Load tasks
        tasksRepository.tasks.collect { tasks ->
          _uiState.value = _uiState.value.copy(tasks = tasks)
          calculateProductivityMetrics()
        }

        // Load focus sessions
        productivityRepository.focusSessions.collect { focusSessions ->
          _uiState.value = _uiState.value.copy(focusSessions = focusSessions)
          calculateProductivityMetrics()
        }

        // Load reminders
        productivityRepository.reminders.collect { reminders ->
          _uiState.value = _uiState.value.copy(reminders = reminders)
        }

        _uiState.value = _uiState.value.copy(isLoading = false)

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = e.message,
          isLoading = false
        )
      }
    }
  }

  private fun calculateProductivityMetrics() {
    viewModelScope.launch {
      try {
        val selectedDate = _uiState.value.selectedDate
        val productivityScore = productivityRepository.getProductivityScoreForDate(selectedDate)
        
        val tasks = _uiState.value.tasks
        val zoneId = ZoneId.systemDefault()
        val startOfDay = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Filter tasks for the selected date
        val tasksForDate = tasks.filter { task ->
          task.dueDateMillis?.let { dueDate ->
            dueDate >= startOfDay && dueDate < endOfDay
          } ?: false
        }

        val tasksPlanned = tasksForDate.size
        val tasksCompleted = tasksForDate.count { it.isCompleted }

        _uiState.value = _uiState.value.copy(
          productivityScore = productivityScore,
          tasksCompleted = tasksCompleted,
          tasksPlanned = tasksPlanned,
          error = null
        )

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to calculate productivity metrics: ${e.message}"
        )
      }
    }
  }

  fun updateSelectedDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(selectedDate = date)
    calculateProductivityMetrics()
  }

  // Focus Timer Functions
  fun startFocusSession(durationMinutes: Int) {
    viewModelScope.launch {
      try {
        val startTime = System.currentTimeMillis()
        val sessionLocalId = productivityRepository.createFocusSession(startTime, durationMinutes)
        
        _uiState.value = _uiState.value.copy(
          focusTimer = FocusTimerState.RUNNING
        )

        startTimerCountdown(startTime, durationMinutes * 60 * 1000L, sessionLocalId)

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to start focus session: ${e.message}"
        )
      }
    }
  }

  private fun startTimerCountdown(startTime: Long, targetDuration: Long, sessionLocalId: String) {
    focusTimerJob?.cancel()
    
    focusTimerJob = viewModelScope.launch {
      kotlinx.coroutines.delay(targetDuration)
      
      // Timer completed
      val endTime = System.currentTimeMillis()
      productivityRepository.completeFocusSession(sessionLocalId, endTime)
      
      _uiState.value = _uiState.value.copy(
        focusTimer = FocusTimerState.COMPLETED
      )

      calculateProductivityMetrics()
    }
  }

  fun stopFocusSession() {
    viewModelScope.launch {
      try {
        focusTimerJob?.cancel()
        focusTimerJob = null

        // Mark timer as stopped but don't log completion
        _uiState.value = _uiState.value.copy(
          focusTimer = FocusTimerState.IDLE
        )

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to stop focus session: ${e.message}"
        )
      }
    }
  }

  fun resetFocusTimer() {
    _uiState.value = _uiState.value.copy(
      focusTimer = FocusTimerState.IDLE
    )
  }

  fun getFocusTimerUi(): FocusTimerUi {
    return when (_uiState.value.focusTimer) {
      FocusTimerState.IDLE -> FocusTimerUi()
      FocusTimerState.RUNNING -> {
        // For simplicity, we'll show a basic timer state
        // In a real implementation, you'd track the actual elapsed time
        FocusTimerUi(isRunning = true, elapsedTime = 0, targetDuration = 25 * 60 * 1000L, remainingTime = 25 * 60 * 1000L)
      }
      FocusTimerState.COMPLETED -> FocusTimerUi(isRunning = false, elapsedTime = 25 * 60 * 1000L, targetDuration = 25 * 60 * 1000L, remainingTime = 0)
    }
  }

  // Reminder Functions
  fun createReminder(request: ReminderRequest) {
    viewModelScope.launch {
      try {
        val zoneId = ZoneId.systemDefault()
        val reminderDateTime = _uiState.value.selectedDate.atTime(request.reminderTime)
          .atZone(zoneId)
          .toInstant()
          .toEpochMilli()

        productivityRepository.createReminder(
          title = request.title,
          description = request.description,
          reminderTimeMillis = reminderDateTime,
          taskLocalId = request.taskLocalId,
        )

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to create reminder: ${e.message}"
        )
      }
    }
  }

  fun deleteReminder(localId: String) {
    viewModelScope.launch {
      try {
        productivityRepository.deleteReminder(localId)
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Failed to delete reminder: ${e.message}"
        )
      }
    }
  }

  fun getCategoryProgress(): List<CategoryProgressUi> {
    val categories = _uiState.value.categories
    val tasks = _uiState.value.tasks
    val selectedDate = _uiState.value.selectedDate
    val zoneId = ZoneId.systemDefault()
    val startOfDay = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = selectedDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    return categories.map { category ->
      val categoryTasks = tasks.filter { task ->
        task.categoryRemoteId == category.remoteId &&
        task.dueDateMillis?.let { dueDate ->
          dueDate >= startOfDay && dueDate < endOfDay
        } ?: false
      }

      val tasksTotal = categoryTasks.size
      val tasksCompleted = categoryTasks.count { it.isCompleted }
      val percentage = if (tasksTotal > 0) {
        (tasksCompleted.toFloat() / tasksTotal) * 100f
      } else {
        0f
      }

      CategoryProgressUi(
        categoryId = category.localId,
        categoryName = category.name,
        color = category.color,
        tasksCompleted = tasksCompleted,
        tasksTotal = tasksTotal,
        percentage = percentage,
      )
    }.filter { it.tasksTotal > 0 }
  }

  fun getEndOfDaySummary(): Map<String, Any> {
    val productivityScore = _uiState.value.productivityScore
    val tasksCompleted = _uiState.value.tasksCompleted
    val focusSessions = _uiState.value.focusSessions
      .filter { session ->
        val sessionDate = java.time.Instant.ofEpochMilli(session.startedAtMillis)
          .atZone(ZoneId.systemDefault())
          .toLocalDate()
        sessionDate == _uiState.value.selectedDate
      }

    val motivationalMessage = when {
      productivityScore >= 80.0 -> "🎉 Outstanding work today! You're crushing it!"
      productivityScore >= 60.0 -> "👍 Great progress! Keep up the good work."
      productivityScore >= 40.0 -> "💪 You're making progress. Stay focused!"
      else -> "🌱 Every journey starts somewhere. Tomorrow is a new day!"
    }

    return mapOf(
      "tasksCompleted" to tasksCompleted,
      "focusSessions" to focusSessions.size,
      "productivityScore" to productivityScore.toInt(),
      "motivationalMessage" to motivationalMessage
    )
  }

  fun getIncompleteTasks(): List<TaskEntity> {
    return _uiState.value.tasks.filter { !it.isCompleted }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  override fun onCleared() {
    super.onCleared()
    focusTimerJob?.cancel()
  }
}