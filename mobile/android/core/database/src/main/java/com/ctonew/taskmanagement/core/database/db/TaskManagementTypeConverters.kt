package com.ctonew.taskmanagement.core.database.db

import androidx.room.TypeConverter
import com.ctonew.taskmanagement.core.database.model.OutboxEntityType
import com.ctonew.taskmanagement.core.database.model.OutboxOperation

class TaskManagementTypeConverters {
  @TypeConverter
  fun outboxEntityTypeToString(value: OutboxEntityType): String = value.name

  @TypeConverter
  fun outboxEntityTypeFromString(value: String): OutboxEntityType = OutboxEntityType.valueOf(value)

  @TypeConverter
  fun outboxOperationToString(value: OutboxOperation): String = value.name

  @TypeConverter
  fun outboxOperationFromString(value: String): OutboxOperation = OutboxOperation.valueOf(value)
}
