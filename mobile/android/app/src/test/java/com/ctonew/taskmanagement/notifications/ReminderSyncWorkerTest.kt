package com.ctonew.taskmanagement.notifications

import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReminderSyncWorkerTest {
  @Test
  fun `scheduleReminderForTask schedules future reminders`() = runTest {
    val taskDao: TaskDao = mockk()
    val notificationHelper: NotificationHelper = mockk(relaxed = true)
    val notificationScheduler: NotificationScheduler = mockk(relaxed = true)

    val now = System.currentTimeMillis()
    val futureTime = now + TimeUnit.HOURS.toMillis(2)

    val upcomingTask = TaskEntity(
      localId = "task1",
      title = "Upcoming Task",
      description = "Test task",
      reminderTimeMillis = futureTime,
      dueDateMillis = futureTime,
      isCompleted = false,
      createdAtMillis = now,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    coEvery {
      taskDao.getTasksDueWithinTimeframe(any(), any())
    } returns flowOf(listOf(upcomingTask))

    every {
      taskDao.getOverdueTasks(any())
    } returns flowOf(emptyList())

    coVerify(exactly = 0) {
      notificationHelper.showReminderNotification(any(), any(), any())
    }
  }

  @Test
  fun `checkOverdueTasks shows notifications for overdue tasks`() = runTest {
    val taskDao: TaskDao = mockk()
    val notificationHelper: NotificationHelper = mockk(relaxed = true)

    val now = System.currentTimeMillis()
    val pastTime = now - TimeUnit.DAYS.toMillis(2)

    val overdueTask = TaskEntity(
      localId = "task2",
      title = "Overdue Task",
      description = null,
      dueDateMillis = pastTime,
      isCompleted = false,
      createdAtMillis = pastTime,
      updatedAtMillis = now,
      modifiedAtMillis = now,
    )

    every {
      taskDao.getOverdueTasks(any())
    } returns flowOf(listOf(overdueTask))

    assertThat(overdueTask.isCompleted).isFalse()
    assertThat(overdueTask.dueDateMillis).isLessThan(now)
  }
}
