import { request } from "./client.js";

export const authApi = {
  signup: (body) => request("/api/auth/signup", { method: "POST", body: JSON.stringify(body) }),
  login: (body) => request("/api/auth/login", { method: "POST", body: JSON.stringify(body) }),
  me: () => request("/api/auth/me"),
  logout: () => request("/api/auth/logout", { method: "POST" }),
  updateProfile: (body) => request("/api/users/profile", { method: "PATCH", body: JSON.stringify(body) }),
  updateInterests: (body) => request("/api/users/interests", { method: "PATCH", body: JSON.stringify(body) }),
  requestSchoolVerification: (body) => request("/api/users/verify-school/request", { method: "POST", body: JSON.stringify(body) }),
  verifySchool: (body) => request("/api/users/verify-school", { method: "POST", body: JSON.stringify(body) }),
};
