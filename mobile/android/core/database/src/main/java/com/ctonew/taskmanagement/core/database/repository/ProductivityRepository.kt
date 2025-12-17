package com.ctonew.taskmanagement.core.database.repository

import com.ctonew.taskmanagement.core.database.dao.FocusSessionDao
import com.ctonew.taskmanagement.core.database.dao.ProductivityLogDao
import com.ctonew.taskmanagement.core.database.dao.ReminderDao
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.model.FocusSessionEntity
import com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity
import com.ctonew.taskmanagement.core.database.model.ReminderEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface ProductivityRepository {
  val focusSessions: Flow<List<FocusSessionEntity>>
  val reminders: Flow<List<ReminderEntity>>
  val productivityLogs: Flow<List<ProductivityLogEntity>>

  suspend fun createFocusSession(startedAtMillis: Long, plannedDurationMinutes: Int): String
  suspend fun completeFocusSession(localId: String, endedAtMillis: Long)
  suspend fun deleteFocusSession(localId: String)

  suspend fun getFocusSessionsForDate(targetDate: LocalDate): List<FocusSessionEntity>
  suspend fun getProductivityScoreForDate(targetDate: LocalDate): Double

  suspend fun createReminder(
    title: String,
    description: String? = null,
    reminderTimeMillis: Long,
    taskLocalId: String? = null,
  ): String

  suspend fun deleteReminder(localId: String)
  suspend fun getDueReminders(currentTimeMillis: Long): List<ReminderEntity>

  suspend fun logProductivityData(data: Map<String, Any>, date: LocalDate)
  suspend fun getProductivityLogsForDate(targetDate: LocalDate): List<ProductivityLogEntity>
}

@Singleton
class DefaultProductivityRepository @Inject constructor(
  private val focusSessionDao: FocusSessionDao,
  private val reminderDao: ReminderDao,
  private val productivityLogDao: ProductivityLogDao,
  private val taskDao: TaskDao,
) : ProductivityRepository {
  override val focusSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.observeFocusSessions()
  override val reminders: Flow<List<ReminderEntity>> = reminderDao.observeReminders()
  override val productivityLogs: Flow<List<ProductivityLogEntity>> = productivityLogDao.observeProductivityLogs()

  override suspend fun createFocusSession(startedAtMillis: Long, plannedDurationMinutes: Int): String {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    val entity = FocusSessionEntity(
      localId = localId,
      startedAtMillis = startedAtMillis,
      endedAtMillis = null,
      modifiedAtMillis = now,
    )

    focusSessionDao.upsert(entity)
    return localId
  }

  override suspend fun completeFocusSession(localId: String, endedAtMillis: Long) {
    val existing = focusSessionDao.getByLocalId(localId) ?: return
    val now = Instant.now().toEpochMilli()

    val updated = existing.copy(
      endedAtMillis = endedAtMillis,
      modifiedAtMillis = now,
    )

    focusSessionDao.upsert(updated)
  }

  override suspend fun deleteFocusSession(localId: String) {
    focusSessionDao.deleteByLocalId(localId)
  }

  override suspend fun getFocusSessionsForDate(targetDate: LocalDate): List<FocusSessionEntity> {
    val zoneId = ZoneId.systemDefault()
    val startOfDay = targetDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    
    return focusSessionDao.getFocusSessionsForDate(startOfDay, endOfDay)
  }

  override suspend fun getProductivityScoreForDate(targetDate: LocalDate): Double {
    val tasksForDate = taskDao.getTasksDueWithinTimeframe(
      targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
      targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val completedTasks = tasksForDate.count { it.isCompleted }
    val totalTasks = tasksForDate.size
    val focusSessions = getFocusSessionsForDate(targetDate)

    // Calculate productivity score similar to frontend logic
    val completionRate = if (totalTasks > 0) (completedTasks.toDouble() / totalTasks) * 70.0 else 0.0
    val focusBonus = (focusSessions.size * 5.0).coerceAtMost(30.0)

    return (completionRate + focusBonus).coerceIn(0.0, 100.0)
  }

  override suspend fun createReminder(
    title: String,
    description: String?,
    reminderTimeMillis: Long,
    taskLocalId: String?,
  ): String {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    val entity = ReminderEntity(
      localId = localId,
      taskLocalId = taskLocalId,
      reminderTimeMillis = reminderTimeMillis,
      modifiedAtMillis = now,
    )

    reminderDao.upsert(entity)
    return localId
  }

  override suspend fun deleteReminder(localId: String) {
    reminderDao.deleteByLocalId(localId)
  }

  override suspend fun getDueReminders(currentTimeMillis: Long): List<ReminderEntity> {
    return reminderDao.getDueReminders(currentTimeMillis)
  }

  override suspend fun logProductivityData(data: Map<String, Any>, date: LocalDate) {
    val now = Instant.now().toEpochMilli()
    val localId = UUID.randomUUID().toString()

    // Serialize data to JSON string
    val moshi = com.squareup.moshi.Moshi.Builder().build()
    val adapter = moshi.adapter<Map<String, Any>>(Map::class.java)
    val payloadJson = adapter.toJson(data)

    val entity = ProductivityLogEntity(
      localId = localId,
      modifiedAtMillis = now,
      payload = payloadJson,
    )

    productivityLogDao.upsert(entity)
  }

  override suspend fun getProductivityLogsForDate(targetDate: LocalDate): List<ProductivityLogEntity> {
    val zoneId = ZoneId.systemDefault()
    val startOfDay = targetDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endOfDay = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    
    return productivityLogDao.getProductivityLogsForDate(startOfDay, endOfDay)
  }
}