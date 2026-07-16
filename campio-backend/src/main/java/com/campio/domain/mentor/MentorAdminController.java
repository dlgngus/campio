package com.campio.domain.mentor;

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/mentors")
@RequiredArgsConstructor
public class MentorAdminController {

  private final MentorService mentorService;

  @GetMapping
  public List<MentorProfileResponse> list(HttpSession session) {
    return mentorService.adminList(session);
  }

  @PatchMapping("/{id}")
  public MentorProfileResponse approve(
      @PathVariable Long id, @Valid @RequestBody MentorApprovalRequest request, HttpSession session) {
    return mentorService.approve(id, request, session);
  }
}
