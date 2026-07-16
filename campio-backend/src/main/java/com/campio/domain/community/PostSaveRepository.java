package com.campio.domain.community;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostSaveRepository extends JpaRepository<PostSave, Long> {

  interface PostSaveCount {
    Long getPostId();
    long getSaveCount();
  }

  Optional<PostSave> findByPostIdAndUserId(Long postId, Long userId);

  List<PostSave> findByUserIdAndPostIdIn(Long userId, Collection<Long> postIds);

  @Query("select s.postId as postId, count(s.id) as saveCount from PostSave s where s.postId in :postIds group by s.postId")
  List<PostSaveCount> countByPostIds(@Param("postIds") Collection<Long> postIds);
}
