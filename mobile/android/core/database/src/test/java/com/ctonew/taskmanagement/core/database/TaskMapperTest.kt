package com.ctonew.taskmanagement.core.database

import com.ctonew.taskmanagement.core.database.mapper.toEntity
import com.ctonew.taskmanagement.core.network.dto.TaskDto
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class TaskMapperTest {
  @Test
  fun `maps TaskDto to TaskEntity`() {
    val now = Instant.parse("2025-01-01T00:00:00Z")
    val dto = TaskDto(
      id = 1,
      title = "Hello",
      description = "Desc",
      notes = null,
      dueDate = now,
      priority = 2,
      recurrenceRule = null,
      categoryId = 5,
      reminderTime = null,
      isCompleted = false,
      completedAt = null,
      createdAt = now,
      updatedAt = now,
    )

    val entity = dto.toEntity(existingLocalId = "local")

    assertThat(entity.localId).isEqualTo("local")
    assertThat(entity.remoteId).isEqualTo(1)
    assertThat(entity.title).isEqualTo("Hello")
    assertThat(entity.dueDateMillis).isEqualTo(now.toEpochMilli())
    assertThat(entity.categoryRemoteId).isEqualTo(5)
  }
}
