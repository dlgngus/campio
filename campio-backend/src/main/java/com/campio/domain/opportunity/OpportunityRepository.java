package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
  @Override
  @EntityGraph(attributePaths = "tags")
  Optional<Opportunity> findById(Long id);

  @Override
  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findAll();

  @Override
  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findAllById(Iterable<Long> ids);
  Optional<Opportunity> findByApplyUrl(String applyUrl);

  Optional<Opportunity> findByTitleAndOrganizationAndDeadline(String title, String organization, LocalDate deadline);

  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findByStatusIgnoreCaseAndRecommendedTrueOrderByDeadlineAsc(String status);

  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findByStatusIgnoreCaseAndDeadlineBetweenOrderByDeadlineAsc(
      String status, LocalDate start, LocalDate end);

  Page<Opportunity> findByStatusIgnoreCaseAndDeadlineBetween(
      String status, LocalDate start, LocalDate end, Pageable pageable);

  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findByStatusIgnoreCaseOrderByPopularityCountDesc(String status);

  Page<Opportunity> findByStatusIgnoreCase(String status, Pageable pageable);

  @EntityGraph(attributePaths = "tags")
  List<Opportunity> findByStatusIgnoreCaseAndCategoryIgnoreCaseOrderByDeadlineAsc(String status, String category);

  @Modifying
  @Query("update Opportunity o set o.popularityCount = coalesce(o.popularityCount, 0) + 1 where o.id = :id")
  void incrementPopularity(@Param("id") Long id);

  @Modifying
  @Query("update Opportunity o set o.popularityCount = o.popularityCount - 1 where o.id = :id and o.popularityCount > 0")
  void decrementPopularity(@Param("id") Long id);
}
