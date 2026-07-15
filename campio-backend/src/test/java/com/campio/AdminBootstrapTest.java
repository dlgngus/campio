package com.campio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campio.domain.user.User;
import com.campio.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:campio-admin-bootstrap;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "campio.auth.allow-mock-user=false",
    "campio.admin.email=owner@campio.local",
    "campio.admin.password=secret-admin-password"
})
class AdminBootstrapTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void configuredAdminIsCreatedWhenSeedDataIsDisabled() {
    User admin = userRepository.findByEmail("owner@campio.local").orElseThrow();

    assertEquals("ADMIN", admin.getRole());
    assertTrue(admin.isVerified());
    assertTrue(passwordEncoder.matches("secret-admin-password", admin.getPassword()));
  }
}
