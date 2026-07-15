# Database Schema

## users

```sql
id bigint primary key
email varchar(255) unique not null
password varchar(255)
name varchar(100) not null
school varchar(100)
major varchar(100)
grade int
role varchar(30)
verified boolean
created_at timestamp
updated_at timestamp
```

## opportunities

```sql
id bigint primary key
title varchar(255) not null
organization varchar(255)
category varchar(50)
description text
requirements text
benefits text
deadline date
start_date date
end_date date
location varchar(255)
is_online boolean
apply_url varchar(500)
thumbnail_url varchar(500)
tags varchar(1000)
status varchar(30)
created_at timestamp
updated_at timestamp
```

## saved_opportunities

```sql
id bigint primary key
user_id bigint
opportunity_id bigint
created_at timestamp
```

## application_records

```sql
id bigint primary key
user_id bigint
opportunity_id bigint
status varchar(30)
memo text
created_at timestamp
updated_at timestamp
```

## community_posts

```sql
id bigint primary key
user_id bigint
opportunity_id bigint
type varchar(30)
title varchar(255)
content text
created_at timestamp
updated_at timestamp
```

## comments

```sql
id bigint primary key
post_id bigint
user_id bigint
content text
created_at timestamp
updated_at timestamp
```

## mentor_profiles

```sql
id bigint primary key
user_id bigint
company varchar(255)
position varchar(255)
experience text
help_topics varchar(1000)
available boolean
created_at timestamp
updated_at timestamp
```

## mentor_questions

```sql
id bigint primary key
mentor_id bigint
user_id bigint
opportunity_id bigint
content text
status varchar(30)
created_at timestamp
updated_at timestamp
```

## opportunity_sources

Tracks every approved source that Campio can ingest from.

```sql
id bigint primary key
name varchar(255) not null
type varchar(30) -- API, RSS, HTML, MANUAL
base_url varchar(1000)
category_hint varchar(50)
crawl_interval_minutes int
robots_allowed boolean
enabled boolean
last_crawled_at timestamp
failure_count int
created_at timestamp
updated_at timestamp
```

## raw_opportunities

Stores fetched source records before normalization and admin review.

```sql
id bigint primary key
source_id bigint
external_id varchar(255)
source_url varchar(1000)
raw_title varchar(500)
raw_content text
raw_payload text
content_hash varchar(128)
fetched_at timestamp
last_seen_at timestamp
normalized_opportunity_id bigint
status varchar(30) -- NEW, NORMALIZED, DUPLICATE, REJECTED, PUBLISHED
error_message text
```

## crawl_jobs

Stores scheduled and completed ingestion runs.

```sql
id bigint primary key
source_id bigint
status varchar(30) -- PENDING, RUNNING, SUCCESS, FAILED
started_at timestamp
finished_at timestamp
items_found int
items_created int
items_updated int
error_message text
```
