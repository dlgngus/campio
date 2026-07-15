# Deployment Checklist

## Owner Inputs To Prepare

- Choose the production frontend hosting provider.
- Choose the production backend hosting provider.
- Create or choose a managed PostgreSQL provider.
- Decide the production frontend domain.
- Decide the production backend/API domain.
- Prepare a production admin email and strong password.
- Confirm who has access to hosting, database, DNS, and repository settings.

## Required Before First Production Deploy

- Create a managed PostgreSQL database.
- Deploy backend on Render as a Docker Web Service.
- Set Render health check path to `/api/health`.
- Deploy backend with `SPRING_PROFILES_ACTIVE=prod`.
- Set backend environment variables:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `FRONTEND_ORIGIN`
  - `CAMPIO_ADMIN_EMAIL`
  - `CAMPIO_ADMIN_PASSWORD`
  - `CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true` for first production data fill
  - `CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true`
  - `PORT` if the host does not inject it automatically
- Confirm backend health:
  - `GET /api/health`
- Deploy frontend with:
  - `VITE_API_BASE_URL=https://your-backend-domain.example`
- For Vercel, use:
  - root directory `campio-frontend`
  - framework preset `Vite`
  - build command `npm run build`
  - output directory `dist`
- Set backend `FRONTEND_ORIGIN` to the exact frontend origin.
- Confirm frontend routes refresh correctly on nested paths.
- Confirm auth/session requests include credentials.

## Data Ingestion Setup Before Real Launch

- Pick the first 3 to 5 real data sources before expanding.
- Prefer official APIs or RSS feeds over HTML scraping.
- Confirm each source allows the intended collection method.
- Record source name, type, base URL, category hint, and crawl interval.
- Decide whether each source is production-ready or staging-only.
- Import or crawl into `raw_opportunities` first. For a fresh production database, enable startup crawl or run crawl jobs after deploy.
- Review raw records before publishing to `opportunities`.
- Keep source attribution and original apply/source URLs visible.

## Smoke Test After Deploy

```bash
curl https://your-backend-domain.example/api/health
curl https://your-backend-domain.example/api/opportunities
```

Browser checks:

- Open frontend home route.
- Open `/explore`.
- Open an opportunity detail page.
- Turn off or misconfigure the backend temporarily in staging and confirm error states show retry actions instead of blank screens.
- Confirm login and signup buttons are disabled while submitting.
- Confirm unauthenticated `/saved` and `/profile` show a login prompt.
- Verify save/unsave only after backend and cookies are correctly configured.
- Confirm save/unsave failure rolls the button state back.
- Confirm a real imported/crawled opportunity appears after admin review and publish.

## Production Follow-Ups

- Replace mock session fallback with real auth before public launch.
- Keep production mock user fallback disabled.
- Review Flyway migrations before any production schema change.
- Add monitoring for failed crawl jobs.
- Add backups and restore testing for PostgreSQL.
- Add source-specific crawl monitoring once real data sources are enabled.
