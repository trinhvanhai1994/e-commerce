// Product Management Service - Handles product management API calls (for admin)

import { httpClient } from './http/client.js'

/**
 * Product Management Service
 * Similar to productService but for admin operations
 */
export const productManagementService = {
  /**
   * Get all products (for admin)
   * Returns both ACTIVE and INACTIVE products (only filters deleted)
   * @returns {Promise<Array>} Products list
   */
  async getProducts() {
    // Use admin endpoint to get all products (ACTIVE + INACTIVE)
    const data = await httpClient.get('/api/dragun/admin/products')
    return Array.isArray(data) ? data : data.data || []
  },

  /**
   * Get product by ID (for admin)
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Product data
   */
  async getProductById(id) {
    // Get all products and find the one with matching ID
    const products = await this.getProducts()
    const product = products.find(p => p.id === Number(id) || p.id === String(id))
    if (!product) {
      throw new Error('Không tìm thấy sản phẩm')
    }
    return product
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
   * Delete product (soft delete)
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Delete response
   */
  async deleteProduct(id) {
    return await httpClient.delete(`/api/dragun/admin/products/${id}`)
  },

  /**
   * Toggle product status (ACTIVE/INACTIVE)
   * @param {number|string} id - Product ID
   * @returns {Promise<Object>} Updated product
   */
  async toggleProductStatus(id) {
    return await httpClient.patch(`/api/dragun/admin/products/${id}/toggle-status`)
  },

  /**
   * Upload image file
   * @param {Blob|File} file - Image file to upload
   * @param {string} path - Relative path (e.g., products/details/black/1.png)
   * @returns {Promise<string>} Full path to access the file
   */
  async uploadImage(file, path) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('path', path)

    // Use fetch directly for multipart/form-data
    const authToken = localStorage.getItem('authToken')
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'
    const url = `${baseURL}/api/dragun/admin/files/upload`

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': authToken ? `Bearer ${authToken}` : '',
      },
      body: formData,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Upload failed' }))
      throw new Error(errorData.message || `Upload failed: ${response.statusText}`)
    }

    const data = await response.json()
    // Service-api standard response format: { success, data, message }
    if (data.success !== undefined) {
      return data.success ? data.data : Promise.reject(data)
    }
    return data
  },
}

export default productManagementService

