const REGION_ALIASES = [
  ["서울특별시", "서울특별시"],
  ["부산광역시", "부산광역시"],
  ["대구광역시", "대구광역시"],
  ["인천광역시", "인천광역시"],
  ["광주광역시", "광주광역시"],
  ["대전광역시", "대전광역시"],
  ["울산광역시", "울산광역시"],
  ["세종특별자치시", "세종특별자치시"],
  ["경기도", "경기도"],
  ["강원특별자치도", "강원특별자치도"],
  ["충청북도", "충청북도"],
  ["충청남도", "충청남도"],
  ["전북특별자치도", "전북특별자치도"],
  ["전라북도", "전북특별자치도"],
  ["전라남도", "전라남도"],
  ["경상북도", "경상북도"],
  ["경상남도", "경상남도"],
  ["제주특별자치도", "제주특별자치도"],
  ["서울", "서울특별시"],
  ["부산", "부산광역시"],
  ["대구", "대구광역시"],
  ["인천", "인천광역시"],
  ["광주", "광주광역시"],
  ["대전", "대전광역시"],
  ["울산", "울산광역시"],
  ["세종", "세종특별자치시"],
  ["경기", "경기도"],
  ["강원", "강원특별자치도"],
  ["충북", "충청북도"],
  ["충남", "충청남도"],
  ["전북", "전북특별자치도"],
  ["전남", "전라남도"],
  ["경북", "경상북도"],
  ["경남", "경상남도"],
  ["제주", "제주특별자치도"],
];

function isNationwide(location) {
  return !location || location === "Nationwide" || location === "전국";
}

function compact(value) {
  return String(value || "").replace(/\s+/g, "");
}

export function resolveOpportunityLocation(opportunity) {
  if (!isNationwide(opportunity?.location)) {
    return opportunity.location;
  }

  const tags = Array.isArray(opportunity?.tags) ? opportunity.tags : [];
  const candidates = [
    ...tags,
    opportunity?.title,
    opportunity?.organization,
  ].filter(Boolean);

  for (const candidate of candidates) {
    const normalized = compact(candidate);
    if (!normalized) continue;
    for (const [needle, label] of REGION_ALIASES) {
      if (normalized.includes(needle)) {
        return label;
      }
    }
  }

  return opportunity?.location || "Nationwide";
}

export function visibleOpportunityTags(opportunity, limit = 3) {
  const location = resolveOpportunityLocation(opportunity);
  const tags = Array.isArray(opportunity?.tags) ? opportunity.tags : [];
  return tags
    .filter((tag) => tag !== location)
    .filter((tag) => resolveOpportunityLocation({ location: "Nationwide", tags: [tag] }) !== location)
    .slice(0, limit);
}
