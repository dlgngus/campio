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
@Table(name = "raw_opportunities")
public class RawOpportunity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sourceId;
  private String externalId;

  @Column(length = 1000)
  private String sourceUrl;

  private String rawTitle;

  @Column(columnDefinition = "text")
  private String rawContent;

  @Column(columnDefinition = "text")
  private String rawPayload;

  private String contentHash;
  private LocalDateTime fetchedAt;
  private LocalDateTime lastSeenAt;
  private Long normalizedOpportunityId;
  private String status;

  @Column(columnDefinition = "text")
  private String errorMessage;
}
