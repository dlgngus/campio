package com.campio.domain.application;

import java.time.LocalDateTime;
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
@Table(name = "application_records")
public class ApplicationRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private Long opportunityId;
  private String status;

  @javax.persistence.Column(columnDefinition = "text")
  private String memo;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

