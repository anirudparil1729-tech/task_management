from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from database import get_session
from middleware import verify_api_key
from models import Category
from schemas import CategoryCreate, CategoryResponse, CategoryUpdate
from utils import utcnow

router = APIRouter(
    prefix="/api/categories", tags=["categories"], dependencies=[Depends(verify_api_key)]
)


@router.post("", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
def create_category(category_data: CategoryCreate, session: Session = Depends(get_session)):
    category = Category(**category_data.model_dump())
    session.add(category)
    session.commit()
    session.refresh(category)
    return category


@router.get("", response_model=list[CategoryResponse])
def get_categories(
    skip: int = 0, limit: int = 100, session: Session = Depends(get_session)
):
    query = select(Category).offset(skip).limit(limit).order_by(Category.name)
    categories = session.exec(query).all()
    return categories


@router.get("/{category_id}", response_model=CategoryResponse)
def get_category(category_id: int, session: Session = Depends(get_session)):
    category = session.get(Category, category_id)
    if not category:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Category not found")
    return category


@router.put("/{category_id}", response_model=CategoryResponse)
def update_category(
    category_id: int, category_data: CategoryUpdate, session: Session = Depends(get_session)
):
    category = session.get(Category, category_id)
    if not category:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Category not found")

    update_data = category_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(category, field, value)

    category.updated_at = utcnow()
    session.add(category)
    session.commit()
    session.refresh(category)
    return category


@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_category(category_id: int, session: Session = Depends(get_session)):
    category = session.get(Category, category_id)
    if not category:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Category not found")

    if category.is_default:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot delete default category",
        )

    session.delete(category)
    session.commit()
    return None
