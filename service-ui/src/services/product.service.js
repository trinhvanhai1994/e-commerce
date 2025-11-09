// Product Service - Handles all product-related API calls

import { httpClient } from './http/client.js'

/**
 * Product Service
 */
export const productService = {
  /**
   * Get all products
   * @returns {Promise<Array>} Products list
   */
  async getProducts() {
    const data = await httpClient.get('/api/dragun/products/list')
    return Array.isArray(data) ? data : data.data || []
  },

  /**
   * Get product by ID
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Product data
   */
  async getProduct(id) {
    return await httpClient.get(`/api/dragun/products/${id}`)
  },

  /**
   * Get product details
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Product details
   */
  async getProductDetails(id) {
    return await httpClient.get(`/api/dragun/products/${id}/details`)
  },
}

export default productService

