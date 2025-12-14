# Full-Stack Monorepo

A modern full-stack monorepo with Next.js frontend and FastAPI backend.

## 📁 Project Structure

```
.
├── frontend/          # Next.js + TypeScript frontend
├── backend/           # FastAPI + Python backend
├── .devcontainer/     # VSCode dev container configuration
├── .github/           # GitHub workflows
├── docker-compose.yml # Multi-container orchestration
└── README.md          # This file
```

## 🚀 Quick Start

### Prerequisites

- **Node.js** 18+ and **pnpm** 8+
- **Python** 3.11+
- **Docker** and **Docker Compose** (optional, for containerized development)

### Installation

**Quick Setup (Recommended):**
```bash
git clone <repository-url>
cd <repository-name>
./scripts/setup.sh
```

This will install all dependencies, set up environment files, and configure git hooks.

**Manual Setup:**

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd <repository-name>
   ```

2. **Install frontend dependencies:**
   ```bash
   cd frontend
   pnpm install
   ```

3. **Install backend dependencies:**
   ```bash
   cd backend
   # Using pip
   pip install -r requirements.txt
   
   # Or using uv (recommended for faster installs)
   pip install uv
   uv pip install -r requirements.txt
   ```

4. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

### Development

#### Run both servers concurrently

From the root directory:
```bash
pnpm dev
```

This will start:
- Frontend dev server at `http://localhost:3000`
- Backend API server at `http://localhost:8000`

#### Run servers individually

**Frontend:**
```bash
cd frontend
pnpm dev
```

**Backend:**
```bash
cd backend
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Testing

**Frontend tests:**
```bash
cd frontend
pnpm test
```

**Backend tests:**
```bash
cd backend
pytest
```

### Linting & Formatting

**Frontend:**
```bash
cd frontend
pnpm lint        # Run ESLint
pnpm format      # Run Prettier
```

**Backend:**
```bash
cd backend
ruff check .     # Run Ruff linter
ruff format .    # Run Ruff formatter
```

### Docker Development

Build and run with Docker Compose:
```bash
docker-compose up --build
```

Access:
- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8000`
- API docs: `http://localhost:8000/docs`

### VSCode DevContainer

This project includes a DevContainer configuration for a consistent development environment.

1. Install the [Remote - Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension
2. Open the project in VSCode
3. Click "Reopen in Container" when prompted

## 🛠️ Tech Stack

### Frontend
- **Framework:** Next.js 14 (App Router)
- **Language:** TypeScript
- **Package Manager:** pnpm
- **Linting:** ESLint
- **Formatting:** Prettier

### Backend
- **Framework:** FastAPI
- **Language:** Python 3.11+
- **Package Manager:** pip/uv
- **Linting & Formatting:** Ruff
- **Testing:** pytest

## 📝 Scripts

### Root-level scripts (in `package.json`)
- `pnpm dev` - Run both frontend and backend concurrently
- `pnpm install:all` - Install dependencies for both frontend and backend
- `pnpm lint:all` - Lint both frontend and backend
- `pnpm format:all` - Format both frontend and backend
- `pnpm test:all` - Run all tests

## 🔧 Configuration Files

- `.gitignore` - Git ignore patterns
- `.env.example` - Environment variable template
- `package.json` - Root package configuration with workspace scripts
- `pnpm-workspace.yaml` - pnpm workspace configuration
- `.pre-commit-config.yaml` - Pre-commit hooks configuration

## 📚 Additional Documentation

- [Frontend Documentation](./frontend/README.md)
- [Backend Documentation](./backend/README.md)

## 🤝 Contributing

1. Create a feature branch from `main`
2. Make your changes
3. Ensure tests pass and code is formatted
4. Submit a pull request

## 📄 License

[Add your license here]
