<template>
  <!-- Mobile sidebar - Ẩn hoàn toàn trên mobile khi đóng, luôn hiện trên desktop -->
  <aside 
    :class="[
      'fixed lg:static inset-y-0 left-0 z-50 w-64 min-h-screen bg-white border-r border-gray-100 flex flex-col py-6 transform transition-transform duration-300 ease-in-out',
      // Trên mobile (< lg): ẩn khi đóng (-translate-x-full), hiện khi mở (translate-x-0)
      // Trên desktop (>= lg): luôn hiện (static position, không translate)
      isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
    ]"
  >
    <!-- Close button for mobile -->
    <div class="flex items-center justify-between px-6 mb-4 lg:hidden">
      <span class="text-lg font-bold text-green-700">Menu</span>
      <button 
        @click="$emit('close')"
        class="p-2 rounded-lg hover:bg-gray-100 transition"
        aria-label="Close menu"
      >
        <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>
    
    <nav class="flex-1 overflow-y-auto">
      <ul class="space-y-2">
        <li>
          <router-link 
            to="/admin" 
            @click="$emit('close')"
            class="flex items-center gap-2 px-4 md:px-6 py-3 text-base md:text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path === '/admin' || $route.path === '/admin/' }"
          >
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="($route.path === '/admin' || $route.path === '/admin/') ? 'bg-green-500' : 'bg-gray-300'"></span>
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
            Dashboard
          </router-link>
        </li>
        <li>
          <router-link 
            to="/admin/orders" 
            @click="$emit('close')"
            class="flex items-center gap-2 px-4 md:px-6 py-3 text-base md:text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/orders') }"
          >
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/orders') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Đơn Hàng
          </router-link>
        </li>
        <li>
          <router-link 
            to="/admin/products" 
            @click="$emit('close')"
            class="flex items-center gap-2 px-4 md:px-6 py-3 text-base md:text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/products') }"
          >
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/products') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Sản Phẩm
          </router-link>
        </li>
        <li>
          <router-link 
            to="/admin/users" 
            @click="$emit('close')"
            class="flex items-center gap-2 px-4 md:px-6 py-3 text-base md:text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/users') }"
          >
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/users') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Khách Hàng
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'

defineProps({
  isOpen: {
    type: Boolean,
    default: false
  }
})

defineEmits(['close'])

const route = useRoute()
</script> 