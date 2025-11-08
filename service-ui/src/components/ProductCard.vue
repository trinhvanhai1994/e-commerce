<template>
  <div
    class="relative bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 overflow-hidden hover:shadow-xl transition-shadow duration-300 flex flex-col h-full"
  >
    <!-- Product Image -->
    <div class="relative cursor-pointer flex-shrink-0" @click="handleViewDetail(product)">
      <img
        :src="getProductImage(product.id)"
        :alt="product.name"
        class="w-full h-48 md:h-64 object-cover hover:scale-105 transition-transform duration-300"
      />
      <div v-if="product.discount" class="absolute top-4 left-4 bg-red-500 text-white px-3 py-1 rounded-full text-sm font-bold">
        -{{ product.discount }}%
      </div>
    </div>

    <!-- Product Info -->
    <div class="p-4 md:p-6 flex flex-col flex-1">
      <div class="flex items-center gap-2 mb-2">
        <span class="text-xs px-2 py-1 bg-green-100 text-green-800 rounded-full">
          {{ getCategoryName(product.category) }}
        </span>
        <div v-if="product.rating" class="flex items-center">
          <div class="flex text-yellow-400">
            <svg v-for="i in 5" :key="i" class="w-3 h-3 md:w-4 md:h-4" :class="i <= product.rating ? 'fill-current' : 'text-gray-300'" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
            </svg>
          </div>
          <span class="text-xs md:text-sm text-gray-600 ml-1">({{ product.rating }})</span>
        </div>
      </div>

      <h3 class="text-lg md:text-xl font-bold text-gray-800 mb-2 line-clamp-2">{{ product.name }}</h3>
      <p class="text-gray-600 text-xs md:text-sm mb-4 line-clamp-3 flex-1">{{ product.shortDesc || product.description }}</p>

      <!-- Price and Stock - Fixed at bottom -->
      <div class="mt-auto">
        <!-- Price -->
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <span class="text-xl md:text-2xl font-bold text-green-600">{{ formatPrice(product.price) }}</span>
            <span v-if="product.oldPrice" class="text-sm text-gray-500 line-through">{{ formatPrice(product.oldPrice) }}</span>
          </div>
          <div v-if="product.stock" class="text-xs md:text-sm text-gray-500">
            Còn {{ product.stock }} sản phẩm
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-2">
          <button
            @click="handleViewDetail(product)"
            class="flex-1 bg-green-500 hover:bg-green-600 text-white font-semibold py-2 px-3 md:px-4 rounded-lg transition-colors duration-200 text-xs md:text-sm"
          >
            Xem chi tiết
          </button>
          <button
            @click="$emit('add-to-cart', product)"
            class="bg-green-100 hover:bg-green-200 text-green-700 font-semibold py-2 px-3 md:px-4 rounded-lg transition-colors duration-200 flex items-center gap-1 md:gap-2 text-xs md:text-sm"
          >
            <svg class="w-3 h-3 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.1 5.4M7 13v6a2 2 0 002 2h6a2 2 0 002-2v-6m-8 0V9a2 2 0 012-2h4a2 2 0 012 2v4m-6 0h4"/>
            </svg>
            <span class="hidden sm:inline">Thêm vào giỏ</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getProductImage } from '../utils/productImage'
import { navigateToSubdomain, navigateToMainDomain } from '../utils/domainUtils'

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(['view-detail', 'add-to-cart'])

function handleViewDetail(product) {
  const path = `/products/${product.id}`
  
  // Check if this is a special product (ID 1 or 2)
  if (product.id === 1 || product.id === 2) {
    const domainChanged = navigateToSubdomain(path)
    if (!domainChanged) {
      emit('view-detail', product)
    }
  } else {
    // Regular product, use main domain
    const domainChanged = navigateToMainDomain(path)
    if (!domainChanged) {
      emit('view-detail', product)
    }
  }
}

// Helper functions
function formatPrice(price) {
  return price.toLocaleString('vi-VN') + '₫'
}

function getCategoryName(category) {
  const categories = {
    'me-den': 'Mè đen',
    'hong-dau': 'Hồng đậu'
  }
  return categories[category] || 'Sản phẩm'
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
