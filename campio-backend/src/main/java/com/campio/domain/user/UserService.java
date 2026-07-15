package com.campio.domain.user;

import com.campio.global.exception.BadRequestException;
import com.campio.global.exception.ForbiddenException;
import com.campio.global.exception.NotFoundException;
import com.campio.global.exception.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

  public static final String SESSION_USER_ID = "campio.userId";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${campio.auth.allow-mock-user:true}")
  private boolean allowMockUser;

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
    return toResponse(findCurrentUser(session));
  }

  @Transactional
  public UserResponse verifySchool(HttpSession session, VerifySchoolRequest request) {
    User user = findCurrentUser(session);
    user.setVerified(true);
    user.setUpdatedAt(LocalDateTime.now());
    return toResponse(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public User findCurrentUser(HttpSession session) {
    Long userId = currentUserId(session);
    return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Current user not found"));
  }

  @Transactional(readOnly = true)
  public User findCurrentUser() {
    Long userId = currentUserId();
    return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Current user not found"));
  }

  @Transactional
  public UserResponse signup(SignupRequest request, HttpSession session) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new BadRequestException("Email already exists");
    }

    User user = new User();
    user.setEmail(request.getEmail());
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
            .findByEmail(request.getEmail())
            .filter(found -> found.getPassword() != null && passwordEncoder.matches(request.getPassword(), found.getPassword()))
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));
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
    Object value = session.getAttribute(SESSION_USER_ID);
    if (value instanceof Long) {
      return (Long) value;
    }
    if (value instanceof Integer) {
      return ((Integer) value).longValue();
    }
    if (allowMockUser) {
      return 1L;
    }
    throw new UnauthorizedException("Login required");
  }

  public Long currentUserId() {
    return allowMockUser ? 1L : null;
  }

  public void requireAdmin(HttpSession session) {
    User user = findCurrentUser(session);
    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
      throw new ForbiddenException("Admin access required");
    }
  }

  private UserResponse toResponse(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .school(user.getSchool())
        .major(user.getMajor())
        .grade(user.getGrade())
        .role(user.getRole())
        .verified(user.isVerified())
        .avatarUrl(user.getAvatarUrl())
        .build();
  }
}
