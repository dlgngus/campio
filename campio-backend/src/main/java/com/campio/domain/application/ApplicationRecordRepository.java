package com.campio.domain.application;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRecordRepository extends JpaRepository<ApplicationRecord, Long> {
  List<ApplicationRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}

