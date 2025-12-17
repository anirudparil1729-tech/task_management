package com.ctonew.taskmanagement.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.ui.dashboard.DashboardScreen
import com.ctonew.taskmanagement.ui.dashboard.DashboardUIState
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Rule
import org.junit.Test

class DashboardUITest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboardDisplaysTaskStats() {
    val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tasks = listOf(
      TaskEntity(
        localId = "1",
        title = "Task 1",
        createdAtMillis = today,
        updatedAtMillis = today,
        modifiedAtMillis = today,
      ),
      TaskEntity(
        localId = "2",
        title = "Task 2",
        isCompleted = true,
        createdAtMillis = today,
        updatedAtMillis = today,
        modifiedAtMillis = today,
      ),
    )

    val uiState = DashboardUIState(
      tasks = tasks,
      completedCount = 1,
      completionPercentage = 50,
    )

    composeTestRule.setContent {
      MaterialTheme {
        DashboardScreen(
          uiState = uiState,
          onOpenTaskDetail = {},
          onTaskToggle = {},
        )
      }
    }

    composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("Total Tasks").assertIsDisplayed()
    composeTestRule.onNodeWithText("2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Completed").assertIsDisplayed()
    composeTestRule.onNodeWithText("1").assertIsDisplayed()
  }

  @Test
  fun dashboardDisplaysOverdueTasks() {
    val today = System.currentTimeMillis()
    val yesterday = today - (24 * 60 * 60 * 1000)

    val overdueTask = TaskEntity(
      localId = "1",
      title = "Overdue Task",
      dueDateMillis = yesterday,
      isCompleted = false,
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    val uiState = DashboardUIState(
      tasks = listOf(overdueTask),
      overdueTasks = listOf(overdueTask),
    )

    composeTestRule.setContent {
      MaterialTheme {
        DashboardScreen(
          uiState = uiState,
          onOpenTaskDetail = {},
          onTaskToggle = {},
        )
      }
    }

    composeTestRule.onNodeWithText("Overdue").assertIsDisplayed()
    composeTestRule.onNodeWithText("Overdue Task").assertIsDisplayed()
  }

  @Test
  fun taskItemToggleWorks() {
    val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    var toggleCalled = false

    val task = TaskEntity(
      localId = "1",
      title = "Test Task",
      createdAtMillis = today,
      updatedAtMillis = today,
      modifiedAtMillis = today,
    )

    val uiState = DashboardUIState(
      tasks = listOf(task),
      todayTasks = listOf(task),
    )

    composeTestRule.setContent {
      MaterialTheme {
        DashboardScreen(
          uiState = uiState,
          onOpenTaskDetail = {},
          onTaskToggle = { toggleCalled = true },
        )
      }
    }

    composeTestRule.onNodeWithText("Test Task").performClick()
    assert(toggleCalled)
  }
}
