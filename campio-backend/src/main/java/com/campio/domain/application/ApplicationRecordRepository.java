package com.campio.domain.application;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {
  List<ApplicationRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
  Optional<ApplicationRecord> findByIdAndUserId(Long id, Long userId);
  Optional<ApplicationRecord> findByUserIdAndOpportunityId(Long userId, Long opportunityId);
}
