<template>
  <AdminLayout>
    <div class="min-h-screen bg-gray-50">
      <div class="max-w-7xl mx-auto py-8 px-4">
        <h1 class="text-3xl font-bold text-green-700 mb-8 text-center">Admin Dashboard</h1>
        
        <!-- Loading state -->
        <div v-if="loading" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
          <p class="mt-4 text-gray-600">Đang tải dữ liệu...</p>
        </div>

        <!-- Error state -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <svg class="w-5 h-5 text-red-400 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
            </svg>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">Lỗi tải dữ liệu</h3>
              <p class="text-sm text-red-700 mt-1">{{ error }}</p>
              <button @click="loadDashboardStats" class="mt-2 text-sm text-red-600 hover:text-red-500 font-medium">
                Thử lại
              </button>
            </div>
          </div>
        </div>

        <!-- Dashboard Content -->
        <div v-else>
          <!-- Statistics Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <!-- Total Visitors -->
            <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-600 mb-1">Số người truy cập</p>
                  <p class="text-3xl font-bold text-blue-600">{{ formatNumber(stats.totalVisitors) }}</p>
                </div>
                <div class="bg-blue-100 p-3 rounded-full">
                  <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Total Buyers -->
            <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-600 mb-1">Số người mua hàng</p>
                  <p class="text-3xl font-bold text-green-600">{{ formatNumber(stats.totalBuyers) }}</p>
                </div>
                <div class="bg-green-100 p-3 rounded-full">
                  <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Total Orders -->
            <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-600 mb-1">Số đơn hàng</p>
                  <p class="text-3xl font-bold text-purple-600">{{ formatNumber(stats.totalOrders) }}</p>
                </div>
                <div class="bg-purple-100 p-3 rounded-full">
                  <svg class="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                </div>
              </div>
            </div>

            <!-- Total Revenue -->
            <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100 hover:shadow-lg transition">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-gray-600 mb-1">Tổng giá trị đơn hàng</p>
                  <p class="text-3xl font-bold text-orange-600">{{ formatPrice(stats.totalRevenue) }}</p>
                </div>
                <div class="bg-orange-100 p-3 rounded-full">
                  <svg class="w-8 h-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Product Sales Table -->
          <div class="bg-white rounded-xl shadow-md p-6 border border-gray-100">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-2xl font-bold text-gray-800">Số lượng đã bán theo sản phẩm</h2>
              <button 
                @click="loadDashboardStats" 
                class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition text-sm font-medium"
              >
                🔄 Làm mới
              </button>
            </div>
            
            <div v-if="stats.productSales && stats.productSales.length > 0" class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-4 py-3 text-left font-semibold text-gray-700">STT</th>
                    <th class="px-4 py-3 text-left font-semibold text-gray-700">Tên sản phẩm</th>
                    <th class="px-4 py-3 text-right font-semibold text-gray-700">Số lượng đã bán</th>
                    <th class="px-4 py-3 text-right font-semibold text-gray-700">Tổng doanh thu</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-200">
                  <tr 
                    v-for="(product, index) in stats.productSales" 
                    :key="product.productId"
                    class="hover:bg-gray-50 transition"
                  >
                    <td class="px-4 py-3 text-gray-600">{{ index + 1 }}</td>
                    <td class="px-4 py-3 font-medium text-gray-900">{{ product.productName || `Sản phẩm #${product.productId}` }}</td>
                    <td class="px-4 py-3 text-right font-semibold text-green-600">{{ formatNumber(product.totalQuantitySold) }}</td>
                    <td class="px-4 py-3 text-right font-semibold text-orange-600">{{ formatPrice(product.totalRevenue) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-center py-12 text-gray-500">
              <svg class="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
              </svg>
              <p>Chưa có dữ liệu bán hàng</p>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
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
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import adminService from '../services/admin.service.js'

const router = useRouter()
const loading = ref(true)
const error = ref(null)
const stats = ref({
  totalVisitors: 0,
  totalBuyers: 0,
  totalOrders: 0,
  totalRevenue: 0,
  productSales: []
})

// Format number with thousand separator
function formatNumber(num) {
  if (!num && num !== 0) return '0'
  return num.toLocaleString('vi-VN')
}

// Format price
function formatPrice(price) {
  if (!price && price !== 0) return '0₫'
  const numPrice = typeof price === 'string' ? parseFloat(price) : price
  return numPrice.toLocaleString('vi-VN') + '₫'
}

// Load dashboard statistics
async function loadDashboardStats() {
  loading.value = true
  error.value = null
  
  try {
    const data = await adminService.getDashboardStats()
    stats.value = {
      totalVisitors: data.totalVisitors || 0,
      totalBuyers: data.totalBuyers || 0,
      totalOrders: data.totalOrders || 0,
      totalRevenue: data.totalRevenue || 0,
      productSales: data.productSales || []
    }
  } catch (err) {
    console.error('Error loading dashboard stats:', err)
    error.value = err.message || 'Không thể tải dữ liệu thống kê'
  } finally {
    loading.value = false
  }
}

function navigateToProducts() {
  router.push('/admin/products')
}

function navigateToUsers() {
  router.push('/admin/users')
}

function navigateToOrders() {
  router.push('/admin/orders')
}

onMounted(() => {
  // Check authentication
  if (!adminService.isAuthenticated()) {
    router.push('/admin/login')
    return
  }
  
  // Load dashboard stats
  loadDashboardStats()
})
</script>
