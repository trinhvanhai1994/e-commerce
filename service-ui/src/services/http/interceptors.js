// Request and Response Interceptors

import { apiConfig } from './config.js'

/**
 * Request interceptors array
 * @type {Array<Function>}
 */
const requestInterceptors = []

/**
 * Response interceptors array
 * @type {Array<Function>}
 */
const responseInterceptors = []

/**
 * Error interceptors array
 * @type {Array<Function>}
 */
const errorInterceptors = []

/**
 * Add request interceptor
 * @param {Function} interceptor - Interceptor function
 */
export function addRequestInterceptor(interceptor) {
  requestInterceptors.push(interceptor)
}

/**
 * Add response interceptor
 * @param {Function} interceptor - Interceptor function
 */
export function addResponseInterceptor(interceptor) {
  responseInterceptors.push(interceptor)
}

/**
 * Add error interceptor
 * @param {Function} interceptor - Interceptor function
 */
export function addErrorInterceptor(interceptor) {
  errorInterceptors.push(interceptor)
}

/**
 * Apply all request interceptors
 * @param {Object} config - Request configuration
 * @returns {Promise<Object>} Modified configuration
 */
export async function applyRequestInterceptors(config) {
  let modifiedConfig = { ...config }
  
  for (const interceptor of requestInterceptors) {
    modifiedConfig = await interceptor(modifiedConfig)
  }
  
  return modifiedConfig
}

/**
 * Apply all response interceptors
 * @param {Response} response - Fetch Response
 * @returns {Promise<Response>} Modified response
 */
export async function applyResponseInterceptors(response) {
  let modifiedResponse = response
  
  for (const interceptor of responseInterceptors) {
    modifiedResponse = await interceptor(modifiedResponse)
  }
  
  return modifiedResponse
}

/**
 * Apply all error interceptors
 * @param {Error} error - Error object
 * @returns {Promise<Error>} Modified error
 */
export async function applyErrorInterceptors(error) {
  let modifiedError = error
  
  for (const interceptor of errorInterceptors) {
    try {
      modifiedError = await interceptor(modifiedError)
    } catch (e) {
      // If interceptor throws, use original error
      modifiedError = error
      break
    }
  }
  
  return modifiedError
}

/**
 * Default logging interceptor (only in development)
 */
if (apiConfig.enableLogging) {
  addRequestInterceptor((config) => {
    console.log('[API Request]', {
      url: config.url,
      method: config.method || 'GET',
      headers: config.headers,
    })
    return config
  })

  addResponseInterceptor((response) => {
    console.log('[API Response]', {
      status: response.status,
      statusText: response.statusText,
      url: response.url,
    })
    return response
  })

  addErrorInterceptor((error) => {
    console.error('[API Error]', error)
    return error
  })
}

