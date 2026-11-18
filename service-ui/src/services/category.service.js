import { httpClient } from './http/client.js'

function unwrap(data) {
  if (data && typeof data === 'object' && 'success' in data) {
    if (!data.success) {
      throw new Error(data.message || 'Request failed')
    }
    return data.data
  }
  return data
}

// Used while the API is unreachable, so forms and filters still show the original 3 categories (V23 seed).
export const FALLBACK_CATEGORIES = [
  { code: 'ME_DEN', name: 'Mè đen', sortOrder: 1, visible: true },
  { code: 'HONG_DAU', name: 'Hồng đậu', sortOrder: 2, visible: true },
  { code: 'COMBO', name: 'Combo', sortOrder: 3, visible: true },
]

// Old lowercase codes still present in some fallback product data (Home.vue, AdminOrders.vue)
const LEGACY_CODES = { 'me-den': 'ME_DEN', 'hong-dau': 'HONG_DAU', combo: 'COMBO' }

/** Display name of a product's category code; an unknown code (e.g. from Pancake sync) is shown as-is. */
export function categoryName(code, categories = FALLBACK_CATEGORIES, emptyLabel = 'Chưa phân loại') {
  if (!code) return emptyLabel
  const key = LEGACY_CODES[code] || code
  const found = categories.find((c) => c.code === key) || FALLBACK_CATEGORIES.find((c) => c.code === key)
  return found ? found.name : code
}

let publicListPromise = null

export const categoryService = {
  /**
   * All categories (hidden ones carry visible:false) in admin order. Shared by every storefront component
   * of the page load; `fresh` skips both this memo and the 30s browser cache.
   */
  async getCategories({ fresh = false } = {}) {
    if (fresh || !publicListPromise) {
      const suffix = fresh ? `?_=${Date.now()}` : ''
      publicListPromise = httpClient
        .get(`/api/dragun/products/categories${suffix}`)
        .then(unwrap)
        .catch((err) => {
          publicListPromise = null
          throw err
        })
    }
    return publicListPromise
  },

  /** Same list, falling back to the original 3 categories when the API fails. */
  async getCategoriesOrFallback() {
    try {
      const list = await this.getCategories()
      return Array.isArray(list) && list.length ? list : FALLBACK_CATEGORIES
    } catch {
      return FALLBACK_CATEGORIES
    }
  },

  // ----- admin -----

  /** Categories with productCount and updatedAt (needed to save). */
  async getAdminCategories() {
    return unwrap(await httpClient.get('/api/dragun/admin/categories'))
  },

  async createCategory({ name, sortOrder, visible }) {
    publicListPromise = null
    return unwrap(await httpClient.post('/api/dragun/admin/categories', { name, sortOrder, visible }))
  },

  /** `updatedAt` from the last read is required; 409 means another admin changed it. */
  async updateCategory(id, { name, sortOrder, visible, updatedAt }) {
    publicListPromise = null
    return unwrap(await httpClient.put(`/api/dragun/admin/categories/${id}`, { name, sortOrder, visible, updatedAt }))
  },

  /** 409 while products still use the category. */
  async deleteCategory(id) {
    publicListPromise = null
    return unwrap(await httpClient.delete(`/api/dragun/admin/categories/${id}`))
  },
}

export default categoryService
