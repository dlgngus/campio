package com.campio.global.config;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class FlywayMigrationConfig {

  private final DataSource dataSource;

  @Value("${campio.flyway.enabled:true}")
  private boolean flywayEnabled;

  @Value("${campio.flyway.baseline-on-migrate:true}")
  private boolean baselineOnMigrate;

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  CommandLineRunner runFlywayMigrations() {
    return args -> {
      if (!flywayEnabled) {
        return;
      }
      Flyway.configure()
          .dataSource(dataSource)
          .locations("classpath:db/migration")
          .baselineOnMigrate(baselineOnMigrate)
          .load()
          .migrate();
    };
  }
}
