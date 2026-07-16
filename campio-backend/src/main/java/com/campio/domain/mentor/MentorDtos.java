package com.campio.domain.mentor;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
  private String name;
  private String school;
  private String major;
  private String company;
  private String position;
  private String experience;
  private String avatarUrl;
  private List<String> helpTopics;
  private boolean available;
  private boolean own;
  private Integer responseRate;
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
  @Size(max = 150)
  private String company;
  @NotBlank
  @Size(max = 150)
  private String position;
  @Size(max = 5000)
  private String experience;
  @Size(max = 12)
  private List<String> helpTopics;
}

@Getter
@Setter
@NoArgsConstructor
class MentorQuestionRequest {
  @NotBlank
  @Size(max = 3000)
  private String content;
  private Long opportunityId;
}

@Getter
@Setter
@NoArgsConstructor
class MentorApprovalRequest {
  private boolean available;
}

@Getter
@Setter
@NoArgsConstructor
class MentorAnswerRequest {
  @NotBlank
  @Size(max = 3000)
  private String answer;
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
  private String mentorName;
  private String questionerName;
  private String content;
  private String answer;
  private String status;
  private String createdAt;
}
