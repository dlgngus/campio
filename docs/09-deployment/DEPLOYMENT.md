# Deployment

Campio deploys as two separate services:

- `campio-frontend`: static React/Vite build
- `campio-backend`: Spring Boot API with PostgreSQL in production

For the selected Vercel + Render path, use:

- Korean handoff: `docs/09-deployment/VERCEL_RENDER_HANDOFF_KO.md`
- Technical checklist: `docs/09-deployment/VERCEL_RENDER_DEPLOY.md`

Do not use the default H2 profile for production.
Production schema changes are managed by Flyway migrations under `campio-backend/src/main/resources/db/migration`.

## Deployment Inputs Needed From Owner

The codebase is ready for a standard frontend/backend/database deployment, but the actual production deploy needs account-specific values.

Required owner decisions:

- Frontend host: Vercel, Netlify, Render Static Site, or another static host
- Backend host: Render, Railway, Fly.io, or another Docker/Java-capable host
- Database: managed PostgreSQL instance
- Production domains:
  - frontend origin, for example `https://campio.example.com`
  - backend origin, for example `https://api.campio.example.com`
- Admin account:
  - `CAMPIO_ADMIN_EMAIL`
  - `CAMPIO_ADMIN_PASSWORD`
- Initial real-data crawl:
  - `CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP`
  - `CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY`

Do not commit real passwords, database URLs, API keys, or hosting secrets to this repository. Put them only in the hosting provider's environment variable settings.

## Frontend

Recommended:
- Vercel
- Netlify
- Render Static Site

Recommended MVP target: Vercel.

Root directory:

```text
campio-frontend
```

Build command:

```bash
npm run build
```

Publish directory:

```text
dist
```

Environment variables:

```text
VITE_API_BASE_URL=https://your-backend-domain.example
```

For Vercel:

- Framework preset: Vite
- Root directory: `campio-frontend`
- Build command: `npm run build`
- Output directory: `dist`
- Environment variable: `VITE_API_BASE_URL=https://your-render-backend.onrender.com`

SPA fallback:

- Vercel: `campio-frontend/vercel.json` is included.
- Docker/nginx: `campio-frontend/nginx.conf` is included.

## Backend

Recommended:
- Docker-capable Web Service: Render, Railway, Fly.io, or any container host
- Java service with Maven build is also fine

Recommended MVP target: Render Web Service with the backend Dockerfile.

Environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/campio
SPRING_DATASOURCE_USERNAME=campio
SPRING_DATASOURCE_PASSWORD=...
FRONTEND_ORIGIN=https://your-frontend-domain.example
CAMPIO_ADMIN_EMAIL=admin@example.com
CAMPIO_ADMIN_PASSWORD=...
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true
CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true
```

Health check:

```text
GET /api/health
```

For Render:

- Service type: Web Service
- Environment: Docker
- Root directory: repository root
- Dockerfile path: `campio-backend/Dockerfile`
- Health check path: `/api/health`
- Add a managed PostgreSQL database and use its internal connection values for the datasource env vars.
- Set `FRONTEND_ORIGIN` to the exact Vercel origin, for example `https://campio.vercel.app`.

Production admin bootstrap:

- Set `CAMPIO_ADMIN_EMAIL` and `CAMPIO_ADMIN_PASSWORD` before the first backend deploy.
- The backend creates that admin user on startup if the email does not already exist.
- Campio no longer inserts sample opportunities, posts, mentors, or users automatically.
- Public opportunity data must come from ingestion: manual admin import, approved API/RSS sources, or approved controlled HTML sources.
- Set `CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true` on the first deploy if the production database should fetch enabled real sources immediately after startup.
- Keep `CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true` to avoid recrawling on every backend restart after data exists.

Docker:

```bash
docker build -t campio-backend ./campio-backend
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/campio \
  -e SPRING_DATASOURCE_USERNAME=campio \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e FRONTEND_ORIGIN=https://your-frontend-domain.example \
  -e CAMPIO_ADMIN_EMAIL=admin@example.com \
  -e CAMPIO_ADMIN_PASSWORD=... \
  -e CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true \
  -e CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true \
  campio-backend
```

## Local Production Smoke Test

Requires Docker:

```bash
docker compose up --build
```

Then check:

```bash
curl http://localhost:8080/api/health
open http://localhost:3000
```

If Docker commands fail with a Docker socket error, start Docker Desktop or the local Docker daemon first.

## Local Development

Frontend:

```bash
cd campio-frontend
npm install
npm run dev
```

Backend:

```bash
cd campio-backend
mvn spring-boot:run
```
