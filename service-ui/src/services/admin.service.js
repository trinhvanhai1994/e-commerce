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
    const response = await httpClient.post('/api/thiyen/admin/login', credentials)
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

  /**
   * Logout - Clear authentication data
   * Note: This is a client-side logout. Server-side token invalidation
   * would require a logout endpoint on the server.
   */
  logout() {
    // Clear all authentication data
    localStorage.removeItem('authToken')
    localStorage.removeItem('adminUser')
    localStorage.removeItem('isAdmin')
  },

  /**
   * Check if user is authenticated
   * Validates JWT token by checking:
   * 1. Token exists in localStorage
   * 2. Token has valid JWT format (3 parts separated by dots)
   * 3. Token is not expired (if can be decoded)
   * @returns {boolean} True if authenticated
   */
  isAuthenticated() {
    try {
      const token = localStorage.getItem('authToken')
      
      // No token = not authenticated
      if (!token || typeof token !== 'string' || token.trim() === '') {
        return false
      }
      
      // JWT token must have 3 parts separated by dots
      const parts = token.split('.')
      if (parts.length !== 3) {
        this.logout()
        return false
      }
      
      // Try to decode JWT and check expiry
      try {
        const payload = JSON.parse(atob(parts[1]))
        const exp = payload.exp
        
        // Check if token is expired
        if (exp && exp * 1000 < Date.now()) {
          this.logout()
          return false
        }
        
        // Token is valid
        return true
      } catch (e) {
        // If token can't be decoded, it's invalid
        this.logout()
        return false
      }
    } catch (error) {
      console.error('🔐 isAuthenticated: Error checking authentication:', error)
      return false
    }
  },

  /**
   * Get current admin user info
   * @returns {Object|null} User info or null
   */
  getCurrentUser() {
    const userStr = localStorage.getItem('adminUser')
    return userStr ? JSON.parse(userStr) : null
  },

  /**
   * Get dashboard statistics
   * @returns {Promise<Object>} Dashboard stats response
   */
  async getDashboardStats() {
    const data = await httpClient.get('/api/dragun/admin/dashboard/stats')
    return data
  },
}

export default adminService

