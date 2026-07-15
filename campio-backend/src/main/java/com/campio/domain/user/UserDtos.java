package com.campio.domain.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserResponse {
  private Long id;
  private String email;
  private String name;
  private String school;
  private String major;
  private Integer grade;
  private String interests;
  private String role;
  private boolean verified;
  private String avatarUrl;
}

@Getter
@Setter
@NoArgsConstructor
class UpdateProfileRequest {
  private String name;
  private String school;
  private String major;
  private Integer grade;
  private String avatarUrl;
}

@Getter
@Setter
@NoArgsConstructor
class UpdateInterestsRequest {
  @NotBlank
  private String interests;
}

@Getter
@Setter
@NoArgsConstructor
class VerifySchoolRequest {
  @NotBlank
  @Email
  private String schoolEmail;
}

@Getter
@Setter
@NoArgsConstructor
class LoginRequest {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String password;
}

@Getter
@Setter
@NoArgsConstructor
class SignupRequest {
  @NotBlank
  private String name;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String password;

  private String avatarUrl;
}
