<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <div v-if="article">
      <!-- Mobile Table of Contents - hiển thị ở đầu trang -->
      <div class="lg:hidden mb-6">
        <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
          <button 
            @click="showMobileTOC = !showMobileTOC"
            class="flex items-center justify-between w-full text-left"
          >
            <h3 class="text-lg font-semibold text-gray-900 flex items-center">
              <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path>
              </svg>
              Mục lục
            </h3>
            <svg 
              :class="['w-5 h-5 text-gray-500 transition-transform duration-200', showMobileTOC ? 'rotate-180' : '']"
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>
          <nav v-show="showMobileTOC" class="mt-4 space-y-1 max-h-64 overflow-y-auto">
            <a 
              v-for="(item, index) in tableOfContents" 
              :key="index"
              :href="`#section-${item.index}`"
              @click="scrollToSection(item.index, $event); showMobileTOC = false"
              :class="[
                'block text-sm transition-colors duration-200 rounded px-3 py-2 font-medium',
                activeSection === item.index ? 'text-green-600 bg-green-50' : 'text-gray-600 hover:text-green-600 hover:bg-gray-100'
              ]"
            >
              {{ item.text }}
            </a>
          </nav>
        </div>
      </div>

      <!-- Desktop Layout -->
      <div class="flex gap-8">
        <!-- Table of Contents Sidebar - chỉ hiển thị trên desktop -->
        <div class="hidden lg:block w-64 flex-shrink-0">
          <div class="sticky top-8">
            <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
              <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                <svg class="w-5 h-5 mr-2 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path>
                </svg>
                Mục lục
              </h3>
              <nav class="space-y-1 max-h-96 overflow-y-auto">
                <a 
                  v-for="(item, index) in tableOfContents" 
                  :key="index"
                  :href="`#section-${item.index}`"
                  @click="scrollToSection(item.index, $event)"
                  :class="[
                    'block text-sm transition-colors duration-200 rounded px-3 py-2 font-medium',
                    activeSection === item.index ? 'text-green-600 bg-green-50' : 'text-gray-600 hover:text-green-600 hover:bg-gray-100'
                  ]"
                >
                  {{ item.text }}
                </a>
              </nav>
            </div>
          </div>
        </div>

        <!-- Main Content -->
        <div class="flex-1 min-w-0">
        <img v-if="article.image" :src="article.image" :alt="article.title" class="w-full h-auto object-contain rounded-lg mb-6" />
        <h1 class="text-2xl md:text-3xl font-bold text-gray-900 mb-3">{{ article.title }}</h1>
        <div class="text-sm text-gray-500 mb-8">{{ article.date }}</div>

        <BlogArticleBody :blocks="article.content" />

          <div class="mt-10 flex items-center justify-between">
            <router-link :to="{ name: 'Blog', query: { ...route.query, page: pageFromQuery } }" class="text-green-600 hover:underline font-medium">← Quay lại danh sách</router-link>
            <div class="flex gap-3">
              <router-link v-for="p in related" :key="p.slug" :to="{ name: 'BlogDetail', params: { slug: p.slug }, query: { ...route.query, page: pageFromQuery } }" class="text-sm text-gray-600 hover:text-green-600">
                {{ p.title }}
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loadError" class="text-center py-20">
      <h2 class="text-xl font-semibold text-gray-700 mb-4">Không tải được bài viết</h2>
      <button type="button" class="text-green-600 hover:underline font-semibold" @click="load">Thử lại</button>
    </div>

    <div v-else class="text-center py-20">
      <h2 class="text-xl font-semibold text-gray-700 mb-4">Không tìm thấy bài viết</h2>
      <router-link :to="{ name: 'Blog', query: { page: pageFromQuery } }" class="text-green-600 hover:underline">Quay lại Blog</router-link>
    </div>
  </div>
  
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch } from "vue"
import { useRoute } from "vue-router"
import { contentService } from "../services/content.service.js"
import BlogArticleBody from "../components/blog/BlogArticleBody.vue"
import { resolveCmsImage } from "../content/homeDefaults.js"

const route = useRoute()
const activeSection = ref(0)
const showMobileTOC = ref(false)
const article = ref(null)
// Network/5xx error (retryable), as opposed to a 404 which shows "not found"
const loadError = ref(false)
const related = ref([])
const loading = ref(true)

const pageFromQuery = computed(() => Number(route.query.page) || 1)

function mapPost(post) {
  if (!post) return null
  const content = (post.contentJson || []).map((block) => {
    const copy = { ...block }
    if (copy.type === "img" && copy.src) copy.src = resolveCmsImage(copy.src)
    return copy
  })
  let date = ""
  if (post.publishedAt) {
    try {
      date = new Date(post.publishedAt).toLocaleDateString("vi-VN")
    } catch (_) {
      date = String(post.publishedAt)
    }
  }
  return {
    slug: post.slug,
    title: post.title,
    date,
    image: resolveCmsImage(post.coverImage),
    content,
  }
}

async function load() {
  loading.value = true
  article.value = null
  loadError.value = false
  const slug = route.params.slug
  try {
    let post = null
    try {
      post = await contentService.getPostBySlug(slug)
    } catch (e) {
      if (e?.status !== 404) loadError.value = true
    }
    article.value = mapPost(post)

    let list = []
    try {
      list = await contentService.getPosts()
    } catch (_) {
      list = []
    }
    related.value = (list || [])
      .filter((p) => p.slug !== slug)
      .slice(0, 3)
      .map((p) => ({ slug: p.slug, title: p.title }))
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

const tableOfContents = computed(() => {
  if (!article.value) return []
  const toc = []
  article.value.content.forEach((block, index) => {
    if (block.type === "h2") {
      toc.push({ text: block.text, level: 0, index })
    }
  })
  return toc
})

const scrollToSection = (index, event) => {
  event.preventDefault()
  const element = document.getElementById(`section-${index}`)
  if (element) element.scrollIntoView({ behavior: "smooth", block: "start" })
}

const handleScroll = () => {
  const sections = document.querySelectorAll("[id^=\"section-\"]")
  const scrollPosition = window.scrollY + 150
  let currentSection = 0
  sections.forEach((section, index) => {
    if (scrollPosition >= section.offsetTop) currentSection = index
  })
  activeSection.value = currentSection
}

watch(() => route.params.slug, load, { immediate: true })

onMounted(() => {
  window.addEventListener("scroll", handleScroll)
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll)
})
</script>
