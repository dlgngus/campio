package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponse {
  private Long id;
  private String title;
  private String organization;
  private String category;
  private String description;
  private String requirements;
  private String benefits;
  private String applicationMethod;
  private String target;
  private LocalDate deadline;
  private LocalDate startDate;
  private LocalDate endDate;
  private String location;
  private Boolean online;
  private String applyUrl;
  private String thumbnailUrl;
  private String status;
  private List<String> tags;
  private boolean saved;
  private Integer popularityCount;
  private boolean recommended;
  private boolean newThisWeek;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
