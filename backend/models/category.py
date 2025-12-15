from datetime import datetime

from sqlmodel import Field, SQLModel

from utils import utcnow


class Category(SQLModel, table=True):
    __tablename__ = "categories"

    id: int | None = Field(default=None, primary_key=True)
    name: str = Field(index=True, max_length=100)
    color: str = Field(max_length=7, default="#3B82F6")
    icon: str | None = Field(default=None, max_length=50)
    is_default: bool = Field(default=False)
    created_at: datetime = Field(default_factory=utcnow)
    updated_at: datetime = Field(default_factory=utcnow)
