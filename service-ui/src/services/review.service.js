import { httpClient } from './http/client.js'
import { customerFetch } from './customer.service.js'

function unwrap(data) {
  if (data && typeof data === 'object' && 'success' in data) {
    if (!data.success) {
      throw new Error(data.message || 'Request failed')
    }
    return data.data
  }
  return data
}

function query(params) {
  const q = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== '')
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&')
  return q ? `?${q}` : ''
}

export const reviewService = {
  /**
   * Approved reviews + { averageRating, reviewCount } of one product.
   * `fresh` bypasses the 30s browser cache (Cache-Control public) right after the customer posted a review.
   */
  async getProductReviews(productId, { fresh = false } = {}) {
    const suffix = fresh ? `?_=${Date.now()}` : ''
    return unwrap(await httpClient.get(`/api/dragun/products/${productId}/reviews${suffix}`))
  },

  /**
   * Logged-in customer's review (task-003), published immediately (APPROVED); admin edits or hides it later.
   * The author name comes from the account, so the form does not send one.
   * `images` are File objects already processed by imageSlotPipeline for slot review_image.
   * `website` is the honeypot field: always empty for real users.
   * `token` is the customer access token: call through authStore.authorized(...) so a 401 refreshes it.
   * Errors carry `status` (401 = log in again, 409 = already reviewed).
   */
  async submitReview(productId, { rating, content, images = [], website = '' }, token) {
    const formData = new FormData()
    formData.append('rating', String(rating))
    formData.append('content', content)
    formData.append('website', website)
    images.forEach((file) => formData.append('images', file))

    // Multipart goes through fetch (the http client sends JSON)
    const { data, message } = await customerFetch(`/api/dragun/products/${productId}/reviews`, {
      method: 'POST',
      body: formData,
      token,
    })
    return { review: data, message }
  },

  // ----- admin -----

  async getAdminReviews({ productId, status, page = 0, size = 20 } = {}) {
    return unwrap(await httpClient.get(`/api/dragun/admin/reviews${query({ productId, status, page, size })}`))
  },

  async getAdminReview(id) {
    return unwrap(await httpClient.get(`/api/dragun/admin/reviews/${id}`))
  },

  async createReview(payload) {
    return unwrap(await httpClient.post('/api/dragun/admin/reviews', payload))
  },

  async updateReview(id, payload) {
    return unwrap(await httpClient.put(`/api/dragun/admin/reviews/${id}`, payload))
  },

  async updateReviewStatus(id, status) {
    return unwrap(await httpClient.patch(`/api/dragun/admin/reviews/${id}/status`, { status }))
  },

  async deleteReview(id) {
    return unwrap(await httpClient.delete(`/api/dragun/admin/reviews/${id}`))
  },
}

export default reviewService
