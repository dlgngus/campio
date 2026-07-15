package com.campio.domain.ingestion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunitySourceRepository extends JpaRepository<OpportunitySource, Long> {
  List<OpportunitySource> findAllByOrderByCreatedAtDesc();

  boolean existsByName(String name);
}
