# FastAPI Backend

Task management API with SQLModel/SQLite persistence, authentication, and comprehensive features.

## Features

- **Authentication**: Static password middleware via `X-API-Key` header
- **Database**: SQLModel with SQLite for persistence
- **Models**:
  - Task (with due dates, priority, recurrence, notes, completion, reminders)
  - SubTask
  - Category (default + custom with color/icon)
  - TimeBlock
  - ProductivityLog

- **Endpoints**:
  - CRUD operations for tasks, categories, and time blocks
  - Sync endpoints with `modified_since` queries for offline support
  - Recurring task expansion
  - Productivity summary (daily score, per-category progress)
  - Notification metadata (next reminder timestamp)

## Setup

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Create a `.env` file (copy from `.env.example`):
```bash
cp .env.example .env
```

3. Configure environment variables:
```
DATABASE_URL=sqlite:///./database.db
API_PASSWORD=your-secret-password
DEBUG=True
CORS_ORIGINS=http://localhost:3000
```

## Running

### Development
```bash
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Production
```bash
python -m uvicorn main:app --host 0.0.0.0 --port 8000
```

## API Documentation

Once running, visit:
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## Authentication

All API endpoints (except `/`, `/health`, `/api/hello`) require authentication via the `X-API-Key` header:

```bash
curl -H "X-API-Key: your-secret-password" http://localhost:8000/api/tasks
```

## API Endpoints

### Tasks
- `POST /api/tasks` - Create a task
- `GET /api/tasks` - List tasks (supports filtering by `category_id`, `is_completed`)
- `GET /api/tasks/{task_id}` - Get task with subtasks
- `PUT /api/tasks/{task_id}` - Update task
- `DELETE /api/tasks/{task_id}` - Delete task
- `POST /api/tasks/{task_id}/subtasks` - Create subtask
- `GET /api/tasks/{task_id}/subtasks` - List subtasks
- `PUT /api/tasks/subtasks/{subtask_id}` - Update subtask
- `DELETE /api/tasks/subtasks/{subtask_id}` - Delete subtask

### Categories
- `POST /api/categories` - Create category
- `GET /api/categories` - List categories
- `GET /api/categories/{category_id}` - Get category
- `PUT /api/categories/{category_id}` - Update category
- `DELETE /api/categories/{category_id}` - Delete category (except default)

### Time Blocks
- `POST /api/time-blocks` - Create time block
- `GET /api/time-blocks` - List time blocks (supports filtering by `task_id`, `start_date`, `end_date`)
- `GET /api/time-blocks/{block_id}` - Get time block
- `PUT /api/time-blocks/{block_id}` - Update time block
- `DELETE /api/time-blocks/{block_id}` - Delete time block

### Sync (Offline Support)
- `GET /api/sync/tasks?modified_since=<timestamp>` - Get tasks modified since timestamp
- `GET /api/sync/categories?modified_since=<timestamp>` - Get categories modified since timestamp
- `GET /api/sync/time-blocks?modified_since=<timestamp>` - Get time blocks modified since timestamp

### Productivity
- `GET /api/productivity/summary?target_date=<date>` - Get daily productivity summary
- `GET /api/productivity/category/{category_id}?target_date=<date>` - Get category productivity

### Notifications
- `GET /api/notifications/next-reminder` - Get next reminder timestamp

## Recurring Tasks

Tasks support recurrence rules in RFC 5545 format:

Examples:
- `FREQ=DAILY` - Daily recurrence
- `FREQ=WEEKLY` - Weekly recurrence
- `FREQ=MONTHLY` - Monthly recurrence
- `FREQ=DAILY;INTERVAL=2` - Every 2 days
- `FREQ=WEEKLY;BYDAY=MO,WE,FR` - Monday, Wednesday, Friday

## Testing

```bash
pytest
```

Run with coverage:
```bash
pytest --cov=. --cov-report=html
```

## Deployment

### Vercel/Netlify (Serverless)

The `handler.py` file provides a serverless handler using Mangum:

```python
from handler import handler
```

Configure your serverless platform to use this handler as the entry point.

### Docker

```bash
docker build -t backend .
docker run -p 8000:8000 backend
```

## Project Structure

```
backend/
├── main.py                 # FastAPI app entry point
├── handler.py             # Serverless handler
├── config.py              # Settings configuration
├── database.py            # Database setup
├── models/                # SQLModel database models
│   ├── task.py
│   ├── subtask.py
│   ├── category.py
│   ├── time_block.py
│   └── productivity_log.py
├── schemas/               # Pydantic schemas
│   ├── task.py
│   ├── subtask.py
│   ├── category.py
│   ├── time_block.py
│   └── productivity.py
├── routers/               # API routes
│   ├── tasks.py
│   ├── categories.py
│   ├── time_blocks.py
│   ├── sync.py
│   ├── productivity.py
│   └── notifications.py
├── middleware/            # Authentication middleware
│   └── auth.py
├── services/              # Business logic
│   ├── recurring.py       # Recurring task expansion
│   └── productivity.py    # Productivity calculations
└── tests/                 # Unit tests
    ├── test_main.py
    ├── test_tasks.py
    ├── test_categories.py
    ├── test_recurring.py
    └── test_productivity.py
```

## Business Rules

### Task Priority
- 0: No priority
- 1: Low
- 2: Medium
- 3: High
- 4: Urgent

### Productivity Score Calculation
- Base: 10 points per completed task
- Time: Up to 50 points based on time spent (5 points per hour)
- Priority bonus: 5 points per high-priority (3+) task completed
- Maximum: 100 points per day

### Category Rules
- Default categories cannot be deleted
- Color must be a valid hex color (#RRGGBB)
- Categories can have custom icons
