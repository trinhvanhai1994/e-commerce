<template>
  <header class="flex items-center justify-between px-4 md:px-6 lg:px-8 py-3 bg-white shadow-sm border-b border-gray-100">
    <div class="flex items-center gap-2 md:gap-3">
      <!-- Mobile menu button - Luôn hiển thị trên mobile, ẩn trên desktop -->
      <button 
        @click="$emit('toggle-sidebar')"
        class="block lg:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors mr-2"
        aria-label="Toggle menu"
        type="button"
      >
        <svg class="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
        </svg>
      </button>
      
      <img :src="getImageUrlFromApi('/images/logo/logo.png')" alt="Thi Yên Logo" class="w-8 h-8 md:w-10 md:h-10 object-contain" />
      <span class="text-lg md:text-xl lg:text-2xl font-bold text-green-700">Quản Lý</span>
    </div>
    <div class="flex items-center gap-2 md:gap-4">
      <div v-if="currentUser" class="flex items-center gap-2 md:gap-3">
        <div class="text-right hidden sm:block">
          <div class="text-xs md:text-sm font-semibold text-gray-800">{{ currentUser.name }}</div>
          <div class="text-xs text-gray-500 hidden md:block">{{ currentUser.email }}</div>
        </div>
        <button 
          @click="handleLogout" 
          class="flex items-center gap-1 md:gap-2 px-2 md:px-4 py-2 text-xs md:text-sm font-semibold text-red-600 hover:bg-red-50 rounded-lg transition"
          title="Đăng xuất"
        >
          <svg class="w-4 h-4 md:w-5 md:h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
          </svg>
          <span class="hidden sm:inline">Đăng xuất</span>
        </button>
      </div>
      <div v-else class="inline-flex items-center justify-center w-8 h-8 md:w-10 md:h-10 rounded-full bg-gray-100">
        <svg class="w-5 h-5 md:w-7 md:h-7 text-gray-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5.121 17.804A13.937 13.937 0 0112 15c2.485 0 4.797.657 6.879 1.804M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import adminService from '../services/admin.service.js'
import { getImageUrlFromApi } from '../utils/imageUtils.js'

defineEmits(['toggle-sidebar'])

const router = useRouter()
const currentUser = ref(null)

onMounted(() => {
  currentUser.value = adminService.getCurrentUser()
})

function handleLogout() {
  if (confirm('Bạn có chắc muốn đăng xuất?')) {
    // Clear authentication
    adminService.logout()
    // Redirect to login page
    router.push('/admin/login').then(() => {
      // Force reload to clear any cached state
      window.location.reload()
    })
  }
}
</script> 