<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-4xl mx-auto py-10 px-4">
      <h1 class="text-3xl font-bold text-green-700 mb-8 text-center">Admin Dashboard</h1>
      
      <!-- Auth Status -->
      <div class="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-semibold text-blue-800">Authentication Status</h3>
            <p class="text-sm text-blue-600">
              Logged in: {{ isLoggedIn ? '✅ Yes' : '❌ No' }}
            </p>
          </div>
          <button 
            @click="setAuth" 
            class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm"
          >
            Set Auth
          </button>
        </div>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div @click="navigateToProducts" class="block bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer border border-gray-100 hover:border-green-400">
          <div class="flex items-center gap-4">
            <span class="bg-green-100 p-3 rounded-full">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 7h18M3 12h18M3 17h18"/></svg>
            </span>
            <div>
              <div class="font-bold text-lg text-gray-800">Quản lý sản phẩm</div>
              <div class="text-gray-500 text-sm">Thêm, sửa, xoá sản phẩm</div>
            </div>
          </div>
        </div>
        
        <div @click="navigateToUsers" class="block bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer border border-gray-100 hover:border-green-400">
          <div class="flex items-center gap-4">
            <span class="bg-green-100 p-3 rounded-full">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5.121 17.804A13.937 13.937 0 0112 15c2.485 0 4.797.657 6.879 1.804M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
            </span>
            <div>
              <div class="font-bold text-lg text-gray-800">Quản lý người dùng</div>
              <div class="text-gray-500 text-sm">Danh sách, phân quyền</div>
            </div>
          </div>
        </div>
        
        <div @click="navigateToOrders" class="block bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition cursor-pointer border border-gray-100 hover:border-green-400">
          <div class="flex items-center gap-4">
            <span class="bg-green-100 p-3 rounded-full">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 17v-6a2 2 0 012-2h2a2 2 0 012 2v6m-6 0h6"/></svg>
            </span>
            <div>
              <div class="font-bold text-lg text-gray-800">Quản lý đơn hàng</div>
              <div class="text-gray-500 text-sm">Xem, xử lý đơn hàng</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(false)

onMounted(() => {
  console.log('🏠 AdminDashboard mounted')
  
  // Check authentication status
  const authStatus = localStorage.getItem('adminLoggedIn')
  isLoggedIn.value = authStatus === 'true'
  
  console.log('🔐 Auth status:', {
    isLoggedIn: isLoggedIn.value,
    adminLoggedIn: authStatus
  })
})

function setAuth() {
  console.log('🔧 Setting admin authentication...')
  
  localStorage.setItem('adminLoggedIn', 'true')
  localStorage.setItem('adminUser', JSON.stringify({
    id: 1,
    name: 'Admin User',
    email: 'admin@example.com',
    role: 'admin'
  }))
  
  isLoggedIn.value = true
  
  console.log('✅ Admin authentication set')
}

function navigateToProducts() {
  console.log('🛍️ Navigating to products...')
  setAuth()
  router.push('/admin/products')
}

function navigateToUsers() {
  console.log('👥 Navigating to users...')
  setAuth()
  router.push('/admin/users')
}

function navigateToOrders() {
  console.log('📦 Navigating to orders...')
  setAuth()
  router.push('/admin/orders')
}
</script>
