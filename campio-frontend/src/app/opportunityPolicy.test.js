import test from "node:test";
import assert from "node:assert/strict";
import { classifyOpportunityCategory, isStudentRelevantOpportunity } from "./studentOpportunityPolicy.js";
import { resolveOpportunityLocation, visibleOpportunityTags } from "./opportunityLocation.js";

test("recognizes English student opportunity categories", () => {
  assert.equal(isStudentRelevantOpportunity({ category: "Internship", title: "Backend role" }), true);
  assert.equal(isStudentRelevantOpportunity({ category: "Government Support", title: "Factory equipment subsidy" }), false);
});

test("classifies broad imported categories from their content", () => {
  assert.equal(classifyOpportunityCategory({ category: "Government Support", title: "대학생 해커톤 참가자 모집" }), "Contest");
  assert.equal(classifyOpportunityCategory({ category: "기타", description: "청년 인턴 현장실습" }), "Internship");
});

test("derives region from tags without repeating it as a visible tag", () => {
  const opportunity = { location: "Nationwide", tags: ["서울", "인턴", "개발"] };
  assert.equal(resolveOpportunityLocation(opportunity), "서울특별시");
  assert.deepEqual(visibleOpportunityTags(opportunity), ["인턴", "개발"]);
});
