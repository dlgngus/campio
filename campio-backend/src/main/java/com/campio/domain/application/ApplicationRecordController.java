package com.campio.domain.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationRecordController {

  private final ApplicationRecordService applicationRecordService;

  @GetMapping("/applications")
  public List<ApplicationRecordResponse> list(HttpSession session) {
    return applicationRecordService.list(session);
  }

  @PostMapping("/opportunities/{id}/apply-record")
  public ApplicationRecordResponse create(
      @PathVariable("id") Long opportunityId,
      @RequestBody ApplicationRecordRequest request,
      HttpSession session) {
    return applicationRecordService.create(opportunityId, request, session);
  }

  @PatchMapping("/applications/{id}")
  public ApplicationRecordResponse update(@PathVariable Long id, @RequestBody ApplicationRecordRequest request, HttpSession session) {
    return applicationRecordService.update(id, request, session);
  }
}
