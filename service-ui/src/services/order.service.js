// Order Service - Handles all order-related API calls

import { httpClient } from './http/client.js'

/**
 * Order Service
 */
export const orderService = {
  /**
   * Create new order
   * @param {Object} orderData - Order data
   * @returns {Promise<Object>} Created order response
   */
  async createOrder(orderData) {
    return await httpClient.post('/api/extend/orders', orderData)
  },

  /**
   * Get order by ID
   * @param {string} orderId - Order ID
   * @returns {Promise<Object>} Order data
   */
  async getOrderById(orderId) {
    return await httpClient.get(`/api/extend/orders/${orderId}`)
  },

  /**
   * Get all orders (admin)
   * @returns {Promise<Object>} Orders response
   */
  async getOrders() {
    return await httpClient.get('/api/extend/orders')
  },

  /**
   * Get orders by customer phone
   * @param {string} phone - Customer phone number
   * @returns {Promise<Object>} Orders response
   */
  async getOrdersByCustomer(phone) {
    return await httpClient.get(`/api/extend/orders/customer/${phone}`)
  },

  /**
   * Update order status
   * @param {string} orderId - Order ID
   * @param {string} status - New status (will be normalized to UPPERCASE)
   * @returns {Promise<Object>} Update response
   */
  async updateOrderStatus(orderId, status) {
    // Normalize status to UPPERCASE
    let normalizedStatus = status
    if (normalizedStatus) {
      normalizedStatus = String(normalizedStatus).toUpperCase().trim()
    }
    
    return await httpClient.put(`/api/extend/orders/${orderId}/status`, { status: normalizedStatus })
  },
}

export default orderService

