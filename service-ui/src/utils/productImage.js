/**
 * Utility function để lấy ảnh sản phẩm dựa trên ID
 * @param {number} productId - ID của sản phẩm
 * @returns {string} - Đường dẫn ảnh sản phẩm
 */
export function getProductImage(productId) {
  const id = Number(productId)
  if (id === 1) {
    return '/images/products/me-den.jpg'
  }
  if (id === 2) {
    return '/images/products/combo-black.png'
  }
  if (id === 3) {
    return '/images/products/hong-dau.jpg'
  }
  if (id === 4) {
    return '/images/products/combo-pink.png'
  }
  if (id === 52) {
    return '/images/products/Combo-mix.png'
  }
  return '/images/products/me-den.jpg' // Fallback
}

/**
 * Lấy ảnh gallery cho sản phẩm dựa trên ID
 * @param {number} productId - ID của sản phẩm
 * @returns {string[]} - Mảng đường dẫn ảnh gallery
 */
export function getProductGallery(productId) {
  const id = Number(productId)
  if (id === 1 || id === 2) {
    return [
      '/images/products/details/black/1.png',
      '/images/products/details/black/2.png',
      '/images/products/details/black/3.png',
      '/images/products/details/black/4.png',
      '/images/products/details/black/5.png',
      '/images/products/details/black/6.png',
      '/images/products/details/black/7.png',
      '/images/products/details/black/8.png'
    ]
  }
  if (id === 3 || id === 4) {
    return [
      '/images/products/details/pink/1.png',
      '/images/products/details/pink/2.png',
      '/images/products/details/pink/3.png',
      '/images/products/details/pink/4.png',
      '/images/products/details/pink/5.png',
      '/images/products/details/pink/6.png',
      '/images/products/details/pink/7.png',
      '/images/products/details/pink/8.png',
      '/images/products/details/pink/9.png'
    ]
  }
  if (id === 52) {
    return [
      '/images/products/details/mix/0.png',
      '/images/products/details/mix/1.png',
      '/images/products/details/mix/2.png',
      '/images/products/details/mix/3.png',
      '/images/products/details/mix/4.png',
      '/images/products/details/mix/5.png',
      '/images/products/details/mix/6.png',
      '/images/products/details/mix/7.png',
      '/images/products/details/mix/8.png',
      '/images/products/details/mix/9.png',
      '/images/products/details/mix/10.png',
      '/images/products/details/mix/11.png',   
    ]
  }
  return ['/images/products/me-den.jpg'] // Fallback
} 