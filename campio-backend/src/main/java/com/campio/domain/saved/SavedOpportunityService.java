package com.campio.domain.saved;

import com.campio.domain.opportunity.OpportunityResponse;
import com.campio.domain.opportunity.OpportunityService;
import com.campio.domain.user.UserService;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedOpportunityService {

  private final SavedOpportunityRepository savedOpportunityRepository;
  private final UserService userService;
  private final OpportunityService opportunityService;

  @Transactional(readOnly = true)
  public List<OpportunityResponse> list(HttpSession session) {
    long userId = userService.currentUserId(session);
    return savedOpportunityRepository.findByUserId(userId).stream()
        .map(saved -> opportunityService.detail(saved.getOpportunityId()))
        .collect(Collectors.toList());
  }

  @Transactional
  public SaveResponse save(Long opportunityId, HttpSession session) {
    long userId = userService.currentUserId(session);
    savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId)
        .orElseGet(() -> {
          SavedOpportunity saved = new SavedOpportunity();
          saved.setUserId(userId);
          saved.setOpportunityId(opportunityId);
          saved.setCreatedAt(LocalDateTime.now());
          return savedOpportunityRepository.save(saved);
        });
    return new SaveResponse(true);
  }

  @Transactional
  public SaveResponse unsave(Long opportunityId, HttpSession session) {
    long userId = userService.currentUserId(session);
    SavedOpportunity saved = savedOpportunityRepository.findByUserIdAndOpportunityId(userId, opportunityId)
        .orElseThrow(() -> new NotFoundException("Saved opportunity not found"));
    savedOpportunityRepository.delete(saved);
    return new SaveResponse(false);
  }
}
