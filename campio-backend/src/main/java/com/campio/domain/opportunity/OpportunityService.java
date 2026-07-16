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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import com.campio.global.exception.BadRequestException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
  public OpportunityPageResponse search(
      int page,
      int size,
      String query,
      String target,
      String category,
      String region,
      boolean deadlineSoon,
      boolean online,
      boolean savedOnly,
      String sort,
      HttpSession session) {
    int safePage = Math.max(0, page);
    int safeSize = Math.min(48, Math.max(1, size));
    Set<Long> savedIds = savedOpportunityIds(session, savedOnly);

    Specification<Opportunity> specification = searchSpecification(
        query, target, category, region, deadlineSoon, online, savedOnly, savedIds);
    Page<Opportunity> result = opportunityRepository.findAll(
        specification, PageRequest.of(safePage, safeSize, searchSort(sort)));

    List<Long> ids = result.getContent().stream().map(Opportunity::getId).collect(Collectors.toList());
    Map<Long, Opportunity> hydrated = opportunityRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(Opportunity::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    List<OpportunityResponse> content = ids.stream()
        .map(hydrated::get)
        .filter(java.util.Objects::nonNull)
        .map(opportunity -> toResponse(opportunity, savedIds.contains(opportunity.getId())))
        .collect(Collectors.toList());

    return new OpportunityPageResponse(
        content,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.isFirst(),
        result.isLast());
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> recommended(HttpSession session) {
    LocalDate today = LocalDate.now();
    Set<String> terms = userService.recommendationTerms(session);
    Specification<Opportunity> active = (root, criteriaQuery, builder) -> builder.and(
        builder.equal(builder.upper(root.get("status")), PUBLISHED),
        builder.or(builder.isNull(root.get("deadline")), builder.greaterThanOrEqualTo(root.get("deadline"), today)));
    Page<Opportunity> candidates = opportunityRepository.findAll(
        active,
        PageRequest.of(0, 200, Sort.by(
            Sort.Order.desc("recommended"), Sort.Order.desc("popularityCount"), Sort.Order.desc("createdAt"))));
    List<Opportunity> list = hydrate(candidates.getContent())
        .stream()
        .sorted(Comparator
            .comparingInt((Opportunity opportunity) -> recommendationScore(opportunity, terms)).reversed()
            .thenComparing(opportunity -> opportunity.getDeadline() == null ? LocalDate.MAX : opportunity.getDeadline()))
        .limit(8)
        .collect(Collectors.toList());
    return mapWithSaved(list, session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> findPublishedByIds(Collection<Long> ids, HttpSession session) {
    List<Long> uniqueIds = ids == null ? List.of() : ids.stream()
        .filter(java.util.Objects::nonNull)
        .distinct()
        .limit(100)
        .collect(Collectors.toList());
    if (uniqueIds.isEmpty()) {
      return List.of();
    }
    Map<Long, Opportunity> found = opportunityRepository.findAllById(uniqueIds).stream()
        .filter(opportunity -> PUBLISHED.equalsIgnoreCase(opportunity.getStatus()))
        .filter(opportunity -> StudentOpportunityPolicy.isStudentRelevant(
            opportunity.getTitle(), opportunity.getOrganization(), opportunity.getCategory(),
            opportunity.getDescription(), opportunity.getTarget(), opportunity.getTags()))
        .collect(Collectors.toMap(Opportunity::getId, Function.identity()));
    List<Opportunity> ordered = uniqueIds.stream().map(found::get)
        .filter(java.util.Objects::nonNull).collect(Collectors.toList());
    return mapWithSaved(ordered, session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> closingSoon(HttpSession session) {
    LocalDate today = LocalDate.now();
    Page<Opportunity> page = opportunityRepository.findByStatusIgnoreCaseAndDeadlineBetween(
        PUBLISHED, today, today.plusDays(30), PageRequest.of(0, 8, Sort.by("deadline").ascending()));
    return mapWithSaved(hydrate(page.getContent()), session);
  }

  @Transactional(readOnly = true)
  public List<OpportunityResponse> popular(HttpSession session) {
    Page<Opportunity> page = opportunityRepository.findByStatusIgnoreCase(
        PUBLISHED,
        PageRequest.of(0, 8, Sort.by(Sort.Order.desc("popularityCount"), Sort.Order.desc("createdAt"))));
    return mapWithSaved(hydrate(page.getContent()), session);
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

  private Set<Long> savedOpportunityIds(HttpSession session, boolean required) {
    Long userId = required
        ? userService.currentUserId(session)
        : userService.optionalCurrentUserId(session);
    if (userId == null) {
      return Set.of();
    }
    return savedOpportunityRepository.findByUserId(userId).stream()
        .map(savedOpportunity -> savedOpportunity.getOpportunityId())
        .collect(Collectors.toSet());
  }

  private Specification<Opportunity> searchSpecification(
      String queryText,
      String target,
      String category,
      String region,
      boolean deadlineSoon,
      boolean online,
      boolean savedOnly,
      Set<Long> savedIds) {
    return (root, criteriaQuery, builder) -> {
      criteriaQuery.distinct(true);
      boolean categoryFilter = hasText(category) && !"All".equalsIgnoreCase(category.trim());
      boolean regionFilter = hasText(region) && !"All".equalsIgnoreCase(region.trim());
      Join<Opportunity, String> tag = hasText(queryText) || categoryFilter || regionFilter
          ? root.join("tags", JoinType.LEFT)
          : null;
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.equal(builder.upper(root.get("status")), PUBLISHED));

      addTextSearch(predicates, builder, root, tag, queryText);
      if (hasText(target)) {
        predicates.add(textContains(builder, root.get("target"), target.trim()));
      }
      if (categoryFilter) {
        predicates.add(categoryPredicate(builder, root, tag, category.trim()));
      }
      if (regionFilter) {
        List<Predicate> locationMatches = new ArrayList<>();
        for (String term : regionTerms(region.trim())) {
          locationMatches.add(textContains(builder, root.get("location"), term));
          locationMatches.add(textContains(builder, root.get("title"), term));
          locationMatches.add(textContains(builder, root.get("organization"), term));
          locationMatches.add(textContains(builder, tag, term));
        }
        predicates.add(builder.or(locationMatches.toArray(new Predicate[0])));
      }
      if (deadlineSoon) {
        LocalDate today = LocalDate.now();
        predicates.add(builder.between(root.get("deadline"), today, today.plusDays(14)));
      }
      if (online) {
        predicates.add(builder.isTrue(root.get("isOnline")));
      }
      if (savedOnly) {
        predicates.add(savedIds.isEmpty() ? builder.disjunction() : root.get("id").in(savedIds));
      }
      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  private void addTextSearch(
      List<Predicate> predicates,
      javax.persistence.criteria.CriteriaBuilder builder,
      javax.persistence.criteria.Root<Opportunity> root,
      Join<Opportunity, String> tag,
      String value) {
    if (!hasText(value)) {
      return;
    }
    String text = value.trim();
    predicates.add(builder.or(
        textContains(builder, root.get("title"), text),
        textContains(builder, root.get("organization"), text),
        textContains(builder, root.get("category"), text),
        textContains(builder, root.get("description"), text),
        textContains(builder, root.get("target"), text),
        textContains(builder, root.get("location"), text),
        tag == null ? builder.disjunction() : textContains(builder, tag, text)));
  }

  private Predicate categoryPredicate(
      javax.persistence.criteria.CriteriaBuilder builder,
      javax.persistence.criteria.Root<Opportunity> root,
      Join<Opportunity, String> tag,
      String category) {
    List<Predicate> matches = new ArrayList<>();
    matches.add(builder.equal(builder.lower(root.get("category")), category.toLowerCase(Locale.ROOT)));
    for (String keyword : StudentOpportunityPolicy.categoryKeywords(category)) {
      matches.add(textContains(builder, root.get("title"), keyword));
      matches.add(textContains(builder, root.get("description"), keyword));
      matches.add(textContains(builder, root.get("target"), keyword));
      matches.add(textContains(builder, tag, keyword));
    }
    return builder.or(matches.toArray(new Predicate[0]));
  }

  private Predicate textContains(
      javax.persistence.criteria.CriteriaBuilder builder,
      javax.persistence.criteria.Expression<String> expression,
      String value) {
    return builder.like(
        builder.lower(builder.coalesce(expression, "")),
        "%" + value.toLowerCase(Locale.ROOT) + "%");
  }

  private Sort searchSort(String value) {
    String normalized = value == null ? "deadline" : value.trim().toLowerCase(Locale.ROOT);
    switch (normalized) {
      case "popular":
        return Sort.by(Sort.Order.desc("popularityCount"), Sort.Order.desc("createdAt"));
      case "latest":
        return Sort.by(Sort.Order.desc("createdAt"));
      case "title":
        return Sort.by(Sort.Order.asc("title"));
      case "deadline":
        return Sort.by(Sort.Order.asc("deadline").nullsLast(), Sort.Order.desc("createdAt"));
      default:
        throw new BadRequestException("Unsupported opportunity sort");
    }
  }

  private List<String> regionTerms(String region) {
    Map<String, String> shortNames = Map.ofEntries(
        Map.entry("서울특별시", "서울"), Map.entry("부산광역시", "부산"),
        Map.entry("대구광역시", "대구"), Map.entry("인천광역시", "인천"),
        Map.entry("광주광역시", "광주"), Map.entry("대전광역시", "대전"),
        Map.entry("울산광역시", "울산"), Map.entry("세종특별자치시", "세종"),
        Map.entry("경기도", "경기"), Map.entry("강원특별자치도", "강원"),
        Map.entry("충청북도", "충북"), Map.entry("충청남도", "충남"),
        Map.entry("전북특별자치도", "전북"), Map.entry("전라남도", "전남"),
        Map.entry("경상북도", "경북"), Map.entry("경상남도", "경남"),
        Map.entry("제주특별자치도", "제주"));
    String shortName = shortNames.get(region);
    return shortName == null ? List.of(region) : List.of(region, shortName);
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private List<Opportunity> hydrate(List<Opportunity> opportunities) {
    List<Long> ids = opportunities.stream().map(Opportunity::getId).collect(Collectors.toList());
    Map<Long, Opportunity> hydrated = opportunityRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(Opportunity::getId, Function.identity()));
    return ids.stream().map(hydrated::get).filter(java.util.Objects::nonNull).collect(Collectors.toList());
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
