from datetime import datetime

from fastapi import APIRouter, Depends, Query
from sqlmodel import Session, select

from database import get_session
from middleware import verify_api_key
from models import Category, Task, TimeBlock
from schemas import CategoryResponse, TaskResponse, TimeBlockResponse

router = APIRouter(prefix="/api/sync", tags=["sync"], dependencies=[Depends(verify_api_key)])


@router.get("/tasks", response_model=list[TaskResponse])
def sync_tasks(
    modified_since: datetime | None = Query(None, description="Get tasks modified after this timestamp"),
    session: Session = Depends(get_session),
):
    query = select(Task)

    if modified_since:
        query = query.where(Task.updated_at > modified_since)

    query = query.order_by(Task.updated_at.desc())

    tasks = session.exec(query).all()
    return tasks


@router.get("/categories", response_model=list[CategoryResponse])
def sync_categories(
    modified_since: datetime | None = Query(None, description="Get categories modified after this timestamp"),
    session: Session = Depends(get_session),
):
    query = select(Category)

    if modified_since:
        query = query.where(Category.updated_at > modified_since)

    query = query.order_by(Category.updated_at.desc())

    categories = session.exec(query).all()
    return categories


@router.get("/time-blocks", response_model=list[TimeBlockResponse])
def sync_time_blocks(
    modified_since: datetime | None = Query(None, description="Get time blocks modified after this timestamp"),
    session: Session = Depends(get_session),
):
    query = select(TimeBlock)

    if modified_since:
        query = query.where(TimeBlock.updated_at > modified_since)

    query = query.order_by(TimeBlock.updated_at.desc())

    time_blocks = session.exec(query).all()
    return time_blocks
