package com.campio.domain.opportunity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpportunityPageResponse {
  private final List<OpportunityResponse> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;
  private final boolean first;
  private final boolean last;
}
