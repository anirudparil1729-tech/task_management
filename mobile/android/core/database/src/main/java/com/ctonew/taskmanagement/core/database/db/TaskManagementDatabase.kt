package com.ctonew.taskmanagement.core.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ctonew.taskmanagement.core.database.dao.CategoryDao
import com.ctonew.taskmanagement.core.database.dao.OutboxDao
import com.ctonew.taskmanagement.core.database.dao.TaskDao
import com.ctonew.taskmanagement.core.database.dao.TimeBlockDao
import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.FocusSessionEntity
import com.ctonew.taskmanagement.core.database.model.OutboxEntity
import com.ctonew.taskmanagement.core.database.model.ProductivityLogEntity
import com.ctonew.taskmanagement.core.database.model.ReminderEntity
import com.ctonew.taskmanagement.core.database.model.SubTaskEntity
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity

@Database(
  entities = [
    TaskEntity::class,
    SubTaskEntity::class,
    CategoryEntity::class,
    TimeBlockEntity::class,
    ProductivityLogEntity::class,
    ReminderEntity::class,
    FocusSessionEntity::class,
    OutboxEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
@TypeConverters(TaskManagementTypeConverters::class)
abstract class TaskManagementDatabase : RoomDatabase() {
  abstract fun taskDao(): TaskDao
  abstract fun categoryDao(): CategoryDao
  abstract fun timeBlockDao(): TimeBlockDao
  abstract fun outboxDao(): OutboxDao
}
