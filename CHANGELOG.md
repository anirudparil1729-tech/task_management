# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Monorepo Structure
- Initial monorepo scaffold with `frontend` and `backend` workspaces
- Root-level configuration and scripts
- pnpm workspace setup

#### Frontend
- Next.js 16 with TypeScript and App Router
- Tailwind CSS 4 for styling
- ESLint and Prettier for code quality
- API client utilities
- Docker configuration
- VSCode integration

#### Backend
- FastAPI application with Python 3.11+
- Ruff for linting and formatting
- pytest for testing with sample tests
- CORS middleware configured
- Sample API endpoints (`/`, `/health`, `/api/hello`)
- Docker configuration

#### Development Tools
- Concurrent dev scripts to run both servers
- Docker Compose for multi-container development
- VSCode DevContainer configuration
- Pre-commit hooks for code quality
- GitHub Actions CI/CD workflow
- Makefile with common commands
- Helper scripts for setup, testing, linting, and formatting

#### Documentation
- Comprehensive README with quick start guide
- Separate documentation for frontend and backend
- Detailed setup guide (SETUP.md)
- Contributing guidelines (CONTRIBUTING.md)
- Project status tracking (PROJECT_STATUS.md)
- GitHub issue and PR templates
- MIT License

#### Configuration
- Git hooks for pre-commit and pre-push
- .editorconfig for consistent coding styles
- Environment variable templates
- VSCode workspace settings

[Unreleased]: https://github.com/username/repo/compare/v0.0.0...HEAD
