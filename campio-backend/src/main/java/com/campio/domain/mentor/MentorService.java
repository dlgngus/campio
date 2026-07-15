package com.campio.domain.mentor;

import com.campio.global.exception.NotFoundException;
import com.campio.domain.user.UserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentorService {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final MentorProfileRepository mentorProfileRepository;
  private final MentorQuestionRepository mentorQuestionRepository;
  private final UserService userService;

  @Transactional(readOnly = true)
  public List<MentorProfileResponse> list() {
    return mentorProfileRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MentorProfileResponse detail(Long id) {
    return toResponse(findMentor(id));
  }

  @Transactional
  public MentorProfileResponse create(MentorProfileRequest request) {
    MentorProfile mentor = new MentorProfile();
    mentor.setCompany(request.getCompany());
    mentor.setPosition(request.getPosition());
    mentor.setExperience(request.getExperience());
    mentor.setHelpTopics(request.getHelpTopics());
    mentor.setAvailable(request.isAvailable());
    mentor.setCreatedAt(LocalDateTime.now());
    mentor.setUpdatedAt(LocalDateTime.now());
    return toResponse(mentorProfileRepository.save(mentor));
  }

  @Transactional
  public MentorProfileResponse apply(MentorApplyRequest request, HttpSession session) {
    MentorProfile mentor = new MentorProfile();
    mentor.setUserId(userService.currentUserId(session));
    mentor.setCompany(request.getCompany());
    mentor.setPosition(request.getPosition());
    mentor.setExperience(request.getExperience());
    mentor.setHelpTopics(request.getHelpTopics());
    mentor.setAvailable(false);
    mentor.setCreatedAt(LocalDateTime.now());
    mentor.setUpdatedAt(LocalDateTime.now());
    return toResponse(mentorProfileRepository.save(mentor));
  }

  @Transactional
  public MentorQuestionResponse askQuestion(Long mentorId, MentorQuestionRequest request, HttpSession session) {
    findMentor(mentorId);
    MentorQuestion question = new MentorQuestion();
    question.setMentorId(mentorId);
    question.setUserId(userService.currentUserId(session));
    question.setOpportunityId(request.getOpportunityId());
    question.setContent(request.getContent());
    question.setStatus("OPEN");
    question.setCreatedAt(LocalDateTime.now());
    question.setUpdatedAt(LocalDateTime.now());
    return toQuestionResponse(mentorQuestionRepository.save(question));
  }

  private MentorProfile findMentor(Long id) {
    return mentorProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("Mentor not found"));
  }

  private MentorProfileResponse toResponse(MentorProfile mentor) {
    return MentorProfileResponse.builder()
        .id(mentor.getId())
        .company(mentor.getCompany())
        .position(mentor.getPosition())
        .experience(mentor.getExperience())
        .helpTopics(mentor.getHelpTopics())
        .available(mentor.isAvailable())
        .build();
  }

  private MentorQuestionResponse toQuestionResponse(MentorQuestion question) {
    return MentorQuestionResponse.builder()
        .id(question.getId())
        .mentorId(question.getMentorId())
        .opportunityId(question.getOpportunityId())
        .content(question.getContent())
        .status(question.getStatus())
        .createdAt(question.getCreatedAt().format(FORMATTER))
        .build();
  }
}
