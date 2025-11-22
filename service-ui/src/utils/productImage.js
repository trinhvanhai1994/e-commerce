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
 * @param {Object} product - Product object từ API (có gallery array)
 * @returns {string[]} - Mảng đường dẫn ảnh gallery từ API server
 */
export function getProductGallery(product) {
  // Nếu product có gallery từ API, sử dụng gallery đó
  if (product && product.gallery && Array.isArray(product.gallery) && product.gallery.length > 0) {
    return product.gallery.map(path => getImageUrlFromApi(path))
  }
  
  // Nếu có mainImage, thêm vào đầu gallery
  if (product && product.mainImage) {
    const gallery = [getImageUrlFromApi(product.mainImage)]
    if (product.gallery && Array.isArray(product.gallery)) {
      gallery.push(...product.gallery.map(path => getImageUrlFromApi(path)))
    }
    return gallery
  }
  
  // Fallback: dùng ID để tìm ảnh mặc định (từ backend)
  const id = product?.id || Number(product)
  const fallbackGalleries = {
    1: [
      '/images/products/details/black/1.png',
      '/images/products/details/black/2.png',
      '/images/products/details/black/3.png',
      '/images/products/details/black/4.png',
      '/images/products/details/black/5.png',
      '/images/products/details/black/6.png',
      '/images/products/details/black/7.png',
      '/images/products/details/black/8.png'
    ],
    2: [
      '/images/products/details/black/1.png',
      '/images/products/details/black/2.png',
      '/images/products/details/black/3.png',
      '/images/products/details/black/4.png',
      '/images/products/details/black/5.png',
      '/images/products/details/black/6.png',
      '/images/products/details/black/7.png',
      '/images/products/details/black/8.png'
    ],
    3: [
      '/images/products/details/pink/1.png',
      '/images/products/details/pink/2.png',
      '/images/products/details/pink/3.png',
      '/images/products/details/pink/4.png',
      '/images/products/details/pink/5.png',
      '/images/products/details/pink/6.png',
      '/images/products/details/pink/7.png',
      '/images/products/details/pink/8.png',
      '/images/products/details/pink/9.png'
    ],
    4: [
      '/images/products/details/pink/1.png',
      '/images/products/details/pink/2.png',
      '/images/products/details/pink/3.png',
      '/images/products/details/pink/4.png',
      '/images/products/details/pink/5.png',
      '/images/products/details/pink/6.png',
      '/images/products/details/pink/7.png',
      '/images/products/details/pink/8.png',
      '/images/products/details/pink/9.png'
    ],
    5: [
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
  const fallbackGallery = fallbackGalleries[id] || ['/images/products/me-den.jpg']
  return fallbackGallery.map(path => getImageUrlFromApi(path))
} 