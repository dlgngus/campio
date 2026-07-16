package com.campio.domain.opportunity;

import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/opportunities")
@RequiredArgsConstructor
public class OpportunityAdminController {

  private final OpportunityService opportunityService;

  @GetMapping
  public List<OpportunityResponse> list(HttpSession session) {
    return opportunityService.adminList(session);
  }
}
