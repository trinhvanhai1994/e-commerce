// Request Type Definitions

/**
 * Standard API Request Options
 * @typedef {Object} ApiRequestOptions
 * @property {string} [method] - HTTP method (GET, POST, PUT, DELETE, etc.)
 * @property {Object} [headers] - Custom headers
 * @property {Object|string} [body] - Request body
 * @property {Object} [params] - Query parameters
 * @property {number} [timeout] - Request timeout in milliseconds
 */

/**
 * Request Interceptor Function
 * @typedef {Function} RequestInterceptor
 * @param {ApiRequestOptions} config - Request configuration
 * @returns {ApiRequestOptions|Promise<ApiRequestOptions>} Modified configuration
 */

/**
 * Response Interceptor Function
 * @typedef {Function} ResponseInterceptor
 * @param {Response} response - Fetch Response object
 * @returns {Response|Promise<Response>} Modified response
 */

/**
 * Error Interceptor Function
 * @typedef {Function} ErrorInterceptor
 * @param {Error} error - Error object
 * @returns {Error|Promise<Error>} Modified error or throw
 */

export const RequestMethods = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
  PATCH: 'PATCH',
}

