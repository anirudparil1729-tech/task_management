package com.ctonew.taskmanagement.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
class NotificationHelperTest {
  private lateinit var context: Context
  private lateinit var notificationHelper: NotificationHelper
  private lateinit var shadowNotificationManager: ShadowNotificationManager

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    notificationHelper = NotificationHelper(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    shadowNotificationManager = shadowOf(notificationManager)
  }

  @Test
  fun `showReminderNotification creates notification with correct title`() {
    notificationHelper.showReminderNotification(
      taskId = "task123",
      title = "Test Task",
      description = "This is a test task",
    )

    val notifications = shadowNotificationManager.allNotifications
    assertThat(notifications).hasSize(1)

    val notification = notifications[0]
    assertThat(notification.extras.getString("android.title")).isEqualTo("Test Task")
    assertThat(notification.extras.getString("android.text")).isEqualTo("This is a test task")
  }

  @Test
  fun `showOverdueNotification creates notification with overdue info`() {
    notificationHelper.showOverdueNotification(
      taskId = "task456",
      title = "Overdue Task",
      daysOverdue = 3,
    )

    val notifications = shadowNotificationManager.allNotifications
    assertThat(notifications).hasSize(1)

    val notification = notifications[0]
    assertThat(notification.extras.getString("android.title")).contains("Overdue")
    assertThat(notification.extras.getString("android.text")).contains("3 day(s) overdue")
  }

  @Test
  fun `showDailySummaryNotification creates summary with stats`() {
    notificationHelper.showDailySummaryNotification(
      completedCount = 5,
      pendingCount = 3,
      overdueCount = 1,
    )

    val notifications = shadowNotificationManager.allNotifications
    assertThat(notifications).hasSize(1)

    val notification = notifications[0]
    assertThat(notification.extras.getString("android.title")).isEqualTo("Daily Summary")
    val text = notification.extras.getString("android.text")
    assertThat(text).contains("5 completed")
    assertThat(text).contains("3 pending")
    assertThat(text).contains("1 overdue")
  }

  @Test
  fun `cancelNotification removes notification`() {
    notificationHelper.showReminderNotification(
      taskId = "task789",
      title = "Test Task",
      description = null,
    )

    assertThat(shadowNotificationManager.allNotifications).hasSize(1)

    notificationHelper.cancelNotification(1000 + "task789".hashCode())

    assertThat(shadowNotificationManager.allNotifications).isEmpty()
  }
}
