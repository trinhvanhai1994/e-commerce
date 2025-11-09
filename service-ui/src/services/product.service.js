// Product Service - Handles all product-related API calls

import { httpClient } from './http/client.js'
import { shouldUseMock, getMockProducts, getMockProduct } from './mock/mockData.js'

/**
 * Product Service
 */
export const productService = {
  /**
   * Get all products
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
   * Get product by ID
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Product data
   */
  async getProduct(id) {
    try {
      if (shouldUseMock()) {
        return getMockProduct(id)
      }
      
      return await httpClient.get(`/api/dragun/products/${id}`)
    } catch (error) {
      if (shouldUseMock()) {
        return getMockProduct(id)
      }
      throw error
    }
  },

  /**
   * Get product details
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Product details
   */
  async getProductDetails(id) {
    try {
      if (shouldUseMock()) {
        return getMockProduct(id)
      }
      
      return await httpClient.get(`/api/dragun/products/${id}/details`)
    } catch (error) {
      if (shouldUseMock()) {
        return getMockProduct(id)
      }
      throw error
    }
  },
}

export default productService

