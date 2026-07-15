import { request } from "./client.js";
import { normalizeMentor, normalizeMentorList } from "./transformers.js";

export const mentorApi = {
  list: async () => normalizeMentorList(await request("/api/mentors")),
  detail: async (id) => normalizeMentor(await request(`/api/mentors/${id}`)),
  create: (body) => request("/api/mentors", { method: "POST", body: JSON.stringify(body) }),
};
