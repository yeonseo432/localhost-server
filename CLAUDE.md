# Marketing Server - Claude Code Guide

## Project Structure

Single Spring Boot (Kotlin) application.

| Service | Stack | Port | Directory |
|---------|-------|------|-----------|
| Backend API | Spring Boot 4 (Kotlin) | 8080 | `src/` |

AI image analysis (receipt/inventory) is handled directly via `EliceAiClient` calling the Elice Cloud ML API (OpenAI-compatible).

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

### AI Integration (Elice Cloud ML API)
- **Receipt (M3)**: `gemini-2.5-flash` — vision-based receipt product matching
- **Inventory (M4)**: `gemini-2.5-flash` — vision-based image comparison
- Client: `EliceAiClient.kt` calls Elice API directly (no separate AI server)
- Config: `elice.api-url`, `elice.api-key`, `elice.model` in `application.yaml`
