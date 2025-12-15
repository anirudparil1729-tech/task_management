# Detailed Setup Guide

This guide provides detailed instructions for setting up the development environment.

## Prerequisites

### Required Software

1. **Node.js** (v18 or later)
   - Download from [nodejs.org](https://nodejs.org/)
   - Verify: `node --version`

2. **pnpm** (v8 or later)
   ```bash
   npm install -g pnpm
   ```
   - Verify: `pnpm --version`

3. **Python** (v3.11 or later)
   - Download from [python.org](https://www.python.org/)
   - Verify: `python --version` or `python3 --version`

4. **pip** (Python package installer)
   - Usually comes with Python
   - Verify: `pip --version` or `pip3 --version`

### Optional Software

1. **Docker** and **Docker Compose**
   - Download from [docker.com](https://www.docker.com/)
   - Required for containerized development

2. **VSCode**
   - Download from [code.visualstudio.com](https://code.visualstudio.com/)
   - Recommended extensions are listed in `.vscode/extensions.json`

3. **Git**
    - Download from [git-scm.com](https://git-scm.com/)
    - Required for version control

4. **Android Studio + Android SDK** (only if you want to build the Android app)
    - Install Android Studio (includes the SDK manager)
    - Ensure you have JDK 17 available

## Installation Steps

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <repository-name>
```

### 2. Install Frontend Dependencies

```bash
cd frontend
pnpm install
cd ..
```

### 3. Install Backend Dependencies

**Option A: Using pip**
```bash
cd backend
pip install -r requirements.txt
cd ..
```

**Option B: Using uv (recommended for faster installs)**
```bash
pip install uv
cd backend
uv pip install -r requirements.txt
cd ..
```

### 4. Install Root Dependencies

```bash
pnpm install
```

### 5. Set Up Environment Variables

```bash
cp .env.example .env
```

Edit `.env` with your specific configuration.

### 6. Install Pre-commit Hooks (Optional)

```bash
pip install pre-commit
pre-commit install
```

## Running the Application

### Development Mode

**Option 1: Run both servers concurrently**
```bash
pnpm dev
```

This will start:
- Frontend at `http://localhost:3000`
- Backend at `http://localhost:8000`

**Option 2: Run servers separately**

Terminal 1 (Frontend):
```bash
cd frontend
pnpm dev
```

Terminal 2 (Backend):
```bash
cd backend
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Android (mobile)

Build the Android debug APK:

```bash
cd mobile/android
./gradlew :app:assembleDebug
```

Create a release bundle (AAB):

```bash
cd mobile/android
./gradlew :app:bundleRelease
```

See `mobile/android/README.md` for design-token sync instructions.

### Docker Development

Build and run with Docker Compose:
```bash
docker-compose up --build
```

Stop containers:
```bash
docker-compose down
```

### VSCode DevContainer

1. Install the [Remote - Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension
2. Open the project in VSCode
3. Press `F1` and select "Remote-Containers: Reopen in Container"
4. Wait for the container to build and start

## Verification

### Frontend

1. Open [http://localhost:3000](http://localhost:3000)
2. You should see the Next.js welcome page

### Backend

1. Open [http://localhost:8000](http://localhost:8000)
2. You should see: `{"message": "Welcome to the API"}`
3. Open [http://localhost:8000/docs](http://localhost:8000/docs)
4. You should see the interactive API documentation

## Common Issues

### Issue: `pnpm: command not found`

**Solution:** Install pnpm globally
```bash
npm install -g pnpm
```

### Issue: Python version mismatch

**Solution:** Use pyenv or conda to manage Python versions
```bash
# Using pyenv
pyenv install 3.11
pyenv local 3.11
```

### Issue: Port already in use

**Solution:** Change the port in the respective configuration or kill the process using the port

For frontend (default 3000):
```bash
# In frontend/package.json, modify the dev script:
"dev": "next dev -p 3001"
```

For backend (default 8000):
```bash
python -m uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

### Issue: Docker build fails

**Solution:** Ensure Docker is running and you have enough disk space
```bash
docker system prune -a  # Clean up Docker resources
```

## Next Steps

- Read the [CONTRIBUTING.md](./CONTRIBUTING.md) guide
- Check out the [frontend documentation](./frontend/README.md)
- Check out the [backend documentation](./backend/README.md)
- Start building features!

## Getting Help

If you encounter issues not covered here:
1. Check existing GitHub issues
2. Create a new issue with details about your problem
3. Reach out to the maintainers
