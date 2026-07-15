package com.campio.domain.saved;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedOpportunityRepository extends JpaRepository<SavedOpportunity, Long> {
  List<SavedOpportunity> findByUserId(Long userId);

  Optional<SavedOpportunity> findByUserIdAndOpportunityId(Long userId, Long opportunityId);
}
