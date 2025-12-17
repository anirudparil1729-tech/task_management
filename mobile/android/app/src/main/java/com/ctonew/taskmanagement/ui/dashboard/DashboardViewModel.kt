package com.ctonew.taskmanagement.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import com.ctonew.taskmanagement.core.datastore.SyncStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUIState(
  val tasks: List<TaskEntity> = emptyList(),
  val overdueTasks: List<TaskEntity> = emptyList(),
  val todayTasks: List<TaskEntity> = emptyList(),
  val upcomingTasks: List<TaskEntity> = emptyList(),
  val completedCount: Int = 0,
  val completionPercentage: Int = 0,
  val lastSyncedAt: Long? = null,
  val isLoading: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
  private val tasksRepository: TasksRepository,
  private val syncStateStore: SyncStateStore,
) : ViewModel() {
  private val _uiState = MutableStateFlow(DashboardUIState())
  val uiState: StateFlow<DashboardUIState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      combine(
        tasksRepository.tasks,
        syncStateStore.lastTasksSyncMillis,
      ) { tasks, lastSync ->
        val completed = tasks.count { it.isCompleted }
        val completionPercentage = if (tasks.isEmpty()) 0 else (completed * 100) / tasks.size

        val now = LocalDate.now()
        val today = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrow = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val oneWeekLater = now.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val overdue = tasks.filter { task ->
          !task.isCompleted && task.dueDateMillis != null && task.dueDateMillis < today
        }.sortedByDescending { it.dueDateMillis }

        val todayList = tasks.filter { task ->
          !task.isCompleted && task.dueDateMillis != null && task.dueDateMillis >= today && task.dueDateMillis < tomorrow
        }.sortedByDescending { it.dueDateMillis }

        val upcoming = tasks.filter { task ->
          !task.isCompleted && task.dueDateMillis != null && task.dueDateMillis >= tomorrow && task.dueDateMillis < oneWeekLater
        }.sortedByDescending { it.dueDateMillis }

        DashboardUIState(
          tasks = tasks,
          overdueTasks = overdue,
          todayTasks = todayList,
          upcomingTasks = upcoming,
          completedCount = completed,
          completionPercentage = completionPercentage,
          lastSyncedAt = lastSync.takeIf { it > 0 },
        )
      }.stateIn(viewModelScope).collect { state ->
        _uiState.value = state
      }
    }
  }

  suspend fun createTask(title: String, description: String? = null): String {
    return tasksRepository.createTask(title, description)
  }

  fun toggleTaskCompletion(task: TaskEntity) {
    viewModelScope.launch {
      val updateDto = com.ctonew.taskmanagement.core.network.dto.TaskUpdateDto(
        isCompleted = !task.isCompleted,
      )
      tasksRepository.updateTask(task.localId, updateDto)
    }
  }
}
