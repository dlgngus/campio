import { request } from "./client.js";

export const applicationApi = {
  list: () => request("/api/applications"),
  saveForOpportunity: (opportunityId, body) =>
    request(`/api/opportunities/${opportunityId}/apply-record`, { method: "POST", body: JSON.stringify(body) }),
  update: (id, body) => request(`/api/applications/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
};
