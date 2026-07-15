package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class JsonApiIngestionAdapter implements IngestionAdapter {

  private final RestTemplate ingestionRestTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public OpportunitySourceType supports() {
    return OpportunitySourceType.API;
  }

  @Override
  public List<FetchedRawOpportunity> fetch(OpportunitySource source) {
    String body = ingestionRestTemplate.getForObject(source.getBaseUrl(), String.class);
    try {
      JsonNode root = objectMapper.readTree(body == null ? "[]" : body);
      JsonNode items = findItems(root);
      List<FetchedRawOpportunity> results = new ArrayList<>();
      for (JsonNode item : items) {
        String title = firstText(item, "rawTitle", "title", "name", "subject");
        String sourceUrl = firstText(item, "sourceUrl", "url", "link", "applyUrl");
        if (isBlank(title) || isBlank(sourceUrl)) {
          continue;
        }
        results.add(
            FetchedRawOpportunity.builder()
                .externalId(firstText(item, "externalId", "id", "guid"))
                .sourceUrl(sourceUrl)
                .rawTitle(title)
                .rawContent(firstText(item, "rawContent", "content", "description", "summary"))
                .rawPayload(item.toString())
                .build());
      }
      return results;
    } catch (Exception ex) {
      throw new BadRequestException("Failed to parse API source response");
    }
  }

  private JsonNode findItems(JsonNode root) {
    if (root.isArray()) {
      return root;
    }
    for (String field : List.of("items", "data", "results")) {
      JsonNode node = root.get(field);
      if (node != null && node.isArray()) {
        return node;
      }
    }
    throw new BadRequestException("API source response must be an array or contain items/data/results");
  }

  private String firstText(JsonNode node, String... fields) {
    for (String field : fields) {
      JsonNode value = node.get(field);
      if (value != null && !value.isNull() && !isBlank(value.asText())) {
        return value.asText();
      }
    }
    return null;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
