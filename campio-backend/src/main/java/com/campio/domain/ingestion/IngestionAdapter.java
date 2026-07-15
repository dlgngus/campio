package com.campio.domain.ingestion;

import java.util.List;

public interface IngestionAdapter {
  OpportunitySourceType supports();

  List<FetchedRawOpportunity> fetch(OpportunitySource source);
}
