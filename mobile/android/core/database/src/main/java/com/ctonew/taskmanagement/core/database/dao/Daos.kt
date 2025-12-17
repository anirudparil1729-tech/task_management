package com.ctonew.taskmanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Query("SELECT * FROM tasks ORDER BY createdAtMillis DESC")
  fun observeTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM tasks WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): TaskEntity?

  @Query("SELECT * FROM tasks WHERE remoteId = :remoteId LIMIT 1")
  suspend fun getByRemoteId(remoteId: Int): TaskEntity?

  @Query(
    """
    SELECT * FROM tasks
    WHERE isCompleted = 0
      AND dueDateMillis IS NOT NULL
      AND dueDateMillis >= :startMillis
      AND dueDateMillis <= :endMillis
    ORDER BY dueDateMillis ASC
    """,
  )
  fun getTasksDueWithinTimeframe(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

  @Query(
    """
    SELECT * FROM tasks
    WHERE isCompleted = 0
      AND dueDateMillis IS NOT NULL
      AND dueDateMillis < :currentTimeMillis
    ORDER BY dueDateMillis ASC
    """,
  )
  fun getOverdueTasks(currentTimeMillis: Long): Flow<List<TaskEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(task: TaskEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(tasks: List<TaskEntity>)

  @Query(
    """
    UPDATE tasks
    SET
      remoteId = :remoteId,
      updatedAtMillis = :updatedAtMillis,
      modifiedAtMillis = :modifiedAtMillis
    WHERE localId = :localId
    """,
  )
  suspend fun updateRemoteId(
    localId: String,
    remoteId: Int,
    updatedAtMillis: Long,
    modifiedAtMillis: Long,
  )

  @Query("DELETE FROM tasks WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories ORDER BY name")
  fun observeCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): CategoryEntity?

  @Query("SELECT * FROM categories WHERE remoteId = :remoteId LIMIT 1")
  suspend fun getByRemoteId(remoteId: Int): CategoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: CategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entities: List<CategoryEntity>)

  @Query("DELETE FROM categories WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)
}

@Dao
interface TimeBlockDao {
  @Query("SELECT * FROM time_blocks ORDER BY startTimeMillis DESC")
  fun observeTimeBlocks(): Flow<List<TimeBlockEntity>>

  @Query("SELECT * FROM time_blocks WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): TimeBlockEntity?

  @Query("SELECT * FROM time_blocks WHERE remoteId = :remoteId LIMIT 1")
  suspend fun getByRemoteId(remoteId: Int): TimeBlockEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: TimeBlockEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(entities: List<TimeBlockEntity>)

  @Query("DELETE FROM time_blocks WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)

  @Query(
    """
    SELECT * FROM time_blocks
    WHERE startTimeMillis >= :startOfDayMillis
      AND startTimeMillis < :endOfDayMillis
    ORDER BY startTimeMillis ASC
    """,
  )
  suspend fun getTimeBlocksForDate(startOfDayMillis: Long, endOfDayMillis: Long): List<TimeBlockEntity>

  @Query(
    """
    SELECT * FROM time_blocks
    WHERE startTimeMillis >= :startOfWeekMillis
      AND startTimeMillis < :endOfWeekMillis
    ORDER BY startTimeMillis ASC
    """,
  )
  suspend fun getTimeBlocksForWeek(startOfWeekMillis: Long, endOfWeekMillis: Long): List<TimeBlockEntity>
}

@Dao
interface OutboxDao {
  @Query("SELECT * FROM outbox ORDER BY createdAtMillis ASC")
  suspend fun getPending(): List<OutboxEntity>

  @Query("SELECT COUNT(*) FROM outbox")
  fun observePendingCount(): Flow<Int>

  @Query(
    """
    SELECT COUNT(*)
    FROM outbox
    WHERE entityType = :entityType AND localId = :localId
    """,
  )
  suspend fun countPendingForEntity(entityType: OutboxEntityType, localId: String): Int

  @Query(
    """
    SELECT *
    FROM outbox
    WHERE
      entityType = :entityType AND
      localId = :localId AND
      operation = :operation
    ORDER BY createdAtMillis ASC
    LIMIT 1
    """,
  )
  suspend fun findFirstForEntityAndOperation(
    entityType: OutboxEntityType,
    localId: String,
    operation: OutboxOperation,
  ): OutboxEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entity: OutboxEntity): Long

  @Update
  suspend fun update(entity: OutboxEntity)

  @Query("DELETE FROM outbox WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM outbox WHERE entityType = :entityType AND localId = :localId")
  suspend fun deleteForEntity(entityType: OutboxEntityType, localId: String)
}

@Dao
interface ReminderDao {
  @Query("SELECT * FROM reminders ORDER BY reminderTimeMillis ASC")
  fun observeReminders(): Flow<List<com.ctonew.taskmanagement.core.database.model.ReminderEntity>>

  @Query("SELECT * FROM reminders WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): com.ctonew.taskmanagement.core.database.model.ReminderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: com.ctonew.taskmanagement.core.database.model.ReminderEntity)

  @Query("DELETE FROM reminders WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)

  @Query(
    """
    SELECT * FROM reminders
    WHERE reminderTimeMillis <= :currentTimeMillis
      AND reminderTimeMillis IS NOT NULL
    ORDER BY reminderTimeMillis ASC
    """,
  )
  suspend fun getDueReminders(currentTimeMillis: Long): List<com.ctonew.taskmanagement.core.database.model.ReminderEntity>
}

@Dao
interface FocusSessionDao {
  @Query("SELECT * FROM focus_sessions ORDER BY startedAtMillis DESC")
  fun observeFocusSessions(): Flow<List<com.ctonew.taskmanagement.core.database.model.FocusSessionEntity>>

  @Query("SELECT * FROM focus_sessions WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): com.ctonew.taskmanagement.core.database.model.FocusSessionEntity?

  @Query(
    """
    SELECT * FROM focus_sessions
    WHERE startedAtMillis >= :startOfDayMillis
      AND startedAtMillis < :endOfDayMillis
    ORDER BY startedAtMillis DESC
    """,
  )
  suspend fun getFocusSessionsForDate(startOfDayMillis: Long, endOfDayMillis: Long): List<com.ctonew.taskmanagement.core.database.model.FocusSessionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: com.ctonew.taskmanagement.core.database.model.FocusSessionEntity)

  @Query("DELETE FROM focus_sessions WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)
}

@Dao
interface ProductivityLogDao {
  @Query("SELECT * FROM productivity_logs ORDER BY modifiedAtMillis DESC")
  fun observeProductivityLogs(): Flow<List<com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity>>

  @Query("SELECT * FROM productivity_logs WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(entity: com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity)

  @Query("DELETE FROM productivity_logs WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)

  @Query(
    """
    SELECT * FROM productivity_logs
    WHERE modifiedAtMillis >= :startOfDayMillis
      AND modifiedAtMillis < :endOfDayMillis
    ORDER BY modifiedAtMillis DESC
    """,
  )
  suspend fun getProductivityLogsForDate(startOfDayMillis: Long, endOfDayMillis: Long): List<com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity>
}
