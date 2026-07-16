package com.campio.domain.user;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final LoginAttemptService loginAttemptService;

  @GetMapping("/auth/me")
  public UserResponse me(HttpSession session) {
    return userService.me(session);
  }

  @PostMapping("/auth/signup")
  public UserResponse signup(
      @Valid @RequestBody SignupRequest request, HttpSession session, HttpServletRequest servletRequest) {
    UserResponse response = userService.signup(request, session);
    servletRequest.changeSessionId();
    return response;
  }

  @PostMapping("/auth/login")
  public UserResponse login(
      @Valid @RequestBody LoginRequest request, HttpSession session, HttpServletRequest servletRequest) {
    String key = servletRequest.getRemoteAddr() + ":" + request.getEmail().trim().toLowerCase();
    loginAttemptService.check(key);
    try {
      UserResponse response = userService.login(request, session);
      loginAttemptService.success(key);
      servletRequest.changeSessionId();
      return response;
    } catch (com.campio.global.exception.UnauthorizedException ex) {
      loginAttemptService.failure(key);
      throw ex;
    }
  }

  @PostMapping("/auth/logout")
  public void logout(HttpSession session) {
    userService.logout(session);
  }

  @PatchMapping("/users/profile")
  public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request, HttpSession session) {
    return userService.updateProfile(session, request);
  }

  @PatchMapping("/users/interests")
  public UserResponse updateInterests(@Valid @RequestBody UpdateInterestsRequest request, HttpSession session) {
    return userService.updateInterests(session, request);
  }

  @PostMapping("/users/verify-school/request")
  public SchoolVerificationChallengeResponse requestSchoolVerification(
      @Valid @RequestBody SchoolVerificationRequest request, HttpSession session) {
    return userService.requestSchoolVerification(session, request);
  }

  @PostMapping("/users/verify-school")
  public UserResponse verifySchool(
      @Valid @RequestBody SchoolVerificationCodeRequest request, HttpSession session) {
    return userService.verifySchool(session, request);
  }
}
