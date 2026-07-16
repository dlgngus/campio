package com.campio.domain.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class KStartupHtmlIngestionAdapterTest {

  private final KStartupHtmlIngestionAdapter adapter =
      new KStartupHtmlIngestionAdapter(new RestTemplate());

  @Test
  void enrichesBizInfoOpportunityFromDetailFields() {
    FetchedRawOpportunity item = FetchedRawOpportunity.builder()
        .rawTitle("청년 일경험 참여기업 모집")
        .description("기업마당 공개 지원사업 공고입니다.")
        .sourceUrl("https://www.bizinfo.go.kr/detail")
        .build();
    String html = ""
        + "<span class=\"s_title\">신청기간</span><div class=\"txt\">2026.07.09 ~ 2026.07.15</div>"
        + "<span class=\"s_title\">사업개요</span><div class=\"txt\">"
        + "<p>미취업 청년에게 일경험 기회를 제공합니다.</p>"
        + "<p>☞ 마을기업, 협동조합 및 비영리단체</p>"
        + "<p>☞ 참여청년 운영비와 멘토수당 지원</p>"
        + "<p>- 운영비 월 20만원</p></div>"
        + "<span class=\"s_title\">사업신청 방법</span><div class=\"txt\">이메일 접수 (apply@example.com)</div>"
        + "<span class=\"s_title\">문의처</span><div class=\"txt\">지원부 02-1234-5678</div>";

    FetchedRawOpportunity enriched = adapter.enrichBizInfoDetail(item, html);

    assertThat(enriched.getDescription()).isEqualTo("미취업 청년에게 일경험 기회를 제공합니다.");
    assertThat(enriched.getTarget()).isEqualTo("마을기업, 협동조합 및 비영리단체");
    assertThat(enriched.getRequirements()).contains("신청기간: 2026.07.09 ~ 2026.07.15");
    assertThat(enriched.getBenefits()).contains("참여청년 운영비와 멘토수당 지원", "운영비 월 20만원");
    assertThat(enriched.getApplicationMethod()).contains("이메일 접수", "문의: 지원부");
  }
}
