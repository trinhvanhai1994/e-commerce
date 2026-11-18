import { onMounted, ref } from 'vue'
import { contentService } from '../services/content.service.js'
import { cloneDefaults, pageDefaultImage } from '../content/pageDefaults.js'
import { resolveSlotImage } from '../content/homeDefaults.js'

/**
 * CMS content of a public page. Starts with the packaged defaults (no flash of empty content),
 * then overlays the sections returned by the API. A section absent from the API keeps its default;
 * enabled=false hides it. On API failure the defaults stay.
 */
export function usePageContent(pageKey) {
  const sections = ref(cloneDefaults(pageKey))

  const extra = (key) => sections.value[key]?.extraJson || {}
  const visible = (key) => sections.value[key]?.enabled !== false

  /** Image of an object following the useDefaultImage/image rule (section extra or an item of it). */
  const imageOf = (sectionKey, obj, index = null) =>
    resolveSlotImage({
      useDefault: obj?.useDefaultImage,
      path: obj?.image,
      defaultPath: pageDefaultImage(pageKey, sectionKey, index),
    })

  async function load() {
    try {
      const data = await contentService.getPage(pageKey)
      if (data && typeof data === 'object') {
        sections.value = { ...cloneDefaults(pageKey), ...data }
      }
    } catch (e) {
      console.warn(`CMS page "${pageKey}" fallback:`, e.message)
    }
  }

  onMounted(load)

  return { sections, extra, visible, imageOf, reload: load }
}
