from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config import settings
from database import create_db_and_tables
from routers import (
    categories_router,
    notifications_router,
    productivity_router,
    sync_router,
    tasks_router,
    time_blocks_router,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    create_db_and_tables()
    yield


app = FastAPI(
    title="Task Management API",
    description="FastAPI backend with SQLModel/SQLite persistence for task management",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(tasks_router)
app.include_router(categories_router)
app.include_router(time_blocks_router)
app.include_router(sync_router)
app.include_router(productivity_router)
app.include_router(notifications_router)


@app.get("/")
def read_root():
    return {"message": "Welcome to the Task Management API"}


@app.get("/health")
def health_check():
    return {"status": "healthy"}


@app.get("/api/hello")
def hello():
    return {"message": "Hello from FastAPI!"}
