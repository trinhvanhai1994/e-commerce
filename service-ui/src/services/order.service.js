// Order Service - Handles all order-related API calls

import { httpClient } from './http/client.js'
import {
  shouldUseMock,
  getMockCreateOrderResponse,
  getMockOrderById,
  getMockOrders,
  getMockOrdersByCustomer,
  getMockUpdateOrderStatusResponse,
} from './mock/mockData.js'

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
    try {
      if (shouldUseMock()) {
        return getMockCreateOrderResponse(orderData)
      }
      
      return await httpClient.post('/api/extend/orders', orderData)
    } catch (error) {
      if (shouldUseMock()) {
        return getMockCreateOrderResponse(orderData)
      }
      throw error
    }
  },

  /**
   * Get order by ID
   * @param {string} orderId - Order ID
   * @returns {Promise<Object>} Order data
   */
  async getOrderById(orderId) {
    try {
      if (shouldUseMock()) {
        return getMockOrderById(orderId)
      }
      
      return await httpClient.get(`/api/extend/orders/${orderId}`)
    } catch (error) {
      if (shouldUseMock()) {
        return getMockOrderById(orderId)
      }
      throw error
    }
  },

  /**
   * Get all orders (admin)
   * @returns {Promise<Object>} Orders response
   */
  async getOrders() {
    try {
      if (shouldUseMock()) {
        return getMockOrders()
      }
      
      return await httpClient.get('/api/extend/orders')
    } catch (error) {
      if (shouldUseMock()) {
        return getMockOrders()
      }
      throw error
    }
  },

  /**
   * Get orders by customer phone
   * @param {string} phone - Customer phone number
   * @returns {Promise<Object>} Orders response
   */
  async getOrdersByCustomer(phone) {
    try {
      if (shouldUseMock()) {
        return getMockOrdersByCustomer(phone)
      }
      
      return await httpClient.get(`/api/extend/orders/customer/${phone}`)
    } catch (error) {
      if (shouldUseMock()) {
        return getMockOrdersByCustomer(phone)
      }
      throw error
    }
  },

  /**
   * Update order status
   * @param {string} orderId - Order ID
   * @param {string} status - New status
   * @returns {Promise<Object>} Update response
   */
  async updateOrderStatus(orderId, status) {
    try {
      if (shouldUseMock()) {
        return getMockUpdateOrderStatusResponse(orderId, status)
      }
      
      return await httpClient.request(`/api/extend/orders/${orderId}/status`, {
        method: 'PUT',
        params: { status },
      })
    } catch (error) {
      if (shouldUseMock()) {
        return getMockUpdateOrderStatusResponse(orderId, status)
      }
      throw error
    }
  },
}

export default orderService

