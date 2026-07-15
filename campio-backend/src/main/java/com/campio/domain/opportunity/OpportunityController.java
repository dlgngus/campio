package com.campio.domain.opportunity;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

  private final OpportunityService opportunityService;

  @GetMapping
  public List<OpportunityResponse> list() {
    return opportunityService.listAll();
  }

  @GetMapping("/recommended")
  public List<OpportunityResponse> recommended() {
    return opportunityService.recommended();
  }

  @GetMapping("/closing-soon")
  public List<OpportunityResponse> closingSoon() {
    return opportunityService.closingSoon();
  }

  @GetMapping("/popular")
  public List<OpportunityResponse> popular() {
    return opportunityService.popular();
  }

  @GetMapping("/{id}")
  public OpportunityResponse detail(@PathVariable Long id) {
    return opportunityService.detail(id);
  }

  @PostMapping
  public OpportunityResponse create(@RequestBody OpportunityRequest request, HttpSession session) {
    return opportunityService.create(request, session);
  }

  @PatchMapping("/{id}")
  public OpportunityResponse update(@PathVariable Long id, @RequestBody OpportunityRequest request, HttpSession session) {
    return opportunityService.update(id, request, session);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, HttpSession session) {
    opportunityService.delete(id, session);
  }
}
