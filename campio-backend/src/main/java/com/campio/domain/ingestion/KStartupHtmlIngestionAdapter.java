package com.campio.domain.ingestion;

import com.campio.global.exception.BadRequestException;
import com.campio.domain.opportunity.StudentOpportunityPolicy;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class KStartupHtmlIngestionAdapter implements IngestionAdapter {

  private static final int MAX_PAGES = 20;
  private static final int MAX_RESPONSE_CHARS = 2_000_000;
  private static final Pattern LISTING_PATTERN = Pattern.compile(
      "([가-힣A-Za-z0-9ㆍ·&/()\\[\\].,'\\-\\s]+?)\\s+D-\\d+\\s+(.+?)\\s+새로운게시글\\s+(.+?)\\s+등록일자\\s+(\\d{4}-\\d{2}-\\d{2})\\s+시작일자\\s+(\\d{4}-\\d{2}-\\d{2})\\s+마감일자\\s+(\\d{4}-\\d{2}-\\d{2})\\s+조회",
      Pattern.DOTALL);
  private static final Pattern BOARD_ITEM_PATTERN = Pattern.compile(
      "(?is)<li[^>]*class=\"[^\"]*notice[^\"]*\"[^>]*>(.*?)</li>");
  private static final Pattern CATEGORY_PATTERN = Pattern.compile(
      "(?is)<span[^>]*class=\"[^\"]*flag(?!_agency)[^\"]*\"[^>]*>(.*?)</span>");
  private static final Pattern TITLE_PATTERN = Pattern.compile(
      "(?is)<p[^>]*class=\"[^\"]*tit[^\"]*\"[^>]*>(.*?)</p>");
  private static final Pattern LIST_VALUE_PATTERN = Pattern.compile(
      "(?is)<span[^>]*class=\"[^\"]*list[^\"]*\"[^>]*>(.*?)</span>");
  private static final Pattern VIEW_ID_PATTERN = Pattern.compile("go_view(?:_blank)?\\((\\d+)\\)");
  private static final Pattern PAGE_PATTERN = Pattern.compile("fn_egov_link_page\\((\\d+)\\)");
  private static final Pattern BIZINFO_ROW_PATTERN = Pattern.compile(
      "(?is)<tr>\\s*<td>\\s*(\\d+)\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td[^>]*class=\"[^\"]*txt_l[^\"]*\"[^>]*>\\s*<a\\s+href=\\s*\"([^\"]+)\"[^>]*>\\s*(.*?)\\s*</a>\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>",
      Pattern.DOTALL);
  private static final Pattern BIZINFO_LAST_PAGE_PATTERN = Pattern.compile("cpage=(\\d+)[^\"]*\"[^>]*title=\"마지막페이지\"");
  private static final Pattern BIZINFO_DATE_RANGE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s*~\\s*(\\d{4}-\\d{2}-\\d{2})");
  private static final List<RegionAlias> REGION_ALIASES = List.of(
      new RegionAlias("서울특별시", "서울특별시"),
      new RegionAlias("부산광역시", "부산광역시"),
      new RegionAlias("대구광역시", "대구광역시"),
      new RegionAlias("인천광역시", "인천광역시"),
      new RegionAlias("광주광역시", "광주광역시"),
      new RegionAlias("대전광역시", "대전광역시"),
      new RegionAlias("울산광역시", "울산광역시"),
      new RegionAlias("세종특별자치시", "세종특별자치시"),
      new RegionAlias("경기도", "경기도"),
      new RegionAlias("강원특별자치도", "강원특별자치도"),
      new RegionAlias("충청북도", "충청북도"),
      new RegionAlias("충청남도", "충청남도"),
      new RegionAlias("전북특별자치도", "전북특별자치도"),
      new RegionAlias("전라북도", "전북특별자치도"),
      new RegionAlias("전라남도", "전라남도"),
      new RegionAlias("경상북도", "경상북도"),
      new RegionAlias("경상남도", "경상남도"),
      new RegionAlias("제주특별자치도", "제주특별자치도"),
      new RegionAlias("서울", "서울특별시"),
      new RegionAlias("부산", "부산광역시"),
      new RegionAlias("대구", "대구광역시"),
      new RegionAlias("인천", "인천광역시"),
      new RegionAlias("광주", "광주광역시"),
      new RegionAlias("대전", "대전광역시"),
      new RegionAlias("울산", "울산광역시"),
      new RegionAlias("세종", "세종특별자치시"),
      new RegionAlias("경기", "경기도"),
      new RegionAlias("강원", "강원특별자치도"),
      new RegionAlias("충북", "충청북도"),
      new RegionAlias("충남", "충청남도"),
      new RegionAlias("전북", "전북특별자치도"),
      new RegionAlias("전남", "전라남도"),
      new RegionAlias("경북", "경상북도"),
      new RegionAlias("경남", "경상남도"),
      new RegionAlias("제주", "제주특별자치도"));

  private final RestTemplate ingestionRestTemplate;

  @Override
  public OpportunitySourceType supports() {
    return OpportunitySourceType.HTML;
  }

  @Override
  public List<FetchedRawOpportunity> fetch(OpportunitySource source) {
    if (!isSupportedHtmlSource(source.getBaseUrl())) {
      throw new BadRequestException("HTML ingestion is only enabled for approved public sources");
    }
    List<FetchedRawOpportunity> results = new ArrayList<>();
    String firstPage = fetchPage(source.getBaseUrl());
    results.addAll(parse(source, firstPage));
    int lastPage = Math.min(MAX_PAGES, findLastPage(firstPage));
    for (int page = 2; page <= lastPage; page++) {
      results.addAll(parse(source, fetchPage(withPage(source.getBaseUrl(), page))));
    }
    return results;
  }

  private String fetchPage(String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 CampioBot/1.0 (+https://campio.local)");
    headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml");
    headers.add(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en;q=0.8");
    try {
      ResponseEntity<String> response = ingestionRestTemplate.exchange(
          url,
          HttpMethod.GET,
          new HttpEntity<>(headers),
          String.class);
      String body = response.getBody();
      if (body != null && body.length() > MAX_RESPONSE_CHARS) {
        throw new BadRequestException("HTML source response is too large");
      }
      return body;
    } catch (ResourceAccessException ex) {
      throw new BadRequestException("Failed to fetch approved HTML source");
    }
  }

  List<FetchedRawOpportunity> parse(OpportunitySource source, String html) {
    if (source.getBaseUrl().contains("bizinfo.go.kr")) {
      return parseBizInfo(source, html == null ? "" : html);
    }
    List<FetchedRawOpportunity> results = parseBoardItems(source, html == null ? "" : html);
    if (!results.isEmpty()) {
      return results;
    }
    return parseNormalizedText(source, html);
  }

  private List<FetchedRawOpportunity> parseBoardItems(OpportunitySource source, String html) {
    List<FetchedRawOpportunity> results = new ArrayList<>();
    Matcher itemMatcher = BOARD_ITEM_PATTERN.matcher(html);
    Set<String> externalIds = new LinkedHashSet<>();
    while (itemMatcher.find()) {
      String block = itemMatcher.group(1);
      String title = extractFirst(block, TITLE_PATTERN);
      String category = extractFirst(block, CATEGORY_PATTERN);
      String viewId = extractFirst(block, VIEW_ID_PATTERN);
      List<String> listValues = extractListValues(block);
      String organization = findOrganization(title, listValues);
      LocalDate startDate = findDate(listValues, "시작일자");
      LocalDate deadline = findDate(listValues, "마감일자");
      if (title.isBlank() || organization.isBlank() || deadline == null) {
        continue;
      }
      FetchedRawOpportunity item = buildOpportunity(
          source,
          firstNonBlank(viewId, hash(title + "|" + organization + "|" + deadline).substring(0, 12)),
          firstNonBlank(category, "Startup"),
          title,
          organization,
          startDate,
          deadline);
      if (externalIds.add(item.getExternalId())) {
        results.add(item);
      }
    }
    return results;
  }

  private List<FetchedRawOpportunity> parseNormalizedText(OpportunitySource source, String html) {
    String text = normalizeText(stripTags(html == null ? "" : html));
    Matcher matcher = LISTING_PATTERN.matcher(text);
    List<FetchedRawOpportunity> results = new ArrayList<>();
    while (matcher.find()) {
      String category = clean(matcher.group(1));
      String title = clean(matcher.group(2));
      String organization = clean(matcher.group(3));
      LocalDate startDate = LocalDate.parse(matcher.group(5));
      LocalDate deadline = LocalDate.parse(matcher.group(6));
      if (title.isBlank() || organization.isBlank()) {
        continue;
      }
      results.add(buildOpportunity(source, hash(title + "|" + organization + "|" + deadline), category, title, organization, startDate, deadline));
    }
    return results;
  }

  private List<FetchedRawOpportunity> parseBizInfo(OpportunitySource source, String html) {
    List<FetchedRawOpportunity> results = new ArrayList<>();
    Matcher matcher = BIZINFO_ROW_PATTERN.matcher(html);
    Set<String> externalIds = new LinkedHashSet<>();
    while (matcher.find()) {
      String externalId = "bizinfo-" + clean(matcher.group(1));
      String category = clean(stripTags(matcher.group(2)));
      String detailPath = clean(matcher.group(3));
      String title = clean(stripTags(matcher.group(4)));
      String period = clean(stripTags(matcher.group(5)));
      String owner = clean(stripTags(matcher.group(6)));
      String organization = clean(stripTags(matcher.group(7)));
      LocalDate registeredAt = LocalDate.parse(matcher.group(8));
      LocalDate startDate = null;
      LocalDate deadline = null;
      Matcher dateMatcher = BIZINFO_DATE_RANGE_PATTERN.matcher(period);
      if (dateMatcher.find()) {
        startDate = LocalDate.parse(dateMatcher.group(1));
        deadline = LocalDate.parse(dateMatcher.group(2));
      }
      if (title.isBlank() || organization.isBlank()) {
        continue;
      }
      String sourceUrl = absolutizeBizInfoUrl(detailPath);
      String content = category + " / " + owner + " / " + organization + " / 신청기간 " + period;
      String location = resolveLocation(owner, title, organization);
      FetchedRawOpportunity item = FetchedRawOpportunity.builder()
          .externalId(externalId)
          .sourceUrl(sourceUrl)
          .rawTitle(title)
          .rawContent(content)
          .rawPayload("{\"source\":\"Bizinfo\",\"category\":\"" + escape(category) + "\",\"period\":\"" + escape(period) + "\"}")
          .organization(organization)
          .category(StudentOpportunityPolicy.classifyCategory(title, category, content))
          .description("기업마당 공개 지원사업 공고입니다. 자세한 내용과 신청은 원본 출처에서 확인하세요.")
          .deadline(deadline)
          .startDate(startDate == null ? registeredAt : startDate)
          .location(location)
          .online(false)
          .applyUrl(sourceUrl)
          .tags(buildTags("기업마당", category, owner, location))
          .build();
      if (externalIds.add(item.getExternalId())) {
        results.add(item);
      }
    }
    return results;
  }

  private FetchedRawOpportunity buildOpportunity(
      OpportunitySource source,
      String idPart,
      String category,
      String title,
      String organization,
      LocalDate startDate,
      LocalDate deadline) {
    String externalId = idPart.matches("\\d+") ? "kstartup-" + idPart : idPart;
    String sourceUrl = source.getBaseUrl() + (idPart.matches("\\d+") ? "?schM=view&pbancSn=" + idPart : "#" + externalId.substring(0, Math.min(12, externalId.length())));
    String location = resolveLocation(title, organization);
    return FetchedRawOpportunity.builder()
        .externalId(externalId)
        .sourceUrl(sourceUrl)
        .rawTitle(title)
        .rawContent(category + " / " + organization + " / 마감일자 " + deadline)
        .rawPayload("{\"source\":\"K-Startup\",\"category\":\"" + escape(category) + "\"}")
        .organization(organization)
        .category(firstNonBlank(source.getCategoryHint(), category))
        .description("K-Startup 공개 모집중 사업공고입니다. 자세한 내용과 신청은 원본 출처에서 확인하세요.")
        .deadline(deadline)
        .startDate(startDate)
        .location(location)
        .online(false)
        .applyUrl(sourceUrl)
        .tags(buildTags("K-Startup", category, location))
        .build();
  }

  private List<String> buildTags(String sourceName, String category, String ownerOrLocation) {
    return buildTags(sourceName, category, ownerOrLocation, ownerOrLocation);
  }

  private List<String> buildTags(String sourceName, String category, String owner, String location) {
    List<String> tags = new ArrayList<>();
    addTag(tags, sourceName);
    addTag(tags, category);
    if (!location.equals(owner)) {
      addTag(tags, owner);
    }
    return tags;
  }

  private void addTag(List<String> tags, String value) {
    String cleaned = clean(value);
    if (!cleaned.isBlank() && !tags.contains(cleaned)) {
      tags.add(cleaned);
    }
  }

  private String resolveLocation(String... values) {
    for (String value : values) {
      String normalized = clean(value).replaceAll("\\s+", "");
      if (normalized.isBlank()) {
        continue;
      }
      for (RegionAlias alias : REGION_ALIASES) {
        if (normalized.contains(alias.needle())) {
          return alias.label();
        }
      }
    }
    return "Nationwide";
  }

  private record RegionAlias(String needle, String label) {
  }

  private String stripTags(String html) {
    return html
        .replaceAll("(?is)<script.*?</script>", " ")
        .replaceAll("(?is)<style.*?</style>", " ")
        .replaceAll("(?is)<[^>]+>", " ");
  }

  private String normalizeText(String value) {
    return decodeHtml(value)
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private String decodeHtml(String value) {
    String decoded = value == null ? "" : value;
    Matcher matcher = Pattern.compile("&#(\\d+);").matcher(decoded);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(buffer, Matcher.quoteReplacement(Character.toString((char) Integer.parseInt(matcher.group(1)))));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private String clean(String value) {
    return normalizeText(value == null ? "" : value);
  }

  private String extractFirst(String block, Pattern pattern) {
    Matcher matcher = pattern.matcher(block);
    return matcher.find() ? clean(stripTags(matcher.group(1))) : "";
  }

  private List<String> extractListValues(String block) {
    Matcher matcher = LIST_VALUE_PATTERN.matcher(block);
    List<String> values = new ArrayList<>();
    while (matcher.find()) {
      String value = clean(stripTags(matcher.group(1)));
      if (!value.isBlank()) {
        values.add(value);
      }
    }
    return values;
  }

  private String findOrganization(String title, List<String> listValues) {
    String organization = "";
    for (String value : listValues) {
      if (value.startsWith("등록일자")) {
        break;
      }
      if (!value.equals(title)) {
        organization = value;
      }
    }
    return organization;
  }

  private LocalDate findDate(List<String> values, String label) {
    for (String value : values) {
      if (value.startsWith(label)) {
        return LocalDate.parse(value.replace(label, "").trim());
      }
    }
    return null;
  }

  private int findLastPage(String html) {
    Matcher bizinfoMatcher = BIZINFO_LAST_PAGE_PATTERN.matcher(html == null ? "" : html);
    if (bizinfoMatcher.find()) {
      return Integer.parseInt(bizinfoMatcher.group(1));
    }
    Matcher matcher = PAGE_PATTERN.matcher(html == null ? "" : html);
    int lastPage = 1;
    while (matcher.find()) {
      lastPage = Math.max(lastPage, Integer.parseInt(matcher.group(1)));
    }
    return lastPage;
  }

  private String withPage(String url, int page) {
    String withoutFragment = url.split("#", 2)[0];
    if (withoutFragment.contains("bizinfo.go.kr")) {
      if (withoutFragment.matches(".*([?&])cpage=\\d+.*")) {
        return withoutFragment.replaceAll("([?&])cpage=\\d+", "$1cpage=" + page);
      }
      String separator = withoutFragment.contains("?") ? "&" : "?";
      return withoutFragment + separator + "rows=15&cpage=" + page;
    }
    String separator = withoutFragment.contains("?") ? "&" : "?";
    return withoutFragment + separator + "page=" + page;
  }

  private boolean isSupportedHtmlSource(String baseUrl) {
    if (baseUrl == null) return false;
    try {
      String host = URI.create(baseUrl).getHost();
      return "www.k-startup.go.kr".equalsIgnoreCase(host)
          || "k-startup.go.kr".equalsIgnoreCase(host)
          || "www.bizinfo.go.kr".equalsIgnoreCase(host)
          || "bizinfo.go.kr".equalsIgnoreCase(host);
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  private String absolutizeBizInfoUrl(String detailPath) {
    if (detailPath.startsWith("http")) {
      return detailPath;
    }
    return "https://www.bizinfo.go.kr" + (detailPath.startsWith("/") ? detailPath : "/" + detailPath);
  }

  private String firstNonBlank(String preferred, String fallback) {
    return preferred == null || preferred.isBlank() ? fallback : preferred;
  }

  private String escape(String value) {
    return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte b : encoded) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }
}
