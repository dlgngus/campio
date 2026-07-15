package com.campio.domain.ingestion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {
  List<CrawlJob> findAllByOrderByIdDesc();
}
