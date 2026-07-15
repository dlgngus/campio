package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpportunityRequest {
  private String title;
  private String organization;
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
  private String status;
  private Integer popularityCount;
  private boolean recommended;
  private boolean newThisWeek;
}
