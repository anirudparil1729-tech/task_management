package com.ctonew.taskmanagement.notifications

import android.util.Log
import com.ctonew.taskmanagement.core.datastore.NotificationTokenStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskManagementFirebaseService : FirebaseMessagingService() {
  @Inject
  lateinit var notificationTokenStore: NotificationTokenStore

  @Inject
  lateinit var notificationHelper: NotificationHelper

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "FCM token refreshed: $token")
    serviceScope.launch {
      notificationTokenStore.setFcmToken(token)
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d(TAG, "FCM message received from: ${message.from}")

    message.data.let { data ->
      val type = data["type"] ?: return@let
      when (type) {
        "reminder" -> handleReminderNotification(data)
        "daily_summary" -> handleDailySummaryNotification(data)
        "focus_nudge" -> handleFocusNudgeNotification(data)
        "overdue" -> handleOverdueNotification(data)
        else -> Log.w(TAG, "Unknown notification type: $type")
      }
    }
  }

  private fun handleReminderNotification(data: Map<String, String>) {
    val taskId = data["task_id"] ?: return
    val taskTitle = data["task_title"] ?: "Task reminder"
    val taskDescription = data["task_description"]

    notificationHelper.showReminderNotification(
      taskId = taskId,
      title = taskTitle,
      description = taskDescription,
    )
  }

  private fun handleDailySummaryNotification(data: Map<String, String>) {
    val completedCount = data["completed_count"]?.toIntOrNull() ?: 0
    val pendingCount = data["pending_count"]?.toIntOrNull() ?: 0
    val overdueCount = data["overdue_count"]?.toIntOrNull() ?: 0

    notificationHelper.showDailySummaryNotification(
      completedCount = completedCount,
      pendingCount = pendingCount,
      overdueCount = overdueCount,
    )
  }

  private fun handleFocusNudgeNotification(data: Map<String, String>) {
    val message = data["message"] ?: "Time to focus!"
    notificationHelper.showFocusNudgeNotification(message)
  }

  private fun handleOverdueNotification(data: Map<String, String>) {
    val taskId = data["task_id"] ?: return
    val taskTitle = data["task_title"] ?: "Overdue task"
    val daysOverdue = data["days_overdue"]?.toIntOrNull() ?: 1

    notificationHelper.showOverdueNotification(
      taskId = taskId,
      title = taskTitle,
      daysOverdue = daysOverdue,
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceScope.cancel()
  }

  companion object {
    private const val TAG = "TaskMgmtFCMService"
  }
}
