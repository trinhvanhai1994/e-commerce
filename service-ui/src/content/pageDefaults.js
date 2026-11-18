/**
 * Packaged content of the story (/me) and philosophy (/healthcare) pages.
 * page-sections.seed.json is copied from service-api/src/main/resources/cms-seed/page-sections.json by
 * scripts/sync-cms-seed.mjs (runs before dev/build): edit the service-api file, not this copy.
 * Used when a section is not in the DB yet or the content API fails, so the pages look exactly as before the CMS.
 */
import seed from './page-sections.seed.json'

export const PAGE_DEFAULTS = seed.reduce((acc, row) => {
  acc[row.page_key] = acc[row.page_key] || {}
  acc[row.page_key][row.section_key] = {
    sectionKey: row.section_key,
    enabled: row.enabled !== false,
    sortOrder: row.sort_order || 0,
    extraJson: row.extra_json || {},
  }
  return acc
}, {})

/** Packaged images shown while a section uses "Dùng ảnh mặc định". Index = position in items[]. */
export const PAGE_DEFAULT_IMAGES = {
  story: {
    hero: '/images/me/banner.jpg',
    feature_1: '/images/me/COMBO-MIX.png',
    feature_2: '/images/me/item4.png',
    values: ['/images/icons/tu-nhien.png', '/images/icons/can-bang.png', '/images/icons/tien-loi.png'],
    products: ['/images/me/item1.png', '/images/me/item2.png'],
    gallery: ['/images/struct/1.png', '/images/struct/2.png', '/images/struct/3.png', '/images/struct/4.png'],
  },
  philosophy: {
    hero: '/images/me/banner.jpg',
    principles: ['/images/icons/tu-nhien.png', '/images/icons/can-bang.png', '/images/icons/tien-loi.png'],
  },
}

export function pageDefaultImage(pageKey, sectionKey, index = null) {
  const value = PAGE_DEFAULT_IMAGES[pageKey]?.[sectionKey]
  if (Array.isArray(value)) return value[index ?? 0] || value[0] || ''
  return value || ''
}

export function cloneDefaults(pageKey) {
  return JSON.parse(JSON.stringify(PAGE_DEFAULTS[pageKey] || {}))
}
