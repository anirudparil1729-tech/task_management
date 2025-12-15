from datetime import date

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlmodel import Session

from database import get_session
from middleware import verify_api_key
from models import Category
from schemas import CategoryProductivity, ProductivitySummary
from services import get_productivity_summary

router = APIRouter(
    prefix="/api/productivity", tags=["productivity"], dependencies=[Depends(verify_api_key)]
)


@router.get("/summary", response_model=ProductivitySummary)
def get_daily_summary(
    target_date: date = Query(None, description="Target date for productivity summary"),
    session: Session = Depends(get_session),
):
    if target_date is None:
        target_date = date.today()

    summary = get_productivity_summary(session, target_date)
    return summary


@router.get("/category/{category_id}", response_model=CategoryProductivity)
def get_category_productivity(
    category_id: int,
    target_date: date = Query(None, description="Target date for productivity summary"),
    session: Session = Depends(get_session),
):
    if target_date is None:
        target_date = date.today()

    category = session.get(Category, category_id)
    if not category:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Category not found")

    summary = get_productivity_summary(session, target_date)

    for cat_data in summary["categories"]:
        if cat_data["category_id"] == category_id:
            return CategoryProductivity(**cat_data)

    return CategoryProductivity(
        category_id=category_id,
        category_name=category.name,
        tasks_completed=0,
        time_spent=0,
        score=0.0,
    )
