package com.ctonew.taskmanagement.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ctonew.taskmanagement.MainActivity
import com.ctonew.taskmanagement.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  private val notificationManager = NotificationManagerCompat.from(context)

  fun showReminderNotification(
    taskId: String,
    title: String,
    description: String?,
  ) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      putExtra("task_id", taskId)
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val completeIntent = NotificationActionReceiver.createCompleteIntent(context, taskId)
    val snoozeIntent = NotificationActionReceiver.createSnoozeIntent(context, taskId)

    val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_REMINDERS)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(title)
      .setContentText(description ?: "You have a task due soon")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .addAction(
        R.drawable.ic_check,
        "Complete",
        completeIntent,
      )
      .addAction(
        R.drawable.ic_snooze,
        "Snooze",
        snoozeIntent,
      )
      .build()

    notificationManager.notify(NOTIFICATION_ID_REMINDER_BASE + taskId.hashCode(), notification)
  }

  fun showOverdueNotification(
    taskId: String,
    title: String,
    daysOverdue: Int,
  ) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      putExtra("task_id", taskId)
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      taskId.hashCode(),
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val completeIntent = NotificationActionReceiver.createCompleteIntent(context, taskId)

    val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_OVERDUE)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("⚠️ Overdue: $title")
      .setContentText("This task is $daysOverdue day(s) overdue")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .addAction(
        R.drawable.ic_check,
        "Complete",
        completeIntent,
      )
      .build()

    notificationManager.notify(NOTIFICATION_ID_OVERDUE_BASE + taskId.hashCode(), notification)
  }

  fun showDailySummaryNotification(
    completedCount: Int,
    pendingCount: Int,
    overdueCount: Int,
  ) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val summaryText = buildString {
      append("✅ $completedCount completed")
      if (pendingCount > 0) append(" • 📋 $pendingCount pending")
      if (overdueCount > 0) append(" • ⚠️ $overdueCount overdue")
    }

    val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_DAILY_SUMMARY)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Daily Summary")
      .setContentText(summaryText)
      .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(NOTIFICATION_ID_DAILY_SUMMARY, notification)
  }

  fun showFocusNudgeNotification(message: String) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_FOCUS)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Focus Mode")
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(NOTIFICATION_ID_FOCUS_NUDGE, notification)
  }

  fun showFocusCompletionNotification(durationMinutes: Int) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_FOCUS)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Focus Session Complete!")
      .setContentText("Great job! You focused for $durationMinutes minutes.")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    notificationManager.notify(NOTIFICATION_ID_FOCUS_COMPLETE, notification)
  }

  fun cancelNotification(notificationId: Int) {
    notificationManager.cancel(notificationId)
  }

  companion object {
    private const val NOTIFICATION_ID_REMINDER_BASE = 1000
    private const val NOTIFICATION_ID_OVERDUE_BASE = 2000
    private const val NOTIFICATION_ID_DAILY_SUMMARY = 3000
    private const val NOTIFICATION_ID_FOCUS_NUDGE = 4000
    private const val NOTIFICATION_ID_FOCUS_COMPLETE = 4001
  }
}
