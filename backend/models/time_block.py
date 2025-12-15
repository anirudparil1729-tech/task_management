from datetime import datetime

from sqlmodel import Field, SQLModel

from utils import utcnow


class TimeBlock(SQLModel, table=True):
    __tablename__ = "time_blocks"

    id: int | None = Field(default=None, primary_key=True)
    task_id: int | None = Field(default=None, foreign_key="tasks.id", index=True)
    start_time: datetime = Field(index=True)
    end_time: datetime = Field(index=True)
    title: str | None = Field(default=None, max_length=255)
    description: str | None = Field(default=None)
    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
