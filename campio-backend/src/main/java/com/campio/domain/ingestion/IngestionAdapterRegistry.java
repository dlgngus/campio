package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class IngestionAdapterRegistry {

  private final Map<OpportunitySourceType, IngestionAdapter> adapters = new EnumMap<>(OpportunitySourceType.class);

  public IngestionAdapterRegistry(List<IngestionAdapter> adapterList) {
    adapterList.forEach(adapter -> adapters.put(adapter.supports(), adapter));
  }

  public IngestionAdapter get(OpportunitySourceType sourceType) {
    IngestionAdapter adapter = adapters.get(sourceType);
    if (adapter == null) {
      throw new BadRequestException("No ingestion adapter registered for " + sourceType.name());
    }
    return adapter;
  }
}
