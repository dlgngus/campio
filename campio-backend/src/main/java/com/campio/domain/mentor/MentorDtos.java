package com.campio.domain.mentor;

import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MentorProfileResponse {
  private Long id;
  private String company;
  private String position;
  private String experience;
  private List<String> helpTopics;
  private boolean available;
}

@Getter
@Setter
@NoArgsConstructor
class MentorProfileRequest {
  private String company;
  private String position;
  private String experience;
  private List<String> helpTopics;
  private boolean available;
}

@Getter
@Setter
@NoArgsConstructor
class MentorApplyRequest {
  @NotBlank
  private String company;
  @NotBlank
  private String position;
  private String experience;
  private List<String> helpTopics;
}

@Getter
@Setter
@NoArgsConstructor
class MentorQuestionRequest {
  @NotBlank
  private String content;
  private Long opportunityId;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MentorQuestionResponse {
  private Long id;
  private Long mentorId;
  private Long opportunityId;
  private String content;
  private String status;
  private String createdAt;
}
