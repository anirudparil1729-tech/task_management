package com.ctonew.taskmanagement.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationAlarmReceiver : BroadcastReceiver() {
  @Inject
  lateinit var notificationHelper: NotificationHelper

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action ?: return

    Log.d(TAG, "Alarm received: $action")

    when (action) {
      ACTION_REMINDER -> handleReminderAlarm(intent)
    }
  }

  private fun handleReminderAlarm(intent: Intent) {
    val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
    val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
    val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION)

    notificationHelper.showReminderNotification(
      taskId = taskId,
      title = taskTitle,
      description = taskDescription,
    )
    Log.d(TAG, "Showed reminder notification for task: $taskTitle")
  }

  companion object {
    private const val TAG = "NotificationAlarm"
    const val ACTION_REMINDER = "com.ctonew.taskmanagement.ACTION_REMINDER"
    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_TASK_TITLE = "task_title"
    const val EXTRA_TASK_DESCRIPTION = "task_description"
  }
}
