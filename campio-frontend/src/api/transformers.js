import { resolveOpportunityLocation } from "../app/opportunityLocation.js";
import { classifyOpportunityCategory } from "../app/studentOpportunityPolicy.js";

export function normalizeOpportunity(item) {
  if (!item) return item;

  return {
    ...item,
    category: classifyOpportunityCategory(item),
    isOnline: item.isOnline ?? item.online ?? false,
    location: resolveOpportunityLocation(item),
    saved: Boolean(item.saved),
    tags: Array.isArray(item.tags) ? item.tags : [],
  };
}

export function normalizeOpportunityList(items = []) {
  return items.map(normalizeOpportunity);
}

export function normalizePost(item) {
  if (!item) return item;

  return {
    ...item,
    relatedOpportunityTitle: item.relatedOpportunityTitle || "",
  };
}

export function normalizePostList(items = []) {
  return items.map(normalizePost);
}

export function normalizeMentor(item) {
  if (!item) return item;

  return {
    ...item,
    helpTopics: Array.isArray(item.helpTopics) ? item.helpTopics : [],
  };
}

export function normalizeMentorList(items = []) {
  return items.map(normalizeMentor);
}
