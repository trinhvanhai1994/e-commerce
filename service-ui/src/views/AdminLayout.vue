<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <AdminHeader @toggle-sidebar="toggleSidebar" />
    <div class="flex flex-1 relative">
      <!-- Mobile overlay - Chỉ hiện trên mobile khi menu mở -->
      <div 
        v-if="isMobileMenuOpen" 
        class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden transition-opacity duration-300"
        @click="closeMobileMenu"
      ></div>
      
      <!-- Sidebar - Ẩn hoàn toàn trên mobile khi đóng, chỉ hiện khi mở -->
      <!-- Trên desktop: luôn hiện -->
      <AdminSidebar :is-open="isMobileMenuOpen" @close="closeMobileMenu" />
      
      <!-- Main content - Full width trên mobile khi sidebar đóng -->
      <main class="flex-1 p-4 md:p-6 lg:p-8 bg-gray-50 w-full">
        <slot />
      </main>
    </div>
    <AdminFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import AdminHeader from './AdminHeader.vue'
import AdminSidebar from './AdminSidebar.vue'
import AdminFooter from './AdminFooter.vue'

const isMobileMenuOpen = ref(false)

function toggleSidebar() {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

function closeMobileMenu() {
  isMobileMenuOpen.value = false
}

// Close menu on window resize to desktop
function handleResize() {
  if (window.innerWidth >= 1024) {
    isMobileMenuOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script> 