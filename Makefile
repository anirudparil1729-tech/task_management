.PHONY: help install dev build test lint format clean docker-up docker-down

help:
	@echo "Available commands:"
	@echo "  make install      - Install all dependencies"
	@echo "  make dev          - Run both frontend and backend in development mode"
	@echo "  make build        - Build frontend for production"
	@echo "  make test         - Run all tests"
	@echo "  make lint         - Lint all code"
	@echo "  make format       - Format all code"
	@echo "  make clean        - Clean build artifacts and dependencies"
	@echo "  make docker-up    - Start Docker containers"
	@echo "  make docker-down  - Stop Docker containers"

install:
	@echo "Installing dependencies..."
	pnpm install
	cd frontend && pnpm install
	cd backend && pip install -r requirements.txt

dev:
	@echo "Starting development servers..."
	pnpm dev

build:
	@echo "Building frontend..."
	cd frontend && pnpm build

test:
	@echo "Running tests..."
	pnpm test:all

lint:
	@echo "Linting code..."
	pnpm lint:all

format:
	@echo "Formatting code..."
	pnpm format:all

clean:
	@echo "Cleaning build artifacts..."
	rm -rf frontend/node_modules
	rm -rf frontend/.next
	rm -rf frontend/out
	rm -rf backend/__pycache__
	rm -rf backend/.pytest_cache
	rm -rf backend/.ruff_cache
	rm -rf node_modules

docker-up:
	@echo "Starting Docker containers..."
	docker-compose up --build

docker-down:
	@echo "Stopping Docker containers..."
	docker-compose down
