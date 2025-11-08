// Base Adapter Interface for API implementations

/**
 * Base Adapter Class
 * All API adapters should extend this class
 */
export class BaseAdapter {
  /**
   * Make HTTP request
   * @param {string} endpoint - API endpoint
   * @param {Object} options - Request options
   * @returns {Promise<*>} Response data
   */
  async request(endpoint, options = {}) {
    throw new Error('request() method must be implemented by adapter')
  }

  /**
   * GET request
   * @param {string} endpoint - API endpoint
   * @param {Object} [params] - Query parameters
   * @returns {Promise<*>} Response data
   */
  async get(endpoint, params = {}) {
    return this.request(endpoint, { method: 'GET', params })
  }

  /**
   * POST request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async post(endpoint, data = {}) {
    return this.request(endpoint, { method: 'POST', body: data })
  }

  /**
   * PUT request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async put(endpoint, data = {}) {
    return this.request(endpoint, { method: 'PUT', body: data })
  }

  /**
   * DELETE request
   * @param {string} endpoint - API endpoint
   * @returns {Promise<*>} Response data
   */
  async delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' })
  }

  /**
   * PATCH request
   * @param {string} endpoint - API endpoint
   * @param {Object} [data] - Request body
   * @returns {Promise<*>} Response data
   */
  async patch(endpoint, data = {}) {
    return this.request(endpoint, { method: 'PATCH', body: data })
  }
}

