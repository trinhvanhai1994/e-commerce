<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductImage } from '../utils/productImage'
import { orderAPI } from '../utils/api'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref(null)

// Debug mode for development
const isDev = ref(import.meta.env.DEV)

// Computed properties for safe data access
const orderId = computed(() => order.value?.orderId || 'N/A')
const orderDate = computed(() => order.value?.createdAt ? formatDate(order.value.createdAt) : 'N/A')
const orderStatus = computed(() => order.value?.status === 'confirmed' ? 'Đã xác nhận' : 'Chờ xác nhận')
const orderTotal = computed(() => order.value?.total ? formatPrice(order.value.total) : 'N/A')
const hasItems = computed(() => order.value?.items && order.value.items.length > 0)
const hasCustomerInfo = computed(() => !!order.value?.customerInfo)

onMounted(async () => {
  // Get order details from route params
  const orderId = route.params.orderId
  console.log('OrderSuccess mounted with orderId:', orderId) // Debug log
  
  if (orderId) {
    await loadOrderDetails(orderId)
  } else {
    console.error('No orderId provided in route params')
    error.value = 'Không có mã đơn hàng'
  }
  loading.value = false
})

// Load order details from API
async function loadOrderDetails(orderId) {
  try {
    console.log('Loading order details for ID:', orderId) // Debug log
    
    const response = await orderAPI.getOrderById(orderId)
    console.log('Order API Response:', response) // Debug log
    console.log('Response type:', typeof response) // Debug log
    console.log('Response keys:', Object.keys(response || {})) // Debug log
    
    // Handle different response formats
    if (response && response.success && response.order) {
      // Format: {success: true, order: {...}}
      console.log('Using response.order format')
      order.value = response.order
    } else if (response && response.data) {
      // Format: {data: {...}}
      console.log('Using response.data format')
      order.value = response.data
    } else if (response && response.order) {
      // Format: {order: {...}}
      console.log('Using response.order format (no success)')
      order.value = response.order
    } else if (response && response.id) {
      // Format: {...} (direct order object)
      console.log('Using direct response format')
      order.value = response
    } else {
      console.error('Unexpected order response format:', response)
      console.error('Response structure:', JSON.stringify(response, null, 2))
      
      // Fallback to localStorage if API fails
      const savedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]')
      const foundOrder = savedOrders.find(o => o.id === orderId)
      if (foundOrder) {
        console.log('Using fallback from localStorage')
        order.value = foundOrder
      } else {
        console.error('No order found in localStorage either')
        error.value = 'Không tìm thấy thông tin đơn hàng'
      }
    }
    
    // Debug log final order value
    console.log('Final order value:', order.value)
    
    // Validate order data
    if (order.value) {
      console.log('Order validation:')
      console.log('- Has id:', !!order.value.id)
      console.log('- Has customerInfo:', !!order.value.customerInfo)
      console.log('- Has items:', !!order.value.items && Array.isArray(order.value.items))
      console.log('- Has total:', !!order.value.total)
      console.log('- Has createdAt:', !!order.value.createdAt)
      
      // Check if order has required fields
      if (!order.value.id || !order.value.customerInfo || !order.value.items || !order.value.total) {
        console.error('Order missing required fields')
        error.value = 'Dữ liệu đơn hàng không đầy đủ'
        order.value = null
      }
    } else {
      console.error('Order is null after loading')
      error.value = 'Không thể tải thông tin đơn hàng'
    }
    
  } catch (err) {
    console.error('Error loading order details:', err)
    error.value = err.message || 'Không thể tải thông tin đơn hàng'
    
    // Fallback to localStorage
    const savedOrders = JSON.parse(localStorage.getItem('completedOrders') || '[]')
    const foundOrder = savedOrders.find(o => o.id === orderId)
    if (foundOrder) {
      console.log('Using fallback from localStorage after error')
      order.value = foundOrder
    }
  }
}

function formatPrice(val) {
  return val.toLocaleString('vi-VN') + '₫'
}

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function continueShopping() {
  router.push('/products')
}

function goHome() {
  router.push('/')
}

// Hàm lấy tên địa chỉ từ mã
function getAddressText(provinceCode, districtCode, wardCode) {
  // Dữ liệu địa chỉ tĩnh để hiển thị
  const provinces = {
    '01': 'Hà Nội', '02': 'Hà Giang', '04': 'Cao Bằng', '06': 'Bắc Kạn', '08': 'Tuyên Quang',
    '10': 'Lào Cai', '11': 'Điện Biên', '12': 'Lai Châu', '14': 'Sơn La', '15': 'Yên Bái',
    '17': 'Hòa Bình', '19': 'Thái Nguyên', '20': 'Lạng Sơn', '22': 'Quảng Ninh', '24': 'Bắc Giang',
    '25': 'Phú Thọ', '26': 'Vĩnh Phúc', '27': 'Bắc Ninh', '30': 'Hải Dương', '31': 'Hải Phòng',
    '33': 'Hưng Yên', '34': 'Thái Bình', '35': 'Hà Nam', '36': 'Nam Định', '37': 'Ninh Bình',
    '38': 'Thanh Hóa', '40': 'Nghệ An', '42': 'Hà Tĩnh', '44': 'Quảng Bình', '45': 'Quảng Trị',
    '46': 'Thừa Thiên Huế', '48': 'Đà Nẵng', '49': 'Quảng Nam', '51': 'Quảng Ngãi', '52': 'Bình Định',
    '54': 'Phú Yên', '56': 'Khánh Hòa', '58': 'Ninh Thuận', '60': 'Bình Thuận', '62': 'Kon Tum',
    '64': 'Gia Lai', '66': 'Đắk Lắk', '67': 'Đắk Nông', '68': 'Lâm Đồng', '70': 'Bình Phước',
    '72': 'Tây Ninh', '74': 'Bình Dương', '75': 'Đồng Nai', '77': 'Bà Rịa - Vũng Tàu', '79': 'Hồ Chí Minh',
    '80': 'Long An', '82': 'Tiền Giang', '83': 'Bến Tre', '84': 'Trà Vinh', '86': 'Vĩnh Long',
    '87': 'Đồng Tháp', '89': 'An Giang', '91': 'Kiên Giang', '92': 'Cần Thơ', '93': 'Hậu Giang',
    '94': 'Sóc Trăng', '95': 'Bạc Liêu', '96': 'Cà Mau'
  }
  
  const provinceName = provinces[provinceCode] || 'Tỉnh/Thành phố'
  const districtName = districtCode ? `Quận/Huyện ${districtCode}` : ''
  const wardName = wardCode ? `Xã/Phường ${wardCode}` : ''
  
  return [provinceName, districtName, wardName].filter(Boolean).join(', ')
}

// Hàm hiển thị địa chỉ giao hàng đầy đủ
function getFullDeliveryAddress(customerInfo) {
  if (!customerInfo) return ''
  return customerInfo.address;
}

</script>

<template>
  <div class="min-h-screen bg-yellow-50 py-4 md:py-8">
    <div class="max-w-4xl mx-auto px-2 md:px-4">
      <div v-if="loading" class="text-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto"></div>
        <p class="mt-4 text-gray-600">Đang tải thông tin đơn hàng...</p>
      </div>

      <div v-else-if="error" class="text-center py-12">
        <div class="bg-white rounded-xl shadow-lg p-8">
          <svg class="w-16 h-16 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"/>
          </svg>
          <h2 class="text-2xl font-bold text-gray-800 mb-4">Lỗi tải thông tin</h2>
          <p class="text-gray-600 mb-6">{{ error }}</p>
          <button @click="loadOrderDetails(route.params.orderId)" class="bg-green-500 text-white px-6 py-3 rounded-full font-bold hover:bg-green-600 transition mr-4">
            Thử lại
          </button>
          <button @click="goHome" class="bg-gray-500 text-white px-6 py-3 rounded-full font-bold hover:bg-gray-600 transition">
            Về trang chủ
          </button>
        </div>
      </div>

      <div v-else-if="!order" class="text-center py-12">
        <div class="bg-white rounded-xl shadow-lg p-8">
          <svg class="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
          <h2 class="text-2xl font-bold text-gray-800 mb-4">Không tìm thấy đơn hàng</h2>
          <p class="text-gray-600 mb-6">Đơn hàng bạn đang tìm không tồn tại hoặc đã bị xóa.</p>
          <button @click="goHome" class="bg-green-500 text-white px-6 py-3 rounded-full font-bold hover:bg-green-600 transition">
            Về trang chủ
          </button>
        </div>
      </div>

      <div v-else class="space-y-6">
        <!-- Validation Error -->
        <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-4">
          <h3 class="text-sm font-semibold text-red-800 mb-2">Lỗi:</h3>
          <p class="text-sm text-red-700">{{ error }}</p>
        </div>

        <!-- Header -->
        <div class="bg-white rounded-xl shadow-lg p-4 md:p-6">
          <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-4 gap-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </div>
              <div>
                <h1 class="text-xl md:text-2xl font-bold text-gray-800">Đặt hàng thành công!</h1>
                <p class="text-gray-600 text-sm md:text-base">Cảm ơn bạn đã tin tưởng Thi Yên</p>
              </div>
            </div>
            <div class="text-left md:text-right">
              <p class="text-sm text-gray-500">Mã đơn hàng</p>
              <p class="text-lg font-bold text-green-600 break-all">#{{ orderId }}</p>
            </div>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-3 md:gap-4 mt-6">
            <div class="bg-gray-50 rounded-lg p-3 md:p-4">
              <p class="text-xs md:text-sm text-gray-500 mb-1">Ngày đặt hàng</p>
              <p class="font-semibold text-sm md:text-base">{{ orderDate }}</p>
            </div>
            <div class="bg-gray-50 rounded-lg p-3 md:p-4">
              <p class="text-xs md:text-sm text-gray-500 mb-1">Trạng thái</p>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                {{ orderStatus }}
              </span>
            </div>
            <div class="bg-gray-50 rounded-lg p-3 md:p-4">
              <p class="text-xs md:text-sm text-gray-500 mb-1">Tổng tiền</p>
              <p class="font-bold text-lg text-green-600">{{ orderTotal }}</p>
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="bg-white rounded-xl shadow-lg p-4 md:p-6">
          <h2 class="text-lg md:text-xl font-bold text-gray-800 mb-4">Chi tiết đơn hàng</h2>
          <div v-if="hasItems" class="space-y-3 md:space-y-4">
            <div v-for="item in order.items" :key="item.id" class="flex items-center gap-3 md:gap-4 py-3 md:py-4 border-b border-gray-100 last:border-b-0">
              <img :src="getProductImage(item.id)" :alt="item.name" class="w-12 h-12 md:w-16 md:h-16 object-cover rounded-lg flex-shrink-0">
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-800 text-sm md:text-base truncate">{{ item.name }}</h3>
                <p class="text-xs md:text-sm text-gray-600">Số lượng: {{ item.quantity }}</p>
              </div>
              <div class="text-right flex-shrink-0">
                <p class="font-bold text-gray-800 text-sm md:text-base">{{ formatPrice(item.price * item.quantity) }}</p>
                <p class="text-xs md:text-sm text-gray-500">{{ formatPrice(item.price) }} x {{ item.quantity }}</p>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-gray-500">
            <p>Không có sản phẩm nào trong đơn hàng</p>
          </div>
          
          <!-- Order Summary -->
          <div v-if="order" class="mt-6 pt-4 border-t border-gray-200">
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600">Tạm tính:</span>
                <span class="font-semibold">{{ order.subTotal ? formatPrice(order.subTotal) : 'N/A' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Phí vận chuyển:</span>
                <span class="font-semibold" :class="order.shippingFee > 0 ? 'text-red-600' : 'text-green-600'">
                  {{ order.shippingFee > 0 ? formatPrice(order.shippingFee) : 'Miễn phí' }}
                </span>
              </div>
              <div class="flex justify-between text-lg font-bold border-t pt-2">
                <span>Tổng cộng:</span>
                <span class="text-green-600">{{ orderTotal }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Customer Info -->
        <div v-if="hasCustomerInfo" class="bg-white rounded-xl shadow-lg p-4 md:p-6">
          <h2 class="text-lg md:text-xl font-bold text-gray-800 mb-4">Thông tin giao hàng</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
            <div>
              <h3 class="font-semibold text-gray-800 mb-2 text-sm md:text-base">Người nhận</h3>
              <p class="text-gray-600 text-sm md:text-base">{{ order.customerInfo.name || 'N/A' }}</p>
              <p class="text-gray-600 text-sm md:text-base">{{ order.customerInfo.phone || 'N/A' }}</p>
              <p v-if="order.customerInfo.email" class="text-gray-600 text-sm md:text-base">{{ order.customerInfo.email }}</p>
            </div>
            <div>
              <h3 class="font-semibold text-gray-800 mb-2 text-sm md:text-base">Địa chỉ giao hàng</h3>
              <p class="text-gray-600 text-sm md:text-base">{{ getFullDeliveryAddress(order.customerInfo) }}</p>
            </div>
          </div>
          <div v-if="order.customerInfo.notes" class="mt-4">
            <h3 class="font-semibold text-gray-800 mb-2 text-sm md:text-base">Ghi chú</h3>
            <p class="text-gray-600 bg-gray-50 p-3 rounded-lg text-sm md:text-base">{{ order.customerInfo.notes }}</p>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex flex-col sm:flex-row gap-3 md:gap-4 justify-center px-4 md:px-0">
          <button @click="continueShopping" class="bg-green-500 text-white px-6 md:px-8 py-3 rounded-full font-bold hover:bg-green-600 transition flex items-center justify-center gap-2 text-sm md:text-base">
            <svg class="w-4 h-4 md:w-5 md:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.4 9.4m0 0L17 22"/>
            </svg>
            Tiếp tục mua hàng
          </button>
          <button @click="goHome" class="bg-gray-500 text-white px-6 md:px-8 py-3 rounded-full font-bold hover:bg-gray-600 transition flex items-center justify-center gap-2 text-sm md:text-base">
            <svg class="w-4 h-4 md:w-5 md:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
            </svg>
            Về trang chủ
          </button>
        </div>
      </div>
    </div>
  </div>
</template> 