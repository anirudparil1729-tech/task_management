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
