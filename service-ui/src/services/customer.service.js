// Customer accounts (task-003): register, login, session refresh, profile, password, my orders.
// Calls go through fetch, not httpClient: the session logic needs the HTTP status (401 = refresh or log in again)
// and the server's message on every error, and the REST adapter drops both. The customer token is sent only on
// these calls (and on review/order calls that pass it explicitly), never on admin APIs.

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678').replace(/\/$/, '')

/**
 * @param {string} path - API path starting with /api/
 * @param {{method?: string, body?: object|FormData, token?: string}} options
 * @returns {Promise<{data: *, message: string}>}
 * @throws {Error} with `status` (0 when the network failed) and the server's message
 */
export async function customerFetch(path, { method = 'GET', body, token } = {}) {
  const headers = { Accept: 'application/json' }
  const isForm = typeof FormData !== 'undefined' && body instanceof FormData
  if (body !== undefined && !isForm) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : isForm ? body : JSON.stringify(body),
    })
  } catch (e) {
    const error = new Error('Không kết nối được máy chủ. Vui lòng kiểm tra mạng và thử lại.')
    error.status = 0
    throw error
  }
  const payload = await response.json().catch(() => ({}))
  if (!response.ok || payload.success === false) {
    const error = new Error(payload.message || `Có lỗi xảy ra (${response.status})`)
    error.status = response.status
    throw error
  }
  return { data: payload.data, message: payload.message }
}

export const customerService = {
  async register({ phone, fullName, email, password, consent, website = '' }) {
    return (await customerFetch('/api/dragun/auth/register', {
      method: 'POST',
      body: { phone, fullName, email, password, consent, website },
    })).data
  },

  async login({ phone, password }) {
    return (await customerFetch('/api/dragun/auth/login', { method: 'POST', body: { phone, password } })).data
  },

  async refresh(refreshToken) {
    return (await customerFetch('/api/dragun/auth/refresh', { method: 'POST', body: { refreshToken } })).data
  },

  async logout(refreshToken) {
    await customerFetch('/api/dragun/auth/logout', { method: 'POST', body: { refreshToken } })
  },

  // ----- needs the access token (use authStore.authorized, which refreshes on 401) -----

  async getProfile(token) {
    return (await customerFetch('/api/dragun/account/profile', { token })).data
  },

  async updateProfile(token, profile) {
    return customerFetch('/api/dragun/account/profile', { method: 'PUT', body: profile, token })
  },

  async changePassword(token, { currentPassword, newPassword }) {
    return customerFetch('/api/dragun/account/password', {
      method: 'PUT',
      body: { currentPassword, newPassword },
      token,
    })
  },

  async getMyOrders(token) {
    return (await customerFetch('/api/dragun/account/orders', { token })).data || []
  },
}

export default customerService
