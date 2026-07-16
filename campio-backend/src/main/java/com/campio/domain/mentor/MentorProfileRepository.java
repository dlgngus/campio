package com.campio.domain.mentor;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
  @EntityGraph(attributePaths = "helpTopics")
  List<MentorProfile> findByAvailableTrueOrderByIdAsc();
  @EntityGraph(attributePaths = "helpTopics")
  Optional<MentorProfile> findByUserId(Long userId);
  @Override
  @EntityGraph(attributePaths = "helpTopics")
  List<MentorProfile> findAll();
  @Override
  @EntityGraph(attributePaths = "helpTopics")
  List<MentorProfile> findAllById(Iterable<Long> ids);
  @Override
  @EntityGraph(attributePaths = "helpTopics")
  Optional<MentorProfile> findById(Long id);
}
