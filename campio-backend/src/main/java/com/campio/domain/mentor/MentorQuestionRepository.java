package com.campio.domain.mentor;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorQuestionRepository extends JpaRepository<MentorQuestion, Long> {
  List<MentorQuestion> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
  List<MentorQuestion> findByUserIdOrderByCreatedAtDesc(Long userId);

  interface MentorQuestionStats {
    Long getMentorId();
    long getTotalCount();
    long getAnsweredCount();
  }

  @org.springframework.data.jpa.repository.Query(
      "select q.mentorId as mentorId, count(q.id) as totalCount, sum(case when q.status = 'ANSWERED' then 1 else 0 end) as answeredCount from MentorQuestion q where q.mentorId in :mentorIds group by q.mentorId")
  List<MentorQuestionStats> statsByMentorIds(
      @org.springframework.data.repository.query.Param("mentorIds") java.util.Collection<Long> mentorIds);
}
