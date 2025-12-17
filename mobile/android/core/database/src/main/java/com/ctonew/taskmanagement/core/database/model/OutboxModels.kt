package com.ctonew.taskmanagement.core.database.model

enum class OutboxEntityType {
  TASK,
  SUBTASK,
  CATEGORY,
  TIME_BLOCK,
  PRODUCTIVITY_LOG,
  REMINDER,
  FOCUS_SESSION,
}

enum class OutboxOperation {
  CREATE,
  UPDATE,
  DELETE,
}
