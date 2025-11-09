import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ORDER_STATUS } from '../constants/orderStatus.js'

export const useOrderStore = defineStore('order', () => {
  const orders = ref([])

  // Generate unique order ID
  function generateOrderId() {
    return 'ORD' + Date.now() + Math.random().toString(36).substr(2, 5).toUpperCase()
  }

  // Save order to localStorage and store
  function saveOrder(orderData) {
    const orderId = generateOrderId()
    const order = {
      id: orderId,
      ...orderData,
      createdAt: new Date().toISOString(),
      status: ORDER_STATUS.ORDER_STATUS_CONFIRMED
    }

    // Add to current orders array
    orders.value.push(order)

    // Save to localStorage
    const savedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]')
    savedOrders.push(order)
    localStorage.setItem('completedOrders', JSON.stringify(savedOrders))

    return orderId
  }

  // Load orders from localStorage
  function loadOrders() {
    const savedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]')
    orders.value = savedOrders
  }

  // Get order by ID
  function getOrderById(orderId) {
    return orders.value.find(order => order.id === orderId)
  }

  // Get all orders
  function getAllOrders() {
    return orders.value
  }

  // Clear all orders (for testing)
  function clearOrders() {
    orders.value = []
    localStorage.removeItem('completedOrders')
  }

  return {
    orders,
    saveOrder,
    loadOrders,
    getOrderById,
    getAllOrders,
    clearOrders
  }
}) 