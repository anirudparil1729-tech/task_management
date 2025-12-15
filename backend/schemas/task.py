from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class TaskBase(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    description: str | None = None
    notes: str | None = None
    due_date: datetime | None = None
    priority: int = Field(default=0, ge=0, le=4)
    recurrence_rule: str | None = Field(default=None, max_length=255)
    category_id: int | None = None
    reminder_time: datetime | None = None


class TaskCreate(TaskBase):
    pass


class TaskUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=255)
    description: str | None = None
    notes: str | None = None
    due_date: datetime | None = None
    priority: int | None = Field(default=None, ge=0, le=4)
    recurrence_rule: str | None = None
    is_completed: bool | None = None
    category_id: int | None = None
    reminder_time: datetime | None = None


class TaskResponse(TaskBase):
    model_config = ConfigDict(from_attributes=True)

    id: int
    is_completed: bool
    completed_at: datetime | None
    created_at: datetime
    updated_at: datetime


class TaskWithSubTasks(TaskResponse):
    subtasks: list["SubTaskResponse"] = []


from schemas.subtask import SubTaskResponse

TaskWithSubTasks.model_rebuild()
