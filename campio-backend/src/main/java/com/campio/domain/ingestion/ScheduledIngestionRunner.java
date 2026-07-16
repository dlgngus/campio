package com.campio.domain.ingestion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledIngestionRunner {

  private final IngestionService ingestionService;

  @Value("${campio.ingestion.scheduler-enabled:false}")
  private boolean schedulerEnabled;

  @Scheduled(
      fixedDelayString = "${campio.ingestion.scheduler-poll-ms:60000}",
      initialDelayString = "${campio.ingestion.scheduler-initial-delay-ms:60000}")
  public void runDueSources() {
    if (!schedulerEnabled) return;
    try {
      ingestionService.runDueSources();
    } catch (RuntimeException ex) {
      log.error("Scheduled ingestion poll failed", ex);
    }
  }
}
