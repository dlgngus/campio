package com.campio.domain.opportunity;

import com.campio.domain.saved.SavedOpportunityRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.Set;
import java.net.URI;
import java.util.Comparator;
import java.util.Locale;
import com.campio.global.exception.BadRequestException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OpportunityService {

  private static final String PUBLISHED = "PUBLISHED";
  private static final Set<String> ALLOWED_STATUSES = Set.of("DRAFT", PUBLISHED, "ARCHIVED");

  private final OpportunityRepository opportunityRepository;
  private final SavedOpportunityRepository savedOpportunityRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<OpportunityResponse> listAll(HttpSession session) {
    return mapWithSaved(
        filterStudentRelevant(opportunityRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(PUBLISHED)),
        session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> recommended(HttpSession session) {
    LocalDate today = LocalDate.now();
    Set<String> terms = userService.recommendationTerms(session);
    List<Opportunity> list = filterStudentRelevant(
        opportunityRepository.findByStatusIgnoreCaseOrderByCreatedAtDesc(PUBLISHED))
        .stream()
        .filter(opportunity -> opportunity.getDeadline() == null || !opportunity.getDeadline().isBefore(today))
        .sorted(Comparator
            .comparingInt((Opportunity opportunity) -> recommendationScore(opportunity, terms)).reversed()
            .thenComparing(opportunity -> opportunity.getDeadline() == null ? LocalDate.MAX : opportunity.getDeadline()))
        .limit(8)
        .collect(Collectors.toList());
    return mapWithSaved(filterStudentRelevant(list), session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> closingSoon(HttpSession session) {
    LocalDate today = LocalDate.now();
    return mapWithSaved(
        filterStudentRelevant(opportunityRepository.findByStatusIgnoreCaseAndDeadlineBetweenOrderByDeadlineAsc(
            PUBLISHED, today, today.plusDays(30))),
        session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> popular(HttpSession session) {
    return mapWithSaved(
        filterStudentRelevant(opportunityRepository.findByStatusIgnoreCaseOrderByPopularityCountDesc(PUBLISHED)),
        session);
  }

  @Transactional(readOnly = true)
  public OpportunityResponse detail(Long id, HttpSession session) {
    Opportunity opportunity = findOpportunity(id);
    if (!PUBLISHED.equalsIgnoreCase(opportunity.getStatus()) && !userService.isAdmin(session)) {
      throw new NotFoundException("Opportunity not found");
    }
    return toResponse(opportunity, isSaved(id, session));
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
    return toResponse(opportunityRepository.save(opportunity), isSaved(id, session));
  }

  @Transactional
  public void delete(Long id, HttpSession session) {
    userService.requireAdmin(session);
    Opportunity opportunity = findOpportunity(id);
    opportunityRepository.delete(opportunity);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> byCategory(String category) {
    return mapWithSaved(
        opportunityRepository.findByStatusIgnoreCaseAndCategoryIgnoreCaseOrderByDeadlineAsc(PUBLISHED, category),
        null);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> adminList(HttpSession session) {
    userService.requireAdmin(session);
    return opportunityRepository.findAll().stream()
        .map(opportunity -> toResponse(opportunity, false))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> savedByIds(Collection<Long> ids, HttpSession session) {
    userService.currentUserId(session);
    Map<Long, Opportunity> opportunities = opportunityRepository.findAllById(ids).stream()
        .filter(opportunity -> PUBLISHED.equalsIgnoreCase(opportunity.getStatus()))
        .filter(opportunity -> StudentOpportunityPolicy.isStudentRelevant(
            opportunity.getTitle(), opportunity.getOrganization(), opportunity.getCategory(),
            opportunity.getDescription(), opportunity.getTarget(), opportunity.getTags()))
        .collect(Collectors.toMap(Opportunity::getId, Function.identity()));
    return ids.stream().map(opportunities::get).filter(java.util.Objects::nonNull)
        .map(opportunity -> toResponse(opportunity, true)).collect(Collectors.toList());
  }

  private void applyRequest(Opportunity opportunity, OpportunityRequest request) {
    validateRequest(request);
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
    opportunity.setStatus(request.getStatus().trim().toUpperCase());
    opportunity.setPopularityCount(request.getPopularityCount() == null ? 0 : request.getPopularityCount());
    opportunity.setRecommended(request.isRecommended());
    opportunity.setNewThisWeek(request.isNewThisWeek());
  }

  private void validateRequest(OpportunityRequest request) {
    if (request.getStartDate() != null
        && request.getEndDate() != null
        && request.getEndDate().isBefore(request.getStartDate())) {
      throw new BadRequestException("End date must not be before start date");
    }
    String status = request.getStatus() == null ? "" : request.getStatus().trim().toUpperCase();
    if (!ALLOWED_STATUSES.contains(status)) {
      throw new BadRequestException("Unsupported opportunity status");
    }
    validateHttpUrl(request.getApplyUrl(), "Apply URL");
    validateHttpUrl(request.getThumbnailUrl(), "Thumbnail URL");
  }

  private void validateHttpUrl(String value, String field) {
    if (value == null || value.isBlank()) {
      return;
    }
    try {
      URI uri = URI.create(value);
      if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
        throw new IllegalArgumentException();
      }
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException(field + " must be an HTTP or HTTPS URL");
    }
  }

  private Opportunity findOpportunity(Long id) {
    return opportunityRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Opportunity not found"));
  }

  private boolean isSaved(Long opportunityId, HttpSession session) {
    Long userId = userService.optionalCurrentUserId(session);
    if (userId == null) {
      return false;
    }
    return savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId).isPresent();
  }

  private List<OpportunityResponse> mapWithSaved(List<Opportunity> opportunities, HttpSession session) {
    Long userId = userService.optionalCurrentUserId(session);
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

  private List<Opportunity> filterStudentRelevant(List<Opportunity> opportunities) {
    return opportunities.stream()
        .filter(opportunity -> StudentOpportunityPolicy.isStudentRelevant(
            opportunity.getTitle(),
            opportunity.getOrganization(),
            opportunity.getCategory(),
            opportunity.getDescription(),
            opportunity.getTarget(),
            opportunity.getTags()))
        .collect(Collectors.toList());
  }

  private int recommendationScore(Opportunity opportunity, Set<String> terms) {
    int score = opportunity.isRecommended() ? 10 : 0;
    String text = String.join(" ",
        opportunity.getTitle() == null ? "" : opportunity.getTitle(),
        opportunity.getCategory() == null ? "" : opportunity.getCategory(),
        opportunity.getOrganization() == null ? "" : opportunity.getOrganization(),
        opportunity.getDescription() == null ? "" : opportunity.getDescription(),
        opportunity.getTags() == null ? "" : String.join(" ", opportunity.getTags()))
        .toLowerCase(Locale.ROOT);
    for (String term : terms) {
      if (text.contains(term)) score += 3;
    }
    return score;
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
