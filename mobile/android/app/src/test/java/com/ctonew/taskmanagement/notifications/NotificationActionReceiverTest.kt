package com.ctonew.taskmanagement.notifications

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationActionReceiverTest {
  private lateinit var context: Context
  private lateinit var tasksRepository: TasksRepository
  private lateinit var notificationHelper: NotificationHelper

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    tasksRepository = mockk(relaxed = true)
    notificationHelper = mockk(relaxed = true)
  }

  @Test
  fun `complete action marks task as completed`() = runTest {
    val receiver = NotificationActionReceiver()
    receiver.tasksRepository = tasksRepository
    receiver.notificationHelper = notificationHelper

    val intent = Intent().apply {
      action = "com.ctonew.taskmanagement.ACTION_COMPLETE"
      putExtra("task_id", "task123")
    }

    receiver.onReceive(context, intent)

    Thread.sleep(100)

    coVerify {
      tasksRepository.markTaskCompleted("task123", true)
    }
  }

  @Test
  fun `snooze action cancels notification`() = runTest {
    val receiver = NotificationActionReceiver()
    receiver.tasksRepository = tasksRepository
    receiver.notificationHelper = notificationHelper

    val intent = Intent().apply {
      action = "com.ctonew.taskmanagement.ACTION_SNOOZE"
      putExtra("task_id", "task456")
    }

    receiver.onReceive(context, intent)

    Thread.sleep(100)

    coVerify {
      notificationHelper.cancelNotification(1000 + "task456".hashCode())
    }
  }
}
