#!/bin/bash

set -e

echo "🔍 Linting all code..."

# Lint frontend
echo ""
echo "📝 Linting frontend..."
cd frontend
pnpm lint
cd ..

# Lint backend
echo ""
echo "🐍 Linting backend..."
cd backend
ruff check .
cd ..

echo ""
echo "✅ All code is properly linted!"
