# Codex Master Prompt

Copy this prompt into Codex after placing this docs folder in the project root.

---

You are building Campio, a full-stack web platform where university students can discover every opportunity in one place.

Before writing code, read these files:

1. `PROJECT.md`
2. `docs/01-product/PRODUCT_SPEC.md`
3. `docs/02-design/DESIGN_SYSTEM.md`
4. `docs/03-screens/HOME.md`
5. `docs/04-frontend/FRONTEND_SPEC.md`
6. `docs/05-backend/BACKEND_SPEC.md`
7. `docs/07-api/API_REFERENCE.md`

Core product rule:
Campio is opportunity discovery first. Mentoring and community are supporting features.

Build the project in this order:

## Phase 1 — Frontend foundation

Create `campio-frontend` with React + Vite.

Implement:
- Routing
- Global CSS tokens
- Layout
- Navbar
- Mobile tab bar
- Reusable components
- API-backed data loading

Do not add mock data files for production screens.

## Phase 2 — Frontend MVP pages

Implement these pages using backend APIs and real stored data:
- Landing
- Login
- Signup
- Onboarding
- Home
- Explore
- Opportunity Detail
- Saved
- Community
- Mentors
- Profile
- Admin Opportunities

The UI must follow the dark Framer-inspired design system.

## Phase 3 — Backend foundation

Create `campio-backend` with Spring Boot.

Implement:
- Domain entities
- Repositories
- Services
- Controllers
- DTOs
- H2 local database
- Admin bootstrap from environment variables
- CORS for frontend
- Auth endpoints

## Phase 4 — API integration

Connect frontend API layer to backend:
- Opportunities
- Saved opportunities
- Applications
- Community posts
- Mentors
- Profile

## Phase 5 — Polish

Add:
- loading states
- empty states
- error states
- responsive mobile layout
- hover motion
- form validation
- admin opportunity creation/editing

## Hard constraints

- No light mode.
- Do not use Tailwind unless the project already uses it.
- Prefer normal CSS modules or global CSS variables.
- Do not create huge components.
- Do not hardcode colors.
- Use design tokens.
- Do not create or reintroduce mock data files for production screens.
- Keep implementation easy to understand for a junior full-stack developer.
