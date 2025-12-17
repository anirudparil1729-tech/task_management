package com.ctonew.taskmanagement

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ctonew.taskmanagement.notifications.NotificationChannelManager
import com.ctonew.taskmanagement.notifications.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskManagementApplication : Application(), Configuration.Provider {
  @Inject
  lateinit var workerFactory: HiltWorkerFactory

  @Inject
  lateinit var notificationChannelManager: NotificationChannelManager

  @Inject
  lateinit var notificationScheduler: NotificationScheduler

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()

  override fun onCreate() {
    super.onCreate()
    notificationChannelManager.createNotificationChannels()
    notificationScheduler.startPeriodicReminderSync()
    notificationScheduler.startPeriodicDailySummary()
  }
}
