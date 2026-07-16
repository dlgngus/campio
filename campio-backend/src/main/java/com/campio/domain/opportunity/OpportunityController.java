package com.campio.domain.opportunity;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

  private final OpportunityService opportunityService;

  @GetMapping
  public List<OpportunityResponse> list(HttpSession session) {
    return opportunityService.listAll(session);
  }

  @GetMapping("/search")
  public OpportunityPageResponse search(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String target,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String region,
      @RequestParam(defaultValue = "false") boolean deadlineSoon,
      @RequestParam(defaultValue = "false") boolean online,
      @RequestParam(defaultValue = "false") boolean saved,
      @RequestParam(defaultValue = "deadline") String sort,
      HttpSession session) {
    return opportunityService.search(
        page, size, q, target, category, region, deadlineSoon, online, saved, sort, session);
  }

  @GetMapping("/batch")
  public List<OpportunityResponse> batch(
      @RequestParam List<Long> ids, HttpSession session) {
    return opportunityService.findPublishedByIds(ids, session);
  }

  @GetMapping("/recommended")
  public List<OpportunityResponse> recommended(HttpSession session) {
    return opportunityService.recommended(session);
  }

  @GetMapping("/closing-soon")
  public List<OpportunityResponse> closingSoon(HttpSession session) {
    return opportunityService.closingSoon(session);
  }

  @GetMapping("/popular")
  public List<OpportunityResponse> popular(HttpSession session) {
    return opportunityService.popular(session);
  }

  @GetMapping("/{id}")
  public OpportunityResponse detail(@PathVariable Long id, HttpSession session) {
    return opportunityService.detail(id, session);
  }

  @PostMapping
  public OpportunityResponse create(@Valid @RequestBody OpportunityRequest request, HttpSession session) {
    return opportunityService.create(request, session);
  }

  @PatchMapping("/{id}")
  public OpportunityResponse update(
      @PathVariable Long id, @Valid @RequestBody OpportunityRequest request, HttpSession session) {
    return opportunityService.update(id, request, session);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, HttpSession session) {
    opportunityService.delete(id, session);
  }
}
