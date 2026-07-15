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

export function isStudentRelevantOpportunity(opportunity) {
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
