package com.campio.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

  private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

  @Test
  void convertsDatabaseUrlIntoJdbcDatasourceProperties() {
    MockEnvironment environment = new MockEnvironment()
        .withProperty("DATABASE_URL", "postgresql://campio:secret%21@db.internal:5432/campio?sslmode=require");

    processor.postProcessEnvironment(environment, null);

    assertEquals("jdbc:postgresql://db.internal:5432/campio?sslmode=require", environment.getProperty("spring.datasource.url"));
    assertEquals("campio", environment.getProperty("spring.datasource.username"));
    assertEquals("secret!", environment.getProperty("spring.datasource.password"));
  }

  @Test
  void convertsSpringDatasourceUrlWhenRenderSuppliesPostgresqlUrl() {
    MockEnvironment environment = new MockEnvironment()
        .withProperty("SPRING_DATASOURCE_URL", "postgresql://user:pass@render-db:5432/app");

    processor.postProcessEnvironment(environment, null);

    assertEquals("jdbc:postgresql://render-db:5432/app", environment.getProperty("spring.datasource.url"));
    assertEquals("user", environment.getProperty("spring.datasource.username"));
    assertEquals("pass", environment.getProperty("spring.datasource.password"));
  }

  @Test
  void keepsExplicitDatasourceCredentialsWhenPresent() {
    MockEnvironment environment = new MockEnvironment()
        .withProperty("DATABASE_URL", "postgresql://url-user:url-pass@render-db:5432/app")
        .withProperty("SPRING_DATASOURCE_USERNAME", "explicit-user")
        .withProperty("SPRING_DATASOURCE_PASSWORD", "explicit-pass");

    processor.postProcessEnvironment(environment, null);

    assertEquals("jdbc:postgresql://render-db:5432/app", environment.getProperty("spring.datasource.url"));
    assertNull(environment.getProperty("spring.datasource.username"));
    assertNull(environment.getProperty("spring.datasource.password"));
  }

  @Test
  void leavesJdbcDatasourceUrlUntouched() {
    MockEnvironment environment = new MockEnvironment()
        .withProperty("SPRING_DATASOURCE_URL", "jdbc:postgresql://render-db:5432/app");

    processor.postProcessEnvironment(environment, null);

    assertNull(environment.getProperty("spring.datasource.url"));
  }
}
