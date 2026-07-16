package com.campio.domain.opportunity;

import java.util.List;

public final class StudentOpportunityPolicy {

  private static final List<String> STUDENT_RELEVANT_KEYWORDS = List.of(
      "대학생",
      "대학원생",
      "재학생",
      "휴학생",
      "졸업예정",
      "청년",
      "만 19",
      "만19",
      "만 18",
      "만18",
      "예비창업",
      "창업동아리",
      "창업교육",
      "취업",
      "인턴",
      "채용",
      "일자리",
      "교육생",
      "수강생",
      "멘토링",
      "공모전",
      "경진대회",
      "해커톤",
      "서포터즈",
      "대외활동",
      "장학",
      "교환학생",
      "캠프",
      "부트캠프");

  private static final List<String> STUDENT_CATEGORIES = List.of(
      "internship", "contest", "competition", "external activity", "scholarship", "exchange",
      "research", "startup", "seminar", "mentoring", "campus event", "full-time job");

  private StudentOpportunityPolicy() {}

  static List<String> categoryKeywords(String category) {
    switch (safe(category).trim().toLowerCase()) {
      case "scholarship": return List.of("장학", "학자금", "등록금");
      case "contest": return List.of("공모전", "경진대회", "해커톤", "대회");
      case "internship": return List.of("인턴", "일경험", "채용", "취업", "현장실습");
      case "external activity": return List.of("서포터즈", "봉사", "대외활동", "캠프");
      case "mentoring": return List.of("멘토", "멘토링", "컨설팅");
      case "seminar": return List.of("세미나", "교육", "아카데미", "강의", "부트캠프");
      case "startup": return List.of("창업", "스타트업", "예비창업");
      case "research": return List.of("연구", "r&d", "기술개발", "논문");
      case "exchange": return List.of("교환학생", "교류", "유학");
      default: return List.of();
    }
  }

  public static boolean isStudentRelevant(
      String title,
      String organization,
      String category,
      String description,
      String target,
      List<String> tags) {
    String haystack = String.join(
        " ",
        safe(title),
        safe(organization),
        safe(category),
        safe(description),
        safe(target),
        tags == null ? "" : String.join(" ", tags));
    String normalizedCategory = safe(category).trim().toLowerCase();
    return STUDENT_CATEGORIES.contains(normalizedCategory)
        || STUDENT_RELEVANT_KEYWORDS.stream().anyMatch(haystack::contains);
  }

  public static boolean hasStudentAudienceSignal(
      String title, String organization, String description, String target, List<String> tags) {
    String haystack = String.join(
        " ",
        safe(title),
        safe(organization),
        safe(description),
        safe(target),
        tags == null ? "" : String.join(" ", tags));
    return STUDENT_RELEVANT_KEYWORDS.stream().anyMatch(haystack::contains);
  }

  public static String classifyCategory(String title, String category, String content) {
    String text = String.join(" ", safe(title), safe(category), safe(content));
    if (containsAny(text, "장학", "학자금")) {
      return "Scholarship";
    }
    if (containsAny(text, "공모전", "경진대회", "해커톤", "대회", "아이디어")) {
      return "Contest";
    }
    if (containsAny(text, "인턴", "채용", "취업", "일자리", "직무")) {
      return "Internship";
    }
    if (containsAny(text, "멘토링", "멘토")) {
      return "Mentoring";
    }
    if (containsAny(text, "교육", "세미나", "강연", "특강", "부트캠프", "캠프")) {
      return "Seminar";
    }
    if (containsAny(text, "창업", "예비창업", "스타트업")) {
      return "Startup";
    }
    return "External Activity";
  }

  private static boolean containsAny(String text, String... keywords) {
    for (String keyword : keywords) {
      if (text.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
