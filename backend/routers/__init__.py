from routers.categories import router as categories_router
from routers.notifications import router as notifications_router
from routers.productivity import router as productivity_router
from routers.sync import router as sync_router
from routers.tasks import router as tasks_router
from routers.time_blocks import router as time_blocks_router

__all__ = [
    "tasks_router",
    "categories_router",
    "time_blocks_router",
    "sync_router",
    "productivity_router",
    "notifications_router",
]
