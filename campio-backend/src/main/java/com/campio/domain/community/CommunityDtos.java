package com.campio.domain.community;

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
class CommunityPostRequest {
  private Long opportunityId;
  private String type;
  @NotBlank
  @Size(max = 200)
  private String title;
  @NotBlank
  @Size(max = 10000)
  private String content;
}

@Getter
@Setter
@NoArgsConstructor
class CommentRequest {
  @NotBlank
  @Size(max = 3000)
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
  private String authorName;
  private String authorAvatarUrl;
  private boolean own;
  private boolean saved;
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
  private String authorName;
  private String authorAvatarUrl;
  private boolean own;
  private String content;
  private String createdAt;
}
