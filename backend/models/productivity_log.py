import datetime as dt
from datetime import datetime

from sqlmodel import Field, SQLModel

from utils import utcnow


class ProductivityLog(SQLModel, table=True):
    __tablename__ = "productivity_logs"

    id: int | None = Field(default=None, primary_key=True)
    date: dt.date = Field(index=True)
    score: float = Field(default=0.0, ge=0.0, le=100.0)
    category_id: int | None = Field(default=None, foreign_key="categories.id", index=True)
    tasks_completed: int = Field(default=0, ge=0)
    time_spent: int = Field(default=0, ge=0)
    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
