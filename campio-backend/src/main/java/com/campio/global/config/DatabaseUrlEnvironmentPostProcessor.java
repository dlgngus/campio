package com.campio.global.config;

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
    String datasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
    if (datasourceUrl == null || !datasourceUrl.startsWith("postgresql://")) {
      return;
    }

    Map<String, Object> properties = new HashMap<>();
    properties.put("spring.datasource.url", "jdbc:" + datasourceUrl);
    environment.getPropertySources().addFirst(new MapPropertySource("renderDatabaseUrl", properties));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
