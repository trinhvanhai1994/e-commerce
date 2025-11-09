// API utility functions - Backward compatibility layer
// This file re-exports from the new service layer to maintain compatibility

// Re-export config and helper functions
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'

export const apiConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
}

// Re-export image utils for backward compatibility
export { getImageUrlWithCacheBusting } from './imageUtils.js'

// Re-export services as API objects for backward compatibility
import productService from '../services/product.service.js'
import orderService from '../services/order.service.js'
import adminService from '../services/admin.service.js'
import productManagementService from '../services/product-management.service.js'

// Product API functions (backward compatibility)
export const productAPI = {
  getProducts: () => productService.getProducts(),
  getProduct: (id) => productService.getProduct(id),
  getProductDetails: (id) => productService.getProductDetails(id),
}

// Order API functions (backward compatibility)
export const orderAPI = {
  createOrder: (orderData) => orderService.createOrder(orderData),
  getOrderById: (orderId) => orderService.getOrderById(orderId),
  getOrders: () => orderService.getOrders(),
  getOrdersByCustomer: (phone) => orderService.getOrdersByCustomer(phone),
  updateOrderStatus: (orderId, status) => orderService.updateOrderStatus(orderId, status),
}

// Admin API functions (backward compatibility)
export const adminAPI = {
  login: (credentials) => adminService.login(credentials),
  getAdminProducts: () => adminService.getAdminProducts(),
  saveProduct: (productData) => adminService.saveProduct(productData),
  deleteProduct: (productId) => adminService.deleteProduct(productId),
}

// Product management API functions (backward compatibility)
export const productManagementAPI = {
  getProducts: () => productManagementService.getProducts(),
  createProduct: (productData) => productManagementService.createProduct(productData),
  updateProduct: (id, productData) => productManagementService.updateProduct(id, productData),
  deleteProduct: (id) => productManagementService.deleteProduct(id),
}

// Legacy apiCall function - now uses new HTTP client
import { httpClient } from '../services/http/client.js'

/**
 * Legacy apiCall function for backward compatibility
 * @deprecated Use services directly instead
 */
export async function apiCall(endpoint, options = {}) {
  try {
    // Use new HTTP client
    const method = options.method || 'GET'
    const params = options.params || {}
    
    // Extract query params from endpoint if present
    const [path, queryString] = endpoint.split('?')
    if (queryString) {
      const urlParams = new URLSearchParams(queryString)
      urlParams.forEach((value, key) => {
        params[key] = value
      })
    }
    
    let response
    switch (method.toUpperCase()) {
      case 'GET':
        response = await httpClient.get(path, params)
        break
      case 'POST':
        response = await httpClient.post(path, options.body ? JSON.parse(options.body) : {})
        break
      case 'PUT':
        response = await httpClient.put(path, options.body ? JSON.parse(options.body) : {})
        break
      case 'DELETE':
        response = await httpClient.delete(path)
        break
      default:
        response = await httpClient.request(path, {
          method,
          body: options.body ? JSON.parse(options.body) : undefined,
          params,
        })
    }
    
    return response
  } catch (error) {
    throw error
  }
}
