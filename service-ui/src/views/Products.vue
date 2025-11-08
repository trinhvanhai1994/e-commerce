<template>
  <div class="min-h-screen bg-yellow-50">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl md:text-4xl font-bold mb-4 text-green-700">Sản phẩm của chúng tôi</h1>
        <p class="text-gray-600 text-lg">Khám phá sản phẩm</p>
      </div>

      <!-- Search and Filter Section -->
      <div class="bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 p-6 mb-8">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <!-- Search Box -->
          <div class="flex-1">
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                class="w-full pl-10 pr-4 py-3 border border-green-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white/70 backdrop-blur-sm"
              />
              <svg class="absolute left-3 top-3.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>

          <!-- Sort Dropdown -->
          <div class="md:w-64">
            <select
              v-model="sortBy"
              class="w-full px-4 py-3 border border-green-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white/70 backdrop-blur-sm"
            >
              <option value="">Sắp xếp theo</option>
              <option value="name-asc">Tên A-Z</option>
              <option value="name-desc">Tên Z-A</option>
              <option value="price-asc">Giá thấp đến cao</option>
              <option value="price-desc">Giá cao đến thấp</option>
              <option value="newest">Mới nhất</option>
            </select>
          </div>
        </div>

        <!-- Category Filters -->
        <div class="flex flex-wrap gap-2">
          <button
            @click="selectCategory('')"
            :class="[
              'px-4 py-2 rounded-full border font-semibold text-sm transition-colors',
              !selectedCat
                ? 'bg-green-500 text-white border-green-500'
                : 'bg-white text-green-500 border-green-200 hover:bg-green-50',
            ]"
          >
            Tất cả ({{ allProducts.length }})
          </button>
          <button
            v-for="cat in categories"
            :key="cat.id"
            @click="selectCategory(cat.key)"
            :class="[
              'px-4 py-2 rounded-full border font-semibold text-sm transition-colors',
              selectedCat === cat.key
                ? 'bg-green-500 text-white border-green-500'
                : 'bg-white text-green-500 border-green-200 hover:bg-green-50',
            ]"
          >
            {{ cat.name }} ({{ getProductCountByCategory(cat.key) }})
          </button>
        </div>
      </div>

      <!-- Results Summary -->
      <div class="flex justify-between items-center mb-6">
        <div class="text-gray-600">
          Hiển thị {{ filteredProducts.length }} sản phẩm
          <span v-if="searchQuery"> cho "{{ searchQuery }}"</span>
        </div>
        <div v-if="selectedCat" class="text-sm text-gray-500">
          Danh mục: {{ getCategoryName(selectedCat) }}
        </div>
      </div>

      <!-- Products Grid -->
      <div v-if="filteredProducts.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="product in paginatedProducts"
          :key="product.id"
          class="bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 overflow-hidden hover:shadow-xl transition-shadow duration-300 flex flex-col"
        >
          <!-- Product Image -->
          <div class="relative cursor-pointer flex-shrink-0" @click="viewProductDetail(product)">
            <img
              :src="getProductImage(product.id)"
              :alt="product.name"
              class="w-full h-64 object-cover hover:scale-105 transition-transform duration-300"
            />
            <div v-if="product.discount" class="absolute top-4 left-4 bg-red-500 text-white px-3 py-1 rounded-full text-sm font-bold">
              -{{ product.discount }}%
            </div>
          </div>

          <!-- Product Info -->
          <div class="p-6 flex flex-col flex-1">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-xs px-2 py-1 bg-green-100 text-green-800 rounded-full">
                {{ getCategoryName(product.category) }}
              </span>
              <div v-if="product.rating" class="flex items-center">
                <div class="flex text-yellow-400">
                  <svg v-for="i in 5" :key="i" class="w-4 h-4" :class="i <= product.rating ? 'fill-current' : 'text-gray-300'" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span class="text-sm text-gray-600 ml-1">({{ product.rating }})</span>
              </div>
            </div>

            <h3 class="text-xl font-bold text-gray-800 mb-2">{{ product.name }}</h3>
            <p class="text-gray-600 text-sm mb-4 line-clamp-3 flex-1">{{ product.shortDesc }}</p>

            <!-- Price and Stock - Fixed at bottom -->
            <div class="mt-auto">
              <!-- Price -->
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <span class="text-2xl font-bold text-green-600">{{ formatPrice(product.price) }}</span>
                  <span v-if="product.oldPrice" class="text-sm text-gray-500 line-through">{{ formatPrice(product.oldPrice) }}</span>
                </div>
                <div v-if="product.quantity" class="text-sm text-gray-500">
                  {{ product.quantity }}
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex gap-2">
                <button
                  @click="viewProductDetail(product)"
                  class="flex-1 bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-4 rounded-lg transition-colors duration-200"
                >
                  Xem chi tiết
                </button>
                <button
                  @click="addToCart(product)"
                  class="bg-green-100 hover:bg-green-200 text-green-700 font-semibold py-2 px-4 rounded-lg transition-colors duration-200 flex items-center gap-2"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.1 5.4M7 13v6a2 2 0 002 2h6a2 2 0 002-2v-6m-8 0V9a2 2 0 012-2h4a2 2 0 012 2v4m-6 0h4"/>
                  </svg>
                  <span class="hidden sm:inline">Thêm vào giỏ</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No Results -->
      <div v-else class="text-center py-12">
        <div class="bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 p-8">
          <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 12h6m-6-4h6m2 5.291A7.962 7.962 0 0112 15c-2.34 0-4.5-.935-6.072-2.456M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
          </svg>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">Không tìm thấy sản phẩm</h3>
          <p class="text-gray-600 mb-4">
            {{ searchQuery ? `Không có sản phẩm nào phù hợp với "${searchQuery}"` : 'Không có sản phẩm nào trong danh mục này' }}
          </p>
          <button
            @click="clearFilters"
            class="bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-6 rounded-lg transition-colors duration-200"
          >
            Xóa bộ lọc
          </button>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center mt-8">
        <nav class="flex items-center space-x-2">
          <button
            @click="currentPage = Math.max(1, currentPage - 1)"
            :disabled="currentPage === 1"
            class="px-3 py-2 rounded-lg border border-green-300 text-green-600 hover:bg-green-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Trước
          </button>
          
          <button
            v-for="page in visiblePages"
            :key="page"
            @click="currentPage = page"
            :class="[
              'px-3 py-2 rounded-lg border',
              currentPage === page
                ? 'bg-green-500 text-white border-green-500'
                : 'border-green-300 text-green-600 hover:bg-green-50'
            ]"
          >
            {{ page }}
          </button>
          
          <button
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="px-3 py-2 rounded-lg border border-green-300 text-green-600 hover:bg-green-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Sau
          </button>
        </nav>
      </div>
    </div>
    
    <!-- Success Popup -->
    <div
      v-if="showPopup"
      class="fixed top-6 right-6 z-50 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg animate-bounce max-w-sm"
    >
      <div class="flex items-center">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
        {{ popupMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { productAPI } from '@/utils/api.js'
import { getProductImage } from '../utils/productImage'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// Reactive data
const searchQuery = ref('')
const selectedCat = ref(route.query.cat || '')
const sortBy = ref('')
const currentPage = ref(1)
const itemsPerPage = 12
const showPopup = ref(false)
const popupMessage = ref('')

// Categories
const categories = [
  { id: 1, name: 'Mè đen', key: 'ME_DEN' },
  { id: 2, name: 'Hồng đậu', key: 'HONG_DAU' },
  { id: 3, name: 'Combo', key: 'COMBO' }
]

// Products data from API
const allProducts = ref([])

// Fetch products from API
async function fetchProducts() {
  try {
    const data = await productAPI.getProducts()
    const products = Array.isArray(data) ? data : (data.data || [])
    // Lọc sản phẩm chưa bị xóa (deleted = false hoặc không có trường deleted)
    allProducts.value = products.filter(product => !product.deleted)
  } catch (e) {
    console.error('Không thể tải danh sách sản phẩm:', e)
    allProducts.value = []
  }
}

// Computed properties
const filteredProducts = computed(() => {
  let products = allProducts.value

  // Filter by category
  if (selectedCat.value) {
    products = products.filter(p => p.category === selectedCat.value)
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    products = products.filter(p => 
      p.name.toLowerCase().includes(query) || 
      (p.shortDesc && p.shortDesc.toLowerCase().includes(query))
    )
  }

  // Sort products
  if (sortBy.value) {
    switch (sortBy.value) {
      case 'name-asc':
        products.sort((a, b) => a.name.localeCompare(b.name))
        break
      case 'name-desc':
        products.sort((a, b) => b.name.localeCompare(a.name))
        break
      case 'price-asc':
        products.sort((a, b) => a.price - b.price)
        break
      case 'price-desc':
        products.sort((a, b) => b.price - a.price)
        break
      case 'newest':
        products.sort((a, b) => b.id - a.id)
        break
    }
  }

  return products
})

const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / itemsPerPage)
})

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredProducts.value.slice(start, end)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages = []

  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }

  return pages
})

// Methods
function selectCategory(catKey) {
  selectedCat.value = catKey
  currentPage.value = 1
  router.push({ path: "/products", query: catKey ? { cat: catKey } : {} })
}

function getProductCountByCategory(catKey) {
  return allProducts.value.filter(p => p.category === catKey).length
}

function getCategoryName(catKey) {
  const categoryMap = {
    'ME_DEN': 'Mè đen',
    'HONG_DAU': 'Hồng đậu',
    'COMBO': 'Combo'
  }
  return categoryMap[catKey] || catKey || 'Chưa phân loại'
}


function formatPrice(val) {
  return val.toLocaleString("vi-VN") + "₫"
}

function addToCart(product) {
  cartStore.addToCart(product, 1)
  popupMessage.value = `Đã thêm "${product.name}" vào giỏ hàng!`
  showPopup.value = true
  setTimeout(() => {
    showPopup.value = false
  }, 3000)
}

import { navigateToSubdomain, navigateToMainDomain } from '../utils/domainUtils'

function viewProductDetail(product) {
  const path = `/products/${product.id}`
  
  // Check if this is a special product (ID 1 or 2)
  if (product.id === 1 || product.id === 2) {
    const domainChanged = navigateToSubdomain(path)
    if (!domainChanged) {
      router.push(path)
    }
  } else {
    // Regular product, use main domain
    const domainChanged = navigateToMainDomain(path)
    if (!domainChanged) {
      router.push(path)
    }
  }
}

function clearFilters() {
  searchQuery.value = ''
  selectedCat.value = ''
  sortBy.value = ''
  currentPage.value = 1
  const path = "/products"
  const domainChanged = navigateToMainDomain(path)
  if (!domainChanged) {
    router.push({ path })
  }
}

// Watch for route changes
watch(
  () => route.query.cat,
  (newCat) => {
    selectedCat.value = newCat || ""
  }
)

// Watch for search query changes
watch(searchQuery, () => {
  currentPage.value = 1
})

// Watch for category changes
watch(selectedCat, () => {
  currentPage.value = 1
})

// Cập nhật giá sản phẩm trong giỏ hàng khi component mount
onMounted(async () => {
  await fetchProducts()
  await cartStore.updateProductPrices()
})
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.animate-bounce {
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0,0,0);
  }
  40%, 43% {
    transform: translate3d(0,-30px,0);
  }
  70% {
    transform: translate3d(0,-15px,0);
  }
  90% {
    transform: translate3d(0,-4px,0);
  }
}
</style>
