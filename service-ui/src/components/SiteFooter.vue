<template>
  <footer class="bg-gradient-to-br from-yellow-50 via-white to-yellow-50 font-sans border-t border-yellow-200 mt-4 text-black relative overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute inset-0 bg-gradient-to-r from-transparent via-yellow-100/20 to-transparent"></div>

    <div class="max-w-7xl mx-auto px-4 py-8 relative z-10">
      <!-- Main footer content -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
        <!-- Logo + Info -->
        <div class="text-center bg-white/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50">
          <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="h-20 w-auto mb-4 object-contain mx-auto drop-shadow-md" />
          <div class="space-y-3 text-gray-700">
            <div v-if="c.address" class="flex items-center justify-center gap-2">
              <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              <span class="text-sm whitespace-pre-line">{{ c.address }}</span>
            </div>
            <div v-if="c.phone" class="flex items-center justify-center gap-2">
              <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
              </svg>
              <span class="font-bold text-green-700">{{ c.phone }}</span>
            </div>
            <div v-if="c.email" class="flex items-center justify-center gap-2">
              <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
              </svg>
              <span class="text-sm">{{ c.email }}</span>
            </div>
          </div>
        </div>

        <!-- Quy định & chính sách -->
        <div class="text-center bg-white/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50">
          <div class="text-xl font-bold mb-4 text-green-700 flex items-center justify-center gap-2">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            {{ c.policyTitle }}
          </div>
          <ul class="space-y-3">
            <li v-for="(link, idx) in c.policyLinks || []" :key="idx">
              <component
                :is="isInternal(link.to) ? 'router-link' : 'a'"
                v-bind="linkAttrs(link.to)"
                class="text-gray-700 hover:text-green-600 transition-all duration-300 hover:bg-green-50 px-3 py-2 rounded-lg block"
              >
                {{ link.label }}
              </component>
            </li>
          </ul>
        </div>

        <!-- Thông tin liên hệ -->
        <div class="text-center bg-white/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50">
          <div class="text-xl font-bold mb-4 text-green-700 flex items-center justify-center gap-2">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            {{ c.contactTitle }}
          </div>
          <div class="space-y-3">
            <div v-if="c.hotline" class="bg-green-100 rounded-lg p-3">
              <div class="font-bold text-green-800 text-lg">HOTLINE: {{ c.hotline }}</div>
              <div v-if="c.hotlineHours" class="text-sm text-green-700">{{ c.hotlineHours }}</div>
            </div>
            <div v-if="c.faqLabel">
              <component
                :is="isInternal(c.faqLink) ? 'router-link' : 'a'"
                v-bind="linkAttrs(c.faqLink || '/faq')"
                class="text-gray-700 hover:text-green-600 transition-all duration-300 hover:bg-green-50 px-3 py-2 rounded-lg block"
              >
                {{ c.faqLabel }}
              </component>
            </div>
          </div>
        </div>
      </div>

      <!-- Social Media Section -->
      <div v-if="(c.socials || []).length" class="text-center mb-8">
        <div class="flex items-center justify-center gap-2 mb-4">
          <div class="w-6 h-6 bg-green-600 rounded-full flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
          </div>
          <span class="text-green-600 font-medium">{{ c.socialTitle }}</span>
        </div>

        <div class="flex flex-wrap justify-center gap-3">
          <a
            v-for="(s, idx) in c.socials"
            :key="idx"
            :href="s.url || '#'"
            :target="isExternal(s.url) ? '_blank' : null"
            :rel="isExternal(s.url) ? 'noopener' : null"
            class="bg-white rounded-lg px-4 py-2 flex items-center gap-2 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200"
          >
            <svg class="w-5 h-5" :class="socialIcon(s.type).color" fill="currentColor" viewBox="0 0 24 24">
              <path :d="socialIcon(s.type).path" />
            </svg>
            <span class="font-medium text-gray-700">{{ s.label }}</span>
          </a>
        </div>
      </div>

      <!-- Bottom footer -->
      <div v-if="c.copyright" class="border-t border-yellow-200 pt-8">
        <div class="text-center">
          <p class="text-gray-600 text-sm">{{ c.copyright }}</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { defaultHomepage } from '../content/homeDefaults.js'
import { isExternalLink, isInternalLink, linkAttrs } from '../utils/links.js'

const props = defineProps({
  /** extraJson of the CMS `footer` section; missing keys fall back to the packaged defaults. */
  content: { type: Object, default: null },
  logoUrl: { type: String, default: '' },
})

const c = computed(() => ({ ...defaultHomepage.sections.footer.extraJson, ...(props.content || {}) }))

const SOCIAL_ICONS = {
  facebook: {
    color: 'text-blue-600',
    path: 'M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z',
  },
  tiktok: {
    color: 'text-black',
    path: 'M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07z',
  },
  youtube: {
    color: 'text-red-600',
    path: 'M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z',
  },
  instagram: {
    color: 'text-pink-600',
    path: 'M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zM12 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324zM12 16a4 4 0 110-8 4 4 0 010 8zm6.406-11.845a1.44 1.44 0 100 2.881 1.44 1.44 0 000-2.881z',
  },
  zalo: {
    color: 'text-blue-500',
    path: 'M12 2C6.477 2 2 6.03 2 11c0 2.65 1.27 5.03 3.3 6.68L4.5 22l4.2-2.1c1.03.28 2.13.43 3.3.43 5.523 0 10-4.03 10-9S17.523 2 12 2z',
  },
}
const socialIcon = (type) => SOCIAL_ICONS[type] || SOCIAL_ICONS.facebook

const isInternal = isInternalLink
const isExternal = isExternalLink
</script>
