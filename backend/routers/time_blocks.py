from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from database import get_session
from middleware import verify_api_key
from models import TimeBlock
from schemas import TimeBlockCreate, TimeBlockResponse, TimeBlockUpdate
from utils import utcnow

router = APIRouter(
    prefix="/api/time-blocks", tags=["time-blocks"], dependencies=[Depends(verify_api_key)]
)


@router.post("", response_model=TimeBlockResponse, status_code=status.HTTP_201_CREATED)
def create_time_block(block_data: TimeBlockCreate, session: Session = Depends(get_session)):
    time_block = TimeBlock(**block_data.model_dump())
    session.add(time_block)
    session.commit()
    session.refresh(time_block)
    return time_block


@router.get("", response_model=list[TimeBlockResponse])
def get_time_blocks(
    skip: int = 0,
    limit: int = 100,
    task_id: int | None = None,
    start_date: datetime | None = None,
    end_date: datetime | None = None,
    session: Session = Depends(get_session),
):
    query = select(TimeBlock)

    if task_id is not None:
        query = query.where(TimeBlock.task_id == task_id)

    if start_date is not None:
        query = query.where(TimeBlock.start_time >= start_date)

    if end_date is not None:
        query = query.where(TimeBlock.end_time <= end_date)

    query = query.offset(skip).limit(limit).order_by(TimeBlock.start_time.desc())

    time_blocks = session.exec(query).all()
    return time_blocks


@router.get("/{block_id}", response_model=TimeBlockResponse)
def get_time_block(block_id: int, session: Session = Depends(get_session)):
    time_block = session.get(TimeBlock, block_id)
    if not time_block:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Time block not found")
    return time_block


@router.put("/{block_id}", response_model=TimeBlockResponse)
def update_time_block(
    block_id: int, block_data: TimeBlockUpdate, session: Session = Depends(get_session)
):
    time_block = session.get(TimeBlock, block_id)
    if not time_block:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Time block not found")

    update_data = block_data.model_dump(exclude_unset=True)

    if "start_time" in update_data and "end_time" not in update_data:
        if update_data["start_time"] >= time_block.end_time:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="start_time must be before end_time",
            )

    if "end_time" in update_data and "start_time" not in update_data:
        if update_data["end_time"] <= time_block.start_time:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="end_time must be after start_time",
            )

    if "start_time" in update_data and "end_time" in update_data:
        if update_data["start_time"] >= update_data["end_time"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="start_time must be before end_time",
            )

    for field, value in update_data.items():
        setattr(time_block, field, value)

    time_block.updated_at = utcnow()
    session.add(time_block)
    session.commit()
    session.refresh(time_block)
    return time_block


@router.delete("/{block_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_time_block(block_id: int, session: Session = Depends(get_session)):
    time_block = session.get(TimeBlock, block_id)
    if not time_block:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Time block not found")

    session.delete(time_block)
    session.commit()
    return None
