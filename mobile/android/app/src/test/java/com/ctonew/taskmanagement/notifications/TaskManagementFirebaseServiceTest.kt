package com.ctonew.taskmanagement.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ctonew.taskmanagement.core.datastore.NotificationTokenStore
import com.google.firebase.messaging.RemoteMessage
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskManagementFirebaseServiceTest {
  private lateinit var context: Context
  private lateinit var notificationTokenStore: NotificationTokenStore
  private lateinit var notificationHelper: NotificationHelper

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    notificationTokenStore = mockk(relaxed = true)
    notificationHelper = mockk(relaxed = true)
  }

  @Test
  fun `onNewToken saves token to datastore`() = runTest {
    val service = TaskManagementFirebaseService()
    service.notificationTokenStore = notificationTokenStore
    service.notificationHelper = notificationHelper

    val testToken = "test-fcm-token-12345"
    service.onNewToken(testToken)

    Thread.sleep(100)

    coVerify {
      notificationTokenStore.setFcmToken(testToken)
    }
  }

  @Test
  fun `onMessageReceived handles reminder notification`() {
    val service = TaskManagementFirebaseService()
    service.notificationTokenStore = notificationTokenStore
    service.notificationHelper = notificationHelper

    val remoteMessage = mockk<RemoteMessage>(relaxed = true)
    val data = mapOf(
      "type" to "reminder",
      "task_id" to "task123",
      "task_title" to "Test Task",
      "task_description" to "Test Description",
    )

    every { remoteMessage.data } returns data
    every { remoteMessage.from } returns "firebase"

    service.onMessageReceived(remoteMessage)

    verify {
      notificationHelper.showReminderNotification(
        taskId = "task123",
        title = "Test Task",
        description = "Test Description",
      )
    }
  }

  @Test
  fun `onMessageReceived handles daily summary notification`() {
    val service = TaskManagementFirebaseService()
    service.notificationTokenStore = notificationTokenStore
    service.notificationHelper = notificationHelper

    val remoteMessage = mockk<RemoteMessage>(relaxed = true)
    val data = mapOf(
      "type" to "daily_summary",
      "completed_count" to "10",
      "pending_count" to "5",
      "overdue_count" to "2",
    )

    every { remoteMessage.data } returns data
    every { remoteMessage.from } returns "firebase"

    service.onMessageReceived(remoteMessage)

    verify {
      notificationHelper.showDailySummaryNotification(
        completedCount = 10,
        pendingCount = 5,
        overdueCount = 2,
      )
    }
  }

  @Test
  fun `onMessageReceived handles overdue notification`() {
    val service = TaskManagementFirebaseService()
    service.notificationTokenStore = notificationTokenStore
    service.notificationHelper = notificationHelper

    val remoteMessage = mockk<RemoteMessage>(relaxed = true)
    val data = mapOf(
      "type" to "overdue",
      "task_id" to "task456",
      "task_title" to "Overdue Task",
      "days_overdue" to "3",
    )

    every { remoteMessage.data } returns data
    every { remoteMessage.from } returns "firebase"

    service.onMessageReceived(remoteMessage)

    verify {
      notificationHelper.showOverdueNotification(
        taskId = "task456",
        title = "Overdue Task",
        daysOverdue = 3,
      )
    }
  }
}
