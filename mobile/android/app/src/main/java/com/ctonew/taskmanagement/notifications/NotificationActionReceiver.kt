package com.ctonew.taskmanagement.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ctonew.taskmanagement.core.database.repository.TasksRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
  @Inject
  lateinit var tasksRepository: TasksRepository

  @Inject
  lateinit var notificationHelper: NotificationHelper

  private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onReceive(context: Context, intent: Intent) {
    val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
    val action = intent.action ?: return

    Log.d(TAG, "Notification action received: $action for task: $taskId")

    when (action) {
      ACTION_COMPLETE -> handleCompleteAction(taskId)
      ACTION_SNOOZE -> handleSnoozeAction(context, taskId)
    }
  }

  private fun handleCompleteAction(taskId: String) {
    receiverScope.launch {
      try {
        tasksRepository.markTaskCompleted(taskId, true)
        Log.d(TAG, "Task marked as completed: $taskId")
      } catch (e: Exception) {
        Log.e(TAG, "Error completing task: $taskId", e)
      }
    }
  }

  private fun handleSnoozeAction(context: Context, taskId: String) {
    val snoozeMinutes = 15
    val workData = workDataOf(
      ReminderSyncWorker.KEY_TASK_ID to taskId,
      ReminderSyncWorker.KEY_SNOOZE_MINUTES to snoozeMinutes,
    )

    val snoozeWork = OneTimeWorkRequestBuilder<ReminderSyncWorker>()
      .setInputData(workData)
      .addTag("snooze_$taskId")
      .build()

    WorkManager.getInstance(context).enqueue(snoozeWork)

    notificationHelper.cancelNotification(1000 + taskId.hashCode())
    Log.d(TAG, "Task snoozed for $snoozeMinutes minutes: $taskId")
  }

  fun onDestroy() {
    receiverScope.cancel()
  }

  companion object {
    private const val TAG = "NotificationAction"
    private const val ACTION_COMPLETE = "com.ctonew.taskmanagement.ACTION_COMPLETE"
    private const val ACTION_SNOOZE = "com.ctonew.taskmanagement.ACTION_SNOOZE"
    private const val EXTRA_TASK_ID = "task_id"

    fun createCompleteIntent(context: Context, taskId: String): PendingIntent {
      val intent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_COMPLETE
        putExtra(EXTRA_TASK_ID, taskId)
      }
      return PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    }

    fun createSnoozeIntent(context: Context, taskId: String): PendingIntent {
      val intent = Intent(context, NotificationActionReceiver::class.java).apply {
        action = ACTION_SNOOZE
        putExtra(EXTRA_TASK_ID, taskId)
      }
      return PendingIntent.getBroadcast(
        context,
        taskId.hashCode() + 1,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    }
  }
}
