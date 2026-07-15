package com.campio.domain.mentor;

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
@Table(name = "mentor_questions")
public class MentorQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long mentorId;
  private Long userId;
  private Long opportunityId;

  @Column(columnDefinition = "text")
  private String content;

  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
