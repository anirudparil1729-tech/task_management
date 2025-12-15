from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class SubTaskBase(BaseModel):
    title: str = Field(min_length=1, max_length=255)
    is_completed: bool = False
    order: int = 0


class SubTaskCreate(SubTaskBase):
    task_id: int


class SubTaskUpdate(BaseModel):
    title: str | None = Field(default=None, min_length=1, max_length=255)
    is_completed: bool | None = None
    order: int | None = None


class SubTaskResponse(SubTaskBase):
    model_config = ConfigDict(from_attributes=True)

    id: int
    task_id: int
    created_at: datetime
    updated_at: datetime
