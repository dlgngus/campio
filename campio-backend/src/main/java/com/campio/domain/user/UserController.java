package com.campio.domain.user;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpSession;
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

  @GetMapping("/auth/me")
  public UserResponse me(HttpSession session) {
    return userService.me(session);
  }

  @PostMapping("/auth/signup")
  public UserResponse signup(@RequestBody SignupRequest request, HttpSession session) {
    return userService.signup(request, session);
  }

  @PostMapping("/auth/login")
  public UserResponse login(@RequestBody LoginRequest request, HttpSession session) {
    return userService.login(request, session);
  }

  @PostMapping("/auth/logout")
  public void logout(HttpSession session) {
    userService.logout(session);
  }

  @PatchMapping("/users/profile")
  public UserResponse updateProfile(@RequestBody UpdateProfileRequest request, HttpSession session) {
    return userService.updateProfile(session, request);
  }

  @PatchMapping("/users/interests")
  public UserResponse updateInterests(@RequestBody UpdateInterestsRequest request, HttpSession session) {
    return userService.updateInterests(session, request);
  }

  @PostMapping("/users/verify-school")
  public UserResponse verifySchool(@RequestBody VerifySchoolRequest request, HttpSession session) {
    return userService.verifySchool(session, request);
  }
}
