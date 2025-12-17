package com.ctonew.taskmanagement.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NotificationWorkManagerTest {
  private lateinit var context: Context
  private lateinit var workManager: WorkManager
  private lateinit var testDriver: TestDriver

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()

    val config = Configuration.Builder()
      .setMinimumLoggingLevel(android.util.Log.DEBUG)
      .setExecutor(SynchronousExecutor())
      .build()

    WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

    workManager = WorkManager.getInstance(context)
    testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
  }

  @Test
  fun testPeriodicReminderSyncIsScheduled() {
    val notificationScheduler = NotificationScheduler(context)
    notificationScheduler.startPeriodicReminderSync()

    val workInfos = workManager.getWorkInfosByTag(ReminderSyncWorker.WORK_NAME).get()
    assertThat(workInfos).isNotEmpty()

    val workInfo = workInfos[0]
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testPeriodicDailySummaryIsScheduled() {
    val notificationScheduler = NotificationScheduler(context)
    notificationScheduler.startPeriodicDailySummary()

    val workInfos = workManager.getWorkInfosByTag(DailySummaryWorker.WORK_NAME).get()
    assertThat(workInfos).isNotEmpty()

    val workInfo = workInfos[0]
    assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)
  }

  @Test
  fun testPeriodicWorkRepeatInterval() {
    val notificationScheduler = NotificationScheduler(context)
    notificationScheduler.startPeriodicReminderSync()

    val workInfos = workManager.getWorkInfosByTag(ReminderSyncWorker.WORK_NAME).get()
    val workInfo = workInfos[0]

    testDriver.setInitialDelayMet(workInfo.id)

    val delayMs = testDriver.getPeriodDelayMet(workInfo.id)

    assertThat(delayMs).isAtLeast(TimeUnit.MINUTES.toMillis(15))
  }
}
