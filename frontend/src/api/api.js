const BASE_URL = "http://localhost:8080/api";

function getToken() {
  return localStorage.getItem("token");
}

function authHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${getToken()}`,
  };
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
  getAll: () =>
    fetch(`${BASE_URL}/records`).then((r) => r.json()),

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
  getSimilarByImage: (file, limit = 5) => {
    const formData = new FormData();
    formData.append("image", file);
    return fetch(`${BASE_URL}/records/similar-by-image?limit=${limit}`, {
      method: "POST",
      body: formData,
    }).then((r) => r.json());
  },

  search: (keyword) =>
    fetch(`${BASE_URL}/records/search?keyword=${encodeURIComponent(keyword)}`).then((r) => r.json()),

  filter: (regionId, categoryId) =>
    fetch(`${BASE_URL}/records/filter?regionId=${regionId || ""}&categoryId=${categoryId || ""}`).then((r) => r.json()),

  create: (formData) =>
    fetch(`${BASE_URL}/records/create`, {
      method: "POST",
      headers: { Authorization: `Bearer ${getToken()}` },
      body: formData,
    }).then((r) => r.json()),

  update: (id, data) =>
    fetch(`${BASE_URL}/admin/records/edit/${id}`, {
      method: "PUT",
      headers: authHeaders(),
      body: JSON.stringify(data),
    }).then((r) => r.json()),

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