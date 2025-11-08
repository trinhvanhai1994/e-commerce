// Response Type Definitions

/**
 * Standard API Response Structure
 * @typedef {Object} ApiResponse
 * @property {boolean} success - Whether the request was successful
 * @property {*} data - Response data
 * @property {string} [message] - Response message
 * @property {Object} [error] - Error information if failed
 * @property {number} [statusCode] - HTTP status code
 */

/**
 * Paginated Response
 * @typedef {Object} PaginatedResponse
 * @property {Array} items - Array of items
 * @property {number} total - Total number of items
 * @property {number} page - Current page number
 * @property {number} pageSize - Items per page
 * @property {number} totalPages - Total number of pages
 */

/**
 * Error Response
 * @typedef {Object} ErrorResponse
 * @property {boolean} success - Always false
 * @property {string} message - Error message
 * @property {string} [code] - Error code
 * @property {Object} [details] - Additional error details
 */

/**
 * Creates a standard success response
 * @param {*} data - Response data
 * @param {string} [message] - Success message
 * @returns {ApiResponse}
 */
export function createSuccessResponse(data, message = 'Success') {
  return {
    success: true,
    data,
    message,
  }
}

/**
 * Creates a standard error response
 * @param {string} message - Error message
 * @param {string} [code] - Error code
 * @param {Object} [details] - Additional error details
 * @returns {ErrorResponse}
 */
export function createErrorResponse(message, code = 'UNKNOWN_ERROR', details = {}) {
  return {
    success: false,
    message,
    code,
    details,
  }
}

