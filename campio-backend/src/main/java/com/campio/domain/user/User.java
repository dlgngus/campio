package com.campio.domain.user;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private String password;

  @Column(nullable = false)
  private String name;

  private String school;
  private String major;
  private Integer grade;
  @Lob
  private String interests;
  private String role;
  private boolean verified;

  @Lob
  private String avatarUrl;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
