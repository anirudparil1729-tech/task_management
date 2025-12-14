# Backend

FastAPI backend application with Python 3.11+.

## 🚀 Getting Started

### Install dependencies

Using pip:
```bash
pip install -r requirements.txt
```

Using uv (recommended for faster installs):
```bash
pip install uv
uv pip install -r requirements.txt
```

### Run development server
```bash
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

API will be available at:
- API: [http://localhost:8000](http://localhost:8000)
- Interactive docs: [http://localhost:8000/docs](http://localhost:8000/docs)
- Alternative docs: [http://localhost:8000/redoc](http://localhost:8000/redoc)

## 📝 Scripts

Run from the backend directory:

```bash
# Linting
ruff check .

# Formatting
ruff format .

# Testing
pytest

# Coverage
pytest --cov=. --cov-report=html
```

## 🛠️ Tech Stack

- **Framework:** FastAPI
- **Language:** Python 3.11+
- **ASGI Server:** Uvicorn
- **Linting & Formatting:** Ruff
- **Testing:** pytest
- **Validation:** Pydantic

## 📁 Project Structure

```
backend/
├── main.py               # FastAPI application entry point
├── tests/                # Test files
│   └── test_main.py      # Main tests
├── requirements.txt      # Python dependencies
├── pyproject.toml        # Project configuration
└── README.md             # This file
```

## 🔧 Environment Variables

Create a `.env` file in the backend directory:

```env
DATABASE_URL=postgresql://user:password@localhost:5432/dbname
SECRET_KEY=your-secret-key-here
DEBUG=True
ALLOWED_HOSTS=localhost,127.0.0.1
CORS_ORIGINS=http://localhost:3000
```

## 📚 API Endpoints

- `GET /` - Welcome message
- `GET /health` - Health check endpoint
- `GET /api/hello` - Sample API endpoint
- `GET /docs` - Interactive API documentation (Swagger UI)
- `GET /redoc` - Alternative API documentation (ReDoc)

## 📚 Learn More

- [FastAPI Documentation](https://fastapi.tiangolo.com/)
- [Uvicorn Documentation](https://www.uvicorn.org/)
- [Pydantic Documentation](https://docs.pydantic.dev/)
- [Ruff Documentation](https://docs.astral.sh/ruff/)
