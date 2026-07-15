package com.campio.domain.community;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
  List<CommunityPost> findAllByOrderByCreatedAtDesc();
  Optional<CommunityPost> findByIdAndUserId(Long id, Long userId);
}
