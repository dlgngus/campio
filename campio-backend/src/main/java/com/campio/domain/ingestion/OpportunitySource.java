package com.campio.domain.ingestion;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "opportunity_sources")
public class OpportunitySource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String type;

  @Column(length = 1000)
  private String baseUrl;

  private String categoryHint;
  private Integer crawlIntervalMinutes;
  private boolean robotsAllowed;
  private boolean enabled;
  private LocalDateTime lastCrawledAt;
  private Integer failureCount;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
