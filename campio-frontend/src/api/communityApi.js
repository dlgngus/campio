import { request } from "./client.js";
import { normalizePost, normalizePostList } from "./transformers.js";

export const communityApi = {
  listPosts: async () => normalizePostList(await request("/api/posts")),
  myPosts: async () => normalizePostList(await request("/api/posts/mine")),
  postDetail: async (id) => normalizePost(await request(`/api/posts/${id}`)),
  createPost: (body) => request("/api/posts", { method: "POST", body: JSON.stringify(body) }),
  updatePost: (id, body) => request(`/api/posts/${id}`, { method: "PATCH", body: JSON.stringify(body) }),
  deletePost: (id) => request(`/api/posts/${id}`, { method: "DELETE" }),
  savePost: (id) => request(`/api/posts/${id}/save`, { method: "POST" }),
  unsavePost: (id) => request(`/api/posts/${id}/save`, { method: "DELETE" }),
  addComment: (id, body) => request(`/api/posts/${id}/comments`, { method: "POST", body: JSON.stringify(body) }),
  listComments: (id) => request(`/api/posts/${id}/comments`),
  deleteComment: (id, commentId) => request(`/api/posts/${id}/comments/${commentId}`, { method: "DELETE" }),
};
