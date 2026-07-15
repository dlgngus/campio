package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
  Optional<Opportunity> findByApplyUrl(String applyUrl);

  Optional<Opportunity> findByTitleAndOrganizationAndDeadline(String title, String organization, LocalDate deadline);

  List<Opportunity> findByRecommendedTrueOrderByDeadlineAsc();

  List<Opportunity> findByDeadlineBeforeOrDeadlineEqualsOrderByDeadlineAsc(LocalDate before, LocalDate equals);

  List<Opportunity> findAllByOrderByPopularityCountDesc();

  List<Opportunity> findByCategoryIgnoreCaseOrderByDeadlineAsc(String category);
}
