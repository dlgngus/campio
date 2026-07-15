package com.campio.domain.ingestion;

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ingestion")
@RequiredArgsConstructor
public class IngestionAdminController {

  private final IngestionService ingestionService;

  @GetMapping("/sources")
  public List<OpportunitySourceResponse> sources(HttpSession session) {
    return ingestionService.listSources(session);
  }

  @PostMapping("/sources")
  public OpportunitySourceResponse createSource(@Valid @RequestBody OpportunitySourceRequest request, HttpSession session) {
    return ingestionService.createSource(request, session);
  }

  @PatchMapping("/sources/{id}")
  public OpportunitySourceResponse updateSource(
      @PathVariable Long id, @Valid @RequestBody OpportunitySourceRequest request, HttpSession session) {
    return ingestionService.updateSource(id, request, session);
  }

  @DeleteMapping("/sources/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSource(@PathVariable Long id, HttpSession session) {
    ingestionService.deleteSource(id, session);
  }

  @GetMapping("/raw-opportunities")
  public List<RawOpportunityResponse> rawOpportunities(HttpSession session) {
    return ingestionService.listRawOpportunities(session);
  }

  @PostMapping("/raw-opportunities")
  public RawOpportunityResponse createRawOpportunity(
      @Valid @RequestBody RawOpportunityRequest request, HttpSession session) {
    return ingestionService.createRawOpportunity(request, session);
  }

  @PostMapping("/raw-opportunities/import")
  public RawOpportunityImportResponse importRawOpportunities(
      @Valid @RequestBody RawOpportunityImportRequest request, HttpSession session) {
    return ingestionService.importRawOpportunities(request, session);
  }

  @PatchMapping("/raw-opportunities/{id}/status")
  public RawOpportunityResponse updateRawStatus(
      @PathVariable Long id, @Valid @RequestBody RawOpportunityStatusRequest request, HttpSession session) {
    return ingestionService.updateRawStatus(id, request, session);
  }

  @PostMapping("/raw-opportunities/{id}/publish")
  public PublishedOpportunityResponse publishRawOpportunity(
      @PathVariable Long id, @Valid @RequestBody PublishRawOpportunityRequest request, HttpSession session) {
    return ingestionService.publishRawOpportunity(id, request, session);
  }

  @GetMapping("/crawl-jobs")
  public List<CrawlJobResponse> crawlJobs(HttpSession session) {
    return ingestionService.listCrawlJobs(session);
  }

  @PostMapping("/crawl-jobs")
  public CrawlJobResponse createCrawlJob(@Valid @RequestBody CrawlJobRequest request, HttpSession session) {
    return ingestionService.createCrawlJob(request, session);
  }

  @PatchMapping("/crawl-jobs/{id}")
  public CrawlJobResponse updateCrawlJob(
      @PathVariable Long id, @Valid @RequestBody CrawlJobStatusRequest request, HttpSession session) {
    return ingestionService.updateCrawlJob(id, request, session);
  }

  @PostMapping("/crawl-jobs/{id}/run")
  public CrawlJobResponse runCrawlJob(@PathVariable Long id, HttpSession session) {
    return ingestionService.runCrawlJob(id, session);
  }
}
