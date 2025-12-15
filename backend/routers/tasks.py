from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from database import get_session
from middleware import verify_api_key
from models import SubTask, Task
from schemas import SubTaskResponse, TaskCreate, TaskResponse, TaskUpdate, TaskWithSubTasks
from utils import utcnow

router = APIRouter(prefix="/api/tasks", tags=["tasks"], dependencies=[Depends(verify_api_key)])


@router.post("", response_model=TaskResponse, status_code=status.HTTP_201_CREATED)
def create_task(task_data: TaskCreate, session: Session = Depends(get_session)):
    task = Task(**task_data.model_dump())
    session.add(task)
    session.commit()
    session.refresh(task)
    return task


@router.get("", response_model=list[TaskResponse])
def get_tasks(
    skip: int = 0,
    limit: int = 100,
    category_id: int | None = None,
    is_completed: bool | None = None,
    session: Session = Depends(get_session),
):
    query = select(Task)

    if category_id is not None:
        query = query.where(Task.category_id == category_id)

    if is_completed is not None:
        query = query.where(Task.is_completed == is_completed)

    query = query.offset(skip).limit(limit).order_by(Task.created_at.desc())

    tasks = session.exec(query).all()
    return tasks


@router.get("/{task_id}", response_model=TaskWithSubTasks)
def get_task(task_id: int, session: Session = Depends(get_session)):
    task = session.get(Task, task_id)
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    subtasks = session.exec(select(SubTask).where(SubTask.task_id == task_id).order_by(SubTask.order)).all()

    return TaskWithSubTasks(
        **task.model_dump(),
        subtasks=[SubTaskResponse.model_validate(st) for st in subtasks]
    )


@router.put("/{task_id}", response_model=TaskResponse)
def update_task(task_id: int, task_data: TaskUpdate, session: Session = Depends(get_session)):
    task = session.get(Task, task_id)
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    update_data = task_data.model_dump(exclude_unset=True)

    if "is_completed" in update_data:
        if update_data["is_completed"] and not task.is_completed:
            task.completed_at = utcnow()
        elif not update_data["is_completed"] and task.is_completed:
            task.completed_at = None

    for field, value in update_data.items():
        setattr(task, field, value)

    task.updated_at = utcnow()
    session.add(task)
    session.commit()
    session.refresh(task)
    return task


@router.delete("/{task_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_task(task_id: int, session: Session = Depends(get_session)):
    task = session.get(Task, task_id)
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    session.exec(select(SubTask).where(SubTask.task_id == task_id)).all()
    for subtask in session.exec(select(SubTask).where(SubTask.task_id == task_id)).all():
        session.delete(subtask)

    session.delete(task)
    session.commit()
    return None


@router.post("/{task_id}/subtasks", response_model=SubTaskResponse, status_code=status.HTTP_201_CREATED)
def create_subtask(task_id: int, title: str, session: Session = Depends(get_session)):
    task = session.get(Task, task_id)
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    subtask = SubTask(task_id=task_id, title=title)
    session.add(subtask)
    session.commit()
    session.refresh(subtask)
    return subtask


@router.get("/{task_id}/subtasks", response_model=list[SubTaskResponse])
def get_subtasks(task_id: int, session: Session = Depends(get_session)):
    task = session.get(Task, task_id)
    if not task:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")

    subtasks = session.exec(select(SubTask).where(SubTask.task_id == task_id).order_by(SubTask.order)).all()
    return subtasks


@router.put("/subtasks/{subtask_id}", response_model=SubTaskResponse)
def update_subtask(
    subtask_id: int,
    is_completed: bool | None = None,
    title: str | None = None,
    order: int | None = None,
    session: Session = Depends(get_session),
):
    subtask = session.get(SubTask, subtask_id)
    if not subtask:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="SubTask not found")

    if is_completed is not None:
        subtask.is_completed = is_completed
    if title is not None:
        subtask.title = title
    if order is not None:
        subtask.order = order

    subtask.updated_at = utcnow()
    session.add(subtask)
    session.commit()
    session.refresh(subtask)
    return subtask


@router.delete("/subtasks/{subtask_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_subtask(subtask_id: int, session: Session = Depends(get_session)):
    subtask = session.get(SubTask, subtask_id)
    if not subtask:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="SubTask not found")

    session.delete(subtask)
    session.commit()
    return None
