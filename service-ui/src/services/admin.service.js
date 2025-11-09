// Admin Service - Handles all admin-related API calls

import { httpClient } from './http/client.js'
import { shouldUseMock } from './mock/mockData.js'

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
    try {
      if (shouldUseMock()) {
        // Mock login response
        if (credentials.username === 'admin' && credentials.password === 'thiyen1') {
          return {
            success: true,
            token: 'mock-token-' + Date.now(),
            user: {
              id: 1,
              name: 'Admin User',
              email: 'admin@example.com',
              role: 'admin',
            },
          }
        } else {
          throw new Error('Sai tài khoản hoặc mật khẩu!')
        }
      }
      
      const response = await httpClient.post('/api/dragun/admin/login', credentials)
      // ServiceApiAdapter extracts data from ApiResponse, so response should be LoginResponse
      // LoginResponse: { token, user: { id, name, email, role } }
      return response
    } catch (error) {
      if (shouldUseMock() && credentials.username === 'admin' && credentials.password === 'thiyen1') {
        return {
          success: true,
          token: 'mock-token-' + Date.now(),
          user: {
            id: 1,
            name: 'Admin User',
            email: 'admin@example.com',
            role: 'admin',
          },
        }
      }
      throw error
    }
  },

  /**
   * Get admin products
   * @returns {Promise<Array>} Products list
   */
  async getAdminProducts() {
    try {
      if (shouldUseMock()) {
        return []
      }
      
      const data = await httpClient.get('/api/dragun/admin/products')
      return Array.isArray(data) ? data : data.data || []
    } catch (error) {
      if (shouldUseMock()) {
        return []
      }
      throw error
    }
  },

  /**
   * Create or update product
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Product response
   */
  async saveProduct(productData) {
    try {
      if (shouldUseMock()) {
        return {
          success: true,
          product: productData,
          message: productData.id ? 'Cập nhật sản phẩm thành công' : 'Tạo sản phẩm thành công',
        }
      }
      
      const method = productData.id ? 'PUT' : 'POST'
      return await httpClient.request('/api/dragun/admin/products', {
        method,
        body: productData,
      })
    } catch (error) {
      if (shouldUseMock()) {
        return {
          success: true,
          product: productData,
          message: productData.id ? 'Cập nhật sản phẩm thành công' : 'Tạo sản phẩm thành công',
        }
      }
      throw error
    }
  },

  /**
   * Delete product
   * @param {number|string} productId - Product ID
   * @returns {Promise<Object>} Delete response
   */
  async deleteProduct(productId) {
    try {
      if (shouldUseMock()) {
        return {
          success: true,
          message: 'Xóa sản phẩm thành công',
        }
      }
      
      return await httpClient.delete(`/api/dragun/admin/products/${productId}`)
    } catch (error) {
      if (shouldUseMock()) {
        return {
          success: true,
          message: 'Xóa sản phẩm thành công',
        }
      }
      throw error
    }
  },
}

export default adminService

