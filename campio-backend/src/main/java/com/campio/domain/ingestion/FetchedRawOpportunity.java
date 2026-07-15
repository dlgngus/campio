package com.campio.domain.ingestion;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FetchedRawOpportunity {
  private String externalId;
  private String sourceUrl;
  private String rawTitle;
  private String rawContent;
  private String rawPayload;
  private String contentHash;
  private String organization;
  private String category;
  private String description;
  private LocalDate deadline;
  private LocalDate startDate;
  private LocalDate endDate;
  private String location;
  private Boolean online;
  private String applyUrl;
  private List<String> tags;
}
