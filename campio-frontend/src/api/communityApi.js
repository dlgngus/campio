import { request } from "./client.js";
import { normalizePost, normalizePostList } from "./transformers.js";

export const communityApi = {
  listPosts: async () => normalizePostList(await request("/api/posts")),
  postDetail: async (id) => normalizePost(await request(`/api/posts/${id}`)),
  createPost: (body) => request("/api/posts", { method: "POST", body: JSON.stringify(body) }),
  addComment: (id, body) => request(`/api/posts/${id}/comments`, { method: "POST", body: JSON.stringify(body) }),
  listComments: (id) => request(`/api/posts/${id}/comments`),
};
