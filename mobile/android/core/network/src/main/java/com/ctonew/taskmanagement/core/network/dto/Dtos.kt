package com.ctonew.taskmanagement.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class TaskCreateDto(
  val title: String,
  val description: String? = null,
  val notes: String? = null,
  @Json(name = "due_date") val dueDate: Instant? = null,
  val priority: Int = 0,
  @Json(name = "recurrence_rule") val recurrenceRule: String? = null,
  @Json(name = "category_id") val categoryId: Int? = null,
  @Json(name = "reminder_time") val reminderTime: Instant? = null,
)

@JsonClass(generateAdapter = true)
data class TaskUpdateDto(
  val title: String? = null,
  val description: String? = null,
  val notes: String? = null,
  @Json(name = "due_date") val dueDate: Instant? = null,
  val priority: Int? = null,
  @Json(name = "recurrence_rule") val recurrenceRule: String? = null,
  @Json(name = "is_completed") val isCompleted: Boolean? = null,
  @Json(name = "category_id") val categoryId: Int? = null,
  @Json(name = "reminder_time") val reminderTime: Instant? = null,
)

@JsonClass(generateAdapter = true)
data class TaskDto(
  val id: Int,
  val title: String,
  val description: String? = null,
  val notes: String? = null,
  @Json(name = "due_date") val dueDate: Instant? = null,
  val priority: Int = 0,
  @Json(name = "recurrence_rule") val recurrenceRule: String? = null,
  @Json(name = "category_id") val categoryId: Int? = null,
  @Json(name = "reminder_time") val reminderTime: Instant? = null,
  @Json(name = "is_completed") val isCompleted: Boolean,
  @Json(name = "completed_at") val completedAt: Instant? = null,
  @Json(name = "created_at") val createdAt: Instant,
  @Json(name = "updated_at") val updatedAt: Instant,
)

@JsonClass(generateAdapter = true)
data class TaskWithSubTasksDto(
  val id: Int,
  val title: String,
  val description: String? = null,
  val notes: String? = null,
  @Json(name = "due_date") val dueDate: Instant? = null,
  val priority: Int = 0,
  @Json(name = "recurrence_rule") val recurrenceRule: String? = null,
  @Json(name = "category_id") val categoryId: Int? = null,
  @Json(name = "reminder_time") val reminderTime: Instant? = null,
  @Json(name = "is_completed") val isCompleted: Boolean,
  @Json(name = "completed_at") val completedAt: Instant? = null,
  @Json(name = "created_at") val createdAt: Instant,
  @Json(name = "updated_at") val updatedAt: Instant,
  val subtasks: List<SubTaskDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class SubTaskDto(
  val id: Int,
  @Json(name = "task_id") val taskId: Int,
  val title: String,
  @Json(name = "is_completed") val isCompleted: Boolean,
  val order: Int = 0,
  @Json(name = "created_at") val createdAt: Instant,
  @Json(name = "updated_at") val updatedAt: Instant,
)

@JsonClass(generateAdapter = true)
data class CategoryCreateDto(
  val name: String,
  val color: String = "#3B82F6",
  val icon: String? = null,
  @Json(name = "is_default") val isDefault: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class CategoryUpdateDto(
  val name: String? = null,
  val color: String? = null,
  val icon: String? = null,
  @Json(name = "is_default") val isDefault: Boolean? = null,
)

@JsonClass(generateAdapter = true)
data class CategoryDto(
  val id: Int,
  val name: String,
  val color: String,
  val icon: String? = null,
  @Json(name = "is_default") val isDefault: Boolean,
  @Json(name = "created_at") val createdAt: Instant,
  @Json(name = "updated_at") val updatedAt: Instant,
)

@JsonClass(generateAdapter = true)
data class TimeBlockCreateDto(
  @Json(name = "task_id") val taskId: Int? = null,
  @Json(name = "start_time") val startTime: Instant,
  @Json(name = "end_time") val endTime: Instant,
  val title: String? = null,
  val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class TimeBlockUpdateDto(
  @Json(name = "task_id") val taskId: Int? = null,
  @Json(name = "start_time") val startTime: Instant? = null,
  @Json(name = "end_time") val endTime: Instant? = null,
  val title: String? = null,
  val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class TimeBlockDto(
  val id: Int,
  @Json(name = "task_id") val taskId: Int? = null,
  @Json(name = "start_time") val startTime: Instant,
  @Json(name = "end_time") val endTime: Instant,
  val title: String? = null,
  val description: String? = null,
  @Json(name = "created_at") val createdAt: Instant,
  @Json(name = "updated_at") val updatedAt: Instant,
)

@JsonClass(generateAdapter = true)
data class CategoryProductivityDto(
  @Json(name = "category_id") val categoryId: Int? = null,
  @Json(name = "category_name") val categoryName: String? = null,
  @Json(name = "tasks_completed") val tasksCompleted: Int,
  @Json(name = "time_spent") val timeSpent: Int,
  val score: Double,
)

@JsonClass(generateAdapter = true)
data class ProductivitySummaryDto(
  val date: LocalDate,
  @Json(name = "daily_score") val dailyScore: Double,
  @Json(name = "total_tasks_completed") val totalTasksCompleted: Int,
  @Json(name = "total_time_spent") val totalTimeSpent: Int,
  val categories: List<CategoryProductivityDto>,
)

@JsonClass(generateAdapter = true)
data class NextReminderDto(
  @Json(name = "task_id") val taskId: Int? = null,
  @Json(name = "task_title") val taskTitle: String? = null,
  @Json(name = "reminder_time") val reminderTime: Instant? = null,
)
