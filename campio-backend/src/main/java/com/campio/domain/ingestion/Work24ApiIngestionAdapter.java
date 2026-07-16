package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
@RequiredArgsConstructor
public class Work24ApiIngestionAdapter implements IngestionAdapter {

  private static final int MAX_PAGES = 5;
  private static final int MAX_RESPONSE_CHARS = 2_000_000;
  private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private final RestTemplate ingestionRestTemplate;

  @Value("${campio.ingestion.work24-api-key:}")
  private String apiKey;

  @Override
  public OpportunitySourceType supports() {
    return OpportunitySourceType.WORK24_API;
  }

  @Override
  public List<FetchedRawOpportunity> fetch(OpportunitySource source) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new BadRequestException("Work24 API key is not configured");
    }
    List<FetchedRawOpportunity> results = new ArrayList<>();
    int page = 1;
    int total = Integer.MAX_VALUE;
    while (page <= MAX_PAGES && results.size() < total) {
      Document document = fetchDocument(source.getBaseUrl(), page);
      if (page == 1) total = parseInt(text(document.getDocumentElement(), "total"), 0);
      results.addAll(parseItems(document));
      page++;
    }
    return results;
  }

  private Document fetchDocument(String baseUrl, int page) {
    String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .replaceQueryParam("authKey", apiKey)
        .replaceQueryParam("callTp", "L")
        .replaceQueryParam("returnType", "XML")
        .replaceQueryParam("startPage", page)
        .replaceQueryParam("display", 100)
        .build(true)
        .toUriString();
    String body = ingestionRestTemplate.getForObject(url, String.class);
    if (body != null && body.length() > MAX_RESPONSE_CHARS) {
      throw new BadRequestException("Work24 API response is too large");
    }
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(
          (body == null ? "" : body).getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new BadRequestException("Failed to parse Work24 API response");
    }
  }

  private List<FetchedRawOpportunity> parseItems(Document document) {
    NodeList items = document.getElementsByTagName("wanted");
    List<FetchedRawOpportunity> results = new ArrayList<>();
    for (int index = 0; index < items.getLength(); index++) {
      Element item = (Element) items.item(index);
      String id = text(item, "wantedAuthNo");
      String title = text(item, "title");
      String company = text(item, "company");
      String url = firstNonBlank(text(item, "wantedInfoUrl"), text(item, "wantedMobileInfoUrl"));
      LocalDate deadline = parseDate(text(item, "closeDt"));
      if (isBlank(id) || isBlank(title) || isBlank(url) || deadline == null) continue;
      String region = firstNonBlank(text(item, "region"), "Nationwide");
      String education = text(item, "minEdubg");
      String career = text(item, "career");
      String salary = joinNonBlank(" ", text(item, "salTpNm"), text(item, "sal"));
      String workSchedule = text(item, "holidayTpNm");
      String industry = text(item, "indTpNm");
      String description = String.join(" / ", "고용24 인턴·신입 채용정보", industry, region)
          .replaceAll("( / )+$", "");
      results.add(FetchedRawOpportunity.builder()
          .externalId("work24-" + id)
          .sourceUrl(url)
          .rawTitle(title)
          .rawContent(description + " / " + region)
          .rawPayload(item.getTextContent())
          .organization(company)
          .category("Internship")
          .description(description)
          .requirements(joinNonBlank(" / ", "학력: " + education, "경력: " + career))
          .benefits(joinNonBlank(" / ", salary, workSchedule))
          .target(joinNonBlank(" / ", education, career))
          .deadline(deadline)
          .startDate(parseDate(text(item, "regDt")))
          .location(region)
          .online(false)
          .applyUrl(url)
          .tags(List.of("고용24", "인턴", "채용"))
          .build());
    }
    return results;
  }

  private String text(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
  }

  private LocalDate parseDate(String value) {
    if (isBlank(value)) return null;
    String digits = value.replaceAll("[^0-9]", "");
    if (digits.length() != 8) return null;
    try {
      return LocalDate.parse(digits, COMPACT_DATE);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private int parseInt(String value, int fallback) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }

  private String firstNonBlank(String preferred, String fallback) {
    return isBlank(preferred) ? fallback : preferred;
  }

  private String joinNonBlank(String separator, String... values) {
    List<String> parts = new ArrayList<>();
    for (String value : values) if (!isBlank(value) && !value.endsWith(": ")) parts.add(value.trim());
    return String.join(separator, parts);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
