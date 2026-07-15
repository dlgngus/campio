# Campio

Campio is a web platform for university students to discover every opportunity in one place.

Core product direction:

> Opportunity discovery first. Mentoring and community are supporting features.

This repository is designed to be implemented by Codex or Claude Code using the documents in `/docs`.

## Recommended implementation order

1. Read `PROJECT.md`
2. Read `docs/00-ai/CODEX_MASTER_PROMPT.md`
3. Build the React frontend first with mock data
4. Build Spring Boot backend
5. Connect frontend API layer to backend
6. Polish responsive UI, loading states, empty states, and errors
7. Deploy frontend and backend separately

## Product Summary

Campio helps students discover:

- Internships
- Contests
- Scholarships
- Exchange programs
- External activities
- Startup grants
- Seminars
- Campus events
- Mentoring opportunities
- Senior reviews and Q&A

## Tech Stack

Frontend:
- React
- Vite
- React Router
- CSS variables
- Axios or fetch
- Mock data first

Backend:
- Spring Boot
- Spring Web
- Spring Data JPA
- H2 for local development
- MySQL/PostgreSQL-ready schema
- REST API

## Project Structure

Recommended final structure:

```text
campio/
  README.md
  PROJECT.md
  docs/
  campio-frontend/
  campio-backend/
  postman/
```
