package com.campio.domain.ingestion;

import com.campio.domain.opportunity.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionStartupRunner {

  private final IngestionService ingestionService;
  private final OpportunityRepository opportunityRepository;

  @Value("${campio.ingestion.auto-run-on-startup:false}")
  private boolean autoRunOnStartup;

  @Value("${campio.ingestion.auto-run-only-when-empty:true}")
  private boolean autoRunOnlyWhenEmpty;

  @EventListener(ApplicationReadyEvent.class)
  public void runOnStartup() {
    if (!autoRunOnStartup) {
      return;
    }
    if (autoRunOnlyWhenEmpty && opportunityRepository.count() > 0) {
      log.info("Skipping startup ingestion because published opportunities already exist");
      return;
    }
    log.info("Running startup ingestion for enabled sources");
    ingestionService.runEnabledSources();
  }
}
