package com.campio;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campio.domain.community.CommunityPost;
import com.campio.domain.community.CommunityPostRepository;
import com.campio.domain.ingestion.OpportunitySource;
import com.campio.domain.ingestion.OpportunitySourceRepository;
import com.campio.domain.mentor.MentorProfile;
import com.campio.domain.mentor.MentorProfileRepository;
import com.campio.domain.opportunity.Opportunity;
import com.campio.domain.opportunity.OpportunityRepository;
import com.campio.domain.user.User;
import com.campio.domain.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "campio.auth.allow-mock-user=false",
    "campio.ingestion.allow-unresolved-hosts=true",
    "campio.admin.email=admin@campio.local",
    "campio.admin.password=password"
})
class CampioApiSmokeTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private RestTemplate ingestionRestTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OpportunityRepository opportunityRepository;

  @Autowired
  private CommunityPostRepository communityPostRepository;

  @Autowired
  private MentorProfileRepository mentorProfileRepository;

  @Autowired
  private OpportunitySourceRepository opportunitySourceRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void ensureTestFixtures() {
    ensureUser("ryan@campus.edu", "Ryan", "STUDENT");
    ensureOpportunity();
    ensurePost();
    ensureMentor();
    ensureSource();
  }

  @Test
  void publicOpportunityAndCommunityApisReturnStoredDataAndMentorsRequireVerification() throws Exception {
    mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));

    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));

    mockMvc.perform(get("/api/opportunities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$[0].title", notNullValue()));

    mockMvc.perform(get("/api/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

    mockMvc.perform(get("/api/mentors"))
        .andExpect(status().isUnauthorized());

    MockHttpSession verifiedStudent = login("ryan@campus.edu", "password");
    mockMvc.perform(get("/api/mentors").session(verifiedStudent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));
  }

  @Test
  void opportunitySearchIsPagedAndAppliesDatabaseFilters() throws Exception {
    mockMvc.perform(get("/api/opportunities/search")
            .param("page", "0")
            .param("size", "1")
            .param("category", "Internship")
            .param("online", "true")
            .param("sort", "latest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.content[0].category").value("Internship"));
  }

  @Test
  void homeFeedReturnsDashboardSectionsInOneRequest() throws Exception {
    mockMvc.perform(get("/api/opportunities/home-feed"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recommended").isArray())
        .andExpect(jsonPath("$.closing").isArray())
        .andExpect(jsonPath("$.popular").isArray())
        .andExpect(jsonPath("$.latest").isArray());
  }

  @Test
  void unverifiedStudentCannotUseMentorService() throws Exception {
    User student = userRepository.findByEmail("ryan@campus.edu").orElseThrow();
    student.setVerified(false);
    userRepository.save(student);
    MockHttpSession session = login("ryan@campus.edu", "password");

    mockMvc.perform(get("/api/mentors").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("School verification is required"));

    student.setVerified(true);
    userRepository.save(student);
  }

  @Test
  void protectedMutationApisRequireLoginWhenMockUserIsDisabled() throws Exception {
    mockMvc.perform(post("/api/opportunities/1/save"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));

    mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"opportunityId\":1,"
                    + "\"type\":\"Question\","
                    + "\"title\":\"Unauthenticated post\","
                    + "\"content\":\"Should not be accepted\""
                    + "}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));

    mockMvc.perform(
            post("/api/mentors/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"company\":\"Campio\","
                    + "\"position\":\"Mentor\","
                    + "\"experience\":\"Test\","
                    + "\"helpTopics\":[\"Resume\"]"
                    + "}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));

    mockMvc.perform(
            post("/api/opportunities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Admin only\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));

    mockMvc.perform(
            post("/api/admin/ingestion/crawl-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sourceId\":1}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Login required"));
  }

  @Test
  void signupStoresAvatarUrlAndReturnsItFromSession() throws Exception {
    MvcResult signupResult =
        mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"name\":\"Avatar User\","
                        + "\"email\":\"avatar-user@campus.edu\","
                        + "\"password\":\"password\","
                        + "\"avatarUrl\":\"data:image/png;base64,abc123\""
                        + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.avatarUrl").value("data:image/png;base64,abc123"))
            .andReturn();

    MockHttpSession session = (MockHttpSession) signupResult.getRequest().getSession(false);
    mockMvc.perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("avatar-user@campus.edu"))
        .andExpect(jsonPath("$.avatarUrl").value("data:image/png;base64,abc123"));
  }

  @Test
  void adminIngestionWorkflowStoresRawRecordsAndUpdatesJobs() throws Exception {
    MockHttpSession adminSession = login("admin@campio.local", "password");

    mockMvc.perform(get("/api/admin/ingestion/sources").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)));

    MvcResult rawResult =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"sourceId\":1,"
                        + "\"externalId\":\"test-raw-1\","
                        + "\"sourceUrl\":\"https://example.com/test-raw-1\","
                        + "\"rawTitle\":\"Smoke Test Opportunity\","
                        + "\"rawContent\":\"A raw opportunity for test coverage.\","
                        + "\"rawPayload\":\"{}\""
                        + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.contentHash", notNullValue()))
            .andReturn();
    long rawId = readId(rawResult);

    mockMvc.perform(
            patch("/api/admin/ingestion/raw-opportunities/" + rawId + "/status")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"NORMALIZED\",\"normalizedOpportunityId\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("NORMALIZED"))
        .andExpect(jsonPath("$.normalizedOpportunityId").value(1));

    MvcResult publishRawResult =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"sourceId\":1,"
                        + "\"externalId\":\"test-publish-1\","
                        + "\"sourceUrl\":\"https://example.com/test-publish-1\","
                        + "\"rawTitle\":\"Publishable Smoke Test Opportunity\","
                        + "\"rawContent\":\"Publish me.\","
                        + "\"rawPayload\":\"{}\""
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long publishRawId = readId(publishRawResult);

    MvcResult publishedResult =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities/" + publishRawId + "/publish")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"title\":\"Published Smoke Test Opportunity\","
                        + "\"organization\":\"Smoke Test Organization\","
                        + "\"category\":\"Internship\","
                        + "\"description\":\"Published from raw data.\","
                        + "\"applicationMethod\":\"Apply through the Campio form.\","
                        + "\"deadline\":\"2026-12-31\","
                        + "\"tags\":[\"smoke\",\"ingestion\"]"
                        + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rawOpportunityId").value(publishRawId))
            .andExpect(jsonPath("$.rawStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.created").value(true))
            .andReturn();
    long opportunityId = objectMapper.readTree(publishedResult.getResponse().getContentAsString()).get("opportunityId").asLong();

    mockMvc.perform(get("/api/opportunities/" + opportunityId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Published Smoke Test Opportunity"))
        .andExpect(jsonPath("$.organization").value("Smoke Test Organization"))
        .andExpect(jsonPath("$.applicationMethod").value("Apply through the Campio form."));

    mockMvc.perform(
            post("/api/admin/ingestion/raw-opportunities/" + publishRawId + "/publish")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"title\":\"Published Smoke Test Opportunity Again\","
                    + "\"category\":\"Internship\","
                    + "\"deadline\":\"2026-12-31\""
                    + "}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.opportunityId").value(opportunityId))
        .andExpect(jsonPath("$.created").value(false));

    MvcResult jobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();
    long jobId = readId(jobResult);

    mockMvc.perform(
            patch("/api/admin/ingestion/crawl-jobs/" + jobId)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"SUCCESS\",\"itemsFound\":1,\"itemsCreated\":1,\"itemsUpdated\":0}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.finishedAt", notNullValue()));
  }

  @Test
  void nonAdminCannotUseIngestionAdminApis() throws Exception {
    MockHttpSession studentSession = login("ryan@campus.edu", "password");

    mockMvc.perform(get("/api/admin/ingestion/sources").session(studentSession))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Admin access required"));
  }

  @Test
  void ingestionStatusRejectsUnknownValues() throws Exception {
    MockHttpSession adminSession = login("admin@campio.local", "password");

    MvcResult jobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":1}"))
            .andExpect(status().isOk())
            .andReturn();
    long jobId = readId(jobResult);

    mockMvc.perform(
            patch("/api/admin/ingestion/crawl-jobs/" + jobId)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\",\"itemsFound\":1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid request body"));
  }

  @Test
  void rawOpportunityImportUpsertsByContentHash() throws Exception {
    MockHttpSession adminSession = login("admin@campio.local", "password");

    String payload = "{"
        + "\"sourceId\":1,"
        + "\"items\":["
        + "{"
        + "\"externalId\":\"bulk-1\","
        + "\"sourceUrl\":\"https://example.com/bulk-1\","
        + "\"rawTitle\":\"Bulk Import Opportunity\","
        + "\"rawContent\":\"Imported from a JSON adapter.\","
        + "\"rawPayload\":\"{}\""
        + "}"
        + "]"
        + "}";

    MvcResult firstImport =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities/import")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestedCount").value(1))
            .andExpect(jsonPath("$.createdCount").value(1))
            .andExpect(jsonPath("$.updatedCount").value(0))
            .andReturn();
    long firstId = objectMapper.readTree(firstImport.getResponse().getContentAsString()).get("rawOpportunityIds").get(0).asLong();

    MvcResult secondImport =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities/import")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestedCount").value(1))
            .andExpect(jsonPath("$.createdCount").value(0))
            .andExpect(jsonPath("$.updatedCount").value(1))
            .andReturn();
    long secondId = objectMapper.readTree(secondImport.getResponse().getContentAsString()).get("rawOpportunityIds").get(0).asLong();

    org.junit.jupiter.api.Assertions.assertEquals(firstId, secondId);
  }

  @Test
  void crawlJobRunnerFetchesApiAndRssSourcesIntoRawOpportunities() throws Exception {
    MockHttpSession adminSession = login("admin@campio.local", "password");
    MockRestServiceServer server = MockRestServiceServer.bindTo(ingestionRestTemplate).build();

    String apiUrl = "https://mock.campio.local/api/opportunities";
    MvcResult apiSourceResult =
        mockMvc.perform(
                post("/api/admin/ingestion/sources")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"name\":\"Mock API Source\","
                        + "\"type\":\"API\","
                        + "\"baseUrl\":\"" + apiUrl + "\","
                        + "\"categoryHint\":\"Internship\","
                        + "\"crawlIntervalMinutes\":60,"
                        + "\"robotsAllowed\":true,"
                        + "\"enabled\":true"
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long apiSourceId = readId(apiSourceResult);

    server.expect(once(), requestTo(apiUrl))
        .andRespond(withSuccess("{\"items\":[{\"id\":\"api-1\",\"title\":\"API Internship\",\"url\":\"https://example.com/api-1\",\"description\":\"API sourced item\"}]}", MediaType.APPLICATION_JSON));

    String rssUrl = "https://mock.campio.local/rss";
    server.expect(once(), requestTo(rssUrl))
        .andRespond(withSuccess("<rss><channel><item><guid>rss-1</guid><title>RSS Contest</title><link>https://example.com/rss-1</link><description>RSS sourced item</description></item></channel></rss>", MediaType.APPLICATION_XML));

    String htmlUrl = "https://www.k-startup.go.kr/mock/bizpbanc-ongoing.do";
    server.expect(once(), requestTo(htmlUrl))
        .andRespond(withSuccess(
            "<html><body>"
                + "<div id=\"bizPbancList\"><ul><li class=\"notice\">"
                + "<span class=\"flag type03\">시설ㆍ공간ㆍ보육</span>"
                + "<a href=\"javascript:go_view(178499);\"><p class=\"tit\">2026 아트코리아랩 입주기업 모집 공모</p></a>"
                + "<span class=\"list\">2026 아트코리아랩 입주기업 모집 공모</span>"
                + "<span class=\"list\">예술경영지원센터</span>"
                + "<span class=\"list\">등록일자 2026-07-10</span>"
                + "<span class=\"list\">시작일자 2026-07-13</span>"
                + "<span class=\"list\">마감일자 2026-07-30</span>"
                + "<span class=\"list\">조회 1,581</span>"
                + "</li></ul></div>"
                + "<div class=\"paginate\"><a onclick=\"fn_egov_link_page(2); return false;\">2</a></div>"
                + "</body></html>",
            MediaType.TEXT_HTML));

    server.expect(once(), requestTo(htmlUrl + "?page=2"))
        .andRespond(withSuccess(
            "<html><body>"
                + "<div id=\"bizPbancList\"><ul><li class=\"notice\">"
                + "<span class=\"flag type03\">창업교육</span>"
                + "<a href=\"javascript:go_view(178500);\"><p class=\"tit\">국민대학교 청년도약 인재양성 부트캠프 참가자 모집</p></a>"
                + "<span class=\"list\">청년도약 인재양성 부트캠프</span>"
                + "<span class=\"list\">국민대학교 글로벌창업대학원</span>"
                + "<span class=\"list\">등록일자 2026-07-10</span>"
                + "<span class=\"list\">시작일자 2026-07-09</span>"
                + "<span class=\"list\">마감일자 2026-07-31</span>"
                + "<span class=\"list\">조회 1,293</span>"
                + "</li></ul></div>"
                + "</body></html>",
            MediaType.TEXT_HTML));

    String bizInfoUrl = "https://www.bizinfo.go.kr/sii/siia/selectSIIA200View.do?rows=15&cpage=1";
    server.expect(once(), requestTo(bizInfoUrl))
        .andRespond(withSuccess(
            "<html><body><table><tbody>"
                + "<tr>"
                + "<td>1505</td>"
                + "<td>기술</td>"
                + "<td class=\"txt_l\"><a href=\"/sii/siia/selectSIIA200Detail.do?pblancId=PBLN_000000000124308\" title=\"2026년 울산시 기업지원 수혜기업 모집 공고 페이지 이동\">2026년 울산시 기업지원 수혜기업 모집 공고</a></td>"
                + "<td>2026-07-13 ~ 2026-07-27</td>"
                + "<td>울산광역시</td>"
                + "<td>울산테크노파크</td>"
                + "<td>2026-07-13</td>"
                + "<td>360</td>"
                + "</tr>"
                + "</tbody></table></body></html>",
            MediaType.TEXT_HTML));

    MvcResult apiJobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":" + apiSourceId + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long apiJobId = readId(apiJobResult);

    mockMvc.perform(post("/api/admin/ingestion/crawl-jobs/" + apiJobId + "/run").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.itemsFound").value(1))
        .andExpect(jsonPath("$.itemsCreated").value(1));

    MvcResult rssSourceResult =
        mockMvc.perform(
                post("/api/admin/ingestion/sources")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"name\":\"Mock RSS Source\","
                        + "\"type\":\"RSS\","
                        + "\"baseUrl\":\"" + rssUrl + "\","
                        + "\"categoryHint\":\"Contest\","
                        + "\"crawlIntervalMinutes\":60,"
                        + "\"robotsAllowed\":true,"
                        + "\"enabled\":true"
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long rssSourceId = readId(rssSourceResult);

    MvcResult rssJobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":" + rssSourceId + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long rssJobId = readId(rssJobResult);

    mockMvc.perform(post("/api/admin/ingestion/crawl-jobs/" + rssJobId + "/run").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.itemsFound").value(1))
        .andExpect(jsonPath("$.itemsCreated").value(1));

    MvcResult htmlSourceResult =
        mockMvc.perform(
                post("/api/admin/ingestion/sources")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"name\":\"Mock K-Startup HTML Source\","
                        + "\"type\":\"HTML\","
                        + "\"baseUrl\":\"" + htmlUrl + "\","
                        + "\"categoryHint\":\"Startup\","
                        + "\"crawlIntervalMinutes\":1440,"
                        + "\"robotsAllowed\":true,"
                        + "\"enabled\":true"
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long htmlSourceId = readId(htmlSourceResult);

    MvcResult htmlJobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":" + htmlSourceId + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long htmlJobId = readId(htmlJobResult);

    mockMvc.perform(post("/api/admin/ingestion/crawl-jobs/" + htmlJobId + "/run").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.itemsFound").value(2))
        .andExpect(jsonPath("$.itemsCreated").value(2));

    MvcResult bizInfoSourceResult =
        mockMvc.perform(
                post("/api/admin/ingestion/sources")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"name\":\"Mock Bizinfo Source\","
                        + "\"type\":\"HTML\","
                        + "\"baseUrl\":\"" + bizInfoUrl + "\","
                        + "\"categoryHint\":\"Government Support\","
                        + "\"crawlIntervalMinutes\":1440,"
                        + "\"robotsAllowed\":true,"
                        + "\"enabled\":true"
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long bizInfoSourceId = readId(bizInfoSourceResult);

    MvcResult bizInfoJobResult =
        mockMvc.perform(
                post("/api/admin/ingestion/crawl-jobs")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sourceId\":" + bizInfoSourceId + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long bizInfoJobId = readId(bizInfoJobResult);

    mockMvc.perform(post("/api/admin/ingestion/crawl-jobs/" + bizInfoJobId + "/run").session(adminSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.itemsFound").value(1))
        .andExpect(jsonPath("$.itemsCreated").value(1));

    mockMvc.perform(get("/api/opportunities"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title == '국민대학교 청년도약 인재양성 부트캠프 참가자 모집')]").isArray())
        .andExpect(jsonPath("$[?(@.title == '2026 아트코리아랩 입주기업 모집 공모')]").doesNotExist());

    server.verify();
  }

  @Test
  void rawOpportunityPublishRequiresDeadline() throws Exception {
    MockHttpSession adminSession = login("admin@campio.local", "password");

    MvcResult rawResult =
        mockMvc.perform(
                post("/api/admin/ingestion/raw-opportunities")
                    .session(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"sourceId\":1,"
                        + "\"externalId\":\"test-no-deadline\","
                        + "\"sourceUrl\":\"https://example.com/test-no-deadline\","
                        + "\"rawTitle\":\"No Deadline Opportunity\","
                        + "\"rawContent\":\"Missing deadline.\","
                        + "\"rawPayload\":\"{}\""
                        + "}"))
            .andExpect(status().isOk())
            .andReturn();
    long rawId = readId(rawResult);

    mockMvc.perform(
            post("/api/admin/ingestion/raw-opportunities/" + rawId + "/publish")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"No Deadline Opportunity\",\"category\":\"Internship\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Deadline is required before publishing"));
  }

  @Test
  void communityAndMentorMutationEndpointsWork() throws Exception {
    MockHttpSession studentSession = login("ryan@campus.edu", "password");

    MvcResult postResult =
        mockMvc.perform(
                post("/api/posts")
                    .session(studentSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{"
                        + "\"opportunityId\":1,"
                        + "\"type\":\"Question\","
                        + "\"title\":\"Smoke test post\","
                        + "\"content\":\"Initial content\""
                        + "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Smoke test post"))
            .andReturn();
    long postId = readId(postResult);

    mockMvc.perform(
            post("/api/posts/" + postId + "/comments")
                .session(studentSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Smoke test comment\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("Smoke test comment"));

    mockMvc.perform(
            patch("/api/posts/" + postId)
                .session(studentSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"opportunityId\":1,"
                    + "\"type\":\"Question\","
                    + "\"title\":\"Updated smoke test post\","
                    + "\"content\":\"Updated content\""
                    + "}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated smoke test post"));

    mockMvc.perform(delete("/api/posts/" + postId).session(studentSession))
        .andExpect(status().isNoContent());

    mockMvc.perform(
            post("/api/mentors/apply")
                .session(studentSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"
                    + "\"company\":\"Campio\","
                    + "\"position\":\"Peer Mentor\","
                    + "\"experience\":\"Application review support\","
                    + "\"helpTopics\":[\"Resume\"]"
                    + "}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.available").value(false));

    mockMvc.perform(
            post("/api/mentors/1/questions")
                .session(studentSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Can you review my application?\",\"opportunityId\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("OPEN"));
  }

  @Test
  void communitySavesExposeRealCountsAndCurrentUserState() throws Exception {
    MockHttpSession studentSession = login("ryan@campus.edu", "password");
    MvcResult created = mockMvc.perform(
            post("/api/posts").session(studentSession).contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"QUESTION\",\"title\":\"Saved post contract\",\"content\":\"Save me\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authorName").value("Ryan"))
        .andExpect(jsonPath("$.own").value(true))
        .andReturn();
    long postId = readId(created);

    mockMvc.perform(post("/api/posts/" + postId + "/save").session(studentSession))
        .andExpect(status().isNoContent());
    mockMvc.perform(get("/api/posts/" + postId).session(studentSession))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.saved").value(true))
        .andExpect(jsonPath("$.savedCount").value(1));
    mockMvc.perform(delete("/api/posts/" + postId + "/save").session(studentSession))
        .andExpect(status().isNoContent());
  }

  @Test
  void adminCanApproveMentorAndMentorCanAnswerQuestions() throws Exception {
    MockHttpSession studentSession = login("ryan@campus.edu", "password");
    MvcResult application = mockMvc.perform(
            post("/api/mentors/apply").session(studentSession).contentType(MediaType.APPLICATION_JSON)
                .content("{\"company\":\"Campio\",\"position\":\"Mentor\",\"experience\":\"Review\",\"helpTopics\":[\"Resume\"]}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false)).andReturn();
    long mentorId = readId(application);

    MockHttpSession adminSession = login("admin@campio.local", "password");
    mockMvc.perform(patch("/api/admin/mentors/" + mentorId).session(adminSession)
            .contentType(MediaType.APPLICATION_JSON).content("{\"available\":true}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));

    MvcResult question = mockMvc.perform(post("/api/mentors/" + mentorId + "/questions")
            .session(studentSession).contentType(MediaType.APPLICATION_JSON)
            .content("{\"content\":\"How should I prepare?\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OPEN")).andReturn();
    long questionId = readId(question);
    mockMvc.perform(patch("/api/mentors/questions/" + questionId + "/answer")
            .session(studentSession).contentType(MediaType.APPLICATION_JSON)
            .content("{\"answer\":\"Start with the eligibility checklist.\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ANSWERED"))
        .andExpect(jsonPath("$.answer").value("Start with the eligibility checklist."));
  }

  @Test
  void schoolVerificationUsesExpiringChallengeAndMutationOriginIsChecked() throws Exception {
    MockHttpSession studentSession = login("ryan@campus.edu", "password");
    MvcResult challenge = mockMvc.perform(post("/api/users/verify-school/request")
            .session(studentSession).contentType(MediaType.APPLICATION_JSON)
            .content("{\"schoolEmail\":\"ryan@campus.edu\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.expiresInSeconds").value(600))
        .andExpect(jsonPath("$.developmentCode", notNullValue()))
        .andReturn();
    String code = objectMapper.readTree(challenge.getResponse().getContentAsString()).get("developmentCode").asText();
    mockMvc.perform(post("/api/users/verify-school").session(studentSession)
            .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.verified").value(true));

    mockMvc.perform(post("/api/opportunities/1/save").session(studentSession)
            .header("Origin", "https://attacker.example"))
        .andExpect(status().isForbidden());
  }

  private MockHttpSession login(String email, String password) throws Exception {
    MvcResult result =
        mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
    HttpSession session = result.getRequest().getSession(false);
    return (MockHttpSession) session;
  }

  private long readId(MvcResult result) throws Exception {
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("id").asLong();
  }

  private User ensureUser(String email, String name, String role) {
    return userRepository.findByEmail(email).orElseGet(() -> {
      User user = new User();
      user.setEmail(email);
      user.setPassword(passwordEncoder.encode("password"));
      user.setName(name);
      user.setRole(role);
      user.setVerified(true);
      user.setCreatedAt(LocalDateTime.now());
      user.setUpdatedAt(LocalDateTime.now());
      return userRepository.save(user);
    });
  }

  private Opportunity ensureOpportunity() {
    if (opportunityRepository.count() > 0) {
      return opportunityRepository.findAll().get(0);
    }
    Opportunity opportunity = new Opportunity();
    opportunity.setTitle("Fixture API Opportunity");
    opportunity.setOrganization("Fixture Source");
    opportunity.setCategory("Internship");
    opportunity.setDescription("Created by test fixture.");
    opportunity.setRequirements("Eligibility and portfolio.");
    opportunity.setBenefits("Experience and mentorship.");
    opportunity.setTarget("University students");
    opportunity.setDeadline(LocalDate.of(2026, 12, 31));
    opportunity.setLocation("Online");
    opportunity.setIsOnline(true);
    opportunity.setApplyUrl("https://example.com/fixture-opportunity");
    opportunity.setStatus("PUBLISHED");
    opportunity.setTags(List.of("fixture", "api"));
    opportunity.setPopularityCount(0);
    opportunity.setRecommended(false);
    opportunity.setNewThisWeek(false);
    opportunity.setCreatedAt(LocalDateTime.now());
    opportunity.setUpdatedAt(LocalDateTime.now());
    return opportunityRepository.save(opportunity);
  }

  private void ensurePost() {
    if (communityPostRepository.count() > 0) {
      return;
    }
    User user = ensureUser("ryan@campus.edu", "Ryan", "STUDENT");
    Opportunity opportunity = ensureOpportunity();
    CommunityPost post = new CommunityPost();
    post.setUserId(user.getId());
    post.setOpportunityId(opportunity.getId());
    post.setType("Question");
    post.setTitle("Fixture question");
    post.setContent("Created by test fixture.");
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    communityPostRepository.save(post);
  }

  private void ensureMentor() {
    if (mentorProfileRepository.count() > 0) {
      mentorProfileRepository.findAll().forEach(mentor -> {
        mentor.setAvailable(true);
        mentor.setUpdatedAt(LocalDateTime.now());
        mentorProfileRepository.save(mentor);
      });
      return;
    }
    User user = ensureUser("ryan@campus.edu", "Ryan", "STUDENT");
    MentorProfile mentor = new MentorProfile();
    mentor.setUserId(user.getId());
    mentor.setCompany("Fixture Company");
    mentor.setPosition("Fixture Mentor");
    mentor.setExperience("Created by test fixture.");
    mentor.setHelpTopics(List.of("Resume"));
    mentor.setAvailable(true);
    mentor.setCreatedAt(LocalDateTime.now());
    mentor.setUpdatedAt(LocalDateTime.now());
    mentorProfileRepository.save(mentor);
  }

  private void ensureSource() {
    if (opportunitySourceRepository.count() > 0) {
      return;
    }
    OpportunitySource source = new OpportunitySource();
    source.setName("Test API Fixture Source");
    source.setType("API");
    source.setBaseUrl("https://example.com/api/opportunities");
    source.setCategoryHint("Internship");
    source.setCrawlIntervalMinutes(60);
    source.setRobotsAllowed(true);
    source.setEnabled(true);
    source.setFailureCount(0);
    source.setCreatedAt(LocalDateTime.now());
    source.setUpdatedAt(LocalDateTime.now());
    opportunitySourceRepository.save(source);
  }
}
