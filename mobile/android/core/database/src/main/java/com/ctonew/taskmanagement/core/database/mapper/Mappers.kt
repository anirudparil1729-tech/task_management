package com.ctonew.taskmanagement.core.database.mapper

import com.ctonew.taskmanagement.core.database.model.CategoryEntity
import com.ctonew.taskmanagement.core.database.model.TaskEntity
import com.ctonew.taskmanagement.core.database.model.TimeBlockEntity
import com.ctonew.taskmanagement.core.network.dto.CategoryCreateDto
import com.ctonew.taskmanagement.core.network.dto.CategoryDto
import com.ctonew.taskmanagement.core.network.dto.TaskCreateDto
import com.ctonew.taskmanagement.core.network.dto.TaskDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockCreateDto
import com.ctonew.taskmanagement.core.network.dto.TimeBlockDto
import java.time.Instant
import java.util.UUID

internal fun TaskDto.toEntity(existingLocalId: String? = null): TaskEntity {
  val localId = existingLocalId ?: UUID.randomUUID().toString()
  return TaskEntity(
    localId = localId,
    remoteId = id,
    title = title,
    description = description,
    notes = notes,
    dueDateMillis = dueDate?.toEpochMilli(),
    priority = priority,
    recurrenceRule = recurrenceRule,
    categoryRemoteId = categoryId,
    reminderTimeMillis = reminderTime?.toEpochMilli(),
    isCompleted = isCompleted,
    completedAtMillis = completedAt?.toEpochMilli(),
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    modifiedAtMillis = updatedAt.toEpochMilli(),
  )
}

internal fun TaskEntity.toCreateDto(): TaskCreateDto = TaskCreateDto(
  title = title,
  description = description,
  notes = notes,
  dueDate = dueDateMillis?.let { Instant.ofEpochMilli(it) },
  priority = priority,
  recurrenceRule = recurrenceRule,
  categoryId = categoryRemoteId,
  reminderTime = reminderTimeMillis?.let { Instant.ofEpochMilli(it) },
)


internal fun CategoryDto.toEntity(existingLocalId: String? = null): CategoryEntity {
  val localId = existingLocalId ?: UUID.randomUUID().toString()
  return CategoryEntity(
    localId = localId,
    remoteId = id,
    name = name,
    color = color,
    icon = icon,
    isDefault = isDefault,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    modifiedAtMillis = updatedAt.toEpochMilli(),
  )
}

internal fun CategoryEntity.toCreateDto(): CategoryCreateDto = CategoryCreateDto(
  name = name,
  color = color,
  icon = icon,
  isDefault = isDefault,
)

internal fun TimeBlockDto.toEntity(existingLocalId: String? = null): TimeBlockEntity {
  val localId = existingLocalId ?: UUID.randomUUID().toString()
  return TimeBlockEntity(
    localId = localId,
    remoteId = id,
    taskRemoteId = taskId,
    startTimeMillis = startTime.toEpochMilli(),
    endTimeMillis = endTime.toEpochMilli(),
    estimatedDurationMillis = null, // Not available in DTO
    actualDurationMillis = null, // Not available in DTO
    title = title,
    description = description,
    createdAtMillis = createdAt.toEpochMilli(),
    updatedAtMillis = updatedAt.toEpochMilli(),
    modifiedAtMillis = updatedAt.toEpochMilli(),
  )
}

internal fun TimeBlockEntity.toCreateDto(): TimeBlockCreateDto = TimeBlockCreateDto(
  taskId = taskRemoteId,
  startTime = Instant.ofEpochMilli(startTimeMillis),
  endTime = Instant.ofEpochMilli(endTimeMillis),
  estimatedDurationMillis = estimatedDurationMillis,
  actualDurationMillis = actualDurationMillis,
  title = title,
  description = description,
)
