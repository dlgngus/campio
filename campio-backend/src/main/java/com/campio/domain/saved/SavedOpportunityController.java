package com.campio.domain.saved;

import com.campio.domain.opportunity.OpportunityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SavedOpportunityController {

  private final SavedOpportunityService savedOpportunityService;

  @GetMapping("/saved")
  public List<OpportunityResponse> list(HttpSession session) {
    return savedOpportunityService.list(session);
  }

  @PostMapping("/opportunities/{id}/save")
  public SaveResponse save(@PathVariable("id") Long id, HttpSession session) {
    return savedOpportunityService.save(id, session);
  }

  @DeleteMapping("/opportunities/{id}/save")
  public SaveResponse unsave(@PathVariable("id") Long id, HttpSession session) {
    return savedOpportunityService.unsave(id, session);
  }
}
