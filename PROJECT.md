# Campio AI Project Rules

This file is the highest-priority project instruction for Codex, Claude Code, or any AI coding agent.

## Product Rule

Campio is NOT primarily a mentoring app.
Campio is an opportunity discovery platform for university students.

Mentors, reviews, and community exist only to help students make better decisions about opportunities.

## Design Rule

Follow the design system in:

- `docs/02-design/DESIGN_SYSTEM.md`
- `docs/02-design/COLORS.md`
- `docs/02-design/COMPONENTS.md`

Never create a light theme.
Never use random colors.
Never use blue as a button background.
Use the accent blue only for:
- links
- focus rings
- active states
- selected states

## Implementation Rule

Start with frontend UI using mock data.
Then create backend APIs.
Then connect frontend to backend.

## Frontend Rules

- Use React + Vite.
- Use React Router.
- Use reusable components.
- Use CSS variables from `tokens.css`.
- Do not hardcode colors inside components.
- Keep pages clean and componentized.
- Every page must be responsive.
- Every async area must support loading, empty, and error states.

## Backend Rules

- Use Spring Boot.
- Use domain-based package structure.
- Use Controller, Service, Repository layers.
- Use DTOs for request/response.
- Keep auth as mock/JWT-ready placeholder for MVP.
- Seed sample data for local development.

## Must-have MVP Pages

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

## Must-have MVP Backend Domains

- User
- Opportunity
- SavedOpportunity
- ApplicationRecord
- CommunityPost
- Comment
- MentorProfile

## Quality Bar

Do not produce placeholder-looking UI.
Campio should feel like a real premium student opportunity platform inspired by Framer, Linear, Apple, and Raycast.
