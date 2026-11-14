import { httpClient } from './http/client.js'

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
  /** Approved reviews + { averageRating, reviewCount } of one product. */
  async getProductReviews(productId) {
    return unwrap(await httpClient.get(`/api/dragun/products/${productId}/reviews`))
  },

  /**
   * Customer submission (stored as PENDING until an admin approves it).
   * `images` are File objects already processed by imageSlotPipeline for slot review_image.
   * `website` is the honeypot field: always empty for real users.
   */
  async submitReview(productId, { authorName, rating, content, images = [], website = '' }) {
    const formData = new FormData()
    formData.append('authorName', authorName)
    formData.append('rating', String(rating))
    formData.append('content', content)
    formData.append('website', website)
    images.forEach((file) => formData.append('images', file))

    // Multipart goes through fetch, like contentService.uploadCmsImage (the http client sends JSON)
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'
    const response = await fetch(`${baseURL}/api/dragun/products/${productId}/reviews`, {
      method: 'POST',
      body: formData,
    })
    const data = await response.json().catch(() => ({}))
    if (!response.ok || data.success === false) {
      throw new Error(data.message || `Gửi đánh giá thất bại (${response.status})`)
    }
    return { review: data.data, message: data.message }
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
