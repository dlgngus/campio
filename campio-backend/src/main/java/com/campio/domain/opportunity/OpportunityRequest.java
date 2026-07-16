package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpportunityRequest {
  @NotBlank
  @Size(max = 255)
  private String title;
  @Size(max = 255)
  private String organization;
  @NotBlank
  @Size(max = 50)
  private String category;
  @Size(max = 50000)
  private String description;
  @Size(max = 30000)
  private String requirements;
  @Size(max = 30000)
  private String benefits;
  @Size(max = 255)
  private String target;
  private LocalDate deadline;
  private LocalDate startDate;
  private LocalDate endDate;
  @Size(max = 255)
  private String location;
  private Boolean isOnline;
  @Size(max = 1000)
  private String applyUrl;
  @Size(max = 1000)
  private String thumbnailUrl;
  @Size(max = 30)
  private List<String> tags;
  @NotBlank
  @Size(max = 30)
  private String status;
  private Integer popularityCount;
  private boolean recommended;
  private boolean newThisWeek;
}
