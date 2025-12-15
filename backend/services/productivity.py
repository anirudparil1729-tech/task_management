from datetime import date, datetime, timedelta

from sqlmodel import Session, func, select

from models import Category, ProductivityLog, Task, TimeBlock
from utils import utcnow


def calculate_daily_score(
    session: Session, target_date: date, category_id: int | None = None
) -> float:
    start_of_day = datetime.combine(target_date, datetime.min.time())
    end_of_day = datetime.combine(target_date, datetime.max.time())

    query = select(Task).where(
        Task.is_completed == True,
        Task.completed_at >= start_of_day,
        Task.completed_at <= end_of_day,
    )

    if category_id:
        query = query.where(Task.category_id == category_id)

    completed_tasks = session.exec(query).all()

    time_blocks_query = select(TimeBlock).where(
        TimeBlock.start_time >= start_of_day, TimeBlock.end_time <= end_of_day
    )

    if category_id:
        time_blocks_query = time_blocks_query.where(TimeBlock.task_id.in_([t.id for t in completed_tasks]))

    time_blocks = session.exec(time_blocks_query).all()

    time_spent = sum(
        (block.end_time - block.start_time).total_seconds() / 60 for block in time_blocks
    )

    task_score = len(completed_tasks) * 10
    time_score = min(time_spent / 60 * 5, 50)

    high_priority_tasks = sum(1 for task in completed_tasks if task.priority >= 3)
    priority_bonus = high_priority_tasks * 5

    total_score = min(task_score + time_score + priority_bonus, 100.0)

    return round(total_score, 2)


def get_productivity_summary(session: Session, target_date: date) -> dict:
    start_of_day = datetime.combine(target_date, datetime.min.time())
    end_of_day = datetime.combine(target_date, datetime.max.time())

    completed_tasks = session.exec(
        select(Task).where(
            Task.is_completed == True,
            Task.completed_at >= start_of_day,
            Task.completed_at <= end_of_day,
        )
    ).all()

    time_blocks = session.exec(
        select(TimeBlock).where(
            TimeBlock.start_time >= start_of_day, TimeBlock.end_time <= end_of_day
        )
    ).all()

    total_time_spent = sum(
        (block.end_time - block.start_time).total_seconds() / 60 for block in time_blocks
    )

    category_stats = {}
    for task in completed_tasks:
        cat_id = task.category_id
        if cat_id not in category_stats:
            category_stats[cat_id] = {"tasks": 0, "time": 0}
        category_stats[cat_id]["tasks"] += 1

    for block in time_blocks:
        if block.task_id:
            task = session.get(Task, block.task_id)
            if task:
                cat_id = task.category_id
                if cat_id not in category_stats:
                    category_stats[cat_id] = {"tasks": 0, "time": 0}
                category_stats[cat_id]["time"] += (
                    block.end_time - block.start_time
                ).total_seconds() / 60

    categories_data = []
    for cat_id, stats in category_stats.items():
        category = session.get(Category, cat_id) if cat_id else None
        score = calculate_daily_score(session, target_date, cat_id)

        categories_data.append(
            {
                "category_id": cat_id,
                "category_name": category.name if category else "Uncategorized",
                "tasks_completed": stats["tasks"],
                "time_spent": int(stats["time"]),
                "score": score,
            }
        )

    daily_score = calculate_daily_score(session, target_date)

    return {
        "date": target_date,
        "daily_score": daily_score,
        "total_tasks_completed": len(completed_tasks),
        "total_time_spent": int(total_time_spent),
        "categories": categories_data,
    }


def update_productivity_logs(session: Session, target_date: date):
    summary = get_productivity_summary(session, target_date)

    for cat_data in summary["categories"]:
        log = session.exec(
            select(ProductivityLog).where(
                ProductivityLog.date == target_date,
                ProductivityLog.category_id == cat_data["category_id"],
            )
        ).first()

        if log:
            log.score = cat_data["score"]
            log.tasks_completed = cat_data["tasks_completed"]
            log.time_spent = cat_data["time_spent"]
            log.updated_at = utcnow()
        else:
            log = ProductivityLog(
                date=target_date,
                score=cat_data["score"],
                category_id=cat_data["category_id"],
                tasks_completed=cat_data["tasks_completed"],
                time_spent=cat_data["time_spent"],
            )
            session.add(log)

    session.commit()
