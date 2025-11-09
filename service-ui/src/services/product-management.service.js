// Product Management Service - Handles product management API calls (for admin)

import { httpClient } from './http/client.js'
import { shouldUseMock, getMockProducts, getMockProduct } from './mock/mockData.js'

/**
 * Product Management Service
 * Similar to productService but for admin operations
 */
export const productManagementService = {
  /**
   * Get all products (for admin)
   * @returns {Promise<Array>} Products list
   */
  async getProducts() {
    try {
      if (shouldUseMock()) {
        return getMockProducts()
      }
      
      const data = await httpClient.get('/api/dragun/products/list')
      return Array.isArray(data) ? data : data.data || []
    } catch (error) {
      if (shouldUseMock()) {
        return getMockProducts()
      }
      throw error
    }
  },

  /**
   * Create new product
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Created product
   */
  async createProduct(productData) {
    try {
      if (shouldUseMock()) {
        return {
          success: true,
          product: { ...productData, id: Date.now() },
          message: 'Tạo sản phẩm thành công',
        }
      }
      
      return await httpClient.post('/api/dragun/admin/products', productData)
    } catch (error) {
      if (shouldUseMock()) {
        return {
          success: true,
          product: { ...productData, id: Date.now() },
          message: 'Tạo sản phẩm thành công',
        }
      }
      throw error
    }
  },

  /**
   * Update product
   * @param {number|string} id - Product ID
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Updated product
   */
  async updateProduct(id, productData) {
    try {
      if (shouldUseMock()) {
        return {
          success: true,
          product: { ...productData, id },
          message: 'Cập nhật sản phẩm thành công',
        }
      }
      
      return await httpClient.put('/api/dragun/admin/products', productData)
    } catch (error) {
      if (shouldUseMock()) {
        return {
          success: true,
          product: { ...productData, id },
          message: 'Cập nhật sản phẩm thành công',
        }
      }
      throw error
    }
  },

  /**
   * Delete product
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Delete response
   */
  async deleteProduct(id) {
    try {
      if (shouldUseMock()) {
        return {
          success: true,
          message: 'Xóa sản phẩm thành công',
        }
      }
      
      return await httpClient.delete(`/api/dragun/admin/products/${id}`)
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

export default productManagementService

