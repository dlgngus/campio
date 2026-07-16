package com.campio.domain.opportunity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpportunityHomeResponse {
  private final List<OpportunityResponse> recommended;
  private final List<OpportunityResponse> closing;
  private final List<OpportunityResponse> popular;
  private final List<OpportunityResponse> latest;
}
