import { getImageUrlFromApi } from './imageUtils.js'

/**
 * @param {unknown} path
 * @returns {boolean}
 */
function isValidImagePath(path) {
  return typeof path === 'string' && path.trim() !== ''
}

/**
 * Ảnh đầu tiên hợp lệ trong gallery (giữ thứ tự API).
 * @param {unknown} gallery
 * @returns {string|null}
 */
function firstGalleryPath(gallery) {
  if (!Array.isArray(gallery) || gallery.length === 0) {
    return null
  }
  const first = gallery.find((path) => isValidImagePath(path))
  return first ? first.trim() : null
}

/**
 * Utility function để lấy ảnh sản phẩm từ API hoặc fallback.
 * Ưu tiên: mainImage → image → gallery[0] → map ID cứng.
 * @param {Object|number} productOrId - Product object (có mainImage / gallery) hoặc productId
 * @returns {string} - Đường dẫn ảnh sản phẩm từ API server
 */
export function getProductImage(productOrId) {
  if (productOrId && typeof productOrId === 'object') {
    if (isValidImagePath(productOrId.mainImage)) {
      return getImageUrlFromApi(productOrId.mainImage.trim())
    }

    if (isValidImagePath(productOrId.image)) {
      // Tránh vòng lặp: chỉ dùng image nếu là path (không phải URL đã resolve sẵn từ lần gọi trước)
      const imagePath = productOrId.image.trim()
      // Nếu đã là full URL từ getImageUrlFromApi, trả về nguyên; nếu là path thì wrap
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://') || imagePath.startsWith('data:')) {
        return imagePath
      }
      return getImageUrlFromApi(imagePath)
    }

    const galleryPath = firstGalleryPath(productOrId.gallery)
    if (galleryPath) {
      return getImageUrlFromApi(galleryPath)
    }
  }

  // Fallback: dùng ID để tìm ảnh mặc định (giữ hành vi cũ khi không có main/gallery)
  const id = typeof productOrId === 'object' ? productOrId?.id : Number(productOrId)
  const fallbackPaths = {
    1: '/images/products/me-den.jpg',
    2: '/images/products/combo-black.png',
    3: '/images/products/hong-dau.jpg',
    4: '/images/products/combo-pink.png',
    5: '/images/products/Combo-mix.png',
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
    const validPaths = product.gallery.filter((path) => isValidImagePath(path))
    if (validPaths.length > 0) {
      return validPaths.map((path) => getImageUrlFromApi(path.trim()))
    }
    // Nếu tất cả đều null/empty, return empty array (không dùng fallback)
    return []
  }

  // Nếu có mainImage, thêm vào đầu gallery
  if (product && isValidImagePath(product.mainImage)) {
    const gallery = [getImageUrlFromApi(product.mainImage.trim())]
    if (product.gallery && Array.isArray(product.gallery)) {
      const validPaths = product.gallery.filter((path) => isValidImagePath(path))
      gallery.push(...validPaths.map((path) => getImageUrlFromApi(path.trim())))
    }
    return gallery
  }

  // Nếu không có gallery từ API hoặc gallery rỗng/null, return empty array
  // KHÔNG dùng fallback để tránh hiển thị ảnh mặc định sai khi API trả về null
  return []
}
