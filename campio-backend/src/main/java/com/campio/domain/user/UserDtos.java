package com.campio.domain.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
  @Size(max = 100)
  private String name;
  @Size(max = 100)
  private String school;
  @Size(max = 100)
  private String major;
  @Min(1)
  @Max(8)
  private Integer grade;
  @Size(max = 1500000)
  private String avatarUrl;
}

@Getter
@Setter
@NoArgsConstructor
class UpdateInterestsRequest {
  @NotBlank
  @Size(max = 2000)
  private String interests;
}

@Getter
@Setter
@NoArgsConstructor
class SchoolVerificationCodeRequest {
  @NotBlank
  @Pattern(regexp = "\\d{6}")
  private String code;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SchoolVerificationChallengeResponse {
  private int expiresInSeconds;
  private String developmentCode;
}

@Getter
@Setter
@NoArgsConstructor
class SchoolVerificationRequest {
  @NotBlank
  @Email
  @Size(max = 255)
  private String schoolEmail;
}

@Getter
@Setter
@NoArgsConstructor
class LoginRequest {
  @NotBlank
  @Email
  @Size(max = 255)
  private String email;

  @NotBlank
  @Size(min = 8, max = 72)
  private String password;
}

@Getter
@Setter
@NoArgsConstructor
class SignupRequest {
  @NotBlank
  @Size(max = 100)
  private String name;

  @NotBlank
  @Email
  @Size(max = 255)
  private String email;

  @NotBlank
  @Size(min = 8, max = 72)
  private String password;

  @Size(max = 1500000)
  private String avatarUrl;
}
