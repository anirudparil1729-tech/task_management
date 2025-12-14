#!/bin/bash

set -e

echo "🚀 Setting up the monorepo..."

# Check if pnpm is installed
if ! command -v pnpm &> /dev/null; then
    echo "📦 Installing pnpm..."
    npm install -g pnpm
else
    echo "✅ pnpm is already installed"
fi

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.11 or later."
    exit 1
else
    echo "✅ Python is installed"
fi

# Install frontend dependencies
echo "📦 Installing frontend dependencies..."
cd frontend
pnpm install
cd ..

# Install backend dependencies
echo "📦 Installing backend dependencies..."
cd backend
if command -v uv &> /dev/null; then
    echo "Using uv for faster installs..."
    uv pip install -r requirements.txt
else
    pip install -r requirements.txt
fi
cd ..

# Install root dependencies
echo "📦 Installing root dependencies..."
pnpm install

# Copy environment files if they don't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from .env.example..."
    cp .env.example .env
fi

if [ ! -f frontend/.env.local ]; then
    echo "📝 Creating frontend/.env.local file..."
    cp frontend/.env.example frontend/.env.local
fi

if [ ! -f backend/.env ]; then
    echo "📝 Creating backend/.env file..."
    cp backend/.env.example backend/.env
fi

# Set up git hooks
echo "🔧 Setting up git hooks..."
git config core.hooksPath .githooks

# Install pre-commit (optional)
if command -v pre-commit &> /dev/null; then
    echo "🔧 Installing pre-commit hooks..."
    pre-commit install
else
    echo "⚠️  pre-commit not found. Run 'pip install pre-commit && pre-commit install' to enable pre-commit hooks."
fi

echo "✅ Setup complete!"
echo ""
echo "To start development servers:"
echo "  pnpm dev"
echo ""
echo "Or run them separately:"
echo "  Frontend: cd frontend && pnpm dev"
echo "  Backend:  cd backend && python -m uvicorn main:app --reload"
