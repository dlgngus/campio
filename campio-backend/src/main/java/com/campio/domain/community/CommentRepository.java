package com.campio.domain.community;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
  Optional<Comment> findByIdAndPostIdAndUserId(Long id, Long postId, Long userId);

  interface PostCommentCount {
    Long getPostId();
    long getCommentCount();
  }

  @org.springframework.data.jpa.repository.Query(
      "select c.postId as postId, count(c.id) as commentCount from Comment c where c.postId in :postIds group by c.postId")
  List<PostCommentCount> countByPostIds(
      @org.springframework.data.repository.query.Param("postIds") java.util.Collection<Long> postIds);

  void deleteByPostId(Long postId);
}
