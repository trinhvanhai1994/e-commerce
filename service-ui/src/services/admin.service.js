// Admin Service - Handles all admin-related API calls

import { httpClient } from './http/client.js'

/**
 * Admin Service
 */
export const adminService = {
  /**
   * Admin login
   * @param {Object} credentials - Login credentials { username, password }
   * @returns {Promise<Object>} Login response
   */
  async login(credentials) {
    const response = await httpClient.post('/api/dragun/admin/login', credentials)
    // ServiceApiAdapter extracts data from ApiResponse, so response should be LoginResponse
    // LoginResponse: { token, user: { id, name, email, role } }
    return response
  },

  /**
   * Get admin products
   * @returns {Promise<Array>} Products list
   */
  async getAdminProducts() {
    const data = await httpClient.get('/api/dragun/admin/products')
    return Array.isArray(data) ? data : data.data || []
  },

  /**
   * Create or update product
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Product response
   */
  async saveProduct(productData) {
    const method = productData.id ? 'PUT' : 'POST'
    return await httpClient.request('/api/dragun/admin/products', {
      method,
      body: productData,
    })
  },

  /**
   * Delete product
   * @param {number|string} productId - Product ID
   * @returns {Promise<Object>} Delete response
   */
  async deleteProduct(productId) {
    return await httpClient.delete(`/api/dragun/admin/products/${productId}`)
  },
}

export default adminService

