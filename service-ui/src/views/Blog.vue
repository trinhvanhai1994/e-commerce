<template>
  <div class="max-w-7xl mx-auto px-4 py-8 grid grid-cols-1 md:grid-cols-4 gap-8">
    <!-- Main Blog Grid -->
    <div class="md:col-span-3">
      <h1 class="text-2xl md:text-3xl font-bold text-green-600 dark:text-green-400 mb-6 text-center md:text-left uppercase tracking-wide transition-colors duration-300">Blog chăm sóc sức khỏe</h1>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="post in paginatedPosts" :key="post.id" class="bg-white dark:bg-gray-800 rounded-xl shadow hover:shadow-lg transition-all duration-300 p-0 flex flex-col overflow-hidden">
          <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="block">
            <img :src="post.image" :alt="post.title" class="w-full h-auto object-contain rounded-t-xl hover:opacity-90 transition-opacity duration-200" />
          </router-link>
          <div class="p-4 flex-1 flex flex-col">
            <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="block">
              <h2 class="font-bold text-base md:text-lg text-gray-800 dark:text-gray-100 mb-2 line-clamp-2 hover:text-green-600 dark:hover:text-green-400 transition-colors duration-200">{{ post.title }}</h2>
            </router-link>
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3 line-clamp-2 transition-colors duration-300">{{ post.excerpt }}</p>
            <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug }, query: { ...route.query, page: currentPage } }" class="mt-auto text-green-500 dark:text-green-400 hover:underline text-sm font-semibold transition-colors duration-300">Đọc tiếp</router-link>
          </div>
        </div>
      </div>
      <!-- Pagination -->
      <div class="flex justify-center mt-8 gap-2">
        <button v-for="page in totalPages" :key="page" @click="goToPage(page)" :class="['w-8 h-8 rounded-full flex items-center justify-center border transition-colors duration-300', currentPage === page ? 'bg-green-500 text-white border-green-500' : 'bg-white dark:bg-gray-800 text-green-500 dark:text-green-400 border-green-200 dark:border-gray-600 hover:bg-green-50 dark:hover:bg-gray-700']">
          {{ page }}
        </button>
      </div>
    </div>
    <!-- Sidebar -->
    <aside class="space-y-8">
      <!-- Bài viết xem nhiều -->
      <div>
        <h3 class="font-bold text-green-600 dark:text-green-400 mb-4 text-base uppercase tracking-wide transition-colors duration-300">Bài viết xem nhiều</h3>
        <ul class="space-y-3">
          <li v-for="item in popularPosts" :key="item.id" class="flex gap-3 items-center">
            <img :src="item.image" :alt="item.title" class="w-12 h-12 object-contain rounded" />
            <router-link :to="`/blog/${item.slug}`" class="text-sm font-semibold text-gray-700 dark:text-gray-300 hover:text-green-500 dark:hover:text-green-400 line-clamp-2 transition-colors duration-300">{{ item.title }}</router-link>
          </li>
        </ul>
      </div>
      <!-- Sản phẩm nổi bật -->
      <div>
        <h3 class="font-bold text-green-600 dark:text-green-400 mb-4 text-base uppercase tracking-wide transition-colors duration-300">Sản phẩm nổi bật</h3>
        <ul class="space-y-3">
          <li v-for="sp in featuredProducts" :key="sp.id" class="flex gap-3 items-center">
            <img :src="sp.image" :alt="sp.name" class="w-12 h-12 object-contain rounded" />
            <div class="flex-1">
              <div class="text-sm font-semibold text-gray-700 dark:text-gray-300 line-clamp-2 transition-colors duration-300">{{ sp.name }}</div>
              <div class="text-xs text-green-500 dark:text-green-400 font-bold transition-colors duration-300">{{ formatPrice(sp.price) }}</div>
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

// Dữ liệu mẫu bài viết
const blogPosts = ref([
  {
    id: 1,
    title: 'Dinh Dưỡng Đen Nguyên Bản Từ 5 Loại Hạt Trong Bột Ngũ Hắc Mè Đen',
    excerpt: 'Bột ngũ hắc mè đen Thi Yên là bữa ăn thay thế dinh dưỡng từ 5 loại hạt đen nguyên bản, phù hợp với lối sống lành mạnh và hiện đại. Dưỡng sinh dễ dàng cho cơ thể khỏe mạnh.',
    image: '/images/blogs/blog1/avatar1.png',
    slug: 'blog1'
  },
  {
    id: 2,
    title: 'Hiểu Cơ Bản Về Dưỡng Sinh: Cân Bằng Âm Dương & Thuận Tự Nhiên',
    excerpt: 'Khám phá triết lý dưỡng sinh của Thi Yên, kết hợp tinh hoa dưỡng sinh cổ truyền và dinh dưỡng hiện đại. Mỗi sản phẩm là bữa ăn tiện lợi, cân bằng âm dương, thuận theo ngũ hành, nuôi dưỡng cơ thể, giấc ngủ, dưỡng nhan và hỗ trợ sức khỏe toàn diện',
    image: '/images/blogs/blog2/avatar2.png',
    slug: 'blog2'
  },
  {
    id: 3,
    title: 'Dưỡng Sinh Hiện Đại Là Gì? Vì Sao Người Trẻ Nên Nuôi Cơ Thể Từ Gốc Mỗi Ngày?',
    excerpt: 'Khám phá dưỡng sinh hiện đại với Thi Yên: Bột ngũ hắc mè đen và bột ngũ sắc hồng đậu giúp nuôi dưỡng cơ thể từ bên trong, cải thiện giấc ngủ, dưỡng nhan, dinh dưỡng cho tóc. Sản phẩm thuần tự nhiên, tiện lợi, phù hợp cho người trẻ và lối sống lành mạnh.',
    image: '/images/blogs/blog3/avatar3.png',
    slug: 'blog3'
  },
  {
    id: 4,
    title: 'Bột Ngũ Sắc Hồng Đậu Thi Yên: Bí Quyết Dưỡng Huyết, Dưỡng Nhan Từ 5 Nguyên Liệu Đỏ Nguyên Bản',
    excerpt: 'Bột ngũ sắc hồng đậu Thi Yên kết hợp 5 nguyên liệu đỏ tự nhiên giúp bổ máu, dưỡng nhan, cân bằng nội tiết và cải thiện giấc ngủ. Sản phẩm dưỡng sinh tiện lợi, lý tưởng cho phụ nữ, người ăn chay và người lớn tuổi.',
    image: '/images/blogs/blog4/avatar4.png',
    slug: 'blog4'
  },
  {
    id: 5,
    title: 'Ngũ Hắc Mè Đen & Ngũ Sắc Hồng Đậu: Bộ Đôi Ngũ Cốc Dưỡng Sinh Thuần Thực Vật Cho Bữa Sáng Lành Mạnh',
    excerpt: 'Bột ngũ hắc mè đen và bột ngũ sắc hồng đậu Thi Yên là bộ đôi ngũ cốc dưỡng sinh thuần thực vật, giúp bổ huyết, dưỡng nhan, cải thiện giấc ngủ và sức khỏe toàn diện. Giải pháp bữa sáng tiện lợi, phù hợp với lối sống lành mạnh.',
    image: '/images/blogs/blog5/avatar5.png',
    slug: 'blog5'
  }
])

// Dữ liệu bài viết xem nhiều (lấy từ blogPosts)
const popularPosts = computed(() => blogPosts.value.slice(0, 5))

// Dữ liệu sản phẩm (lấy giống Products.vue)
const allProducts = ref([])
async function fetchProducts() {
  try {
    const data = await productAPI.getProducts()
    const products = Array.isArray(data) ? data : (data.data || [])
    allProducts.value = products.filter(p => !p.deleted)
  } catch (e) {
    allProducts.value = []
  }
}

const featuredProducts = computed(() => {
  return allProducts.value.slice(0, 4).map(p => ({
    id: p.id,
    name: p.name,
    image: getProductImage(p.id),
    price: p.price
  }))
})

// Phân trang
const route = useRoute()
const router = useRouter()
const currentPage = ref(1)
watch(
  () => route.query.page,
  (newVal) => {
    const pageFromQuery = Number(newVal) || 1
    if (pageFromQuery !== currentPage.value) {
      currentPage.value = pageFromQuery
    }
  },
  { immediate: true }
)
const pageSize = 9
const totalPages = computed(() => Math.ceil(blogPosts.value.length / pageSize))
const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return blogPosts.value.slice(start, start + pageSize)
})

function goToPage(page) {
  const pageNumber = Number(page) || 1
  const clamped = Math.min(Math.max(pageNumber, 1), totalPages.value)
  router.push({ name: 'Blog', query: { ...route.query, page: clamped } })
}

function formatPrice(val) {
  return val.toLocaleString('vi-VN') + 'đ'
}

const cat = route.query.cat

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style> 