package com.ctonew.taskmanagement.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  private val workManager = WorkManager.getInstance(context)

  fun startPeriodicReminderSync() {
    val reminderWork = PeriodicWorkRequestBuilder<ReminderSyncWorker>(
      repeatInterval = 15,
      repeatIntervalTimeUnit = TimeUnit.MINUTES,
    ).addTag(ReminderSyncWorker.WORK_NAME)
      .build()

    workManager.enqueueUniquePeriodicWork(
      ReminderSyncWorker.WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      reminderWork,
    )
    Log.d(TAG, "Periodic reminder sync started")
  }

  fun startPeriodicDailySummary(hourOfDay: Int = 20) {
    val summaryWork = PeriodicWorkRequestBuilder<DailySummaryWorker>(
      repeatInterval = 1,
      repeatIntervalTimeUnit = TimeUnit.DAYS,
    ).addTag(DailySummaryWorker.WORK_NAME)
      .build()

    workManager.enqueueUniquePeriodicWork(
      DailySummaryWorker.WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      summaryWork,
    )
    Log.d(TAG, "Periodic daily summary started")
  }

  fun scheduleReminder(
    taskId: String,
    taskTitle: String,
    taskDescription: String?,
    reminderTimeMillis: Long,
  ) {
    val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
      action = NotificationAlarmReceiver.ACTION_REMINDER
      putExtra(NotificationAlarmReceiver.EXTRA_TASK_ID, taskId)
      putExtra(NotificationAlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
      putExtra(NotificationAlarmReceiver.EXTRA_TASK_DESCRIPTION, taskDescription)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    alarmManager.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      reminderTimeMillis,
      pendingIntent,
    )
    Log.d(TAG, "Scheduled reminder for task: $taskTitle at $reminderTimeMillis")
  }

  fun cancelReminder(taskId: String) {
    val intent = Intent(context, NotificationAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
    )

    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
      pendingIntent.cancel()
      Log.d(TAG, "Cancelled reminder for task: $taskId")
    }
  }

  companion object {
    private const val TAG = "NotificationScheduler"
  }
}
