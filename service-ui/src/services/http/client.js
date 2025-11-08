// HTTP Client - Main entry point for API calls

import { apiConfig } from './config.js'
import { RestAdapter } from './adapters/rest.adapter.js'
import { ServiceApiAdapter } from './adapters/service-api.adapter.js'

/**
 * HTTP Client Factory
 * Creates appropriate adapter based on configuration
 */
class HttpClient {
  constructor() {
    this.adapter = null
    this.initializeAdapter()
  }

  /**
   * Initialize adapter based on config
   */
  initializeAdapter() {
    // Check if service-api adapter should be used
    const useServiceApi = import.meta.env.VITE_USE_SERVICE_API === 'true'
    
    if (useServiceApi) {
      this.adapter = new ServiceApiAdapter({
        serviceApiUrl: import.meta.env.VITE_SERVICE_API_URL || apiConfig.baseURL,
      })
    } else {
      this.adapter = new RestAdapter()
    }
  }

  /**
   * Switch to a different adapter
   * @param {BaseAdapter} adapter - Adapter instance
   */
  setAdapter(adapter) {
    this.adapter = adapter
  }

  /**
   * Get current adapter
   * @returns {BaseAdapter} Current adapter
   */
  getAdapter() {
    return this.adapter
  }

  /**
   * Make HTTP request
   * @param {string} endpoint - API endpoint
   * @param {Object} options - Request options
   * @returns {Promise<*>} Response data
   */
  async request(endpoint, options = {}) {
    return this.adapter.request(endpoint, options)
  }

  /**
   * GET request
   * @param {string} endpoint - API endpoint
   * @param {Object} [params] - Query parameters
   * @returns {Promise<*>} Response data
   */
  async get(endpoint, params = {}) {
    return this.adapter.get(endpoint, params)
  }

  /**
   * POST request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async post(endpoint, data = {}) {
    return this.adapter.post(endpoint, data)
  }

  /**
   * PUT request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async put(endpoint, data = {}) {
    return this.adapter.put(endpoint, data)
  }

  /**
   * DELETE request
   * @param {string} endpoint - API endpoint
   * @returns {Promise<*>} Response data
   */
  async delete(endpoint) {
    return this.adapter.delete(endpoint)
  }

  /**
   * PATCH request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async patch(endpoint, data = {}) {
    return this.adapter.patch(endpoint, data)
  }
}

// Export singleton instance
export const httpClient = new HttpClient()

// Export class for custom instances
export { HttpClient }

// Export adapters for direct use if needed
export { RestAdapter, ServiceApiAdapter } from './adapters/index.js'

