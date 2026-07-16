delete from saved_opportunities
where user_id is null or opportunity_id is null
   or not exists (select 1 from users u where u.id = saved_opportunities.user_id)
   or not exists (select 1 from opportunities o where o.id = saved_opportunities.opportunity_id);

delete from application_records
where user_id is null or opportunity_id is null
   or not exists (select 1 from users u where u.id = application_records.user_id)
   or not exists (select 1 from opportunities o where o.id = application_records.opportunity_id);

delete from application_records a
where a.id not in (
  select min(a2.id) from application_records a2 group by a2.user_id, a2.opportunity_id
);

delete from comments
where post_id is null or user_id is null
   or not exists (select 1 from community_posts p where p.id = comments.post_id)
   or not exists (select 1 from users u where u.id = comments.user_id);

delete from community_posts
where user_id is null or not exists (select 1 from users u where u.id = community_posts.user_id);

update community_posts set opportunity_id = null
where opportunity_id is not null
  and not exists (select 1 from opportunities o where o.id = community_posts.opportunity_id);

delete from mentor_profiles
where user_id is null or not exists (select 1 from users u where u.id = mentor_profiles.user_id);

delete from mentor_profiles m
where m.id not in (
  select min(m2.id) from mentor_profiles m2 group by m2.user_id
);

delete from mentor_questions
where mentor_id is null or user_id is null
   or not exists (select 1 from mentor_profiles m where m.id = mentor_questions.mentor_id)
   or not exists (select 1 from users u where u.id = mentor_questions.user_id);

update mentor_questions set opportunity_id = null
where opportunity_id is not null
  and not exists (select 1 from opportunities o where o.id = mentor_questions.opportunity_id);

delete from raw_opportunities
where source_id is null or not exists (select 1 from opportunity_sources s where s.id = raw_opportunities.source_id);

delete from raw_opportunities r
where r.id not in (
  select min(r2.id) from raw_opportunities r2 group by r2.source_id, r2.content_hash
);

update raw_opportunities set normalized_opportunity_id = null
where normalized_opportunity_id is not null
  and not exists (select 1 from opportunities o where o.id = raw_opportunities.normalized_opportunity_id);

delete from crawl_jobs
where source_id is null or not exists (select 1 from opportunity_sources s where s.id = crawl_jobs.source_id);

delete from opportunity_tags
where opportunity_id is null or not exists (select 1 from opportunities o where o.id = opportunity_tags.opportunity_id);

delete from mentor_help_topics
where mentor_profile_id is null or not exists (select 1 from mentor_profiles m where m.id = mentor_help_topics.mentor_profile_id);

alter table saved_opportunities alter column user_id set not null;
alter table saved_opportunities alter column opportunity_id set not null;
alter table application_records alter column user_id set not null;
alter table application_records alter column opportunity_id set not null;
alter table community_posts alter column user_id set not null;
alter table comments alter column post_id set not null;
alter table comments alter column user_id set not null;
alter table mentor_profiles alter column user_id set not null;
alter table mentor_questions alter column mentor_id set not null;
alter table mentor_questions alter column user_id set not null;
alter table raw_opportunities alter column source_id set not null;
alter table crawl_jobs alter column source_id set not null;

create unique index if not exists ux_application_records_user_opportunity
  on application_records (user_id, opportunity_id);
create unique index if not exists ux_mentor_profiles_user_id
  on mentor_profiles (user_id);
create unique index if not exists ux_raw_opportunities_source_hash
  on raw_opportunities (source_id, content_hash);

alter table opportunity_tags add constraint fk_opportunity_tags_opportunity
  foreign key (opportunity_id) references opportunities(id) on delete cascade;
alter table saved_opportunities add constraint fk_saved_user
  foreign key (user_id) references users(id) on delete cascade;
alter table saved_opportunities add constraint fk_saved_opportunity
  foreign key (opportunity_id) references opportunities(id) on delete cascade;
alter table application_records add constraint fk_application_user
  foreign key (user_id) references users(id) on delete cascade;
alter table application_records add constraint fk_application_opportunity
  foreign key (opportunity_id) references opportunities(id) on delete cascade;
alter table community_posts add constraint fk_post_user
  foreign key (user_id) references users(id) on delete cascade;
alter table community_posts add constraint fk_post_opportunity
  foreign key (opportunity_id) references opportunities(id) on delete set null;
alter table comments add constraint fk_comment_post
  foreign key (post_id) references community_posts(id) on delete cascade;
alter table comments add constraint fk_comment_user
  foreign key (user_id) references users(id) on delete cascade;
alter table mentor_profiles add constraint fk_mentor_user
  foreign key (user_id) references users(id) on delete cascade;
alter table mentor_help_topics add constraint fk_mentor_topic_profile
  foreign key (mentor_profile_id) references mentor_profiles(id) on delete cascade;
alter table mentor_questions add constraint fk_mentor_question_mentor
  foreign key (mentor_id) references mentor_profiles(id) on delete cascade;
alter table mentor_questions add constraint fk_mentor_question_user
  foreign key (user_id) references users(id) on delete cascade;
alter table mentor_questions add constraint fk_mentor_question_opportunity
  foreign key (opportunity_id) references opportunities(id) on delete set null;
alter table raw_opportunities add constraint fk_raw_source
  foreign key (source_id) references opportunity_sources(id) on delete cascade;
alter table raw_opportunities add constraint fk_raw_normalized_opportunity
  foreign key (normalized_opportunity_id) references opportunities(id) on delete set null;
alter table crawl_jobs add constraint fk_crawl_job_source
  foreign key (source_id) references opportunity_sources(id) on delete cascade;
