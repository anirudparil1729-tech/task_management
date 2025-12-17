package com.ctonew.taskmanagement.ui.dashboard

import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import com.ctonew.taskmanagement.core.datastore.SyncStateStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

class DashboardViewModelTest {
  private val tasksRepository = mockk<TasksRepository>()
  private val syncStateStore = mockk<SyncStateStore>()
  private val testDispatcher = StandardTestDispatcher()

  private fun createViewModel(): DashboardViewModel {
    return DashboardViewModel(tasksRepository, syncStateStore)
  }

  @Test
  fun testFilteringTodayTasks() = runTest(testDispatcher) {
    val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrow = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val todayTask = TaskEntity(
      localId = "1",
      title = "Today Task",
      dueDateMillis = today + 3600000,
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    val tomorrowTask = TaskEntity(
      localId = "2",
      title = "Tomorrow Task",
      dueDateMillis = tomorrow + 3600000,
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    coEvery { tasksRepository.tasks } returns flowOf(listOf(todayTask, tomorrowTask))
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(0L)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(1, state.todayTasks.size)
    assertEquals("Today Task", state.todayTasks[0].title)
  }

  @Test
  fun testCompletionPercentage() = runTest(testDispatcher) {
    val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val completedTask = TaskEntity(
      localId = "1",
      title = "Completed Task",
      isCompleted = true,
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    val incompleteTask = TaskEntity(
      localId = "2",
      title = "Incomplete Task",
      isCompleted = false,
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    coEvery { tasksRepository.tasks } returns flowOf(listOf(completedTask, incompleteTask))
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(0L)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(50, state.completionPercentage)
    assertEquals(1, state.completedCount)
  }

  @Test
  fun testOverdueTasksFiltering() = runTest(testDispatcher) {
    val now = System.currentTimeMillis()
    val yesterday = now - (24 * 60 * 60 * 1000)

    val overdueTask = TaskEntity(
      localId = "1",
      title = "Overdue Task",
      dueDateMillis = yesterday,
      isCompleted = false,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    val completeTask = TaskEntity(
      localId = "2",
      title = "Completed Overdue",
      dueDateMillis = yesterday,
      isCompleted = true,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    coEvery { tasksRepository.tasks } returns flowOf(listOf(overdueTask, completeTask))
    coEvery { syncStateStore.lastTasksSyncMillis } returns flowOf(0L)

    val viewModel = createViewModel()

    val state = viewModel.uiState.value
    assertEquals(1, state.overdueTasks.size)
    assertEquals("Overdue Task", state.overdueTasks[0].title)
  }
}
