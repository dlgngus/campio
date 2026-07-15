package com.campio.domain.mentor;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
  List<MentorProfile> findByAvailableTrueOrderByIdAsc();
}

