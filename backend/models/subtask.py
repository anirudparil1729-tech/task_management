from datetime import datetime

from sqlmodel import Field, SQLModel

from utils import utcnow


class SubTask(SQLModel, table=True):
    __tablename__ = "subtasks"

    id: int | None = Field(default=None, primary_key=True)
    task_id: int = Field(foreign_key="tasks.id", index=True, ondelete="CASCADE")
    title: str = Field(max_length=255)
    is_completed: bool = Field(default=False)
    order: int = Field(default=0)
    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
