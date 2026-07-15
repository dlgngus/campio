package com.campio.domain.mentor;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorQuestionRepository extends JpaRepository<MentorQuestion, Long> {
  List<MentorQuestion> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
}
