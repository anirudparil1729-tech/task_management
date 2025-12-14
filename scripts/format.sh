#!/bin/bash

set -e

echo "💅 Formatting all code..."

# Format frontend
echo ""
echo "📝 Formatting frontend..."
cd frontend
pnpm format
cd ..

# Format backend
echo ""
echo "🐍 Formatting backend..."
cd backend
ruff format .
cd ..

echo ""
echo "✅ All code is properly formatted!"
