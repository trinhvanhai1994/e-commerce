import { getImageUrlFromApi } from './imageUtils.js'

/**
 * Utility function để lấy ảnh sản phẩm từ API hoặc fallback
 * @param {Object|number} productOrId - Product object (có mainImage) hoặc productId
 * @returns {string} - Đường dẫn ảnh sản phẩm từ API server
 */
export function getProductImage(productOrId) {
  // Nếu là object product và có mainImage từ API
  if (productOrId && typeof productOrId === 'object' && productOrId.mainImage) {
    return getImageUrlFromApi(productOrId.mainImage)
  }
  
  // Nếu là object product và có image
  if (productOrId && typeof productOrId === 'object' && productOrId.image) {
    return getImageUrlFromApi(productOrId.image)
  }
  
  // Fallback: dùng ID để tìm ảnh mặc định (từ backend)
  const id = typeof productOrId === 'object' ? productOrId?.id : Number(productOrId)
  const fallbackPaths = {
    1: '/images/products/me-den.jpg',
    2: '/images/products/combo-black.png',
    3: '/images/products/hong-dau.jpg',
    4: '/images/products/combo-pink.png',
    5: '/images/products/Combo-mix.png'
  }
  const fallbackPath = fallbackPaths[id] || '/images/products/me-den.jpg'
  return getImageUrlFromApi(fallbackPath)
}

/**
 * Lấy ảnh gallery cho sản phẩm từ API hoặc fallback
 * QUAN TRỌNG: Giữ nguyên thứ tự từ API, KHÔNG sort lại
 * @param {Object} product - Product object từ API (có gallery array đã được sort theo display_order)
 * @returns {string[]} - Mảng đường dẫn ảnh gallery từ API server (giữ nguyên thứ tự)
 */
export function getProductGallery(product) {
  // Nếu product có gallery từ API, sử dụng gallery đó (giữ nguyên thứ tự từ backend)
  // Backend đã sort theo display_order, không cần sort lại ở frontend
  // QUAN TRỌNG: Filter null/empty để không hiển thị ảnh mặc định sai
  if (product && product.gallery && Array.isArray(product.gallery) && product.gallery.length > 0) {
    // Filter null, undefined, và empty string, chỉ giữ lại paths hợp lệ
    const validPaths = product.gallery.filter(path => path && path.trim() !== '')
    if (validPaths.length > 0) {
      return validPaths.map(path => getImageUrlFromApi(path))
    }
    // Nếu tất cả đều null/empty, return empty array (không dùng fallback)
    return []
  }
  
  // Nếu có mainImage, thêm vào đầu gallery
  if (product && product.mainImage) {
    const gallery = [getImageUrlFromApi(product.mainImage)]
    if (product.gallery && Array.isArray(product.gallery)) {
      // Filter null/empty từ gallery
      const validPaths = product.gallery.filter(path => path && path.trim() !== '')
      gallery.push(...validPaths.map(path => getImageUrlFromApi(path)))
    }
    return gallery
  }
  
  // Nếu không có gallery từ API hoặc gallery rỗng/null, return empty array
  // KHÔNG dùng fallback để tránh hiển thị ảnh mặc định sai khi API trả về null
  return []
} 