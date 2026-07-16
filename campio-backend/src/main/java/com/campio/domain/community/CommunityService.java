package com.campio.domain.community;

import com.campio.domain.opportunity.Opportunity;
import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.domain.user.User;
import com.campio.domain.user.UserRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final CommunityPostRepository communityPostRepository;
  private final CommentRepository commentRepository;
  private final PostSaveRepository postSaveRepository;
  private final OpportunityRepository opportunityRepository;
  private final UserRepository userRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<CommunityPostResponse> listPosts(HttpSession session) {
    return toResponses(communityPostRepository.findAllByOrderByCreatedAtDesc(), session);
  }

  @Transactional(readOnly = true)
  public List<CommunityPostResponse> myPosts(HttpSession session) {
    return toResponses(
        communityPostRepository.findByUserIdOrderByCreatedAtDesc(userService.currentUserId(session)), session);
  }

  @Transactional(readOnly = true)
  public CommunityPostResponse detail(Long id, HttpSession session) {
    return toResponses(List.of(findPost(id)), session).get(0);
  }

  @Transactional
  public CommunityPostResponse create(CommunityPostRequest request, HttpSession session) {
    validateOpportunity(request.getOpportunityId());
    CommunityPost post = new CommunityPost();
    post.setUserId(userService.currentUserId(session));
    applyRequest(post, request);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    return toResponses(List.of(communityPostRepository.save(post)), session).get(0);
  }

  @Transactional
  public CommunityPostResponse update(Long id, CommunityPostRequest request, HttpSession session) {
    validateOpportunity(request.getOpportunityId());
    CommunityPost post = findOwnPost(id, session);
    applyRequest(post, request);
    post.setUpdatedAt(LocalDateTime.now());
    return toResponses(List.of(communityPostRepository.save(post)), session).get(0);
  }

  @Transactional
  public void delete(Long id, HttpSession session) {
    communityPostRepository.delete(findOwnPost(id, session));
  }

  @Transactional
  public CommentResponse addComment(Long postId, CommentRequest request, HttpSession session) {
    findPost(postId);
    Comment comment = new Comment();
    comment.setPostId(postId);
    comment.setUserId(userService.currentUserId(session));
    comment.setContent(request.getContent().trim());
    comment.setCreatedAt(LocalDateTime.now());
    comment.setUpdatedAt(LocalDateTime.now());
    return toCommentResponse(commentRepository.save(comment), session);
  }

  @Transactional(readOnly = true)
  public List<CommentResponse> comments(Long postId, HttpSession session) {
    findPost(postId);
    List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    Map<Long, User> users = usersById(comments.stream().map(Comment::getUserId).collect(Collectors.toSet()));
    Long currentUserId = userService.optionalCurrentUserId(session);
    return comments.stream()
        .map(comment -> toCommentResponse(comment, users.get(comment.getUserId()), currentUserId))
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteComment(Long postId, Long commentId, HttpSession session) {
    long userId = userService.currentUserId(session);
    Comment comment = commentRepository.findByIdAndPostIdAndUserId(commentId, postId, userId)
        .orElseThrow(() -> new NotFoundException("Comment not found"));
    commentRepository.delete(comment);
  }

  @Transactional
  public void save(Long postId, HttpSession session) {
    findPost(postId);
    long userId = userService.currentUserId(session);
    if (postSaveRepository.findByPostIdAndUserId(postId, userId).isPresent()) return;
    PostSave save = new PostSave();
    save.setPostId(postId);
    save.setUserId(userId);
    save.setCreatedAt(LocalDateTime.now());
    postSaveRepository.save(save);
  }

  @Transactional
  public void unsave(Long postId, HttpSession session) {
    long userId = userService.currentUserId(session);
    postSaveRepository.findByPostIdAndUserId(postId, userId).ifPresent(postSaveRepository::delete);
  }

  private void applyRequest(CommunityPost post, CommunityPostRequest request) {
    post.setOpportunityId(request.getOpportunityId());
    String type = request.getType() == null ? "QUESTION" : request.getType().trim().toUpperCase();
    if (!Set.of("QUESTION", "REVIEW", "TIP", "INFO").contains(type)) {
      throw new BadRequestException("Unsupported post type");
    }
    post.setType(type);
    post.setTitle(request.getTitle().trim());
    post.setContent(request.getContent().trim());
  }

  private void validateOpportunity(Long opportunityId) {
    if (opportunityId != null && !opportunityRepository.existsById(opportunityId)) {
      throw new BadRequestException("Related opportunity not found");
    }
  }

  private CommunityPost findPost(Long id) {
    return communityPostRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
  }

  private CommunityPost findOwnPost(Long id, HttpSession session) {
    long userId = userService.currentUserId(session);
    return communityPostRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
  }

  private List<CommunityPostResponse> toResponses(List<CommunityPost> posts, HttpSession session) {
    if (posts.isEmpty()) return Collections.emptyList();
    Set<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toSet());
    Map<Long, User> users = usersById(posts.stream().map(CommunityPost::getUserId).collect(Collectors.toSet()));
    Map<Long, String> opportunityTitles = opportunityRepository
        .findAllById(posts.stream().map(CommunityPost::getOpportunityId).filter(java.util.Objects::nonNull).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(Opportunity::getId, Opportunity::getTitle));
    Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds).stream()
        .collect(Collectors.toMap(CommentRepository.PostCommentCount::getPostId, CommentRepository.PostCommentCount::getCommentCount));
    Map<Long, Long> saveCounts = postSaveRepository.countByPostIds(postIds).stream()
        .collect(Collectors.toMap(PostSaveRepository.PostSaveCount::getPostId, PostSaveRepository.PostSaveCount::getSaveCount));
    Long currentUserId = userService.optionalCurrentUserId(session);
    Set<Long> savedPostIds = currentUserId == null ? Collections.emptySet() : postSaveRepository
        .findByUserIdAndPostIdIn(currentUserId, postIds).stream().map(PostSave::getPostId).collect(Collectors.toSet());

    return posts.stream().map(post -> {
      User author = users.get(post.getUserId());
      return CommunityPostResponse.builder()
          .id(post.getId())
          .opportunityId(post.getOpportunityId())
          .type(post.getType())
          .title(post.getTitle())
          .content(post.getContent())
          .relatedOpportunityTitle(opportunityTitles.get(post.getOpportunityId()))
          .authorName(author == null ? "Campio User" : author.getName())
          .authorAvatarUrl(author == null ? null : author.getAvatarUrl())
          .own(currentUserId != null && currentUserId.equals(post.getUserId()))
          .saved(savedPostIds.contains(post.getId()))
          .commentCount(commentCounts.getOrDefault(post.getId(), 0L).intValue())
          .savedCount(saveCounts.getOrDefault(post.getId(), 0L).intValue())
          .createdAt(post.getCreatedAt().format(FORMATTER))
          .build();
    }).collect(Collectors.toList());
  }

  private CommentResponse toCommentResponse(Comment comment, HttpSession session) {
    Long currentUserId = userService.optionalCurrentUserId(session);
    User author = userRepository.findById(comment.getUserId()).orElse(null);
    return toCommentResponse(comment, author, currentUserId);
  }

  private CommentResponse toCommentResponse(Comment comment, User author, Long currentUserId) {
    return CommentResponse.builder()
        .id(comment.getId())
        .postId(comment.getPostId())
        .authorName(author == null ? "Campio User" : author.getName())
        .authorAvatarUrl(author == null ? null : author.getAvatarUrl())
        .own(currentUserId != null && currentUserId.equals(comment.getUserId()))
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt().format(FORMATTER))
        .build();
  }

  private Map<Long, User> usersById(Collection<Long> ids) {
    if (ids.isEmpty()) return Collections.emptyMap();
    return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
  }
}
