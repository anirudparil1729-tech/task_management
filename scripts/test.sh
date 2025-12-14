#!/bin/bash

set -e

echo "🧪 Running all tests..."

# Run frontend tests
echo ""
echo "📝 Running frontend tests..."
cd frontend
pnpm test
cd ..

# Run backend tests
echo ""
echo "🐍 Running backend tests..."
cd backend
pytest
cd ..

echo ""
echo "✅ All tests passed!"
