const STUDENT_RELEVANT_KEYWORDS = [
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
  "부트캠프",
];

const STUDENT_CATEGORIES = new Set([
  "internship", "contest", "competition", "external activity", "scholarship", "exchange",
  "research", "startup", "seminar", "mentoring", "campus event", "full-time job",
]);

export function isStudentRelevantOpportunity(opportunity) {
  if (STUDENT_CATEGORIES.has(String(opportunity?.category || "").trim().toLowerCase())) {
    return true;
  }
  const text = [
    opportunity?.title,
    opportunity?.organization,
    opportunity?.category,
    opportunity?.description,
    opportunity?.target,
    ...(Array.isArray(opportunity?.tags) ? opportunity.tags : []),
  ]
    .filter(Boolean)
    .join(" ");

  return STUDENT_RELEVANT_KEYWORDS.some((keyword) => text.includes(keyword));
}

export function classifyOpportunityCategory(opportunity) {
  const current = opportunity?.category;
  if (current && current !== "Government Support" && current !== "기타") {
    return current;
  }

  const text = [
    opportunity?.title,
    opportunity?.organization,
    opportunity?.description,
    opportunity?.target,
    ...(Array.isArray(opportunity?.tags) ? opportunity.tags : []),
  ]
    .filter(Boolean)
    .join(" ");

  if (/장학|학자금|등록금/.test(text)) return "Scholarship";
  if (/공모전|경진대회|해커톤|챌린지|대회/.test(text)) return "Contest";
  if (/인턴|일경험|채용|취업|직무|현장실습/.test(text)) return "Internship";
  if (/서포터즈|봉사|대외활동|캠프/.test(text)) return "External Activity";
  if (/멘토|멘토링|컨설팅/.test(text)) return "Mentoring";
  if (/세미나|교육|아카데미|강의|부트캠프|수강생|교육생/.test(text)) return "Seminar";
  if (/창업|스타트업|예비창업/.test(text)) return "Startup";
  if (/연구|R&D|기술개발|논문/.test(text)) return "Research";
  return current || "External Activity";
}
