package com.campio.domain.application;

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
public class ApplicationRecordService {

  private final ApplicationRecordRepository applicationRecordRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<ApplicationRecordResponse> list(HttpSession session) {
    long userId = userService.currentUserId(session);
    return applicationRecordRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public ApplicationRecordResponse create(Long opportunityId, ApplicationRecordRequest request, HttpSession session) {
    ApplicationRecord record = new ApplicationRecord();
    record.setUserId(userService.currentUserId(session));
    record.setOpportunityId(opportunityId);
    record.setStatus(request.getStatus());
    record.setMemo(request.getMemo());
    record.setCreatedAt(LocalDateTime.now());
    record.setUpdatedAt(LocalDateTime.now());
    return toResponse(applicationRecordRepository.save(record));
  }

  @Transactional
  public ApplicationRecordResponse update(Long id, ApplicationRecordRequest request) {
    ApplicationRecord record =
        applicationRecordRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Application record not found"));
    record.setStatus(request.getStatus());
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
}
