package com.campio.domain.ingestion;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class IngestionHttpConfig {

  @Bean
  RestTemplate ingestionRestTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(8))
        .setReadTimeout(Duration.ofSeconds(15))
        .build();
  }
}
