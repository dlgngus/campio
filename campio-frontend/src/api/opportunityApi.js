import { request } from "./client.js";
import { normalizeOpportunity, normalizeOpportunityList } from "./transformers.js";

export const opportunityApi = {
  list: async () => normalizeOpportunityList(await request("/api/opportunities")),
  adminList: async () => normalizeOpportunityList(await request("/api/admin/opportunities")),
  recommended: async () => normalizeOpportunityList(await request("/api/opportunities/recommended")),
  closingSoon: async () => normalizeOpportunityList(await request("/api/opportunities/closing-soon")),
  popular: async () => normalizeOpportunityList(await request("/api/opportunities/popular")),
  detail: async (id) => normalizeOpportunity(await request(`/api/opportunities/${id}`)),
  create: (body) => request("/api/opportunities", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) => request(`/api/opportunities/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  remove: (id) => request(`/api/opportunities/${id}`, { method: "DELETE" }),
};
