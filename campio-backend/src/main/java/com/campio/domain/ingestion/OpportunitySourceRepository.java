package com.campio.domain.ingestion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunitySourceRepository extends JpaRepository<OpportunitySource, Long> {
  List<OpportunitySource> findAllByOrderByCreatedAtDesc();

  boolean existsByName(String name);

  Optional<OpportunitySource> findByName(String name);
}
