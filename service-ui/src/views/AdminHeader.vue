<template>
  <header class="flex items-center justify-between px-8 py-3 bg-white shadow-sm border-b border-gray-100">
    <div class="flex items-center gap-3">
      <img :src="getImageUrlFromApi('/images/logo/logo.png')" alt="Thi Yên Logo" class="w-10 h-10 object-contain" />
      <span class="text-2xl font-bold text-green-700">Quản Lý</span>
    </div>
    <div class="flex items-center gap-4">
      <div v-if="currentUser" class="flex items-center gap-3">
        <div class="text-right">
          <div class="text-sm font-semibold text-gray-800">{{ currentUser.name }}</div>
          <div class="text-xs text-gray-500">{{ currentUser.email }}</div>
        </div>
        <button 
          @click="handleLogout" 
          class="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-red-600 hover:bg-red-50 rounded-lg transition"
          title="Đăng xuất"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
          </svg>
          <span>Đăng xuất</span>
        </button>
      </div>
      <div v-else class="inline-flex items-center justify-center w-10 h-10 rounded-full bg-gray-100">
        <svg class="w-7 h-7 text-gray-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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