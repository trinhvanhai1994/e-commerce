/**
 * Order Status Constants
 * Matches OrderStatus enum in service-api
 * 
 * Enum values from service-api:
 * - ORDER_STATUS_PENDING = "PENDING"
 * - ORDER_STATUS_CONFIRMED = "CONFIRMED"
 * - ORDER_STATUS_SHIPPING = "SHIPPING"
 * - ORDER_STATUS_DELIVERED = "DELIVERED"
 * - ORDER_STATUS_CANCELLED = "CANCELLED"
 */

export const ORDER_STATUS = {
  ORDER_STATUS_PENDING: 'PENDING',
  ORDER_STATUS_CONFIRMED: 'CONFIRMED',
  ORDER_STATUS_SHIPPING: 'SHIPPING',
  ORDER_STATUS_DELIVERED: 'DELIVERED',
  ORDER_STATUS_CANCELLED: 'CANCELLED'
}

/**
 * Get status display text in Vietnamese
 * @param {string} status - Order status value
 * @returns {string} Display text
 */
export function getStatusText(status) {
  const statusMap = {
    [ORDER_STATUS.ORDER_STATUS_PENDING]: 'Chờ xác nhận',
    [ORDER_STATUS.ORDER_STATUS_CONFIRMED]: 'Đã xác nhận',
    [ORDER_STATUS.ORDER_STATUS_SHIPPING]: 'Đang giao',
    [ORDER_STATUS.ORDER_STATUS_DELIVERED]: 'Đã giao',
    [ORDER_STATUS.ORDER_STATUS_CANCELLED]: 'Đã hủy',
    // Backward compatibility with lowercase
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'SHIPPING': 'Đang giao',
    'DELIVERED': 'Đã giao',
    'CANCELLED': 'Đã hủy',
    '': 'Chờ xác nhận'
  }
  return statusMap[status] || 'Chờ xác nhận'
}

/**
 * Get status CSS class for styling
 * @param {string} status - Order status value
 * @returns {string} CSS class
 */
export function getStatusClass(status) {
  const classMap = {
    [ORDER_STATUS.ORDER_STATUS_PENDING]: 'bg-yellow-100 text-yellow-800',
    [ORDER_STATUS.ORDER_STATUS_CONFIRMED]: 'bg-blue-100 text-blue-800',
    [ORDER_STATUS.ORDER_STATUS_SHIPPING]: 'bg-orange-100 text-orange-800',
    [ORDER_STATUS.ORDER_STATUS_DELIVERED]: 'bg-green-100 text-green-800',
    [ORDER_STATUS.ORDER_STATUS_CANCELLED]: 'bg-red-100 text-red-800',
    // Backward compatibility with lowercase
    'pending': 'bg-yellow-100 text-yellow-800',
    'confirmed': 'bg-blue-100 text-blue-800',
    'shipping': 'bg-orange-100 text-orange-800',
    'delivered': 'bg-green-100 text-green-800',
    'cancelled': 'bg-red-100 text-red-800',
    '': 'bg-yellow-100 text-yellow-800'
  }
  return classMap[status] || 'bg-yellow-100 text-yellow-800'
}

/**
 * Get status select dropdown CSS class
 * @param {string} status - Order status value
 * @returns {string} CSS class
 */
export function getStatusSelectClass(status) {
  const classMap = {
    [ORDER_STATUS.ORDER_STATUS_PENDING]: 'bg-yellow-50 border-yellow-300 text-yellow-800 hover:bg-yellow-100',
    [ORDER_STATUS.ORDER_STATUS_CONFIRMED]: 'bg-blue-50 border-blue-300 text-blue-800 hover:bg-blue-100',
    [ORDER_STATUS.ORDER_STATUS_SHIPPING]: 'bg-orange-50 border-orange-300 text-orange-800 hover:bg-orange-100',
    [ORDER_STATUS.ORDER_STATUS_DELIVERED]: 'bg-green-50 border-green-300 text-green-800 hover:bg-green-100',
    [ORDER_STATUS.ORDER_STATUS_CANCELLED]: 'bg-red-50 border-red-300 text-red-800 hover:bg-red-100',
    // Backward compatibility with lowercase
    'pending': 'bg-yellow-50 border-yellow-300 text-yellow-800 hover:bg-yellow-100',
    'confirmed': 'bg-blue-50 border-blue-300 text-blue-800 hover:bg-blue-100',
    'shipping': 'bg-orange-50 border-orange-300 text-orange-800 hover:bg-orange-100',
    'delivered': 'bg-green-50 border-green-300 text-green-800 hover:bg-green-100',
    'cancelled': 'bg-red-50 border-red-300 text-red-800 hover:bg-red-100',
    '': 'bg-yellow-50 border-yellow-300 text-yellow-800 hover:bg-yellow-100'
  }
  return classMap[status] || 'bg-yellow-50 border-yellow-300 text-yellow-800 hover:bg-yellow-100'
}

/**
 * Check if status is valid
 * @param {string} status - Status to validate
 * @returns {boolean} True if valid
 */
export function isValidStatus(status) {
  return Object.values(ORDER_STATUS).includes(status)
}

/**
 * Get all status values as array
 * @returns {string[]} Array of status values
 */
export function getAllStatuses() {
  return Object.values(ORDER_STATUS)
}

