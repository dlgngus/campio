package com.campio.domain.community;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
class CommunityPostRequest {
  private Long opportunityId;
  private String type;
  @NotBlank
  private String title;
  @NotBlank
  private String content;
}

@Getter
@Setter
@NoArgsConstructor
class CommentRequest {
  @NotBlank
  private String content;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CommunityPostResponse {
  private Long id;
  private Long opportunityId;
  private String type;
  private String title;
  private String content;
  private String relatedOpportunityTitle;
  private int commentCount;
  private int savedCount;
  private String createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CommentResponse {
  private Long id;
  private Long postId;
  private String content;
  private String createdAt;
}

