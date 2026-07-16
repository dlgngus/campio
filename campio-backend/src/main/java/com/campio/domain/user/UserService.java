package com.campio.domain.user;

import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.ForbiddenException;
import com.campio.global.exception.NotFoundException;
import com.campio.global.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Arrays;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.ObjectProvider;

@Service
@RequiredArgsConstructor
public class UserService {

  public static final String SESSION_USER_ID = "campio.userId";
  private static final String VERIFICATION_EMAIL = "campio.verification.email";
  private static final String VERIFICATION_CODE = "campio.verification.code";
  private static final String VERIFICATION_EXPIRES_AT = "campio.verification.expiresAt";
  private static final String VERIFICATION_ATTEMPTS = "campio.verification.attempts";
  private static final int VERIFICATION_TTL_SECONDS = 600;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectProvider<SchoolVerificationMailer> schoolVerificationMailer;

  @Value("${campio.auth.allow-mock-user:false}")
  private boolean allowMockUser;

  @Value("${campio.school-verification.expose-code:true}")
  private boolean exposeVerificationCode;

  @Transactional(readOnly = true)
  public UserResponse me(HttpSession session) {
    return toResponse(findCurrentUser(session));
  }

  @Transactional
  public UserResponse updateProfile(HttpSession session, UpdateProfileRequest request) {
    User user = findCurrentUser(session);
    if (request.getName() != null) {
      user.setName(request.getName());
    }
    if (request.getSchool() != null) {
      user.setSchool(request.getSchool());
    }
    if (request.getMajor() != null) {
      user.setMajor(request.getMajor());
    }
    if (request.getGrade() != null) {
      user.setGrade(request.getGrade());
    }
    if (request.getAvatarUrl() != null) {
      user.setAvatarUrl(request.getAvatarUrl());
    }
    user.setUpdatedAt(LocalDateTime.now());
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public UserResponse updateInterests(HttpSession session, UpdateInterestsRequest request) {
    User user = findCurrentUser(session);
    user.setInterests(request.getInterests());
    user.setUpdatedAt(LocalDateTime.now());
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public SchoolVerificationChallengeResponse requestSchoolVerification(
      HttpSession session, SchoolVerificationRequest request) {
    User user = findCurrentUser(session);
    String email = normalizeEmail(request.getSchoolEmail());
    if (!email.equals(normalizeEmail(user.getEmail()))) {
      throw new BadRequestException("School email must match the account email");
    }
    if (!isAcademicEmail(email)) throw new BadRequestException("A valid academic email is required");
    String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    SchoolVerificationMailer mailer = schoolVerificationMailer.getIfAvailable();
    if (!exposeVerificationCode && mailer == null) {
      throw new BadRequestException("School verification email delivery is not configured");
    }
    if (mailer != null) mailer.send(email, code);
    session.setAttribute(VERIFICATION_EMAIL, email);
    session.setAttribute(VERIFICATION_CODE, code);
    session.setAttribute(VERIFICATION_EXPIRES_AT, Instant.now().plusSeconds(VERIFICATION_TTL_SECONDS).toEpochMilli());
    session.setAttribute(VERIFICATION_ATTEMPTS, 0);
    return new SchoolVerificationChallengeResponse(
        VERIFICATION_TTL_SECONDS, exposeVerificationCode ? code : null);
  }

  @Transactional
  public UserResponse verifySchool(HttpSession session, SchoolVerificationCodeRequest request) {
    User user = findCurrentUser(session);
    Object email = session.getAttribute(VERIFICATION_EMAIL);
    Object code = session.getAttribute(VERIFICATION_CODE);
    Object expiresAt = session.getAttribute(VERIFICATION_EXPIRES_AT);
    Object attemptsValue = session.getAttribute(VERIFICATION_ATTEMPTS);
    int attempts = attemptsValue instanceof Integer ? (Integer) attemptsValue : 0;
    if (!(email instanceof String)
        || !(code instanceof String)
        || !(expiresAt instanceof Long)
        || Instant.now().toEpochMilli() > (Long) expiresAt
        || !email.equals(normalizeEmail(user.getEmail()))) {
      throw new BadRequestException("Invalid or expired verification code");
    }
    if (!code.equals(request.getCode())) {
      attempts++;
      session.setAttribute(VERIFICATION_ATTEMPTS, attempts);
      if (attempts >= 5) clearVerificationChallenge(session);
      throw new BadRequestException("Invalid or expired verification code");
    }
    user.setVerified(true);
    user.setUpdatedAt(LocalDateTime.now());
    clearVerificationChallenge(session);
    return toResponse(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public User findCurrentUser(HttpSession session) {
    Long userId = currentUserId(session);
    return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Current user not found"));
  }

  @Transactional
  public UserResponse signup(SignupRequest request, HttpSession session) {
    String email = normalizeEmail(request.getEmail());
    if (!isAcademicEmail(email)) {
      throw new BadRequestException("A valid academic email is required");
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new BadRequestException("Email already exists");
    }

    User user = new User();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setName(request.getName());
    user.setAvatarUrl(request.getAvatarUrl());
    user.setRole("STUDENT");
    user.setVerified(false);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    User saved = userRepository.save(user);
    session.setAttribute(SESSION_USER_ID, saved.getId());
    return toResponse(saved);
  }

  @Transactional
  public UserResponse login(LoginRequest request, HttpSession session) {
    User user =
        userRepository
            .findByEmail(normalizeEmail(request.getEmail()))
            .filter(found -> found.getPassword() != null && passwordEncoder.matches(request.getPassword(), found.getPassword()))
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
    session.setAttribute(SESSION_USER_ID, user.getId());
    return toResponse(user);
  }

  @Transactional
  public void logout(HttpSession session) {
    session.invalidate();
  }

  public List<UserResponse> listUsers() {
    return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
  }

  public Long currentUserId(HttpSession session) {
    Long userId = optionalCurrentUserId(session);
    if (userId != null) {
      return userId;
    }
    throw new UnauthorizedException("Login required");
  }

  public Long optionalCurrentUserId(HttpSession session) {
    Object value = session == null ? null : session.getAttribute(SESSION_USER_ID);
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    if (allowMockUser) {
      return 1L;
    }
    return null;
  }

  public void requireAdmin(HttpSession session) {
    User user = findCurrentUser(session);
    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
      throw new ForbiddenException("Admin access required");
    }
  }

  @Transactional(readOnly = true)
  public User requireVerifiedStudent(HttpSession session) {
    User user = findCurrentUser(session);
    if (!user.isVerified()) {
      throw new ForbiddenException("School verification is required");
    }
    return user;
  }

  @Transactional(readOnly = true)
  public boolean isAdmin(HttpSession session) {
    Long userId = optionalCurrentUserId(session);
    return userId != null
        && userRepository.findById(userId)
            .map(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
            .orElse(false);
  }

  @Transactional(readOnly = true)
  public Set<String> recommendationTerms(HttpSession session) {
    Long userId = optionalCurrentUserId(session);
    if (userId == null) return Set.of();
    return userRepository.findById(userId)
        .map(user -> {
          String combined = String.join(",", user.getInterests() == null ? "" : user.getInterests(),
              user.getMajor() == null ? "" : user.getMajor(), user.getSchool() == null ? "" : user.getSchool());
          return Arrays.stream(combined.split(","))
              .map(String::trim)
              .filter(value -> !value.isBlank())
              .map(value -> value.toLowerCase(Locale.ROOT))
              .collect(Collectors.toSet());
        })
        .orElse(Set.of());
  }

  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .school(user.getSchool())
        .major(user.getMajor())
        .grade(user.getGrade())
        .interests(user.getInterests())
        .role(user.getRole())
        .verified(user.isVerified())
        .avatarUrl(user.getAvatarUrl())
        .build();
  }

  private String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }

  private boolean isAcademicEmail(String email) {
    int at = email.lastIndexOf('@');
    if (at < 1 || at == email.length() - 1) return false;
    String domain = email.substring(at + 1);
    return domain.matches(".+\\.edu(\\.[a-z]{2})?") || domain.matches(".+\\.ac\\.[a-z]{2}");
  }

  private void clearVerificationChallenge(HttpSession session) {
    session.removeAttribute(VERIFICATION_EMAIL);
    session.removeAttribute(VERIFICATION_CODE);
    session.removeAttribute(VERIFICATION_EXPIRES_AT);
    session.removeAttribute(VERIFICATION_ATTEMPTS);
  }
}
