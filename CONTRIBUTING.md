# Contributing Guide

Thank you for your interest in contributing to this project!

## Development Setup

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd <repository-name>
   ```

2. **Install dependencies:**
   ```bash
   # Install pnpm globally
   npm install -g pnpm
   
   # Install frontend dependencies
   cd frontend
   pnpm install
   cd ..
   
   # Install backend dependencies
   cd backend
   pip install -r requirements.txt
   cd ..
   ```

3. **Set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

4. **Install pre-commit hooks (optional but recommended):**
   ```bash
   pip install pre-commit
   pre-commit install
   ```

## Development Workflow

### Running the Development Servers

**Option 1: Run both servers concurrently (from root):**
```bash
pnpm dev
```

**Option 2: Run servers individually:**

Frontend:
```bash
cd frontend
pnpm dev
```

Backend:
```bash
cd backend
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Before Committing

1. **Format your code:**
   ```bash
   # Frontend
   cd frontend
   pnpm format
   
   # Backend
   cd backend
   ruff format .
   ```

2. **Lint your code:**
   ```bash
   # Frontend
   cd frontend
   pnpm lint
   
   # Backend
   cd backend
   ruff check .
   ```

3. **Run tests:**
   ```bash
   # Frontend
   cd frontend
   pnpm test
   
   # Backend
   cd backend
   pytest
   ```

### Commit Guidelines

- Write clear, concise commit messages
- Use conventional commit format:
  - `feat:` for new features
  - `fix:` for bug fixes
  - `docs:` for documentation changes
  - `style:` for formatting changes
  - `refactor:` for code refactoring
  - `test:` for adding or updating tests
  - `chore:` for maintenance tasks

Example:
```
feat(frontend): add user authentication page
fix(backend): resolve CORS issue with API endpoints
docs: update installation instructions
```

## Pull Request Process

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit them

3. Push your branch:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Open a Pull Request on GitHub with:
   - A clear description of the changes
   - Any related issue numbers
   - Screenshots (if applicable)

5. Wait for code review and address any feedback

## Code Style

### Frontend (TypeScript/React)
- Use functional components with hooks
- Follow the existing ESLint configuration
- Use Prettier for consistent formatting
- Use meaningful variable and function names
- Add JSDoc comments for complex functions

### Backend (Python/FastAPI)
- Follow PEP 8 style guide
- Use Ruff for linting and formatting
- Use type hints for function parameters and return values
- Write docstrings for functions and classes
- Keep functions focused and single-purpose

## Testing

- Write tests for new features and bug fixes
- Ensure all tests pass before submitting a PR
- Aim for good test coverage

### Frontend Tests
```bash
cd frontend
pnpm test
```

### Backend Tests
```bash
cd backend
pytest
pytest --cov=. --cov-report=html  # With coverage
```

## Questions?

If you have questions or need help, please:
- Open an issue on GitHub
- Reach out to the maintainers

Thank you for contributing! 🎉
