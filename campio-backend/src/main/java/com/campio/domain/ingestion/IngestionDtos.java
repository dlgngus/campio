package com.campio.domain.ingestion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
class OpportunitySourceRequest {
  @NotBlank
  private String name;
  @NotNull
  private OpportunitySourceType type;
  @NotBlank
  private String baseUrl;
  private String categoryHint;
  private Integer crawlIntervalMinutes;
  private boolean robotsAllowed;
  private boolean enabled;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class OpportunitySourceResponse {
  private Long id;
  private String name;
  private String type;
  private String baseUrl;
  private String categoryHint;
  private Integer crawlIntervalMinutes;
  private boolean robotsAllowed;
  private boolean enabled;
  private LocalDateTime lastCrawledAt;
  private Integer failureCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

@Getter
@Setter
@NoArgsConstructor
class RawOpportunityRequest {
  @NotNull
  private Long sourceId;
  private String externalId;
  @NotBlank
  private String sourceUrl;
  @NotBlank
  private String rawTitle;
  private String rawContent;
  private String rawPayload;
  private String contentHash;
}

@Getter
@Setter
@NoArgsConstructor
class RawOpportunityImportRequest {
  @NotNull
  private Long sourceId;
  @Valid
  @NotEmpty
  private List<RawOpportunityImportItem> items = new ArrayList<>();
}

@Getter
@Setter
@NoArgsConstructor
class RawOpportunityImportItem {
  private String externalId;
  @NotBlank
  private String sourceUrl;
  @NotBlank
  private String rawTitle;
  private String rawContent;
  private String rawPayload;
  private String contentHash;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class RawOpportunityImportResponse {
  private Long sourceId;
  private int requestedCount;
  private int createdCount;
  private int updatedCount;
  private List<Long> rawOpportunityIds;
}

@Getter
@Setter
@NoArgsConstructor
class RawOpportunityStatusRequest {
  @NotNull
  private RawOpportunityStatus status;
  private Long normalizedOpportunityId;
  private String errorMessage;
}

@Getter
@Setter
@NoArgsConstructor
class PublishRawOpportunityRequest {
  @NotBlank
  private String title;
  private String organization;
  @NotBlank
  private String category;
  private String description;
  private String requirements;
  private String benefits;
  private String target;
  private LocalDate deadline;
  private LocalDate startDate;
  private LocalDate endDate;
  private String location;
  private Boolean isOnline;
  private String applyUrl;
  private String thumbnailUrl;
  private List<String> tags;
  private boolean recommended;
  private boolean newThisWeek;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class RawOpportunityResponse {
  private Long id;
  private Long sourceId;
  private String externalId;
  private String sourceUrl;
  private String rawTitle;
  private String rawContent;
  private String rawPayload;
  private String contentHash;
  private LocalDateTime fetchedAt;
  private LocalDateTime lastSeenAt;
  private Long normalizedOpportunityId;
  private String status;
  private String errorMessage;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PublishedOpportunityResponse {
  private Long rawOpportunityId;
  private Long opportunityId;
  private String rawStatus;
  private boolean created;
}

@Getter
@Setter
@NoArgsConstructor
class CrawlJobRequest {
  @NotNull
  private Long sourceId;
}

@Getter
@Setter
@NoArgsConstructor
class CrawlJobStatusRequest {
  @NotNull
  private CrawlJobStatus status;
  private Integer itemsFound;
  private Integer itemsCreated;
  private Integer itemsUpdated;
  private String errorMessage;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CrawlJobResponse {
  private Long id;
  private Long sourceId;
  private String status;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private Integer itemsFound;
  private Integer itemsCreated;
  private Integer itemsUpdated;
  private String errorMessage;
}
