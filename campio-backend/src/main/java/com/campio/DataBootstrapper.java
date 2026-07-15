package com.campio;

import com.campio.domain.ingestion.OpportunitySource;
import com.campio.domain.ingestion.OpportunitySourceRepository;
import com.campio.domain.ingestion.OpportunitySourceType;
import com.campio.domain.user.User;
import com.campio.domain.user.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataBootstrapper {

  @Value("${campio.admin.email:}")
  private String adminEmail;

  @Value("${campio.admin.password:}")
  private String adminPassword;

  @Value("${campio.ingestion.bootstrap-sources-enabled:false}")
  private boolean bootstrapSourcesEnabled;

  @Bean
  CommandLineRunner bootstrapAdminAndSources(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      OpportunitySourceRepository sourceRepository) {
    return args -> {
      createConfiguredAdminIfMissing(userRepository, passwordEncoder);
      createRealSourcesIfMissing(sourceRepository);
    };
  }

  private void createConfiguredAdminIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
      return;
    }
    if (userRepository.findByEmail(adminEmail).isPresent()) {
      return;
    }

    User admin = new User();
    admin.setEmail(adminEmail);
    admin.setPassword(passwordEncoder.encode(adminPassword));
    admin.setName("Admin");
    admin.setRole("ADMIN");
    admin.setVerified(true);
    admin.setCreatedAt(LocalDateTime.now());
    admin.setUpdatedAt(LocalDateTime.now());
    userRepository.save(admin);
  }

  private void createRealSourcesIfMissing(OpportunitySourceRepository sourceRepository) {
    createSourceIfMissing(
        sourceRepository,
        "K-Startup 모집중 사업공고",
        "https://www.k-startup.go.kr/web/contents/bizpbanc-ongoing.do",
        "Startup");
    createSourceIfMissing(
        sourceRepository,
        "기업마당 지원사업 공고",
        "https://www.bizinfo.go.kr/sii/siia/selectSIIA200View.do?rows=15&cpage=1",
        "Government Support");
  }

  private void createSourceIfMissing(
      OpportunitySourceRepository sourceRepository,
      String name,
      String baseUrl,
      String categoryHint) {
    if (sourceRepository.existsByName(name)) {
      return;
    }
    OpportunitySource source = new OpportunitySource();
    source.setName(name);
    source.setType(OpportunitySourceType.HTML.name());
    source.setBaseUrl(baseUrl);
    source.setCategoryHint(categoryHint);
    source.setCrawlIntervalMinutes(1440);
    source.setRobotsAllowed(true);
    source.setEnabled(bootstrapSourcesEnabled);
    source.setFailureCount(0);
    source.setCreatedAt(LocalDateTime.now());
    source.setUpdatedAt(LocalDateTime.now());
    sourceRepository.save(source);
  }
}
