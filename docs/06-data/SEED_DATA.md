# Real Data Bootstrap

Campio should not insert sample opportunities, posts, mentors, or users into application databases.

The only automatic bootstrap is the configured admin account from:

- `CAMPIO_ADMIN_EMAIL`
- `CAMPIO_ADMIN_PASSWORD`

## Initial Opportunity Data

Use the ingestion pipeline:

1. Create an admin account through environment variables.
2. Sign in as admin.
3. Register an approved source under `/api/admin/ingestion/sources`.
4. Import manual JSON or run an approved API/RSS crawl job.
5. Review records in `raw_opportunities`.
6. Publish valid records into `opportunities`.

The backend currently bootstraps real source configurations for:

- `K-Startup 모집중 사업공고`
- `기업마당 지원사업 공고`

Run crawl jobs for these sources to fetch current public announcements. Each adapter follows public pagination links, capped at 20 pages per crawl, and publishes valid records with parsed title, organization, start date, deadline, category, and source URL. Records with ambiguous deadlines, such as open-ended `상시 접수`, remain in `raw_opportunities` for review instead of being auto-published.

For deployment, set `CAMPIO_INGESTION_AUTO_RUN_ON_STARTUP=true` to run enabled sources after the backend starts. Keep `CAMPIO_INGESTION_AUTO_RUN_ONLY_WHEN_EMPTY=true` so automatic startup ingestion only runs when no published opportunities exist. This prevents a fresh production database from launching with an empty opportunity feed without recrawling on every restart.

## Allowed Sources

- Manual admin JSON import from verified opportunities
- Official public APIs with allowed usage
- RSS feeds with allowed usage
- Controlled public HTML adapters that are explicitly implemented and documented, currently K-Startup and 기업마당

Do not use sample frontend files, synthetic backend seeders, or copied third-party full text as production content.
