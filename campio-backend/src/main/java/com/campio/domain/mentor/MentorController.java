package com.campio.domain.mentor;

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

  private final MentorService mentorService;

  @GetMapping
  public List<MentorProfileResponse> list(HttpSession session) {
    return mentorService.list(session);
  }

  @GetMapping("/{id}")
  public MentorProfileResponse detail(@PathVariable Long id, HttpSession session) {
    return mentorService.detail(id, session);
  }

  @PostMapping("/apply")
  public MentorProfileResponse apply(@Valid @RequestBody MentorApplyRequest request, HttpSession session) {
    return mentorService.apply(request, session);
  }

  @PostMapping("/{id}/questions")
  public MentorQuestionResponse askQuestion(
      @PathVariable Long id, @Valid @RequestBody MentorQuestionRequest request, HttpSession session) {
    return mentorService.askQuestion(id, request, session);
  }

  @GetMapping("/questions/mine")
  public List<MentorQuestionResponse> myQuestions(HttpSession session) {
    return mentorService.myQuestions(session);
  }

  @GetMapping("/questions/received")
  public List<MentorQuestionResponse> receivedQuestions(HttpSession session) {
    return mentorService.receivedQuestions(session);
  }

  @PatchMapping("/questions/{questionId}/answer")
  public MentorQuestionResponse answer(
      @PathVariable Long questionId, @Valid @RequestBody MentorAnswerRequest request, HttpSession session) {
    return mentorService.answer(questionId, request, session);
  }
}
