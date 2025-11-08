// Image utility functions

/**
 * Helper function để tạo URL ảnh với cache-busting
 * @param {string} imageUrl - Image URL
 * @returns {string} Image URL with cache-busting timestamp
 */
export function getImageUrlWithCacheBusting(imageUrl) {
  if (!imageUrl) return ''
  
  // Thêm timestamp để tránh cache
  const timestamp = Date.now()
  const separator = imageUrl.includes('?') ? '&' : '?'
  return `${imageUrl}${separator}_t=${timestamp}`
}

