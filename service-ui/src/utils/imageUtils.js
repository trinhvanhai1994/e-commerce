// Image utility functions

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5678'

/**
 * Build full image URL from API server
 * @param {string} imagePath - Image path (e.g., /images/products/details/black/1.png)
 * @returns {string} Full URL to access image from API server
 */
export function getImageUrlFromApi(imagePath) {
  if (!imagePath) return ''
  
  // Nếu đã là full URL, trả về nguyên
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // Nếu path bắt đầu bằng /images/, giữ nguyên
  // Nếu không, thêm /images/ vào đầu
  let normalizedPath = imagePath
  if (!normalizedPath.startsWith('/images/')) {
    if (normalizedPath.startsWith('/')) {
      normalizedPath = '/images' + normalizedPath
    } else {
      normalizedPath = '/images/' + normalizedPath
    }
  }
  
  // Build full URL từ API server
  const baseUrl = API_BASE_URL.replace(/\/$/, '') // Remove trailing slash
  return `${baseUrl}${normalizedPath}`
}

/**
 * Helper function để tạo URL ảnh với cache-busting
 * @param {string} imageUrl - Image URL
 * @param {number} [customTimestamp] - Optional custom timestamp (nếu không có sẽ dùng Date.now())
 * @returns {string} Image URL with cache-busting timestamp
 */
export function getImageUrlWithCacheBusting(imageUrl, customTimestamp = null) {
  if (!imageUrl) return ''
  
  // Nếu là path tương đối, convert sang URL từ API
  const fullUrl = imageUrl.startsWith('http://') || imageUrl.startsWith('https://') 
    ? imageUrl 
    : getImageUrlFromApi(imageUrl)
  
  // Thêm timestamp để tránh cache
  // Sử dụng customTimestamp nếu có, nếu không dùng Date.now()
  // Điều này cho phép force reload bằng cách truyền timestamp mới
  const timestamp = customTimestamp || Date.now()
  const separator = fullUrl.includes('?') ? '&' : '?'
  return `${fullUrl}${separator}_t=${timestamp}`
}

