package com.campio.domain.ingestion;

import com.campio.domain.opportunity.Opportunity;
import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

  private final OpportunitySourceRepository sourceRepository;
  private final RawOpportunityRepository rawOpportunityRepository;
  private final CrawlJobRepository crawlJobRepository;
  private final OpportunityRepository opportunityRepository;
  private final IngestionAdapterRegistry adapterRegistry;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<OpportunitySourceResponse> listSources(HttpSession session) {
    userService.requireAdmin(session);
    return sourceRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toSourceResponse).collect(Collectors.toList());
  }

  @Transactional
  public OpportunitySourceResponse createSource(OpportunitySourceRequest request, HttpSession session) {
    userService.requireAdmin(session);
    OpportunitySource source = new OpportunitySource();
    applySourceRequest(source, request);
    source.setFailureCount(0);
    source.setCreatedAt(LocalDateTime.now());
    source.setUpdatedAt(LocalDateTime.now());
    return toSourceResponse(sourceRepository.save(source));
  }

  @Transactional
  public OpportunitySourceResponse updateSource(Long id, OpportunitySourceRequest request, HttpSession session) {
    userService.requireAdmin(session);
    OpportunitySource source = findSource(id);
    applySourceRequest(source, request);
    source.setUpdatedAt(LocalDateTime.now());
    return toSourceResponse(sourceRepository.save(source));
  }

  @Transactional
  public void deleteSource(Long id, HttpSession session) {
    userService.requireAdmin(session);
    sourceRepository.delete(findSource(id));
  }

  @Transactional(readOnly = true)
  public List<RawOpportunityResponse> listRawOpportunities(HttpSession session) {
    userService.requireAdmin(session);
    return rawOpportunityRepository.findAllByOrderByFetchedAtDesc().stream().map(this::toRawResponse).collect(Collectors.toList());
  }

  @Transactional
  public RawOpportunityResponse createRawOpportunity(RawOpportunityRequest request, HttpSession session) {
    userService.requireAdmin(session);
    return toRawResponse(upsertRawOpportunity(
        request.getSourceId(),
        request.getExternalId(),
        request.getSourceUrl(),
        request.getRawTitle(),
        request.getRawContent(),
        request.getRawPayload(),
        request.getContentHash()));
  }

  @Transactional
  public RawOpportunityImportResponse importRawOpportunities(RawOpportunityImportRequest request, HttpSession session) {
    userService.requireAdmin(session);
    findSource(request.getSourceId());
    List<Long> ids = new ArrayList<>();
    int createdCount = 0;
    int updatedCount = 0;
    for (RawOpportunityImportItem item : request.getItems()) {
      RawOpportunity raw = upsertRawOpportunity(
          request.getSourceId(),
          item.getExternalId(),
          item.getSourceUrl(),
          item.getRawTitle(),
          item.getRawContent(),
          item.getRawPayload(),
          item.getContentHash());
      ids.add(raw.getId());
      if (raw.getFetchedAt().equals(raw.getLastSeenAt())) {
        createdCount++;
      } else {
        updatedCount++;
      }
    }
    return RawOpportunityImportResponse.builder()
        .sourceId(request.getSourceId())
        .requestedCount(request.getItems().size())
        .createdCount(createdCount)
        .updatedCount(updatedCount)
        .rawOpportunityIds(ids)
        .build();
  }

  @Transactional
  public RawOpportunityResponse updateRawStatus(Long id, RawOpportunityStatusRequest request, HttpSession session) {
    userService.requireAdmin(session);
    RawOpportunity raw = findRawOpportunity(id);
    raw.setStatus(request.getStatus().name());
    raw.setNormalizedOpportunityId(request.getNormalizedOpportunityId());
    raw.setErrorMessage(request.getErrorMessage());
    raw.setLastSeenAt(LocalDateTime.now());
    return toRawResponse(rawOpportunityRepository.save(raw));
  }

  @Transactional
  public PublishedOpportunityResponse publishRawOpportunity(
      Long rawOpportunityId, PublishRawOpportunityRequest request, HttpSession session) {
    userService.requireAdmin(session);
    RawOpportunity raw = findRawOpportunity(rawOpportunityId);
    if (raw.getNormalizedOpportunityId() != null && RawOpportunityStatus.PUBLISHED.name().equals(raw.getStatus())) {
      return PublishedOpportunityResponse.builder()
          .rawOpportunityId(raw.getId())
          .opportunityId(raw.getNormalizedOpportunityId())
          .rawStatus(raw.getStatus())
          .created(false)
          .build();
    }
    if (request.getDeadline() == null) {
      throw new BadRequestException("Deadline is required before publishing");
    }

    Opportunity opportunity = new Opportunity();
    opportunity.setTitle(request.getTitle());
    opportunity.setOrganization(firstNonBlank(request.getOrganization(), sourceName(raw.getSourceId())));
    opportunity.setCategory(request.getCategory());
    opportunity.setDescription(firstNonBlank(request.getDescription(), raw.getRawContent()));
    opportunity.setRequirements(request.getRequirements());
    opportunity.setBenefits(request.getBenefits());
    opportunity.setTarget(request.getTarget());
    opportunity.setDeadline(request.getDeadline());
    opportunity.setStartDate(request.getStartDate());
    opportunity.setEndDate(request.getEndDate());
    opportunity.setLocation(request.getLocation());
    opportunity.setIsOnline(request.getIsOnline());
    opportunity.setApplyUrl(firstNonBlank(request.getApplyUrl(), raw.getSourceUrl()));
    opportunity.setThumbnailUrl(request.getThumbnailUrl());
    opportunity.setTags(request.getTags());
    opportunity.setStatus("PUBLISHED");
    opportunity.setPopularityCount(0);
    opportunity.setRecommended(request.isRecommended());
    opportunity.setNewThisWeek(request.isNewThisWeek());
    opportunity.setCreatedAt(LocalDateTime.now());
    opportunity.setUpdatedAt(LocalDateTime.now());
    Opportunity saved = opportunityRepository.save(opportunity);

    raw.setNormalizedOpportunityId(saved.getId());
    raw.setStatus(RawOpportunityStatus.PUBLISHED.name());
    raw.setErrorMessage(null);
    raw.setLastSeenAt(LocalDateTime.now());
    rawOpportunityRepository.save(raw);

    return PublishedOpportunityResponse.builder()
        .rawOpportunityId(raw.getId())
        .opportunityId(saved.getId())
        .rawStatus(raw.getStatus())
        .created(true)
        .build();
  }

  @Transactional(readOnly = true)
  public List<CrawlJobResponse> listCrawlJobs(HttpSession session) {
    userService.requireAdmin(session);
    return crawlJobRepository.findAllByOrderByIdDesc().stream().map(this::toJobResponse).collect(Collectors.toList());
  }

  @Transactional
  public CrawlJobResponse createCrawlJob(CrawlJobRequest request, HttpSession session) {
    userService.requireAdmin(session);
    findSource(request.getSourceId());
    CrawlJob job = new CrawlJob();
    job.setSourceId(request.getSourceId());
    job.setStatus(CrawlJobStatus.PENDING.name());
    job.setItemsFound(0);
    job.setItemsCreated(0);
    job.setItemsUpdated(0);
    return toJobResponse(crawlJobRepository.save(job));
  }

  @Transactional
  public CrawlJobResponse updateCrawlJob(Long id, CrawlJobStatusRequest request, HttpSession session) {
    userService.requireAdmin(session);
    CrawlJob job = findCrawlJob(id);
    CrawlJobStatus status = request.getStatus();
    job.setStatus(status.name());
    job.setItemsFound(defaultInt(request.getItemsFound()));
    job.setItemsCreated(defaultInt(request.getItemsCreated()));
    job.setItemsUpdated(defaultInt(request.getItemsUpdated()));
    job.setErrorMessage(request.getErrorMessage());
    if (status == CrawlJobStatus.RUNNING && job.getStartedAt() == null) {
      job.setStartedAt(LocalDateTime.now());
    }
    if (status == CrawlJobStatus.SUCCESS || status == CrawlJobStatus.FAILED) {
      job.setFinishedAt(LocalDateTime.now());
      updateSourceAfterJob(job);
    }
    return toJobResponse(crawlJobRepository.save(job));
  }

  @Transactional
  public CrawlJobResponse runCrawlJob(Long id, HttpSession session) {
    userService.requireAdmin(session);
    return runCrawlJobInternal(id);
  }

  @Transactional
  public List<CrawlJobResponse> runEnabledSources() {
    List<CrawlJobResponse> responses = new ArrayList<>();
    for (OpportunitySource source : sourceRepository.findAll()) {
      if (!source.isEnabled()) {
        continue;
      }
      CrawlJob job = new CrawlJob();
      job.setSourceId(source.getId());
      job.setStatus(CrawlJobStatus.PENDING.name());
      job.setItemsFound(0);
      job.setItemsCreated(0);
      job.setItemsUpdated(0);
      CrawlJob saved = crawlJobRepository.save(job);
      responses.add(runCrawlJobInternal(saved.getId()));
    }
    return responses;
  }

  private CrawlJobResponse runCrawlJobInternal(Long id) {
    CrawlJob job = findCrawlJob(id);
    OpportunitySource source = findSource(job.getSourceId());
    job.setStatus(CrawlJobStatus.RUNNING.name());
    job.setStartedAt(LocalDateTime.now());
    job.setErrorMessage(null);
    crawlJobRepository.save(job);

    try {
      IngestionAdapter adapter = adapterRegistry.get(OpportunitySourceType.valueOf(source.getType()));
      List<FetchedRawOpportunity> fetchedItems = adapter.fetch(source);
      int createdCount = 0;
      int updatedCount = 0;
      for (FetchedRawOpportunity item : fetchedItems) {
        RawOpportunity raw = upsertRawOpportunity(
            source.getId(),
            item.getExternalId(),
            item.getSourceUrl(),
            item.getRawTitle(),
            item.getRawContent(),
            item.getRawPayload(),
            item.getContentHash());
        boolean created = raw.getFetchedAt().equals(raw.getLastSeenAt());
        autoPublishIfReady(source, raw, item);
        if (created) {
          createdCount++;
        } else {
          updatedCount++;
        }
      }
      job.setStatus(CrawlJobStatus.SUCCESS.name());
      job.setItemsFound(fetchedItems.size());
      job.setItemsCreated(createdCount);
      job.setItemsUpdated(updatedCount);
      job.setFinishedAt(LocalDateTime.now());
      job.setErrorMessage(null);
      updateSourceAfterJob(job);
    } catch (RuntimeException ex) {
      log.warn("Crawl job failed. jobId={}, sourceId={}, sourceName={}", job.getId(), source.getId(), source.getName(), ex);
      job.setStatus(CrawlJobStatus.FAILED.name());
      job.setFinishedAt(LocalDateTime.now());
      job.setErrorMessage(ex.getMessage());
      job.setItemsFound(0);
      job.setItemsCreated(0);
      job.setItemsUpdated(0);
      updateSourceAfterJob(job);
    }
    return toJobResponse(crawlJobRepository.save(job));
  }

  private void applySourceRequest(OpportunitySource source, OpportunitySourceRequest request) {
    source.setName(request.getName());
    source.setType(request.getType().name());
    source.setBaseUrl(request.getBaseUrl());
    source.setCategoryHint(request.getCategoryHint());
    source.setCrawlIntervalMinutes(request.getCrawlIntervalMinutes());
    source.setRobotsAllowed(request.isRobotsAllowed());
    source.setEnabled(request.isEnabled());
  }

  private void updateSourceAfterJob(CrawlJob job) {
    OpportunitySource source = findSource(job.getSourceId());
    source.setLastCrawledAt(LocalDateTime.now());
    source.setFailureCount(CrawlJobStatus.FAILED.name().equals(job.getStatus()) ? defaultInt(source.getFailureCount()) + 1 : 0);
    source.setUpdatedAt(LocalDateTime.now());
    sourceRepository.save(source);
  }

  private void autoPublishIfReady(OpportunitySource source, RawOpportunity raw, FetchedRawOpportunity item) {
    if (raw.getNormalizedOpportunityId() != null || item.getDeadline() == null || item.getRawTitle() == null || item.getRawTitle().isBlank()) {
      return;
    }
    String applyUrl = firstNonBlank(item.getApplyUrl(), item.getSourceUrl());
    if (applyUrl == null || applyUrl.isBlank() || opportunityRepository.findByApplyUrl(applyUrl).isPresent()) {
      return;
    }
    String organization = firstNonBlank(item.getOrganization(), source.getName());
    if (opportunityRepository.findByTitleAndOrganizationAndDeadline(item.getRawTitle(), organization, item.getDeadline()).isPresent()) {
      return;
    }

    Opportunity opportunity = new Opportunity();
    opportunity.setTitle(item.getRawTitle());
    opportunity.setOrganization(organization);
    opportunity.setCategory(firstNonBlank(item.getCategory(), source.getCategoryHint()));
    opportunity.setDescription(firstNonBlank(item.getDescription(), item.getRawContent()));
    opportunity.setRequirements(null);
    opportunity.setBenefits(null);
    opportunity.setTarget(null);
    opportunity.setDeadline(item.getDeadline());
    opportunity.setStartDate(item.getStartDate());
    opportunity.setEndDate(item.getEndDate());
    opportunity.setLocation(item.getLocation());
    opportunity.setIsOnline(item.getOnline());
    opportunity.setApplyUrl(applyUrl);
    opportunity.setThumbnailUrl(null);
    opportunity.setTags(item.getTags());
    opportunity.setStatus("PUBLISHED");
    opportunity.setPopularityCount(0);
    opportunity.setRecommended(false);
    opportunity.setNewThisWeek(false);
    opportunity.setCreatedAt(LocalDateTime.now());
    opportunity.setUpdatedAt(LocalDateTime.now());
    Opportunity saved = opportunityRepository.save(opportunity);

    raw.setNormalizedOpportunityId(saved.getId());
    raw.setStatus(RawOpportunityStatus.PUBLISHED.name());
    raw.setErrorMessage(null);
    raw.setLastSeenAt(LocalDateTime.now());
    rawOpportunityRepository.save(raw);
  }

  private OpportunitySource findSource(Long id) {
    return sourceRepository.findById(id).orElseThrow(() -> new NotFoundException("Opportunity source not found"));
  }

  private RawOpportunity findRawOpportunity(Long id) {
    return rawOpportunityRepository.findById(id).orElseThrow(() -> new NotFoundException("Raw opportunity not found"));
  }

  private CrawlJob findCrawlJob(Long id) {
    return crawlJobRepository.findById(id).orElseThrow(() -> new NotFoundException("Crawl job not found"));
  }

  private RawOpportunity upsertRawOpportunity(
      Long sourceId,
      String externalId,
      String sourceUrl,
      String rawTitle,
      String rawContent,
      String rawPayload,
      String contentHash) {
    findSource(sourceId);
    String resolvedHash = contentHash == null || contentHash.isBlank()
        ? hash(rawTitle + "|" + sourceUrl + "|" + rawContent)
        : contentHash;
    RawOpportunity raw = rawOpportunityRepository.findBySourceIdAndContentHash(sourceId, resolvedHash).orElseGet(RawOpportunity::new);
    boolean isNew = raw.getFetchedAt() == null;
    raw.setSourceId(sourceId);
    raw.setExternalId(externalId);
    raw.setSourceUrl(sourceUrl);
    raw.setRawTitle(rawTitle);
    raw.setRawContent(rawContent);
    raw.setRawPayload(rawPayload);
    raw.setContentHash(resolvedHash);
    if (isNew) {
      raw.setFetchedAt(LocalDateTime.now());
      raw.setStatus(RawOpportunityStatus.NEW.name());
    }
    raw.setLastSeenAt(isNew ? raw.getFetchedAt() : LocalDateTime.now());
    return rawOpportunityRepository.save(raw);
  }

  private OpportunitySourceResponse toSourceResponse(OpportunitySource source) {
    return OpportunitySourceResponse.builder()
        .id(source.getId())
        .name(source.getName())
        .type(source.getType())
        .baseUrl(source.getBaseUrl())
        .categoryHint(source.getCategoryHint())
        .crawlIntervalMinutes(source.getCrawlIntervalMinutes())
        .robotsAllowed(source.isRobotsAllowed())
        .enabled(source.isEnabled())
        .lastCrawledAt(source.getLastCrawledAt())
        .failureCount(source.getFailureCount())
        .createdAt(source.getCreatedAt())
        .updatedAt(source.getUpdatedAt())
        .build();
  }

  private RawOpportunityResponse toRawResponse(RawOpportunity raw) {
    return RawOpportunityResponse.builder()
        .id(raw.getId())
        .sourceId(raw.getSourceId())
        .externalId(raw.getExternalId())
        .sourceUrl(raw.getSourceUrl())
        .rawTitle(raw.getRawTitle())
        .rawContent(raw.getRawContent())
        .rawPayload(raw.getRawPayload())
        .contentHash(raw.getContentHash())
        .fetchedAt(raw.getFetchedAt())
        .lastSeenAt(raw.getLastSeenAt())
        .normalizedOpportunityId(raw.getNormalizedOpportunityId())
        .status(raw.getStatus())
        .errorMessage(raw.getErrorMessage())
        .build();
  }

  private CrawlJobResponse toJobResponse(CrawlJob job) {
    return CrawlJobResponse.builder()
        .id(job.getId())
        .sourceId(job.getSourceId())
        .status(job.getStatus())
        .startedAt(job.getStartedAt())
        .finishedAt(job.getFinishedAt())
        .itemsFound(job.getItemsFound())
        .itemsCreated(job.getItemsCreated())
        .itemsUpdated(job.getItemsUpdated())
        .errorMessage(job.getErrorMessage())
        .build();
  }

  private int defaultInt(Integer value) {
    return value == null ? 0 : value;
  }

  private String sourceName(Long sourceId) {
    return sourceRepository.findById(sourceId).map(OpportunitySource::getName).orElse("Unknown source");
  }

  private String firstNonBlank(String preferred, String fallback) {
    return preferred == null || preferred.isBlank() ? fallback : preferred;
  }

  private String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encoded = digest.digest((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte b : encoded) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }
}
