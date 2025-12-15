from datetime import date

from pydantic import BaseModel


class CategoryProductivity(BaseModel):
    category_id: int | None
    category_name: str | None
    tasks_completed: int
    time_spent: int
    score: float


class ProductivitySummary(BaseModel):
    date: date
    daily_score: float
    total_tasks_completed: int
    total_time_spent: int
    categories: list[CategoryProductivity]
