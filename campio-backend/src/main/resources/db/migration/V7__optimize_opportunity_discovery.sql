create index if not exists ix_opportunities_status_deadline
  on opportunities (status, deadline);

create index if not exists ix_opportunities_status_created_at
  on opportunities (status, created_at);

create index if not exists ix_opportunities_status_popularity
  on opportunities (status, popularity_count, created_at);

create index if not exists ix_opportunities_status_category_deadline
  on opportunities (status, category, deadline);
