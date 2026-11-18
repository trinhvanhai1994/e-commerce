<template>
  <div class="max-w-7xl mx-auto px-4 py-8 grid grid-cols-1 md:grid-cols-4 gap-8">
    <div class="md:col-span-3">
      <h1 class="text-2xl md:text-3xl font-bold text-green-600 mb-6 text-center md:text-left uppercase tracking-wide">Blog chăm sóc sức khỏe</h1>
      <div v-if="loading" class="py-10 text-center text-gray-500">Đang tải bài viết...</div>
      <div v-else-if="loadError" class="py-10 text-center text-gray-600">
        Không tải được danh sách bài viết.
        <button type="button" class="ml-2 text-green-600 hover:underline font-semibold" @click="loadPosts">Thử lại</button>
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="post in paginatedPosts" :key="post.id" class="bg-white rounded-xl shadow hover:shadow-lg transition p-0 flex flex-col overflow-hidden">
          <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="block">
            <img :src="cover(post)" :alt="post.title" class="w-full h-auto object-contain rounded-t-xl hover:opacity-90 transition-opacity duration-200" />
          </router-link>
          <div class="p-4 flex-1 flex flex-col">
            <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="block">
              <h2 class="font-bold text-base md:text-lg text-gray-800 mb-2 line-clamp-2 hover:text-green-600 transition-colors duration-200">{{ post.title }}</h2>
            </router-link>
            <p class="text-gray-600 text-sm mb-3 line-clamp-2">{{ post.excerpt }}</p>
            <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="mt-auto text-green-500 hover:underline text-sm font-semibold">Đọc tiếp</router-link>
          </div>
        </div>
      </div>
      <div class="flex justify-center mt-8 gap-2">
        <button v-for="page in totalPages" :key="page" @click="goToPage(page)" :class="['w-8 h-8 rounded-full flex items-center justify-center border', currentPage === page ? 'bg-green-500 text-white border-green-500' : 'bg-white text-green-500 border-green-200 hover:bg-green-50']">
          {{ page }}
        </button>
      </div>
    </div>
    <aside class="space-y-8">
      <div>
        <h3 class="font-bold text-green-600 mb-4 text-base uppercase tracking-wide">Bài viết xem nhiều</h3>
        <ul class="space-y-3">
          <li v-for="item in popularPosts" :key="item.id" class="flex gap-3 items-center">
            <img :src="cover(item)" :alt="item.title" class="w-12 h-12 object-contain rounded" />
            <router-link :to="`/blog/${item.slug}`" class="text-sm font-semibold text-gray-700 hover:text-green-500 line-clamp-2">{{ item.title }}</router-link>
          </li>
        </ul>
      </div>
      <div>
        <h3 class="font-bold text-green-600 mb-4 text-base uppercase tracking-wide">Sản phẩm nổi bật</h3>
        <ul class="space-y-3">
          <li v-for="sp in featuredProducts" :key="sp.id" class="flex gap-3 items-center">
            <img :src="sp.image" :alt="sp.name" class="w-12 h-12 object-contain rounded" />
            <div class="flex-1">
              <div class="text-sm font-semibold text-gray-700 line-clamp-2">{{ sp.name }}</div>
              <div class="text-xs text-green-500 font-bold">{{ formatPrice(sp.price) }}</div>
            </div>
          </li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productAPI } from '@/utils/api.js'
import { getProductImage } from '../utils/productImage'
import { contentService } from '../services/content.service.js'
import { resolveCmsImage } from '../content/homeDefaults.js'

const blogPosts = ref([])
const loading = ref(true)
const loadError = ref(false)
const popularPosts = computed(() => blogPosts.value.slice(0, 5))
const cover = (post) => resolveCmsImage(post.coverImage || post.image)

const allProducts = ref([])
async function fetchProducts() {
  try {
    const data = await productAPI.getProducts()
    const products = Array.isArray(data) ? data : (data.data || [])
    allProducts.value = products.filter((p) => !p.deleted)
  } catch (e) {
    allProducts.value = []
  }
}

const featuredProducts = computed(() =>
  allProducts.value.slice(0, 4).map((p) => ({
    id: p.id,
    name: p.name,
    image: getProductImage(p),
    price: p.price,
  }))
)

const route = useRoute()
const router = useRouter()
const currentPage = ref(1)
watch(
  () => route.query.page,
  (val) => {
    currentPage.value = Number(val) || 1
  },
  { immediate: true }
)

const pageSize = 6
const totalPages = computed(() => Math.max(1, Math.ceil(blogPosts.value.length / pageSize)))
const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return blogPosts.value.slice(start, start + pageSize)
})

function goToPage(page) {
  router.push({ query: { ...route.query, page } })
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price || 0)
}

async function loadPosts() {
  loading.value = true
  loadError.value = false
  try {
    const posts = await contentService.getPosts()
    blogPosts.value = Array.isArray(posts) ? posts : []
  } catch (e) {
    console.warn('Blog list failed', e.message)
    blogPosts.value = []
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPosts()
  fetchProducts()
})
</script>
