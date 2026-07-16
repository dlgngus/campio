package com.campio.domain.application;

import com.campio.domain.user.UserService;
import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationRecordService {

  private static final Set<String> ALLOWED_STATUSES =
      Set.of("INTERESTED", "PREPARING", "APPLIED", "ACCEPTED", "REJECTED", "ARCHIVED");

  private final ApplicationRecordRepository applicationRecordRepository;
  private final UserService userService;
  private final OpportunityRepository opportunityRepository;

  @Transactional(readOnly = true)
  public List<ApplicationRecordResponse> list(HttpSession session) {
    long userId = userService.currentUserId(session);
    return applicationRecordRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public ApplicationRecordResponse create(Long opportunityId, ApplicationRecordRequest request, HttpSession session) {
    long userId = userService.currentUserId(session);
    if (!opportunityRepository.existsById(opportunityId)) {
      throw new NotFoundException("Opportunity not found");
    }
    String status = normalizeStatus(request.getStatus());
    ApplicationRecord record = applicationRecordRepository
        .findByUserIdAndOpportunityId(userId, opportunityId)
        .orElseGet(ApplicationRecord::new);
    if (record.getId() == null) {
      record.setUserId(userId);
      record.setOpportunityId(opportunityId);
      record.setCreatedAt(LocalDateTime.now());
    }
    record.setStatus(status);
    record.setMemo(request.getMemo());
    record.setUpdatedAt(LocalDateTime.now());
    return toResponse(applicationRecordRepository.save(record));
  }

  @Transactional
  public ApplicationRecordResponse update(Long id, ApplicationRecordRequest request, HttpSession session) {
    long userId = userService.currentUserId(session);
    ApplicationRecord record =
        applicationRecordRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new NotFoundException("Application record not found"));
    record.setStatus(normalizeStatus(request.getStatus()));
    record.setMemo(request.getMemo());
    record.setUpdatedAt(LocalDateTime.now());
    return toResponse(applicationRecordRepository.save(record));
  }

  private ApplicationRecordResponse toResponse(ApplicationRecord record) {
    return ApplicationRecordResponse.builder()
        .id(record.getId())
        .opportunityId(record.getOpportunityId())
        .status(record.getStatus())
        .memo(record.getMemo())
        .build();
  }

  private String normalizeStatus(String status) {
    String normalized = status == null ? "" : status.trim().toUpperCase();
    if (!ALLOWED_STATUSES.contains(normalized)) {
      throw new BadRequestException("Unsupported application status");
    }
    return normalized;
  }
}
