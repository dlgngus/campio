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
@Table(name = "crawl_jobs")
public class CrawlJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sourceId;
  private String status;
  private LocalDateTime startedAt;
  private LocalDateTime finishedAt;
  private Integer itemsFound;
  private Integer itemsCreated;
  private Integer itemsUpdated;

  @Column(columnDefinition = "text")
  private String errorMessage;
}
