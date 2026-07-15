const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class ApiError extends Error {
  constructor(message, { status, body } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

export function isApiStatus(error, status) {
  return error instanceof ApiError && error.status === status;
}

export async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
      },
      ...options,
    });
  } catch (error) {
    throw new ApiError("Unable to reach the server. Please try again.", { status: 0, body: null });
  }

  if (!response.ok) {
    let body = null;
    try {
      body = await response.json();
    } catch {
      body = null;
    }
    const message = body?.message || `Request failed with status ${response.status}`;
    throw new ApiError(message, { status: response.status, body });
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
