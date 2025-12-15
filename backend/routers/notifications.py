from datetime import datetime

from fastapi import APIRouter, Depends
from pydantic import BaseModel
from sqlmodel import Session, select

from database import get_session
from middleware import verify_api_key
from models import Task
from utils import utcnow

router = APIRouter(
    prefix="/api/notifications", tags=["notifications"], dependencies=[Depends(verify_api_key)]
)


class NextReminderResponse(BaseModel):
    task_id: int | None
    task_title: str | None
    reminder_time: datetime | None


@router.get("/next-reminder", response_model=NextReminderResponse)
def get_next_reminder(session: Session = Depends(get_session)):
    now = utcnow()

    query = (
        select(Task)
        .where(Task.reminder_time > now, Task.is_completed == False)
        .order_by(Task.reminder_time)
        .limit(1)
    )

    next_task = session.exec(query).first()

    if next_task:
        return NextReminderResponse(
            task_id=next_task.id,
            task_title=next_task.title,
            reminder_time=next_task.reminder_time,
        )

    return NextReminderResponse(task_id=None, task_title=None, reminder_time=None)
