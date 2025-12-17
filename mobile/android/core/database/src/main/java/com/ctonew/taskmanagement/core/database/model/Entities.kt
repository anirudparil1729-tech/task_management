package com.ctonew.taskmanagement.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "tasks",
  indices = [Index(value = ["remoteId"], unique = true)],
)
data class TaskEntity(
  @PrimaryKey val localId: String,
  val remoteId: Int? = null,
  val title: String,
  val description: String? = null,
  val notes: String? = null,
  val dueDateMillis: Long? = null,
  val priority: Int = 0,
  val recurrenceRule: String? = null,
  val categoryRemoteId: Int? = null,
  val reminderTimeMillis: Long? = null,
  val isCompleted: Boolean = false,
  val completedAtMillis: Long? = null,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
  val modifiedAtMillis: Long,
)

@Entity(
  tableName = "subtasks",
  indices = [
    Index(value = ["remoteId"], unique = true),
    Index(value = ["taskRemoteId"]),
  ],
)
data class SubTaskEntity(
  @PrimaryKey val localId: String,
  val remoteId: Int? = null,
  val taskRemoteId: Int,
  val title: String,
  val isCompleted: Boolean = false,
  val order: Int = 0,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
  val modifiedAtMillis: Long,
)

@Entity(
  tableName = "categories",
  indices = [Index(value = ["remoteId"], unique = true)],
)
data class CategoryEntity(
  @PrimaryKey val localId: String,
  val remoteId: Int? = null,
  val name: String,
  val color: String,
  val icon: String? = null,
  val isDefault: Boolean = false,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
  val modifiedAtMillis: Long,
)

@Entity(
  tableName = "time_blocks",
  indices = [Index(value = ["remoteId"], unique = true)],
)
data class TimeBlockEntity(
  @PrimaryKey val localId: String,
  val remoteId: Int? = null,
  val taskRemoteId: Int? = null,
  val startTimeMillis: Long,
  val endTimeMillis: Long,
  val estimatedDurationMillis: Long? = null,
  val actualDurationMillis: Long? = null,
  val title: String? = null,
  val description: String? = null,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
  val modifiedAtMillis: Long,
)

@Entity(tableName = "productivity_logs")
data class ProductivityLogEntity(
  @PrimaryKey val localId: String,
  val modifiedAtMillis: Long,
  val payload: String? = null,
)

@Entity(tableName = "reminders")
data class ReminderEntity(
  @PrimaryKey val localId: String,
  val modifiedAtMillis: Long,
  val taskLocalId: String? = null,
  val reminderTimeMillis: Long? = null,
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
  @PrimaryKey val localId: String,
  val modifiedAtMillis: Long,
  val startedAtMillis: Long,
  val endedAtMillis: Long? = null,
)

@Entity(
  tableName = "outbox",
  indices = [Index(value = ["entityType", "localId"])],
)
data class OutboxEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val entityType: OutboxEntityType,
  val operation: OutboxOperation,
  val localId: String,
  val remoteId: Int? = null,
  val payloadJson: String? = null,
  val createdAtMillis: Long,
  val attemptCount: Int = 0,
  val lastAttemptMillis: Long? = null,
)
