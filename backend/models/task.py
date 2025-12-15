from datetime import datetime

from sqlmodel import Field, SQLModel

from utils import utcnow


class Task(SQLModel, table=True):
    __tablename__ = "tasks"

    id: int | None = Field(default=None, primary_key=True)
    title: str = Field(index=True, max_length=255)
    description: str | None = Field(default=None)
    notes: str | None = Field(default=None)
    due_date: datetime | None = Field(default=None, index=True)
    priority: int = Field(default=0, ge=0, le=4)
    recurrence_rule: str | None = Field(default=None, max_length=255)
    is_completed: bool = Field(default=False, index=True)
    completed_at: datetime | None = Field(default=None)
    category_id: int | None = Field(default=None, foreign_key="categories.id", index=True)
    reminder_time: datetime | None = Field(default=None, index=True)
    created_at: datetime = Field(default_factory=utcnow, index=True)
    updated_at: datetime = Field(default_factory=utcnow, index=True)
