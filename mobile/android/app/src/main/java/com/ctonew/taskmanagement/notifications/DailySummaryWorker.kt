package com.ctonew.taskmanagement.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val taskDao: TaskDao,
  private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "DailySummaryWorker started")

      val now = System.currentTimeMillis()
      val startOfDay = getStartOfDay(now)

      val allTasks = taskDao.observeTasks().first()
      val completedToday = allTasks.count {
        it.isCompleted && (it.completedAtMillis ?: 0) >= startOfDay
      }
      val pendingTasks = allTasks.count { !it.isCompleted }
      val overdueTasks = taskDao.getOverdueTasks(now).first().size

      if (completedToday > 0 || pendingTasks > 0 || overdueTasks > 0) {
        notificationHelper.showDailySummaryNotification(
          completedCount = completedToday,
          pendingCount = pendingTasks,
          overdueCount = overdueTasks,
        )
        Log.d(TAG, "Daily summary notification sent: $completedToday completed, $pendingTasks pending, $overdueTasks overdue")
      }

      Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "DailySummaryWorker failed", e)
      Result.retry()
    }
  }

  private fun getStartOfDay(timeMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
      this.timeInMillis = timeMillis
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
  }

  companion object {
    private const val TAG = "DailySummaryWorker"
    const val WORK_NAME = "daily_summary_work"
  }
}
