import { request } from "./client.js";
import { normalizeOpportunityList } from "./transformers.js";

export const savedApi = {
  list: async () => normalizeOpportunityList(await request("/api/saved")),
  save: (id) => request(`/api/opportunities/${id}/save`, { method: "POST" }),
  unsave: (id) => request(`/api/opportunities/${id}/save`, { method: "DELETE" }),
};
