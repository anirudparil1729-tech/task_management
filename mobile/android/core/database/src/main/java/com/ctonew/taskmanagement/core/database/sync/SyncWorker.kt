package com.ctonew.taskmanagement.core.database.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SyncWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
    return try {
      val entryPoint = EntryPointAccessors.fromApplication(
        applicationContext,
        SyncWorkerEntryPoint::class.java,
      )

      when (entryPoint.syncProcessor().syncOnce()) {
        SyncOutcome.Success -> Result.success()
        SyncOutcome.Retry -> Result.retry()
        SyncOutcome.Failure -> Result.failure()
      }
    } catch (_: Throwable) {
      Result.retry()
    }
  }

  @EntryPoint
  @InstallIn(SingletonComponent::class)
  interface SyncWorkerEntryPoint {
    fun syncProcessor(): SyncProcessor
  }
}
