/**
 * Tính toán phần trăm giảm giá dựa trên giá gốc và giá khuyến mãi
 * @param {number} price - Giá khuyến mãi (giá bán)
 * @param {number} oldPrice - Giá gốc (giá cũ)
 * @returns {number} Phần trăm giảm giá (làm tròn đến số nguyên)
 */
export function calculateDiscount(price, oldPrice) {
  // Nếu không có giá gốc hoặc giá gốc <= giá khuyến mãi, không có giảm giá
  if (!oldPrice || oldPrice <= price || oldPrice <= 0) {
    return 0
  }
  
  // Tính phần trăm giảm giá: ((oldPrice - price) / oldPrice) * 100
  const discount = ((oldPrice - price) / oldPrice) * 100
  
  // Làm tròn đến số nguyên
  return Math.round(discount)
}

/**
 * Lấy phần trăm giảm giá từ product object
 * Ưu tiên tính toán từ price và oldPrice, nếu không có thì dùng discount field
 * @param {Object} product - Product object
 * @returns {number} Phần trăm giảm giá
 */
export function getProductDiscount(product) {
  if (!product) return 0
  
  // Nếu có oldPrice và price, tính toán từ đó (ưu tiên)
  if (product.oldPrice && product.price && product.oldPrice > product.price) {
    return calculateDiscount(product.price, product.oldPrice)
  }
  
  // Nếu không có, dùng discount field từ API (nếu có)
  return product.discount || 0
}

