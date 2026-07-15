import { request } from "./client.js";

const basePath = "/api/admin/ingestion";

export const ingestionApi = {
  listSources: () => request(`${basePath}/sources`),
  createSource: (body) => request(`${basePath}/sources`, { method: "POST", body: JSON.stringify(body) }),
  updateSource: (id, body) => request(`${basePath}/sources/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  listRawOpportunities: () => request(`${basePath}/raw-opportunities`),
  listCrawlJobs: () => request(`${basePath}/crawl-jobs`),
  createCrawlJob: (sourceId) => request(`${basePath}/crawl-jobs`, { method: "POST", body: JSON.stringify({ sourceId }) }),
  runCrawlJob: (id) => request(`${basePath}/crawl-jobs/${id}/run`, { method: "POST" }),
};
