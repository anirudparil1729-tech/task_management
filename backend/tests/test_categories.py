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


def test_create_category(client: TestClient):
    response = client.post(
        "/api/categories",
        json={"name": "Work", "color": "#FF5733", "icon": "briefcase"},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["name"] == "Work"
    assert data["color"] == "#FF5733"
    assert data["icon"] == "briefcase"


def test_get_categories(client: TestClient):
    client.post(
        "/api/categories",
        json={"name": "Work", "color": "#FF5733"},
        headers={"X-API-Key": "secret-password"},
    )
    client.post(
        "/api/categories",
        json={"name": "Personal", "color": "#33FF57"},
        headers={"X-API-Key": "secret-password"},
    )

    response = client.get("/api/categories", headers={"X-API-Key": "secret-password"})
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2


def test_update_category(client: TestClient):
    create_response = client.post(
        "/api/categories",
        json={"name": "Work", "color": "#FF5733"},
        headers={"X-API-Key": "secret-password"},
    )
    category_id = create_response.json()["id"]

    response = client.put(
        f"/api/categories/{category_id}",
        json={"name": "Work Updated", "color": "#3357FF"},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Work Updated"
    assert data["color"] == "#3357FF"


def test_delete_category(client: TestClient):
    create_response = client.post(
        "/api/categories",
        json={"name": "Temp Category", "color": "#FF5733"},
        headers={"X-API-Key": "secret-password"},
    )
    category_id = create_response.json()["id"]

    response = client.delete(
        f"/api/categories/{category_id}", headers={"X-API-Key": "secret-password"}
    )
    assert response.status_code == 204


def test_delete_default_category_fails(client: TestClient):
    create_response = client.post(
        "/api/categories",
        json={"name": "Default", "color": "#FF5733", "is_default": True},
        headers={"X-API-Key": "secret-password"},
    )
    category_id = create_response.json()["id"]

    response = client.delete(
        f"/api/categories/{category_id}", headers={"X-API-Key": "secret-password"}
    )
    assert response.status_code == 400
    assert "Cannot delete default category" in response.json()["detail"]


def test_category_color_validation(client: TestClient):
    response = client.post(
        "/api/categories",
        json={"name": "Invalid Color", "color": "not-a-color"},
        headers={"X-API-Key": "secret-password"},
    )
    assert response.status_code == 422
