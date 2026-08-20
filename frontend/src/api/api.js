const BASE_URL = "http://localhost:8080/api";

function getToken() {
  return sessionStorage.getItem("token");
}

function authHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${getToken()}`,
  };
}

async function jsonOrThrow(response) {
  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = { message: text };
    }
  }

  if (!response.ok) {
    throw new Error(data?.error || data?.message || `Request failed with status ${response.status}`);
  }

  return data;
}

function withParams(path, params) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });
  return `${BASE_URL}${path}?${searchParams.toString()}`;
}

// ── Auth ──────────────────────────────────────────────
export const authApi = {
  login: (username, password) =>
    fetch(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    }).then((r) => r.json()),

  register: (data) =>
    fetch(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then((r) => r.json()),
};

// ── Records ───────────────────────────────────────────
export const recordsApi = {
  getAll: (page = 0, size = 9) =>
    fetch(withParams("/records", { page, size })).then((r) => r.json()),

  getById: (id) =>
    fetch(`${BASE_URL}/records/${id}`).then((r) => r.json()),

  /** Find records similar to the image already stored for this record. */
  getSimilar: (id, limit = 5) =>
    fetch(`${BASE_URL}/records/${id}/similar?limit=${limit}`).then((r) => r.json()),

  /**
   * Upload an arbitrary image file and get back visually similar records.
   * @param {File} file  - the image file to compare against
   * @param {number} limit - max results
   */
  getSimilarByImage: (file, limit = 5, regionId = null) => {
      const formData = new FormData();
      formData.append("image", file);

      const params = new URLSearchParams();
      params.append("limit", limit);
      if (regionId) params.append("regionId", regionId);

      return fetch(
          `${BASE_URL}/records/similar-by-image?${params.toString()}`,
          {
              method: "POST",
              body: formData,
          }
      ).then((r) => r.json());
  },

  search: (keyword, page = 0, size = 9) =>
    fetch(withParams("/records/search", { keyword, page, size })).then((r) => r.json()),

  filter: (regionId, categoryId, page = 0, size = 9) =>
    fetch(withParams("/records/filter", { regionId, categoryId, page, size })).then((r) => r.json()),

  create: (formData) =>
    fetch(`${BASE_URL}/records/create`, {
      method: "POST",
      headers: { Authorization: `Bearer ${getToken()}` },
      body: formData,
    }).then(jsonOrThrow),

  update: (id, data) =>
    fetch(`${BASE_URL}/admin/records/edit/${id}`, {
      method: "PUT",
      headers: authHeaders(),
      body: JSON.stringify(data),
    }).then(jsonOrThrow),

  delete: (id) =>
    fetch(`${BASE_URL}/admin/records/delete/${id}`, {
      method: "DELETE",
      headers: authHeaders(),
    }).then((r) => r.json()),
};

// ── Regions ───────────────────────────────────────────
export const regionsApi = {
  getAll: () =>
    fetch(`${BASE_URL}/admin/regions`).then((r) => r.json()),

  create: (name) =>
    fetch(`${BASE_URL}/admin/regions/create`, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ name }),
    }).then((r) => r.json()),

  delete: (id) =>
    fetch(`${BASE_URL}/admin/regions/delete/${id}`, {
      method: "DELETE",
      headers: authHeaders(),
    }).then((r) => r.json()),
};

// ── Categories ────────────────────────────────────────
export const categoriesApi = {
  getAll: () =>
    fetch(`${BASE_URL}/admin/categories`).then((r) => r.json()),

  create: (name) =>
    fetch(`${BASE_URL}/admin/categories/create`, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ name }),
    }).then((r) => r.json()),

  delete: (id) =>
    fetch(`${BASE_URL}/admin/categories/delete/${id}`, {
      method: "DELETE",
      headers: authHeaders(),
    }).then((r) => r.json()),
};

// ── Materials ─────────────────────────────────────────
export const materialsApi = {
  getAll: () =>
    fetch(`${BASE_URL}/admin/materials`, { headers: authHeaders() }).then((r) => r.json()),

  create: (name) =>
    fetch(`${BASE_URL}/admin/materials/create`, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ name }),
    }).then((r) => r.json()),

  delete: (id) =>
    fetch(`${BASE_URL}/admin/materials/delete/${id}`, {
      method: "DELETE",
      headers: authHeaders(),
    }).then((r) => r.json()),
};

// ── Techniques ────────────────────────────────────────
export const techniquesApi = {
  getAll: () =>
    fetch(`${BASE_URL}/admin/techniques`, { headers: authHeaders() }).then((r) => r.json()),

  create: (name) =>
    fetch(`${BASE_URL}/admin/techniques/create`, {
      method: "POST",
      headers: authHeaders(),
      body: JSON.stringify({ name }),
    }).then((r) => r.json()),

  delete: (id) =>
    fetch(`${BASE_URL}/admin/techniques/delete/${id}`, {
      method: "DELETE",
      headers: authHeaders(),
    }).then((r) => r.json()),
};

// ── Admin Dashboard ───────────────────────────────────
export const adminApi = {
  getDashboardStats: () =>
    fetch(`${BASE_URL}/admin/dashboard`, { headers: authHeaders() }).then((r) => r.json()),
};
