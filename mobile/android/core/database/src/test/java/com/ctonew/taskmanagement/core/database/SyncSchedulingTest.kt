package com.ctonew.taskmanagement.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.ctonew.taskmanagement.core.database.sync.SyncEngine
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncSchedulingTest {
  @Test
  fun `schedulePeriodicSync enqueues unique periodic work`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    runCatching { WorkManagerTestInitHelper.initializeTestWorkManager(context) }
    val workManager = WorkManager.getInstance(context)

    val engine = SyncEngine(workManager)
    engine.schedulePeriodicSync()

    val infos = workManager.getWorkInfosForUniqueWork(SyncEngine.PERIODIC_WORK_NAME).get()
    assertThat(infos).isNotEmpty()
    assertThat(infos.first().state).isAnyOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)
  }

  @Test
  fun `requestSyncNow enqueues unique one time work`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    runCatching { WorkManagerTestInitHelper.initializeTestWorkManager(context) }
    val workManager = WorkManager.getInstance(context)

    val engine = SyncEngine(workManager)
    engine.requestSyncNow()

    val infos = workManager.getWorkInfosForUniqueWork(SyncEngine.ONE_TIME_WORK_NAME).get()
    assertThat(infos).isNotEmpty()
  }
}
