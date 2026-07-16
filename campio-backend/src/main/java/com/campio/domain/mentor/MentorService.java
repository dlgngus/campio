package com.campio.domain.mentor;

import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.domain.user.User;
import com.campio.domain.user.UserRepository;
import com.campio.domain.user.UserService;
import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.NotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
  private final OpportunityRepository opportunityRepository;
  private final UserService userService;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<MentorProfileResponse> list(HttpSession session) {
    userService.requireVerifiedStudent(session);
    return toResponses(mentorProfileRepository.findByAvailableTrueOrderByIdAsc(), session);
  }

  @Transactional(readOnly = true)
  public List<MentorProfileResponse> adminList(HttpSession session) {
    userService.requireAdmin(session);
    return toResponses(mentorProfileRepository.findAll(), session);
  }

  @Transactional(readOnly = true)
  public MentorProfileResponse detail(Long id, HttpSession session) {
    userService.requireVerifiedStudent(session);
    MentorProfile mentor = findMentor(id);
    Long currentUserId = userService.optionalCurrentUserId(session);
    if (!mentor.isAvailable()
        && !userService.isAdmin(session)
        && (currentUserId == null || !currentUserId.equals(mentor.getUserId()))) {
      throw new NotFoundException("Mentor not found");
    }
    return toResponses(List.of(mentor), session).get(0);
  }

  @Transactional
  public MentorProfileResponse apply(MentorApplyRequest request, HttpSession session) {
    User user = userService.requireVerifiedStudent(session);
    MentorProfile mentor = mentorProfileRepository.findByUserId(user.getId()).orElseGet(MentorProfile::new);
    if (mentor.getId() == null) {
      mentor.setUserId(user.getId());
      mentor.setCreatedAt(LocalDateTime.now());
    }
    mentor.setCompany(request.getCompany().trim());
    mentor.setPosition(request.getPosition().trim());
    mentor.setExperience(request.getExperience() == null ? null : request.getExperience().trim());
    mentor.setHelpTopics(cleanTopics(request.getHelpTopics()));
    mentor.setAvailable(false);
    mentor.setUpdatedAt(LocalDateTime.now());
    return toResponses(List.of(mentorProfileRepository.save(mentor)), session).get(0);
  }

  @Transactional
  public MentorProfileResponse approve(Long mentorId, MentorApprovalRequest request, HttpSession session) {
    userService.requireAdmin(session);
    MentorProfile mentor = findMentor(mentorId);
    mentor.setAvailable(request.isAvailable());
    mentor.setUpdatedAt(LocalDateTime.now());
    return toResponses(List.of(mentorProfileRepository.save(mentor)), session).get(0);
  }

  @Transactional
  public MentorQuestionResponse askQuestion(Long mentorId, MentorQuestionRequest request, HttpSession session) {
    userService.requireVerifiedStudent(session);
    MentorProfile mentor = findMentor(mentorId);
    long userId = userService.currentUserId(session);
    if (!mentor.isAvailable() && !userService.isAdmin(session) && !Long.valueOf(userId).equals(mentor.getUserId())) {
      throw new NotFoundException("Mentor not found");
    }
    if (request.getOpportunityId() != null && !opportunityRepository.existsById(request.getOpportunityId())) {
      throw new BadRequestException("Related opportunity not found");
    }
    MentorQuestion question = new MentorQuestion();
    question.setMentorId(mentorId);
    question.setUserId(userId);
    question.setOpportunityId(request.getOpportunityId());
    question.setContent(request.getContent().trim());
    question.setStatus("OPEN");
    question.setCreatedAt(LocalDateTime.now());
    question.setUpdatedAt(LocalDateTime.now());
    return toQuestionResponses(List.of(mentorQuestionRepository.save(question))).get(0);
  }

  @Transactional(readOnly = true)
  public List<MentorQuestionResponse> myQuestions(HttpSession session) {
    userService.requireVerifiedStudent(session);
    return toQuestionResponses(mentorQuestionRepository.findByUserIdOrderByCreatedAtDesc(userService.currentUserId(session)));
  }

  @Transactional(readOnly = true)
  public List<MentorQuestionResponse> receivedQuestions(HttpSession session) {
    userService.requireVerifiedStudent(session);
    MentorProfile mentor = mentorProfileRepository.findByUserId(userService.currentUserId(session))
        .orElseThrow(() -> new NotFoundException("Mentor profile not found"));
    return toQuestionResponses(mentorQuestionRepository.findByMentorIdOrderByCreatedAtDesc(mentor.getId()));
  }

  @Transactional
  public MentorQuestionResponse answer(Long questionId, MentorAnswerRequest request, HttpSession session) {
    userService.requireVerifiedStudent(session);
    MentorProfile mentor = mentorProfileRepository.findByUserId(userService.currentUserId(session))
        .orElseThrow(() -> new NotFoundException("Mentor profile not found"));
    MentorQuestion question = mentorQuestionRepository.findById(questionId)
        .filter(item -> mentor.getId().equals(item.getMentorId()))
        .orElseThrow(() -> new NotFoundException("Question not found"));
    question.setAnswer(request.getAnswer().trim());
    question.setStatus("ANSWERED");
    question.setUpdatedAt(LocalDateTime.now());
    return toQuestionResponses(List.of(mentorQuestionRepository.save(question))).get(0);
  }

  private MentorProfile findMentor(Long id) {
    return mentorProfileRepository.findById(id).orElseThrow(() -> new NotFoundException("Mentor not found"));
  }

  private List<String> cleanTopics(List<String> topics) {
    if (topics == null) return Collections.emptyList();
    return topics.stream().filter(java.util.Objects::nonNull).map(String::trim).filter(value -> !value.isBlank())
        .distinct().limit(12).collect(Collectors.toList());
  }

  private List<MentorProfileResponse> toResponses(List<MentorProfile> mentors, HttpSession session) {
    if (mentors.isEmpty()) return Collections.emptyList();
    Map<Long, User> users = usersById(mentors.stream().map(MentorProfile::getUserId).collect(Collectors.toSet()));
    Set<Long> mentorIds = mentors.stream().map(MentorProfile::getId).collect(Collectors.toSet());
    Map<Long, MentorQuestionRepository.MentorQuestionStats> stats = mentorQuestionRepository.statsByMentorIds(mentorIds)
        .stream().collect(Collectors.toMap(MentorQuestionRepository.MentorQuestionStats::getMentorId, Function.identity()));
    Long currentUserId = userService.optionalCurrentUserId(session);
    return mentors.stream().map(mentor -> {
      User user = users.get(mentor.getUserId());
      MentorQuestionRepository.MentorQuestionStats mentorStats = stats.get(mentor.getId());
      int responseRate = mentorStats == null || mentorStats.getTotalCount() == 0 ? 0
          : (int) Math.round(mentorStats.getAnsweredCount() * 100.0 / mentorStats.getTotalCount());
      return MentorProfileResponse.builder()
          .id(mentor.getId())
          .name(user == null ? "Campio Mentor" : user.getName())
          .school(user == null ? null : user.getSchool())
          .major(user == null ? null : user.getMajor())
          .avatarUrl(user == null ? null : user.getAvatarUrl())
          .company(mentor.getCompany())
          .position(mentor.getPosition())
          .experience(mentor.getExperience())
          .helpTopics(mentor.getHelpTopics())
          .available(mentor.isAvailable())
          .own(currentUserId != null && currentUserId.equals(mentor.getUserId()))
          .responseRate(responseRate)
          .build();
    }).collect(Collectors.toList());
  }

  private List<MentorQuestionResponse> toQuestionResponses(List<MentorQuestion> questions) {
    if (questions.isEmpty()) return Collections.emptyList();
    Map<Long, MentorProfile> mentors = mentorProfileRepository.findAllById(
        questions.stream().map(MentorQuestion::getMentorId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(MentorProfile::getId, Function.identity()));
    Set<Long> userIds = questions.stream().map(MentorQuestion::getUserId).collect(Collectors.toSet());
    userIds.addAll(mentors.values().stream().map(MentorProfile::getUserId).collect(Collectors.toSet()));
    Map<Long, User> users = usersById(userIds);
    return questions.stream().map(question -> {
      MentorProfile mentor = mentors.get(question.getMentorId());
      User mentorUser = mentor == null ? null : users.get(mentor.getUserId());
      User questioner = users.get(question.getUserId());
      return MentorQuestionResponse.builder()
          .id(question.getId())
          .mentorId(question.getMentorId())
          .opportunityId(question.getOpportunityId())
          .mentorName(mentorUser == null ? "Campio Mentor" : mentorUser.getName())
          .questionerName(questioner == null ? "Campio User" : questioner.getName())
          .content(question.getContent())
          .answer(question.getAnswer())
          .status(question.getStatus())
          .createdAt(question.getCreatedAt().format(FORMATTER))
          .build();
    }).collect(Collectors.toList());
  }

  private Map<Long, User> usersById(Collection<Long> ids) {
    if (ids.isEmpty()) return Collections.emptyMap();
    return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
  }
}
