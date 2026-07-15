package com.campio.domain.opportunity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "opportunities")
public class Opportunity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  private String organization;
  private String category;

  @Column(columnDefinition = "text")
  private String description;

  @Column(columnDefinition = "text")
  private String requirements;

  @Column(columnDefinition = "text")
  private String benefits;

  private String target;
  private LocalDate deadline;
  private LocalDate startDate;
  private LocalDate endDate;
  private String location;
  private Boolean isOnline;

  @Column(length = 1000)
  private String applyUrl;

  private String thumbnailUrl;
  private String status;
  private Integer popularityCount;
  private boolean recommended;
  private boolean newThisWeek;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @ElementCollection
  @CollectionTable(name = "opportunity_tags", joinColumns = @JoinColumn(name = "opportunity_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();
}
