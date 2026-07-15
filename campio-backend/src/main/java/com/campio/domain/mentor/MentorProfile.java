package com.campio.domain.mentor;

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
@Table(name = "mentor_profiles")
public class MentorProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;
  private String company;
  private String position;

  @Column(columnDefinition = "text")
  private String experience;

  @ElementCollection
  @CollectionTable(name = "mentor_help_topics", joinColumns = @JoinColumn(name = "mentor_profile_id"))
  @Column(name = "help_topic")
  private List<String> helpTopics = new ArrayList<>();

  private boolean available;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

