package com.ctonew.taskmanagement.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderSyncWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val taskDao: TaskDao,
  private val notificationHelper: NotificationHelper,
  private val notificationScheduler: NotificationScheduler,
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      Log.d(TAG, "ReminderSyncWorker started")

      val now = System.currentTimeMillis()
      val upcomingTasks = taskDao.getTasksDueWithinTimeframe(
        startMillis = now,
        endMillis = now + TimeUnit.HOURS.toMillis(24),
      ).first()

      upcomingTasks.forEach { task ->
        if (!task.isCompleted) {
          scheduleReminderForTask(task, now)
        }
      }

      checkOverdueTasks(now)

      Log.d(TAG, "ReminderSyncWorker completed successfully")
      Result.success()
    } catch (e: Exception) {
      Log.e(TAG, "ReminderSyncWorker failed", e)
      Result.retry()
    }
  }

  private suspend fun scheduleReminderForTask(task: TaskEntity, now: Long) {
    val reminderTime = task.reminderTimeMillis ?: task.dueDateMillis ?: return

    if (reminderTime > now) {
      notificationScheduler.scheduleReminder(
        taskId = task.localId,
        taskTitle = task.title,
        taskDescription = task.description,
        reminderTimeMillis = reminderTime,
      )
      Log.d(TAG, "Scheduled reminder for task: ${task.title} at $reminderTime")
    } else if (reminderTime <= now && reminderTime > now - TimeUnit.HOURS.toMillis(1)) {
      notificationHelper.showReminderNotification(
        taskId = task.localId,
        title = task.title,
        description = task.description,
      )
      Log.d(TAG, "Showed immediate reminder for task: ${task.title}")
    }
  }

  private suspend fun checkOverdueTasks(now: Long) {
    val overdueTasks = taskDao.getOverdueTasks(now).first()

    overdueTasks.forEach { task ->
      if (!task.isCompleted) {
        val daysOverdue = TimeUnit.MILLISECONDS.toDays(now - (task.dueDateMillis ?: now)).toInt()
        notificationHelper.showOverdueNotification(
          taskId = task.localId,
          title = task.title,
          daysOverdue = maxOf(1, daysOverdue),
        )
        Log.d(TAG, "Showed overdue notification for task: ${task.title}")
      }
    }
  }

  companion object {
    private const val TAG = "ReminderSyncWorker"
    const val WORK_NAME = "reminder_sync_work"
    const val KEY_TASK_ID = "task_id"
    const val KEY_SNOOZE_MINUTES = "snooze_minutes"
  }
}
