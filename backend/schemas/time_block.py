from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field, field_validator


class TimeBlockBase(BaseModel):
    task_id: int | None = None
    start_time: datetime
    end_time: datetime
    title: str | None = Field(default=None, max_length=255)
    description: str | None = None

    @field_validator("end_time")
    @classmethod
    def validate_end_time(cls, v: datetime, info) -> datetime:
        if "start_time" in info.data and v <= info.data["start_time"]:
            raise ValueError("end_time must be after start_time")
        return v


class TimeBlockCreate(TimeBlockBase):
    pass


class TimeBlockUpdate(BaseModel):
    task_id: int | None = None
    start_time: datetime | None = None
    end_time: datetime | None = None
    title: str | None = Field(default=None, max_length=255)
    description: str | None = None


class TimeBlockResponse(TimeBlockBase):
    model_config = ConfigDict(from_attributes=True)

    id: int
    created_at: datetime
    updated_at: datetime
