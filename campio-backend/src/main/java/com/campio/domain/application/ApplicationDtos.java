package com.campio.domain.application;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
class ApplicationRecordRequest {
  @NotBlank
  @Size(max = 30)
  private String status;
  @Size(max = 5000)
  private String memo;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ApplicationRecordResponse {
  private Long id;
  private Long opportunityId;
  private String status;
  private String memo;
}
