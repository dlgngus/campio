# Campio

> A student opportunity discovery platform for internships, competitions, scholarships, startup programs, and career-building activities.

[![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20Vite-111827?style=flat-square&logo=react)](https://campiokr.vercel.app)
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot-111827?style=flat-square&logo=springboot)](https://campio.onrender.com/api/health)
[![Database](https://img.shields.io/badge/database-PostgreSQL-111827?style=flat-square&logo=postgresql)](#tech-stack)
[![Deploy](https://img.shields.io/badge/deploy-Vercel%20%2B%20Render-111827?style=flat-square&logo=vercel)](#deployment)

Campio helps university students find relevant opportunities without searching across scattered government portals, startup boards, community posts, and school channels. The product focuses on opportunity discovery first, with saved opportunities, community, mentors, and admin ingestion tools supporting better decisions.

**Live**

- Frontend: [campiokr.vercel.app](https://campiokr.vercel.app)
- Backend health: [campio.onrender.com/api/health](https://campio.onrender.com/api/health)

## Highlights

- Clean React/Vite frontend with responsive pages, empty states, loading states, and API error handling
- Spring Boot REST API with domain-based structure and session-based authentication
- PostgreSQL production deployment with Flyway-managed schema migrations
- Admin ingestion pipeline for public opportunity sources
- Crawled opportunity normalization, deduplication, raw review flow, and student-relevance filtering
- Production-ready Render + PostgreSQL + Vercel configuration
- CORS and cross-domain cookie settings for separate frontend/backend domains

## Product Scope

Campio is built around one core workflow:

```text
Discover opportunity -> review details -> save -> track application -> discuss with community/mentors
```

Current product areas:

- Opportunity exploration and search
- Opportunity detail pages
- Saved opportunities
- Login and signup
- Community posts and comments
- Mentor profiles
- Admin opportunity management
- Admin crawling/ingestion dashboard

## Architecture

```text
campio/
  campio-frontend/     React, Vite, React Router
  campio-backend/      Spring Boot, JPA, Flyway
  docs/                Product, design, QA, deployment, ingestion notes
  render.yaml          Render service blueprint
  docker-compose.yml   Local database and service composition
```

Runtime deployment:

```text
Browser
  -> Vercel static frontend
  -> Render Spring Boot API
  -> Render PostgreSQL
```

Data ingestion flow:

```text
opportunity_sources
  -> crawl job
  -> raw_opportunities
  -> review / auto-publish rules
  -> opportunities
```

## Tech Stack

**Frontend**

- React 18
- Vite
- React Router
- Lucide React
- Plain CSS with design tokens

**Backend**

- Java 17
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Security Crypto
- Flyway

**Database and Deployment**

- PostgreSQL
- H2 for local/test runtime
- Render Web Service
- Render PostgreSQL
- Vercel
- Docker

## Local Development

Backend:

```bash
cd campio-backend
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn test
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn spring-boot:run
```

Frontend:

```bash
cd campio-frontend
npm install
npm run dev
```

Frontend environment:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Production frontend should use the backend origin only, without `/api`:

```text
VITE_API_BASE_URL=https://campio.onrender.com
```

## Deployment

The production setup uses:

- Vercel for `campio-frontend`
- Render Web Service for `campio-backend`
- Render PostgreSQL for production data

Important backend environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=postgresql://...
FRONTEND_ORIGIN=https://campiokr.vercel.app
CAMPIO_ADMIN_EMAIL=...
CAMPIO_ADMIN_PASSWORD=...
CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED=false
CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=false
CAMPIO_INGESTION_SCHEDULER_ENABLED=true
CAMPIO_YOUTH_CENTER_API_KEY=...
CAMPIO_WORK24_API_KEY=...
```

`CAMPIO_YOUTH_CENTER_API_KEY` and `CAMPIO_WORK24_API_KEY` are optional. When configured,
Campio registers the official Ontong Youth policy API and Work24 internship API sources.
Set `CAMPIO_INGESTION_BOOTSTRAP_SOURCES_ENABLED=true` to enable registered sources and
`CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true` for the first import.

Full beginner-friendly deployment guide:

- [DEPLOY_NOW_KO.md](docs/09-deployment/DEPLOY_NOW_KO.md)
- [VERCEL_RENDER_HANDOFF_KO.md](docs/09-deployment/VERCEL_RENDER_HANDOFF_KO.md)

## Quality Checks

Commands used during development:

```bash
cd campio-frontend && npm run build
cd campio-backend && JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn test
```

Current backend test suite covers:

- API smoke tests
- Admin bootstrap behavior
- Render/PostgreSQL `DATABASE_URL` conversion
- Flyway startup behavior

## Notes

Campio is actively evolving. The current focus is improving source quality for crawled opportunities so that students see relevant, regional, and actionable opportunities instead of broad generic government support listings.
