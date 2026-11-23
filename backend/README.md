# Backend - Developer Growth Tracker

Spring Boot 3.4.4 + Java 17 + PostgreSQL.

## Run Locally (Standalone)
Requires Java 17+, Maven, and a running PostgreSQL instance (or Docker Compose).

```bash
mvn spring-boot:run
```

Environment variables supported:
- `PORT` (default: `8080`)
- `DATABASE_URL` (default: `jdbc:postgresql://localhost:5432/growth_tracker`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `FRONTEND_URL` (default: `http://localhost:5173`)

## API Endpoints
- `GET /api/health` — Health check
- `GET /api/study-days` — Full study plan
- `GET /api/study-days?from=YYYY-MM-DD&to=YYYY-MM-DD` — Date range query
- `POST /api/study-days/{date}/complete` — Mark day completed
- `DELETE /api/study-days/{date}/complete` — Undo completion
- `GET /api/summary` — Streak and domain completion summary
