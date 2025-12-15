from datetime import datetime, timedelta

import pytest
from fastapi.testclient import TestClient
from sqlmodel import Session, SQLModel, create_engine
from sqlmodel.pool import StaticPool

from database import get_session
from main import app


@pytest.fixture(name="session")
def session_fixture():
    engine = create_engine(
        "sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool
    )
    SQLModel.metadata.create_all(engine)
    with Session(engine) as session:
        yield session


@pytest.fixture(name="client")
def client_fixture(session: Session):
    def get_session_override():
        return session

    app.dependency_overrides[get_session] = get_session_override
    client = TestClient(app)
    yield client
    app.dependency_overrides.clear()


def test_create_task_unauthorized(client: TestClient):
    response = client.post(
        "/api/tasks",
        json={"title": "Test Task", "priority": 1},
    )
    assert response.status_code == 401


def test_create_task(client: TestClient):
    response = client.post(
        "/api/tasks",
        json={"title": "Test Task", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["title"] == "Test Task"
    assert data["priority"] == 1
    assert data["is_completed"] is False
    assert "id" in data


def test_get_tasks(client: TestClient):
    client.post(
        "/api/tasks",
        json={"title": "Task 1", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    client.post(
        "/api/tasks",
        json={"title": "Task 2", "priority": 2},
        headers={"X-API-Key": "secret-password"},
    )

    response = client.get("/api/tasks", headers={"X-API-Key": "secret-password"})
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2


def test_get_task_by_id(client: TestClient):
    create_response = client.post(
        "/api/tasks",
        json={"title": "Test Task", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = create_response.json()["id"]

    response = client.get(f"/api/tasks/{task_id}", headers={"X-API-Key": "secret-password"})
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == task_id
    assert data["title"] == "Test Task"


def test_update_task(client: TestClient):
    create_response = client.post(
        "/api/tasks",
        json={"title": "Original Title", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = create_response.json()["id"]

    response = client.put(
        f"/api/tasks/{task_id}",
        json={"title": "Updated Title", "priority": 3},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Updated Title"
    assert data["priority"] == 3


def test_complete_task(client: TestClient):
    create_response = client.post(
        "/api/tasks",
        json={"title": "Task to Complete", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = create_response.json()["id"]

    response = client.put(
        f"/api/tasks/{task_id}",
        json={"is_completed": True},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["is_completed"] is True
    assert data["completed_at"] is not None


def test_delete_task(client: TestClient):
    create_response = client.post(
        "/api/tasks",
        json={"title": "Task to Delete", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = create_response.json()["id"]

    response = client.delete(f"/api/tasks/{task_id}", headers={"X-API-Key": "secret-password"})
    assert response.status_code == 204

    get_response = client.get(f"/api/tasks/{task_id}", headers={"X-API-Key": "secret-password"})
    assert get_response.status_code == 404


def test_create_subtask(client: TestClient):
    create_response = client.post(
        "/api/tasks",
        json={"title": "Parent Task", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = create_response.json()["id"]

    response = client.post(
        f"/api/tasks/{task_id}/subtasks",
        params={"title": "Subtask 1"},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["title"] == "Subtask 1"
    assert data["task_id"] == task_id


def test_task_priority_validation(client: TestClient):
    response = client.post(
        "/api/tasks",
        json={"title": "Task", "priority": 10},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 422


def test_filter_tasks_by_completion(client: TestClient):
    client.post(
        "/api/tasks",
        json={"title": "Completed Task", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_response = client.post(
        "/api/tasks",
        json={"title": "Incomplete Task", "priority": 1},
        headers={"X-API-Key": "secret-password"},
    )
    task_id = task_response.json()["id"]

    client.put(
        f"/api/tasks/{task_id}",
        json={"is_completed": True},
        headers={"X-API-Key": "secret-password"},
    )

    response = client.get(
        "/api/tasks", params={"is_completed": True}, headers={"X-API-Key": "secret-password"}
    )
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 1
    assert all(task["is_completed"] for task in data)
