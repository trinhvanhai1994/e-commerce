// REST API Adapter - Current implementation using fetch

import { BaseAdapter } from './base.adapter.js'
import { apiConfig } from '../config.js'
import {
  applyRequestInterceptors,
  applyResponseInterceptors,
  applyErrorInterceptors,
} from '../interceptors.js'

/**
 * REST API Adapter
 * Implements REST API calls using native fetch
 */
export class RestAdapter extends BaseAdapter {
  constructor(config = {}) {
    super()
    this.config = { ...apiConfig, ...config }
  }

  /**
   * Build full URL from endpoint
   * @param {string} endpoint - API endpoint
   * @returns {string} Full URL
   */
  buildUrl(endpoint) {
    // Remove leading slash if present
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint
    const baseURL = this.config.baseURL.replace(/\/$/, '')
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
   * Prepare request options
   * @param {Object} options - Request options
   * @returns {Object} Prepared options
   */
  prepareRequestOptions(options = {}) {
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0',
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
        throw new Error(`HTTP error! status: ${modifiedResponse.status}`)
      }

      // Parse JSON response
      const data = await modifiedResponse.json()
      
      return data
    } catch (error) {
      // Apply error interceptors
      const modifiedError = await applyErrorInterceptors(error)
      throw modifiedError
    }
  }
}

