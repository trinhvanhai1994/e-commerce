// Product Management Service - Handles product management API calls (for admin)

import { httpClient } from './http/client.js'

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
    const data = await httpClient.get('/api/dragun/products/list')
    return Array.isArray(data) ? data : data.data || []
  },

  /**
   * Create new product
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Created product
   */
  async createProduct(productData) {
    return await httpClient.post('/api/dragun/admin/products', productData)
  },

  /**
   * Update product
   * @param {number|string} id - Product ID
   * @param {Object} productData - Product data
   * @returns {Promise<Object>} Updated product
   */
  async updateProduct(id, productData) {
    return await httpClient.put('/api/dragun/admin/products', productData)
  },

  /**
   * Delete product
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Delete response
   */
  async deleteProduct(id) {
    return await httpClient.delete(`/api/dragun/admin/products/${id}`)
  },
}

export default productManagementService

