# API Reference

Base URL:

```text
http://localhost:8080
```

## Auth

```text
POST /api/auth/signup
POST /api/auth/login
GET /api/auth/me
POST /api/auth/logout
```

## Users

```text
PATCH /api/users/profile
PATCH /api/users/interests
POST /api/users/verify-school
```

## Opportunities

```text
GET /api/opportunities
GET /api/opportunities/recommended
GET /api/opportunities/closing-soon
GET /api/opportunities/popular
GET /api/opportunities/{id}
POST /api/opportunities
PATCH /api/opportunities/{id}
DELETE /api/opportunities/{id}
```

## Saved

```text
GET /api/saved
POST /api/opportunities/{id}/save
DELETE /api/opportunities/{id}/save
```

## Applications

```text
GET /api/applications
POST /api/opportunities/{id}/apply-record
PATCH /api/applications/{id}
```

## Community

```text
GET /api/posts
GET /api/posts/{id}
POST /api/posts
PATCH /api/posts/{id}
DELETE /api/posts/{id}
POST /api/posts/{id}/comments
```

## Mentors

```text
GET /api/mentors
GET /api/mentors/{id}
POST /api/mentors/apply
POST /api/mentors/{id}/questions
```

## Admin Ingestion

```text
GET /api/admin/ingestion/sources
POST /api/admin/ingestion/sources
PATCH /api/admin/ingestion/sources/{id}
DELETE /api/admin/ingestion/sources/{id}

GET /api/admin/ingestion/raw-opportunities
POST /api/admin/ingestion/raw-opportunities
POST /api/admin/ingestion/raw-opportunities/import
PATCH /api/admin/ingestion/raw-opportunities/{id}/status
POST /api/admin/ingestion/raw-opportunities/{id}/publish

GET /api/admin/ingestion/crawl-jobs
POST /api/admin/ingestion/crawl-jobs
PATCH /api/admin/ingestion/crawl-jobs/{id}
POST /api/admin/ingestion/crawl-jobs/{id}/run
```

Allowed ingestion values:

```text
source.type: API, RSS, HTML, MANUAL
rawOpportunity.status: NEW, NORMALIZED, DUPLICATE, REJECTED, PUBLISHED
crawlJob.status: PENDING, RUNNING, SUCCESS, FAILED
```
