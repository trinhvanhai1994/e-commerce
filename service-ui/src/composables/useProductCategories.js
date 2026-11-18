import { computed, onMounted, ref, unref } from 'vue'
import { categoryService, categoryName, FALLBACK_CATEGORIES } from '../services/category.service.js'

/**
 * Product categories managed in /admin/categories (task-004), for forms, filters and labels.
 * - `categories`: every category in admin order (hidden ones carry visible:false); the original 3 until loaded.
 * - `visibleCategories`: the ones shown as storefront filter buttons.
 * - `options(currentCode)`: select options, keeping a product's current code even when it is not in the list
 *   (for example a code written by the Pancake sync), so saving the form never changes it silently.
 * - `nameOf(code)`: display name, the code itself when unknown.
 * `fresh: true` (admin forms) skips the browser cache so a category added a moment ago is listed.
 */
export function useProductCategories({ fresh = false } = {}) {
  const categories = ref(FALLBACK_CATEGORIES)

  async function load() {
    try {
      const list = await categoryService.getCategories({ fresh })
      if (Array.isArray(list) && list.length) categories.value = list
    } catch (err) {
      console.error('Không tải được danh mục sản phẩm, dùng danh sách mặc định:', err)
    }
  }

  onMounted(load)

  const visibleCategories = computed(() => categories.value.filter((c) => c.visible !== false))

  function options(currentCode) {
    const code = unref(currentCode)
    const list = categories.value.map((c) => ({
      code: c.code,
      label: c.visible === false ? `${c.name} (đang ẩn)` : c.name,
    }))
    if (code && !list.some((o) => o.code === code)) {
      list.push({ code, label: `${code} (chưa có trong danh mục)` })
    }
    return list
  }

  const nameOf = (code) => categoryName(code, categories.value)

  return { categories, visibleCategories, options, nameOf, reload: load }
}
