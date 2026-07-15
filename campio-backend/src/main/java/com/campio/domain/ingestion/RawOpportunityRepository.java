package com.campio.domain.ingestion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawOpportunityRepository extends JpaRepository<RawOpportunity, Long> {
  List<RawOpportunity> findAllByOrderByFetchedAtDesc();

  Optional<RawOpportunity> findBySourceIdAndContentHash(Long sourceId, String contentHash);
}
