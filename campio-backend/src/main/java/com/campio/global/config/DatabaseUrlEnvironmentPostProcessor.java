package com.campio.global.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    String datasourceUrl = firstNonBlank(
        environment.getProperty("SPRING_DATASOURCE_URL"),
        environment.getProperty("DATABASE_URL"));
    if (datasourceUrl == null || datasourceUrl.startsWith("jdbc:postgresql://")) {
      return;
    }
    if (!datasourceUrl.startsWith("postgresql://")) {
      return;
    }

    RenderDatabaseUrl parsed = parseRenderDatabaseUrl(datasourceUrl);
    Map<String, Object> properties = new HashMap<>();
    properties.put("spring.datasource.url", parsed.jdbcUrl);
    if (isBlank(environment.getProperty("SPRING_DATASOURCE_USERNAME")) && parsed.username != null) {
      properties.put("spring.datasource.username", parsed.username);
    }
    if (isBlank(environment.getProperty("SPRING_DATASOURCE_PASSWORD")) && parsed.password != null) {
      properties.put("spring.datasource.password", parsed.password);
    }
    environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", properties));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private RenderDatabaseUrl parseRenderDatabaseUrl(String datasourceUrl) {
    URI uri = URI.create(datasourceUrl);
    StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
    jdbcUrl.append(uri.getHost());
    if (uri.getPort() > 0) {
      jdbcUrl.append(":").append(uri.getPort());
    }
    jdbcUrl.append(uri.getRawPath() == null ? "" : uri.getRawPath());
    if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
      jdbcUrl.append("?").append(uri.getRawQuery());
    }

    String username = null;
    String password = null;
    String userInfo = uri.getRawUserInfo();
    if (userInfo != null && !userInfo.isBlank()) {
      String[] parts = userInfo.split(":", 2);
      username = decode(parts[0]);
      if (parts.length > 1) {
        password = decode(parts[1]);
      }
    }
    return new RenderDatabaseUrl(jdbcUrl.toString(), username, password);
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private String firstNonBlank(String preferred, String fallback) {
    return isBlank(preferred) ? fallback : preferred;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static class RenderDatabaseUrl {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    private RenderDatabaseUrl(String jdbcUrl, String username, String password) {
      this.jdbcUrl = jdbcUrl;
      this.username = username;
      this.password = password;
    }
  }
}
