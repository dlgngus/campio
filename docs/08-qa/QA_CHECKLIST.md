# QA Checklist

## Frontend

- All routes render
- Unknown routes redirect to a valid screen
- Mobile layout works
- Navbar works
- Mobile tab bar works
- Opportunity card click opens detail
- Save button toggles
- Save/unsave failure restores the previous state
- Filters update list
- Empty states display
- Loading skeletons display
- API error states display retry actions
- Unauthenticated `/saved` and `/profile` show a login prompt
- Login/signup buttons disable while submitting
- Forms validate required fields

## Backend

- API starts locally
- `GET /api/health` returns ok
- H2 console works
- Fresh database has no sample opportunities, posts, or mentors
- Opportunities list returns imported/crawled data after admin publish
- Detail returns imported/crawled opportunity data
- Save/unsave works
- Protected write APIs return 401 without login when mock user fallback is disabled
- Admin APIs return 403 for non-admin users
- Configured admin account is created when admin env vars are present
- Community posts return user-created data
- Mentors return user/admin-created data

## Deployment

- `mvn -q test` passes
- `mvn -q -DskipTests package` passes
- `npm run build` passes
- `docker compose config --quiet` passes
- Docker daemon available: `docker compose up --build` starts frontend and backend
- Vercel frontend has `VITE_API_BASE_URL` set to the Render backend URL
- Render backend has `FRONTEND_ORIGIN` set to the exact Vercel origin

## Design

- Dark canvas everywhere
- No random colors
- No blue CTA buttons
- Cards use correct radius
- Buttons are pill-shaped
