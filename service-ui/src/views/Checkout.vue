<template>
  <div class="max-w-4xl mx-auto px-4 py-4 md:py-8">
    <!-- Header với nút X -->
    <div class="flex justify-between items-center mb-6">
      <div></div>
      <h1 class="text-2xl font-bold text-center text-gray-900 dark:text-gray-100 transition-colors duration-300">THANH TOÁN</h1>
      <button @click="router.push('/products')" class="text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 transition-colors duration-300">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-8">
      <!-- Thông tin khách hàng -->
      <div class="space-y-4">
        <!-- Họ và tên -->
        <div>
          <label class="block text-sm font-medium mb-1 text-gray-700 dark:text-gray-300 transition-colors duration-300">Họ và tên</label>
          <input 
            v-model="form.name" 
            class="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:border-blue-500 dark:focus:border-blue-400 transition-colors duration-300"
            placeholder="Nhập họ và tên"
          />
          <div v-if="errors.name" class="text-red-500 text-xs mt-1">{{ errors.name }}</div>
        </div>

        <!-- Số điện thoại -->
        <div>
          <label class="block text-sm font-medium mb-1">Số điện thoại</label>
          <input 
            v-model="form.phone" 
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500"
            placeholder="Nhập số điện thoại"
          />
          <div v-if="errors.phone" class="text-red-500 text-xs mt-1">{{ errors.phone }}</div>
        </div>

        <!-- Địa chỉ chi tiết -->
        <div>
          <label class="block text-sm font-medium mb-1">Địa chỉ chi tiết:</label>
          <input 
            v-model="form.address" 
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500"
            placeholder="Số nhà, tên đường, phường/xã"
          />
          <div v-if="errors.address" class="text-red-500 text-xs mt-1">{{ errors.address }}</div>
        </div>

        <!-- Chọn địa chỉ -->
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium mb-1">Tỉnh/Thành phố</label>
            <select 
              v-model="form.province" 
              @change="onProvinceChange"
              :disabled="loadingProvinces"
              class="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:border-blue-500 dark:focus:border-blue-400 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:text-gray-500 dark:disabled:text-gray-400 appearance-none relative z-10 transition-colors duration-300"
            >
              <option value="">{{ loadingProvinces ? 'Đang tải...' : 'Chọn tỉnh/thành phố' }}</option>
              <option v-for="province in provinces" :key="province.code" :value="province.code">
                {{ province.name }}
              </option>
            </select>
            <div v-if="errors.province" class="text-red-500 text-xs mt-1">{{ errors.province }}</div>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1">Quận/Huyện</label>
            <select 
              v-model="form.district" 
              @change="onDistrictChange"
              :disabled="!form.province || loadingDistricts"
              class="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:border-blue-500 dark:focus:border-blue-400 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:text-gray-500 dark:disabled:text-gray-400 appearance-none relative z-10 transition-colors duration-300"
            >
              <option value="">{{ loadingDistricts ? 'Đang tải...' : 'Chọn quận/huyện' }}</option>
              <option v-for="district in filteredDistricts" :key="district.code" :value="district.code">
                {{ district.name }}
              </option>
            </select>
            <div v-if="errors.district" class="text-red-500 text-xs mt-1">{{ errors.district }}</div>
          </div>

        <div>
            <label class="block text-sm font-medium mb-1">Xã/Phường</label>
            <select 
              v-model="form.ward" 
              :disabled="!form.district || loadingWards"
              class="w-full border border-gray-300 dark:border-gray-600 rounded-lg px-3 py-2 text-sm bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:outline-none focus:border-blue-500 dark:focus:border-blue-400 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:text-gray-500 dark:disabled:text-gray-400 appearance-none relative z-10 transition-colors duration-300"
            >
              <option value="">{{ loadingWards ? 'Đang tải...' : 'Chọn xã/phường' }}</option>
              <option v-for="ward in filteredWards" :key="ward.code" :value="ward.code">
                {{ ward.name }}
              </option>
            </select>
            <div v-if="errors.ward" class="text-red-500 text-xs mt-1">{{ errors.ward }}</div>
          </div>
        </div>

        <!-- Ghi chú -->
        <!-- <div>
          <label class="block text-sm font-medium mb-1">Ghi chú</label>
          <textarea 
            v-model="form.notes" 
            class="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-blue-500"
            placeholder="Ghi chú cho đơn hàng (không bắt buộc)"
            rows="3"
          ></textarea>
        </div> -->
      </div>

      <!-- Tóm tắt đơn hàng -->
      <div class="space-y-4">
        <h2 class="font-bold text-lg mb-4">THÔNG TIN ĐƠN HÀNG</h2>
        
                 <!-- Danh sách sản phẩm -->
        <div class="bg-gray-50 rounded-lg p-4 space-y-3">
          <div v-for="item in cartItems" :key="item.id" class="flex items-center justify-between">
            <div class="flex items-center space-x-3">
              <img :src="getProductImage(item.id)" :alt="item.name" class="w-12 h-12 object-cover rounded" />
              <div>
                <div class="font-medium text-sm">{{ item.name }}</div>
                <!-- Thay đổi số lượng -->
                <div class="flex items-center space-x-2 mt-1">
                  <span class="text-gray-500 text-xs">Số lượng:</span>
                  <div class="flex items-center border border-gray-300 rounded">
                    <button 
                      @click="updateQuantity(item.id, -1)" 
                      class="px-2 py-1 text-gray-500 hover:bg-gray-100 text-xs"
                      :disabled="item.quantity <= 1"
                    >
                      -
                    </button>
                    <span class="px-2 py-1 text-sm font-medium">{{ item.quantity }}</span>
                    <button 
                      @click="updateQuantity(item.id, 1)" 
                      class="px-2 py-1 text-gray-500 hover:bg-gray-100 text-xs"
                    >
                      +
                    </button>
             </div>
            </div>
              </div>
            </div>
            <div class="flex flex-col items-end">
              <span class="text-sm font-medium">{{ formatPrice(item.price) }}</span>
              <span v-if="item.oldPrice && item.oldPrice > item.price" class="text-xs text-gray-500 line-through">{{ formatPrice(item.oldPrice) }}</span>
            </div>
              <button @click="removeItem(item.id)" class="text-red-500 hover:text-red-700">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
          </div>
        </div>

        <!-- Tổng tiền -->
        <div class="bg-gray-50 rounded-lg p-4 space-y-2">
          <div class="flex justify-between">
            <span>Tạm tính:</span>
            <span>{{ formatPrice(subTotal) }}</span>
          </div>
          <div v-if="totalDiscount > 0" class="flex justify-between text-green-600">
            <span>Tiết kiệm:</span>
            <span>-{{ formatPrice(totalDiscount) }}</span>
          </div>
          <div v-if="shippingFee > 0" class="flex justify-between">
            <span>Phí vận chuyển:</span>
            <span>{{ formatPrice(shippingFee) }}</span>
          </div>
          <div class="border-t pt-2 flex justify-between font-bold text-lg">
            <span>Tổng cộng:</span>
            <span>{{ formatPrice(cartTotal) }}</span>
          </div>
        </div>

        <!-- Thông tin thanh toán -->
        <div class="text-center mb-4">
          <h3 class="text-lg font-bold text-gray-800">Thanh toán khi nhận hàng (COD)</h3>
        </div>

        <!-- Nút đặt hàng -->
        <button 
          @click="placeOrder" 
          :disabled="cartItems.length === 0"
          class="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-3 px-4 rounded-lg transition-colors"
        >
          ĐẶT HÀNG
        </button>

        <!-- Thông tin bảo mật -->
        <div class="text-center mt-4">
          <p class="text-xs text-gray-600 leading-relaxed">
            Thông tin cá nhân bạn cung cấp sẽ được bảo mật và chỉ dùng để xử lý đơn hàng, hỗ trợ bạn trong quá trình mua sắm theo chính sách bảo mật của chúng tôi.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useOrderStore } from '../stores/order'
import { getProductImage } from '../utils/productImage'
import { orderAPI } from '../utils/api'
import locationService from '../services/location.service.js'

const router = useRouter()
const cartStore = useCartStore()
const orderStore = useOrderStore()

// Form data
const form = ref({
  name: '',
  address: '',
  province: '',
  district: '',
  ward: '',
  phone: '',
  notes: ''
})

const errors = ref({})
const cartItems = ref([])

// Dữ liệu địa chỉ từ API
const provinces = ref([])
const districts = ref([])
const wards = ref([])
const loadingProvinces = ref(false)
const loadingDistricts = ref(false)
const loadingWards = ref(false)

// Hàm lấy danh sách tỉnh/thành phố
async function fetchProvinces() {
  loadingProvinces.value = true
  try {
    const data = await locationService.getProvinces()
    provinces.value = data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách tỉnh:', error)
  } finally {
    loadingProvinces.value = false
  }
}

// Hàm lấy danh sách quận/huyện
async function fetchDistricts(provinceCode) {
  if (!provinceCode) return
  loadingDistricts.value = true
  try {
    const data = await locationService.getDistricts(provinceCode)
    districts.value = data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách quận/huyện:', error)
  } finally {
    loadingDistricts.value = false
  }
}

// Hàm lấy danh sách xã/phường
async function fetchWards(districtCode) {
  if (!districtCode) return
  loadingWards.value = true
  try {
    const data = await locationService.getWards(districtCode)
    wards.value = data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách xã/phường:', error)
  } finally {
    loadingWards.value = false
  }
}

// Computed properties cho districts và wards
const filteredDistricts = computed(() => {
  return districts.value
})

const filteredWards = computed(() => {
  return wards.value
})

// Hàm xử lý khi thay đổi tỉnh
async function onProvinceChange() {
  form.value.district = ''
  form.value.ward = ''
  districts.value = []
  wards.value = []
  if (form.value.province) {
    await fetchDistricts(form.value.province)
  }
}

// Hàm xử lý khi thay đổi huyện
async function onDistrictChange() {
  form.value.ward = ''
  wards.value = []
  if (form.value.district) {
    await fetchWards(form.value.district)
  }
}

// Hàm cập nhật số lượng sản phẩm
function updateQuantity(itemId, change) {
  const item = cartItems.value.find(item => item.id === itemId)
  if (item) {
    const newQuantity = item.quantity + change
    if (newQuantity > 0) {
      item.quantity = newQuantity
    } else if (newQuantity === 0) {
      removeItem(itemId)
    }
  }
}

// Hàm xóa sản phẩm
function removeItem(itemId) {
  cartItems.value = cartItems.value.filter(item => item.id !== itemId)
  if (cartItems.value.length === 0) {
    router.push('/cart')
  }
}

// Validation
function validateForm() {
  errors.value = {}
  if (!form.value.name) errors.value.name = 'Vui lòng nhập họ và tên'
  if (!form.value.address) errors.value.address = 'Vui lòng nhập địa chỉ'
  if (!form.value.province) errors.value.province = 'Vui lòng chọn tỉnh/thành phố'
  if (!form.value.district) errors.value.district = 'Vui lòng chọn quận/huyện'
  if (!form.value.ward) errors.value.ward = 'Vui lòng chọn xã/phường'
  if (!form.value.phone) errors.value.phone = 'Vui lòng nhập số điện thoại'
  return Object.keys(errors.value).length === 0
}

// Đặt hàng
async function placeOrder() {
  if (!validateForm()) return
  
  // Prepare order data - only send required fields according to API spec
  const orderData = {
    customerInfo: {
      name: form.value.name,
      address: form.value.address,
      province: form.value.province,
      district: form.value.district,
      ward: form.value.ward,
      phone: form.value.phone
    },
    items: cartItems.value.map(item => ({
      productId: item.id,
      quantity: item.quantity
    }))
  }
  
  try {
    // Call API to create order
    const response = await orderAPI.createOrder(orderData)
    console.log('Create Order API Response:', response) // Debug log
    
    // Handle ApiResponse format: {success: true, data: {orderId: "...", message: "..."}}
    // Prefer orderId field, fallback to id for backward compatibility
    let orderId = null
    if (response && response.orderId) {
      // Direct orderId in response (after ServiceApiAdapter processing)
      orderId = response.orderId
    } else if (response && response.data && response.data.orderId) {
      // Nested in data
      orderId = response.data.orderId
    } else if (response && response.id) {
      // Fallback to id (backward compatibility)
      orderId = response.id
    } else if (response && response.data && response.data.id) {
      // Fallback to nested id
      orderId = response.data.id
    } else {
      console.error('Unexpected create order response format:', response)
      throw new Error('Không thể tạo đơn hàng - response format không hợp lệ')
    }
    
    if (orderId) {
      // Clear cart items used in checkout
      localStorage.removeItem('checkoutSelectedItems')
      localStorage.removeItem('checkoutSingleItem')
      
      // Redirect to success page with order ID
      router.push(`/order-success/${orderId}`)
    } else {
      throw new Error('Không nhận được orderId từ API')
    }
    
  } catch (error) {
    console.error('Error creating order:', error)
    alert('Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.')
  }
}

// Computed properties
const subTotal = computed(() => cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0))
const originalTotal = computed(() => cartItems.value.reduce((total, item) => total + (item.oldPrice || item.price) * item.quantity, 0))
const totalDiscount = computed(() => originalTotal.value - subTotal.value)

const shippingFee = computed(() => {
  // Thêm phí ship 20.000đ cho sản phẩm id = 1 hoặc 3 nếu đơn hàng dưới hoặc bằng 299.000đ
  const hasShippingProducts = cartItems.value.some(item => item.id === 1 || item.id === 3)
  if (hasShippingProducts && subTotal.value <= 299000) {
    return 20000
  }
  return 0
})

const cartTotal = computed(() => subTotal.value + shippingFee.value)
const formatPrice = (val) => val.toLocaleString('vi-VN') + '₫'

// Lifecycle
onMounted(async () => {
  // Lấy danh sách tỉnh/thành phố
  await fetchProvinces()
  
  // Ưu tiên lấy sản phẩm được chọn từ cart nếu có
  const selected = localStorage.getItem('checkoutSelectedItems')
  if (selected) {
    try {
      const arr = JSON.parse(selected)
      if (Array.isArray(arr) && arr.length > 0) {
        cartItems.value = arr
        return
      }
    } catch (e) {}
  }
  // Ưu tiên lấy sản phẩm mua ngay nếu có
  const singleItem = localStorage.getItem('checkoutSingleItem')
  if (singleItem) {
    try {
      cartItems.value = [JSON.parse(singleItem)]
    } catch (e) {
      cartItems.value = []
    }
  } else {
    const savedCart = localStorage.getItem('cartItems')
    if (savedCart) {
      try {
        cartItems.value = JSON.parse(savedCart)
      } catch (e) {
        cartItems.value = []
      }
    }
  }
})
</script>

<style scoped>
/* Fix select dropdown trên mobile */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23333' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1rem;
  padding-right: 2.5rem;
}

/* Fix select trên mobile để không bị lệch */
@media (max-width: 768px) {
  select {
    position: relative;
    z-index: 10;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    -webkit-tap-highlight-color: transparent;
    min-height: 2.5rem;
  }
  
  /* Đảm bảo select container không bị overflow */
  .space-y-3 > div {
    position: relative;
    width: 100%;
    max-width: 100%;
    overflow: visible;
    box-sizing: border-box;
  }
  
  /* Fix layout cho mobile */
  .grid.grid-cols-1.lg\\:grid-cols-2 {
    gap: 1.5rem;
  }
  
  /* Đảm bảo form không bị overflow */
  .space-y-4 {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  /* Fix cho container chính */
  .max-w-4xl {
    width: 100%;
    padding-left: 1rem;
    padding-right: 1rem;
    box-sizing: border-box;
  }
  
  /* Đảm bảo label và select align đúng */
  label {
    display: block;
    width: 100%;
    box-sizing: border-box;
  }
}

/* Fix cho iOS Safari */
@supports (-webkit-touch-callout: none) {
  select {
    -webkit-appearance: none;
    appearance: none;
  }
}
</style> 