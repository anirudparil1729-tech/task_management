from datetime import date, datetime, timedelta

import pytest
from sqlmodel import Session, SQLModel, create_engine
from sqlmodel.pool import StaticPool

from models import Category, Task, TimeBlock
from services.productivity import calculate_daily_score, get_productivity_summary


@pytest.fixture(name="session")
def session_fixture():
    engine = create_engine(
        "sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool
    )
    SQLModel.metadata.create_all(engine)
    with Session(engine) as session:
        yield session


def test_calculate_daily_score_no_tasks(session: Session):
    target_date = date.today()
    score = calculate_daily_score(session, target_date)
    assert score == 0.0


def test_calculate_daily_score_with_completed_tasks(session: Session):
    target_date = date.today()
    today_start = datetime.combine(target_date, datetime.min.time())

    task1 = Task(
        title="Task 1",
        priority=1,
        is_completed=True,
        completed_at=today_start + timedelta(hours=2),
    )
    task2 = Task(
        title="Task 2",
        priority=2,
        is_completed=True,
        completed_at=today_start + timedelta(hours=3),
    )

    session.add(task1)
    session.add(task2)
    session.commit()

    score = calculate_daily_score(session, target_date)
    assert score > 0.0


def test_calculate_daily_score_with_high_priority_tasks(session: Session):
    target_date = date.today()
    today_start = datetime.combine(target_date, datetime.min.time())

    task = Task(
        title="High Priority Task",
        priority=4,
        is_completed=True,
        completed_at=today_start + timedelta(hours=2),
    )

    session.add(task)
    session.commit()

    score = calculate_daily_score(session, target_date)
    assert score > 10.0


def test_calculate_daily_score_with_time_blocks(session: Session):
    target_date = date.today()
    today_start = datetime.combine(target_date, datetime.min.time())

    task = Task(
        title="Task with Time Block",
        priority=1,
        is_completed=True,
        completed_at=today_start + timedelta(hours=2),
    )
    session.add(task)
    session.commit()
    session.refresh(task)

    time_block = TimeBlock(
        task_id=task.id,
        start_time=today_start + timedelta(hours=1),
        end_time=today_start + timedelta(hours=3),
    )
    session.add(time_block)
    session.commit()

    score = calculate_daily_score(session, target_date)
    assert score > 10.0


def test_get_productivity_summary_empty(session: Session):
    target_date = date.today()
    summary = get_productivity_summary(session, target_date)

    assert summary["date"] == target_date
    assert summary["daily_score"] == 0.0
    assert summary["total_tasks_completed"] == 0
    assert summary["total_time_spent"] == 0
    assert len(summary["categories"]) == 0


def test_get_productivity_summary_with_category(session: Session):
    target_date = date.today()
    today_start = datetime.combine(target_date, datetime.min.time())

    category = Category(name="Work", color="#FF5733")
    session.add(category)
    session.commit()
    session.refresh(category)

    task = Task(
        title="Work Task",
        priority=1,
        category_id=category.id,
        is_completed=True,
        completed_at=today_start + timedelta(hours=2),
    )
    session.add(task)
    session.commit()

    summary = get_productivity_summary(session, target_date)

    assert summary["total_tasks_completed"] == 1
    assert len(summary["categories"]) == 1
    assert summary["categories"][0]["category_id"] == category.id


def test_productivity_score_max_100(session: Session):
    target_date = date.today()
    today_start = datetime.combine(target_date, datetime.min.time())

    for i in range(20):
        task = Task(
            title=f"Task {i}",
            priority=4,
            is_completed=True,
            completed_at=today_start + timedelta(hours=i % 10),
        )
        session.add(task)

    session.commit()

    score = calculate_daily_score(session, target_date)
    assert score <= 100.0
