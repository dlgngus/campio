package com.campio.domain.opportunity;

import com.campio.domain.saved.SavedOpportunityRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpportunityService {

  private final OpportunityRepository opportunityRepository;
  private final SavedOpportunityRepository savedOpportunityRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<OpportunityResponse> listAll() {
    return mapWithSaved(opportunityRepository.findAll());
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> recommended() {
    List<Opportunity> list = opportunityRepository.findByRecommendedTrueOrderByDeadlineAsc();
    if (list.isEmpty()) {
      list = opportunityRepository.findAll().stream().limit(4).collect(Collectors.toList());
    }
    return mapWithSaved(list);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> closingSoon() {
    LocalDate today = LocalDate.now();
    return mapWithSaved(
        opportunityRepository.findByDeadlineBeforeOrDeadlineEqualsOrderByDeadlineAsc(
            today.plusDays(30), today));
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> popular() {
    return mapWithSaved(opportunityRepository.findAllByOrderByPopularityCountDesc());
  }

  @Transactional(readOnly = true)
  public OpportunityResponse detail(Long id) {
    return toResponse(findOpportunity(id), isSaved(id));
  }

  @Transactional
  public OpportunityResponse create(OpportunityRequest request, HttpSession session) {
    userService.requireAdmin(session);
    Opportunity opportunity = new Opportunity();
    applyRequest(opportunity, request);
    opportunity.setCreatedAt(LocalDateTime.now());
    opportunity.setUpdatedAt(LocalDateTime.now());
    return toResponse(opportunityRepository.save(opportunity), false);
  }

  @Transactional
  public OpportunityResponse update(Long id, OpportunityRequest request, HttpSession session) {
    userService.requireAdmin(session);
    Opportunity opportunity = findOpportunity(id);
    applyRequest(opportunity, request);
    opportunity.setUpdatedAt(LocalDateTime.now());
    return toResponse(opportunityRepository.save(opportunity), isSaved(id));
  }

  @Transactional
  public void delete(Long id, HttpSession session) {
    userService.requireAdmin(session);
    Opportunity opportunity = findOpportunity(id);
    opportunityRepository.delete(opportunity);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> byCategory(String category) {
    return mapWithSaved(opportunityRepository.findByCategoryIgnoreCaseOrderByDeadlineAsc(category));
  }

  private void applyRequest(Opportunity opportunity, OpportunityRequest request) {
    opportunity.setTitle(request.getTitle());
    opportunity.setOrganization(request.getOrganization());
    opportunity.setCategory(request.getCategory());
    opportunity.setDescription(request.getDescription());
    opportunity.setRequirements(request.getRequirements());
    opportunity.setBenefits(request.getBenefits());
    opportunity.setTarget(request.getTarget());
    opportunity.setDeadline(request.getDeadline());
    opportunity.setStartDate(request.getStartDate());
    opportunity.setEndDate(request.getEndDate());
    opportunity.setLocation(request.getLocation());
    opportunity.setIsOnline(request.getIsOnline());
    opportunity.setApplyUrl(request.getApplyUrl());
    opportunity.setThumbnailUrl(request.getThumbnailUrl());
    opportunity.setTags(request.getTags());
    opportunity.setStatus(request.getStatus());
    opportunity.setPopularityCount(request.getPopularityCount() == null ? 0 : request.getPopularityCount());
    opportunity.setRecommended(request.isRecommended());
    opportunity.setNewThisWeek(request.isNewThisWeek());
  }

  private Opportunity findOpportunity(Long id) {
    return opportunityRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Opportunity not found"));
  }

  private boolean isSaved(Long opportunityId) {
    Long userId = userService.currentUserId();
    if (userId == null) {
      return false;
    }
    return savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId).isPresent();
  }

  private List<OpportunityResponse> mapWithSaved(List<Opportunity> opportunities) {
    Long userId = userService.currentUserId();
    Set<Long> savedIds =
        userId == null
            ? Set.of()
            : savedOpportunityRepository.findByUserId(userId).stream()
                .map(savedOpportunity -> savedOpportunity.getOpportunityId())
                .collect(Collectors.toSet());
    return opportunities.stream()
        .map(opportunity -> toResponse(opportunity, savedIds.contains(opportunity.getId())))
        .collect(Collectors.toList());
  }

  private OpportunityResponse toResponse(Opportunity opportunity, boolean saved) {
    return OpportunityResponse.builder()
        .id(opportunity.getId())
        .title(opportunity.getTitle())
        .organization(opportunity.getOrganization())
        .category(opportunity.getCategory())
        .description(opportunity.getDescription())
        .requirements(opportunity.getRequirements())
        .benefits(opportunity.getBenefits())
        .target(opportunity.getTarget())
        .deadline(opportunity.getDeadline())
        .startDate(opportunity.getStartDate())
        .endDate(opportunity.getEndDate())
        .location(opportunity.getLocation())
        .online(opportunity.getIsOnline())
        .applyUrl(opportunity.getApplyUrl())
        .thumbnailUrl(opportunity.getThumbnailUrl())
        .status(opportunity.getStatus())
        .tags(opportunity.getTags())
        .saved(saved)
        .popularityCount(opportunity.getPopularityCount())
        .recommended(opportunity.isRecommended())
        .newThisWeek(opportunity.isNewThisWeek())
        .createdAt(opportunity.getCreatedAt())
        .updatedAt(opportunity.getUpdatedAt())
        .build();
  }
}
