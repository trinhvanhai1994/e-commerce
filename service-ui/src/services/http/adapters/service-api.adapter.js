// Service-API Adapter - For future integration with service-api backend

import { BaseAdapter } from './base.adapter.js'
import { apiConfig } from '../config.js'
import {
  applyRequestInterceptors,
  applyResponseInterceptors,
  applyErrorInterceptors,
} from '../interceptors.js'

/**
 * Service-API Adapter
 * Implements API calls for service-api backend
 * This adapter can be used when service-api is ready
 */
export class ServiceApiAdapter extends BaseAdapter {
  constructor(config = {}) {
    super()
    this.config = {
      ...apiConfig,
      serviceApiUrl: config.serviceApiUrl || apiConfig.baseURL,
      ...config,
    }
  }

  /**
   * Build full URL from endpoint
   * @param {string} endpoint - API endpoint
   * @returns {string} Full URL
   */
  buildUrl(endpoint) {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint
    const baseURL = this.config.serviceApiUrl.replace(/\/$/, '')
    return `${baseURL}/${cleanEndpoint}`
  }

  /**
   * Build query string from params
   * @param {Object} params - Query parameters
   * @returns {string} Query string
   */
  buildQueryString(params) {
    if (!params || Object.keys(params).length === 0) {
      return ''
    }
    
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        searchParams.append(key, String(value))
      }
    })
    
    const queryString = searchParams.toString()
    return queryString ? `?${queryString}` : ''
  }

  /**
   * Prepare request options with service-api specific headers
   * @param {Object} options - Request options
   * @returns {Object} Prepared options
   */
  prepareRequestOptions(options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      // Add service-api specific headers here
      // 'X-API-Version': '1.0',
      // 'X-Client-Type': 'web',
    }

    const requestOptions = {
      method: options.method || 'GET',
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    }

    // Add authentication token if available
    const authToken = this.getAuthToken()
    if (authToken) {
      requestOptions.headers['Authorization'] = `Bearer ${authToken}`
    }

    // Add body for POST, PUT, PATCH
    if (options.body && ['POST', 'PUT', 'PATCH'].includes(requestOptions.method)) {
      if (typeof options.body === 'string') {
        requestOptions.body = options.body
      } else {
        requestOptions.body = JSON.stringify(options.body)
      }
    }

    return requestOptions
  }

  /**
   * Get authentication token
   * @returns {string|null} Auth token
   */
  getAuthToken() {
    // Get token from localStorage or cookie
    return localStorage.getItem('authToken') || null
  }

  /**
   * Make HTTP request
   * @param {string} endpoint - API endpoint
   * @param {Object} options - Request options
   * @returns {Promise<*>} Response data
   */
  async request(endpoint, options = {}) {
    try {
      // Build URL with query params
      const url = this.buildUrl(endpoint) + this.buildQueryString(options.params)
      
      // Prepare request options
      let requestOptions = this.prepareRequestOptions(options)
      
      // Debug: Log token status (only in development)
      if (apiConfig.enableLogging) {
        const token = this.getAuthToken()
        console.log('[API Request Debug]', {
          url,
          method: requestOptions.method,
          hasToken: !!token,
          tokenLength: token ? token.length : 0,
          authorizationHeader: requestOptions.headers['Authorization'] ? 'Present' : 'Missing'
        })
      }
      
      // Apply request interceptors
      requestOptions = await applyRequestInterceptors({
        url,
        ...requestOptions,
      })

      // Make fetch request
      const response = await fetch(requestOptions.url, {
        method: requestOptions.method,
        headers: requestOptions.headers,
        body: requestOptions.body,
      })

      // Apply response interceptors
      const modifiedResponse = await applyResponseInterceptors(response)

      // Check if response is ok
      if (!modifiedResponse.ok) {
        let errorData = {}
        try {
          const text = await modifiedResponse.text()
          if (text) {
            errorData = JSON.parse(text)
          }
        } catch (e) {
          // Response is not JSON, use status text
          errorData = { message: modifiedResponse.statusText }
        }
        
        // Create more descriptive error message
        let errorMessage = errorData.message || errorData.error || `HTTP error! status: ${modifiedResponse.status}`
        
        // Add specific messages for common status codes
        if (modifiedResponse.status === 403) {
          errorMessage = errorData.message || 'Không có quyền truy cập. Vui lòng đăng nhập lại với quyền ADMIN.'
        } else if (modifiedResponse.status === 401) {
          errorMessage = errorData.message || 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
        } else if (modifiedResponse.status === 404) {
          errorMessage = errorData.message || 'Không tìm thấy tài nguyên.'
        }
        
        const error = new Error(errorMessage)
        error.status = modifiedResponse.status
        error.data = errorData
        throw error
      }

      // Parse JSON response
      const data = await modifiedResponse.json()
      
      // Service-api standard response format: { success, data, message }
      if (data.success !== undefined) {
        return data.success ? data.data || data : Promise.reject(data)
      }
      
      return data
    } catch (error) {
      // Apply error interceptors
      const modifiedError = await applyErrorInterceptors(error)
      throw modifiedError
    }
  }
}

