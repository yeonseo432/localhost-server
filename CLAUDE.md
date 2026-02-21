# Marketing Server - Claude Code Guide

## Project Structure

This repo contains **two independent services**:

| Service | Stack | Port | Directory |
|---------|-------|------|-----------|
| Backend API | Spring Boot 4 (Kotlin) | 8080 | `src/` |
| AI Judgment | FastAPI (Python) | 8000 | `ai-server/` |

## Critical Rule: File Ownership

- **Never modify root-level files** (`build.gradle.kts`, `docker-compose.yaml`, `.gitignore`, `Dockerfile`, etc.) from ai-server work.
- Spring Boot code lives in `src/` — the backend team manages it.
- AI server code lives in `ai-server/` — completely self-contained.

## Spring Boot Backend (`src/`)

### Build & Run
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Test
```bash
./gradlew test
```

### Key Dependencies
- Spring Boot 4.0.1, Spring Security, JPA, Redis
- MySQL 8.4 (via root `docker-compose.yaml`)
- Flyway migrations in `src/main/resources/db/migration/`

## AI Server (`ai-server/`)

### Setup
```bash
cd ai-server
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # Fill in ELICE_API_KEY
```

### Run
```bash
cd ai-server
uvicorn app.main:app --reload --port 8000
```

### Test
```bash
cd ai-server
pytest tests/ -v
```

### Endpoints
- `POST /analyze/receipt` — M3: Receipt OCR + product matching
- `POST /analyze/inventory` — M4: Inventory image comparison
- `GET /health` — Health check

### AI Models (Elice Cloud ML API)
- **Receipt (M3)**: `gemini-2.5-flash` — vision-based receipt product matching
- **Inventory (M4)**: `gemini-2.5-flash` — vision-based image comparison

## Integration

Spring Boot calls FastAPI at `http://localhost:8000` (configured in `application.yaml` as `fastapi.base-url`).
The integration client is `FastApiMissionClient.kt`.
