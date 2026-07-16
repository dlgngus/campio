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

  @Value("${campio.ingestion.youth-center-api-key:}")
  private String youthCenterApiKey;

  @Value("${campio.ingestion.work24-api-key:}")
  private String work24ApiKey;

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
    User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
    boolean existingUser = admin.getId() != null;
    if (existingUser && "ADMIN".equalsIgnoreCase(admin.getRole()) && passwordEncoder.matches(adminPassword, admin.getPassword())) {
      return;
    }

    if (!existingUser) {
      admin.setEmail(adminEmail);
      admin.setCreatedAt(LocalDateTime.now());
    }
    admin.setPassword(passwordEncoder.encode(adminPassword));
    if (admin.getName() == null || admin.getName().isBlank()) {
      admin.setName("Admin");
    }
    admin.setRole("ADMIN");
    admin.setVerified(true);
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
    if (youthCenterApiKey != null && !youthCenterApiKey.isBlank()) {
      createSourceIfMissing(
          sourceRepository,
          "온통청년 청년정책",
          "https://www.youthcenter.go.kr/opi/youthPlcyList.do?display=100&pageIndex=1",
          "Government Support",
          OpportunitySourceType.YOUTH_POLICY_API);
    }
    if (work24ApiKey != null && !work24ApiKey.isBlank()) {
      createSourceIfMissing(
          sourceRepository,
          "고용24 인턴 채용정보",
          "https://www.work24.go.kr/cm/openApi/call/wk/callOpenApiSvcInfo210L01.do?callTp=L&returnType=XML&startPage=1&display=100&career=N&keyword=%EC%9D%B8%ED%84%B4",
          "Internship",
          OpportunitySourceType.WORK24_API);
    }
  }

  private void createSourceIfMissing(
      OpportunitySourceRepository sourceRepository,
      String name,
      String baseUrl,
      String categoryHint) {
    createSourceIfMissing(sourceRepository, name, baseUrl, categoryHint, OpportunitySourceType.HTML);
  }

  private void createSourceIfMissing(
      OpportunitySourceRepository sourceRepository,
      String name,
      String baseUrl,
      String categoryHint,
      OpportunitySourceType type) {
    if (sourceRepository.findByName(name).isPresent()) {
      return;
    }
    OpportunitySource source = new OpportunitySource();
    source.setName(name);
    source.setType(type.name());
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
