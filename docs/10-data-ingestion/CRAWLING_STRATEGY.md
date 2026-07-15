# Data Ingestion And Crawling Strategy

Campio should treat crawling as a controlled ingestion pipeline, not as blind page scraping. The goal is to collect accurate opportunity metadata, preserve source attribution, avoid duplicates, and keep each source within its allowed usage.

## Priority Order

Use data sources in this order:

1. Official API
2. RSS, sitemap, or structured public feed
3. Public HTML pages that allow crawling
4. Manual admin entry or student submission

Do not crawl login-only pages, bypass CAPTCHA, ignore robots.txt, or republish full third-party content when a title, summary, metadata, and source link are enough.

## Initial Source Targets

Start with a small source set and make quality measurable before expanding.

| Category | Candidate source type | Notes |
| --- | --- | --- |
| Government support | Public API or public notice page | K-Startup and public institution announcements |
| Youth policy | Public API or public notice page | Youth policy portals and local government feeds |
| Internship | Public job board links or partner feeds | Prefer official organization pages over aggregator scraping |
| Scholarship/exchange | University notices and foundation pages | High value for students, but source formats vary |
| Contest/hackathon | Official event pages and public feeds | Use canonical URLs aggressively for deduplication |

## Source Onboarding Checklist

Before enabling a source in production, collect and verify:

- Source owner or organization name
- Source URL or API endpoint
- Source type: `API`, `RSS`, `HTML`, or `MANUAL`
- Allowed usage:
  - public API terms, feed usage rules, robots.txt, or written permission
- Authentication requirements:
  - no auth, API key, OAuth, or partner credentials
- Expected fields:
  - title, organization, deadline, apply URL, source URL, category, content
- Update frequency:
  - hourly, daily, weekly, or manual
- Failure behavior:
  - retry later, disable source after repeated failures, or require admin review
- Publishing policy:
  - auto-publish only after quality gates, or always require admin review

Do not add a source directly to public `opportunities`. Every real source should first write to `raw_opportunities` so records can be reviewed, deduplicated, and reprocessed when parsing rules change.

## MVP Source Plan

Start with low-risk structured sources:

1. Manual admin JSON import for verified opportunities
2. Official public API sources with stable fields
3. RSS feeds from institutions or event pages
4. HTML adapters only for sources that explicitly allow crawling

For the first production release, use manual import plus API/RSS sources. Add HTML adapters later after there is monitoring for parser failures and source layout changes.

Current controlled HTML adapter:

- `K-Startup 모집중 사업공고`
- URL: `https://www.k-startup.go.kr/web/contents/bizpbanc-ongoing.do`
- Scope: public ongoing business announcement list only
- Pagination: follows public `fn_egov_link_page(...)` links, capped at 20 pages per crawl
- Stored content: title, organization, category hint, start date, deadline, source URL, short source attribution text
- Not stored: full third-party article body, attachments, login-only content, private/member-only content
- Auto-publish rule: only records with a parsed deadline and source attribution are published to `opportunities`; all fetched records are still preserved in `raw_opportunities`.
- SSL behavior: the adapter first uses the shared ingestion `RestTemplate`. If the local JVM truststore cannot validate the K-Startup certificate chain, it uses a K-Startup-only HTTPS fallback so the approved public source can still be fetched.

- `기업마당 지원사업 공고`
- URL: `https://www.bizinfo.go.kr/sii/siia/selectSIIA200View.do?rows=15&cpage=1`
- Scope: public in-progress business support announcement list only
- Pagination: follows public `cpage` links, capped at 20 pages per crawl
- Stored content: title, executing organization, support field, managing government body, application period, source URL, short source attribution text
- Not stored: full third-party article body, attachments, login-only content, private/member-only content
- Auto-publish rule: only records with a concrete `YYYY-MM-DD ~ YYYY-MM-DD` application period are published. Rolling/open-ended records such as `상시 접수` stay in `raw_opportunities` for review.

## Pipeline

```text
opportunity_sources
  -> fetcher
  -> raw_opportunities
  -> normalizer
  -> duplicate detector
  -> quality checks
  -> admin review
  -> opportunities
```

Each source needs its own adapter. The adapter should return source records only; normalization into Campio's `Opportunity` model happens in a shared layer.

## Source Registry

Every source must be configured before it is crawled.

Required fields:

- `name`
- `type`: `API`, `RSS`, `HTML`, or `MANUAL`
- `base_url`
- `category_hint`
- `crawl_interval_minutes`
- `robots_allowed`
- `enabled`
- `last_crawled_at`
- `failure_count`

This makes source ownership and failure behavior visible to admins.

## Raw Record Storage

Fetched records should first be written to `raw_opportunities`.

Required fields:

- `source_id`
- `external_id`
- `source_url`
- `raw_title`
- `raw_content`
- `raw_payload`
- `content_hash`
- `fetched_at`
- `last_seen_at`
- `normalized_opportunity_id`
- `status`

Never skip raw storage. It is needed for debugging parser failures, source changes, duplicate review, and re-normalization.

## Normalization

Normalize raw data into:

- `title`
- `organization`
- `category`
- `description`
- `requirements`
- `benefits`
- `target`
- `deadline`
- `start_date`
- `end_date`
- `location`
- `is_online`
- `apply_url`
- `thumbnail_url`
- `tags`
- `status`

If a deadline is missing or ambiguous, mark the raw record for review instead of guessing.

## Deduplication

Apply duplicate checks in this order:

1. Exact canonical `source_url`
2. Exact `external_id` within the same source
3. `title + organization + deadline`
4. Fuzzy title similarity for admin review

Do not auto-merge fuzzy matches until there is enough reviewed data to trust the threshold.

## Quality Gates

A normalized opportunity can be published only when it has:

- title
- organization or source name
- category
- source URL
- deadline or explicit rolling/open-ended status
- apply URL or source URL

Records that fail this gate stay in review.

## Scheduling

Recommended MVP schedule:

- Official APIs: every 6 to 12 hours
- RSS/sitemaps: every 12 hours
- Public HTML pages: once per day
- Manual submissions: immediate review queue

Use conservative request rates and back off on repeated failures.

## Implementation Phases

### Phase 1

- Add `opportunity_sources`, `raw_opportunities`, and `crawl_jobs`
- Add admin-only source CRUD
- Add manual import from JSON or CSV
- Add duplicate detection for exact URL and title/organization/deadline

### Phase 2

- Add API/RSS source adapters
- Add scheduled crawl jobs
- Add admin review screen for raw records
- Add source health indicators

### Phase 3

- Add controlled HTML adapters for approved sources
- Add fuzzy duplicate review
- Add automatic category/tag suggestions
- Add stale-opportunity detection using `last_seen_at`

## Operating Rule

Campio should store enough text to help students understand an opportunity, but the canonical application and full details should remain linked to the original source.

## Required Owner Decisions

The engineering pipeline can fetch, store, review, and publish records, but production data quality depends on source decisions.

Before launch, the owner should provide:

- The first source list with URLs or API docs
- Any API keys or partner credentials, stored only as hosting secrets
- The allowed crawl interval for each source
- Category mapping rules for each source
- Whether records can auto-publish or need admin review
- The minimum content quality expected before an opportunity appears publicly
