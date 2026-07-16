import { request } from "./client.js";
import { normalizeMentor, normalizeMentorList } from "./transformers.js";

export const mentorApi = {
  list: async () => normalizeMentorList(await request("/api/mentors")),
  detail: async (id) => normalizeMentor(await request(`/api/mentors/${id}`)),
  apply: (body) => request("/api/mentors/apply", { method: "POST", body: JSON.stringify(body) }),
  askQuestion: (id, body) => request(`/api/mentors/${id}/questions`, { method: "POST", body: JSON.stringify(body) }),
  myQuestions: () => request("/api/mentors/questions/mine"),
  receivedQuestions: () => request("/api/mentors/questions/received"),
  answerQuestion: (id, body) => request(`/api/mentors/questions/${id}/answer`, { method: "PATCH", body: JSON.stringify(body) }),
  adminList: async () => normalizeMentorList(await request("/api/admin/mentors")),
  setApproval: (id, available) => request(`/api/admin/mentors/${id}`, { method: "PATCH", body: JSON.stringify({ available }) }),
};
