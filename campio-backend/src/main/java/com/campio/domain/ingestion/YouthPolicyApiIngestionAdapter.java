package com.campio.domain.ingestion;

import com.campio.domain.opportunity.StudentOpportunityPolicy;
import com.campio.global.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class YouthPolicyApiIngestionAdapter implements IngestionAdapter {

  private static final int MAX_RESPONSE_CHARS = 2_000_000;
  private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2})[.\\-/]?(\\d{2})[.\\-/]?(\\d{2})");
  private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private final RestTemplate ingestionRestTemplate;
  private final ObjectMapper objectMapper;

  @Value("${campio.ingestion.youth-center-api-key:}")
  private String apiKey;

  @Override
  public OpportunitySourceType supports() {
    return OpportunitySourceType.YOUTH_POLICY_API;
  }

  @Override
  public List<FetchedRawOpportunity> fetch(OpportunitySource source) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new BadRequestException("Youth Center API key is not configured");
    }
    String url = UriComponentsBuilder.fromHttpUrl(source.getBaseUrl())
        .replaceQueryParam("openApiVlak", apiKey)
        .replaceQueryParam("pageIndex", 1)
        .replaceQueryParam("display", 100)
        .build(true)
        .toUriString();
    String body = ingestionRestTemplate.getForObject(url, String.class);
    if (body != null && body.length() > MAX_RESPONSE_CHARS) {
      throw new BadRequestException("Youth Center API response is too large");
    }
    try {
      JsonNode root = objectMapper.readTree(body == null ? "{}" : body);
      JsonNode items = findItems(root);
      List<FetchedRawOpportunity> results = new ArrayList<>();
      for (JsonNode item : items) {
        FetchedRawOpportunity opportunity = parseItem(source, item);
        if (opportunity != null) results.add(opportunity);
      }
      return results;
    } catch (BadRequestException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BadRequestException("Failed to parse Youth Center API response");
    }
  }

  private FetchedRawOpportunity parseItem(OpportunitySource source, JsonNode item) {
    String title = firstText(item, "polyBizSjnm", "plcyNm", "policyName", "title");
    String id = firstText(item, "bizId", "plcyNo", "policyId", "id");
    String introduction = firstText(item, "polyItcnCn", "plcyExplnCn", "description", "summary");
    String support = firstText(item, "sporCn", "sprtCn", "supportContent");
    String period = firstText(item, "rqutPrdCn", "aplyPrdCn", "applicationPeriod");
    String organization = firstText(item, "cnsgNmor", "sprvsnInstCdNm", "rgtrInstCdNm", "mngtMson");
    String applyUrl = firstText(item, "rqutUrla", "aplyUrlAddr", "rfcSiteUrla1", "refUrlAddr1", "url");
    String target = firstText(item, "ageInfo", "sprtTrgtCn", "supportTarget", "trgtCn");
    String requirements = joinNonBlank(" / ",
        firstText(item, "majrRqisCn", "majorRequirement"),
        firstText(item, "empmSttsCn", "employmentRequirement"),
        firstText(item, "accrRqisCn", "educationRequirement"));
    String applicationMethod = firstText(item, "rqutProcCn", "aplyMthdCn", "applicationMethod");
    if (isBlank(title)) return null;
    List<LocalDate> dates = parseDates(period);
    LocalDate deadline = dates.isEmpty() ? null : dates.get(dates.size() - 1);
    if (deadline == null) return null;
    String description = joinNonBlank(" ", introduction, support, "온통청년 청년정책 정보입니다.");
    String resolvedUrl = firstNonBlank(applyUrl, source.getBaseUrl());
    String content = joinNonBlank(" / ", description, period);
    return FetchedRawOpportunity.builder()
        .externalId("youth-policy-" + firstNonBlank(id, Integer.toUnsignedString(title.hashCode())))
        .sourceUrl(resolvedUrl)
        .rawTitle(title)
        .rawContent(content)
        .rawPayload(item.toString())
        .organization(firstNonBlank(organization, "온통청년"))
        .category(StudentOpportunityPolicy.classifyCategory(title, source.getCategoryHint(), content))
        .description(description)
        .requirements(requirements)
        .benefits(support)
        .applicationMethod(applicationMethod)
        .target(firstNonBlank(target, "청년"))
        .deadline(deadline)
        .startDate(dates.isEmpty() ? null : dates.get(0))
        .location("Nationwide")
        .online(content.contains("온라인"))
        .applyUrl(resolvedUrl)
        .tags(List.of("온통청년", "청년정책"))
        .build();
  }

  private JsonNode findItems(JsonNode node) {
    if (node == null) throw new BadRequestException("Youth Center API response is empty");
    if (node.isArray()) return node;
    for (String field : List.of("youthPolicy", "youthPolicies", "items", "data", "result", "results")) {
      JsonNode child = node.get(field);
      if (child == null) continue;
      if (child.isArray()) return child;
      try {
        return findItems(child);
      } catch (BadRequestException ignored) {
        // Continue with other known wrappers.
      }
    }
    Iterator<JsonNode> children = node.elements();
    while (children.hasNext()) {
      JsonNode child = children.next();
      if (child.isArray() && child.size() > 0 && child.get(0).isObject()) return child;
    }
    throw new BadRequestException("Youth Center API response does not contain policy items");
  }

  private List<LocalDate> parseDates(String value) {
    List<LocalDate> dates = new ArrayList<>();
    Matcher matcher = DATE_PATTERN.matcher(value == null ? "" : value);
    while (matcher.find()) {
      try {
        dates.add(LocalDate.parse(matcher.group(1) + matcher.group(2) + matcher.group(3), COMPACT_DATE));
      } catch (DateTimeParseException ignored) {
        // Ignore invalid dates from free-form application period text.
      }
    }
    return dates;
  }

  private String firstText(JsonNode node, String... fields) {
    for (String field : fields) {
      JsonNode value = node.get(field);
      if (value != null && !value.isNull() && !value.asText().isBlank()) return value.asText().trim();
    }
    return "";
  }

  private String joinNonBlank(String separator, String... values) {
    List<String> parts = new ArrayList<>();
    for (String value : values) if (!isBlank(value)) parts.add(value.trim());
    return String.join(separator, parts);
  }

  private String firstNonBlank(String preferred, String fallback) {
    return isBlank(preferred) ? fallback : preferred;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
