package com.campio.domain.community;

import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final CommunityPostRepository communityPostRepository;
  private final CommentRepository commentRepository;
  private final OpportunityRepository opportunityRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<CommunityPostResponse> listPosts() {
    return communityPostRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CommunityPostResponse detail(Long id) {
    return toResponse(findPost(id));
  }

  @Transactional
  public CommunityPostResponse create(CommunityPostRequest request, HttpSession session) {
    CommunityPost post = new CommunityPost();
    post.setUserId(userService.currentUserId(session));
    post.setOpportunityId(request.getOpportunityId());
    post.setType(request.getType());
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    return toResponse(communityPostRepository.save(post));
  }

  @Transactional
  public CommunityPostResponse update(Long id, CommunityPostRequest request, HttpSession session) {
    CommunityPost post = findOwnPost(id, session);
    post.setOpportunityId(request.getOpportunityId());
    post.setType(request.getType());
    post.setTitle(request.getTitle());
    post.setContent(request.getContent());
    post.setUpdatedAt(LocalDateTime.now());
    return toResponse(communityPostRepository.save(post));
  }

  @Transactional
  public void delete(Long id, HttpSession session) {
    CommunityPost post = findOwnPost(id, session);
    commentRepository.deleteByPostId(id);
    communityPostRepository.delete(post);
  }

  @Transactional
  public CommentResponse addComment(Long postId, CommentRequest request, HttpSession session) {
    findPost(postId);
    Comment comment = new Comment();
    comment.setPostId(postId);
    comment.setUserId(userService.currentUserId(session));
    comment.setContent(request.getContent());
    comment.setCreatedAt(LocalDateTime.now());
    comment.setUpdatedAt(LocalDateTime.now());
    Comment saved = commentRepository.save(comment);
    return CommentResponse.builder()
        .id(saved.getId())
        .postId(saved.getPostId())
        .content(saved.getContent())
        .createdAt(saved.getCreatedAt().format(FORMATTER))
        .build();
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> comments(Long postId) {
    findPost(postId);
    return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
        .map(comment ->
            CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().format(FORMATTER))
                .build())
        .collect(Collectors.toList());
  }

  private CommunityPost findPost(Long id) {
    return communityPostRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
  }

  private CommunityPost findOwnPost(Long id, HttpSession session) {
    long userId = userService.currentUserId(session);
    return communityPostRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Post not found"));
  }

  private CommunityPostResponse toResponse(CommunityPost post) {
    String relatedTitle =
        post.getOpportunityId() == null
            ? null
            : opportunityRepository.findById(post.getOpportunityId()).map(opportunity -> opportunity.getTitle()).orElse(null);
    int commentCount = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).size();
    return CommunityPostResponse.builder()
        .id(post.getId())
        .opportunityId(post.getOpportunityId())
        .type(post.getType())
        .title(post.getTitle())
        .content(post.getContent())
        .relatedOpportunityTitle(relatedTitle)
        .commentCount(commentCount)
        .savedCount(commentCount * 2)
        .createdAt(post.getCreatedAt().format(FORMATTER))
        .build();
  }
}
