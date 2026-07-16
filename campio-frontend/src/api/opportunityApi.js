import { request } from "./client.js";
import { normalizeOpportunity, normalizeOpportunityList } from "./transformers.js";

export const opportunityApi = {
  list: async () => normalizeOpportunityList(await request("/api/opportunities")),
  search: async (params = {}, options = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") query.set(key, String(value));
    });
    const result = await request(`/api/opportunities/search?${query.toString()}`, options);
    return { ...result, content: normalizeOpportunityList(result.content) };
  },
  batch: async (ids = []) => {
    if (!ids.length) return [];
    const query = new URLSearchParams();
    ids.slice(0, 100).forEach((id) => query.append("ids", String(id)));
    return normalizeOpportunityList(await request(`/api/opportunities/batch?${query.toString()}`));
  },
  adminList: async () => normalizeOpportunityList(await request("/api/admin/opportunities")),
  recommended: async () => normalizeOpportunityList(await request("/api/opportunities/recommended")),
  closingSoon: async () => normalizeOpportunityList(await request("/api/opportunities/closing-soon")),
  popular: async () => normalizeOpportunityList(await request("/api/opportunities/popular")),
  detail: async (id) => normalizeOpportunity(await request(`/api/opportunities/${id}`)),
  create: (body) => request("/api/opportunities", { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) => request(`/api/opportunities/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  remove: (id) => request(`/api/opportunities/${id}`, { method: "DELETE" }),
};
