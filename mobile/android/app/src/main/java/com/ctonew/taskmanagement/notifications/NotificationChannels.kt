package com.ctonew.taskmanagement.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channels = listOf(
        NotificationChannel(
          CHANNEL_REMINDERS,
          "Reminders",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Task reminders and alerts"
          enableVibration(true)
          setShowBadge(true)
        },
        NotificationChannel(
          CHANNEL_OVERDUE,
          "Overdue Tasks",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Notifications for overdue tasks"
          enableVibration(true)
          setShowBadge(true)
        },
        NotificationChannel(
          CHANNEL_FOCUS,
          "Focus Timer",
          NotificationManager.IMPORTANCE_LOW,
        ).apply {
          description = "Focus session notifications"
          setShowBadge(false)
        },
        NotificationChannel(
          CHANNEL_DAILY_SUMMARY,
          "Daily Summary",
          NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
          description = "Daily productivity summaries"
          setShowBadge(true)
        },
      )

      val notificationManager = NotificationManagerCompat.from(context)
      channels.forEach { channel ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          notificationManager.createNotificationChannel(channel)
        }
      }
    }
  }

  companion object {
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_OVERDUE = "overdue"
    const val CHANNEL_FOCUS = "focus"
    const val CHANNEL_DAILY_SUMMARY = "daily_summary"
  }
}
