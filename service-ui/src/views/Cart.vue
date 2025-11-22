<template>
  <div class="min-h-screen bg-gradient-to-br from-green-50 via-purple-50 to-indigo-50">
    <!-- Header Section -->
    <div class="bg-gradient-to-r from-green-500 via-purple-500 to-indigo-500 text-white relative overflow-hidden">
      <div class="absolute inset-0 bg-black bg-opacity-10"></div>
      <div class="absolute inset-0 bg-gradient-to-r from-green-500/20 to-purple-500/20"></div>
      <div class="relative max-w-7xl mx-auto px-4 py-12">
        <div class="flex items-center gap-4 mb-4" data-aos="fade-right">
          <router-link to="/" class="group flex items-center justify-center w-12 h-12 bg-white/20 backdrop-blur-sm rounded-full hover:bg-white/30 transition-all duration-300">
            <svg class="w-6 h-6 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
            </svg>
          </router-link>
    <div>
            <h1 class="text-3xl md:text-4xl font-bold mb-2">Giỏ hàng của bạn</h1>
            <p class="text-white/90 text-lg">{{ cartStore.items.length }} sản phẩm đang chờ thanh toán</p>
          </div>
        </div>
        
        <!-- Cart Stats -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8" data-aos="fade-up" data-aos-delay="200">
          <div class="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
            <div class="text-2xl font-bold">{{ cartStore.items.length }}</div>
            <div class="text-sm text-white/80">Sản phẩm</div>
          </div>
          <div class="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
            <div class="text-2xl font-bold">{{ totalItems }}</div>
            <div class="text-sm text-white/80">Số lượng</div>
          </div>
          <div class="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
            <div class="text-2xl font-bold">{{ formatPrice(subTotal) }}</div>
            <div class="text-sm text-white/80">Tạm tính</div>
          </div>
          <div class="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
            <div v-if="finalShippingFee > 0" class="text-2xl font-bold text-red-300">{{ formatPrice(finalShippingFee) }}</div>
            <div v-else class="text-2xl font-bold text-green-300">Miễn phí</div>
            <div class="text-sm text-white/80">Vận chuyển</div>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- Empty Cart -->
      <div v-if="cartStore.items.length === 0" class="text-center py-20" data-aos="fade-up">
        <div class="max-w-md mx-auto">
          <div class="w-32 h-32 mx-auto mb-8 bg-gradient-to-br from-green-100 to-purple-100 rounded-full flex items-center justify-center">
            <svg class="w-16 h-16 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-2.5 5M7 13l2.5 5m0 0h6m-6 0V9"></path>
            </svg>
          </div>
          <h3 class="text-2xl font-bold text-gray-800 mb-4">Giỏ hàng trống</h3>
          <p class="text-gray-600 mb-8 text-lg">Hãy khám phá những sản phẩm tuyệt vời của chúng tôi</p>
          <router-link
            to="/products"
            class="inline-flex items-center px-8 py-4 bg-gradient-to-r from-green-500 to-purple-500 text-white font-bold rounded-full hover:from-green-600 hover:to-purple-600 transform hover:scale-105 transition-all duration-300 shadow-lg hover:shadow-xl"
          >
            <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"></path>
            </svg>
            Khám phá sản phẩm
          </router-link>
        </div>
      </div>

      <!-- Cart Content -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cart Content Column -->
        <div class="lg:col-span-2">
          <!-- Cart Items Table Header -->
          <!-- Desktop Table -->
          <div v-if="cartStore.items.length > 0" class="hidden md:block">
            <div class="grid grid-cols-12 gap-2 px-6 py-3 bg-gray-50 border-b font-bold text-gray-700 text-sm">
              <div class="col-span-1 flex items-center justify-center">
                <input type="checkbox" :checked="selectedIds.length === cartStore.items.length" @change="toggleSelectAll" />
              </div>
              <div class="col-span-5">Sản phẩm</div>
              <div class="col-span-2 text-center">Đơn giá</div>
              <div class="col-span-2 text-center">Số lượng</div>
              <div class="col-span-1 text-center">Thành tiền</div>
              <div class="col-span-1 text-center">Xóa</div>
            </div>
            <div v-for="item in cartStore.items" :key="item.id" class="grid grid-cols-12 gap-2 items-center px-4 py-3 border-b hover:bg-gray-50 transition md:px-6">
              <div class="col-span-1 flex items-center justify-center">
                <input type="checkbox" :checked="selectedIds.includes(item.id)" @change="toggleSelect(item.id)" />
              </div>
              <div class="col-span-5 flex items-center gap-3">
                <img :src="getProductImage(item)" :alt="item.name" class="w-16 h-16 object-cover rounded-lg border" />
                <span class="font-semibold text-base text-gray-900 truncate">{{ item.name }}</span>
              </div>
              <div class="col-span-2 text-center">
                <span class="text-lg font-bold text-green-600">{{ formatPrice(item.price) }}</span>
                <span v-if="item.oldPrice" class="block text-xs text-gray-400 line-through">{{ formatPrice(item.oldPrice) }}</span>
              </div>
              <div class="col-span-2 flex items-center justify-center gap-2">
                <button @click="decreaseQuantity(item.id)" :disabled="(Number(item.quantity) || 1) <= 1" class="w-7 h-7 flex items-center justify-center rounded border border-gray-300 bg-white text-gray-700 hover:bg-gray-100 disabled:opacity-50">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path></svg>
                </button>
                <span class="w-8 text-center font-semibold">{{ Number(item.quantity) || 1 }}</span>
                <button @click="increaseQuantity(item.id)" class="w-7 h-7 flex items-center justify-center rounded border border-gray-300 bg-white text-gray-700 hover:bg-gray-100">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                </button>
              </div>
              <div class="col-span-1 text-center font-bold text-purple-600">
                {{ formatPrice((Number(item.price) || 0) * (Number(item.quantity) || 1)) }}
              </div>
              <div class="col-span-1 flex items-center justify-center">
                <button @click="removeItem(item.id)" class="p-2 rounded hover:bg-red-50 text-red-500" title="Xóa sản phẩm">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
              </div>
            </div>
          </div>
          <!-- Mobile Card Layout -->
          <div v-if="cartStore.items.length > 0" class="md:hidden space-y-4">
            <div v-for="item in cartStore.items" :key="item.id" class="flex gap-3 p-3 rounded-xl border bg-white shadow-sm items-center">
              <input type="checkbox" :checked="selectedIds.includes(item.id)" @change="toggleSelect(item.id)" class="mt-1" />
              <img :src="getProductImage(item)" :alt="item.name" class="w-16 h-16 object-cover rounded-lg border flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <div class="font-semibold text-base text-gray-900 break-words mb-1">{{ item.name }}</div>
                <div class="flex items-center gap-2">
                  <span class="text-lg font-bold text-green-600">{{ formatPrice(item.price) }}</span>
                  <span v-if="item.oldPrice" class="text-xs text-gray-400 line-through">{{ formatPrice(item.oldPrice) }}</span>
                </div>
                <div class="flex items-center gap-2 mt-2">
                  <button @click="decreaseQuantity(item.id)" :disabled="(Number(item.quantity) || 1) <= 1" class="w-7 h-7 flex items-center justify-center rounded border border-gray-300 bg-white text-gray-700 hover:bg-gray-100 disabled:opacity-50">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"></path></svg>
                  </button>
                  <span class="w-8 text-center font-semibold">{{ Number(item.quantity) || 1 }}</span>
                  <button @click="increaseQuantity(item.id)" class="w-7 h-7 flex items-center justify-center rounded border border-gray-300 bg-white text-gray-700 hover:bg-gray-100">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                  </button>
                  <button @click="removeItem(item.id)" class="ml-auto p-2 rounded hover:bg-red-50 text-red-500" title="Xóa sản phẩm">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                  </button>
                </div>
                <div class="text-right font-bold text-purple-600 mt-1">{{ formatPrice((Number(item.price) || 0) * (Number(item.quantity) || 1)) }}</div>
              </div>
            </div>
          </div>
        </div>
        <!-- Order Summary Column -->
        <div class="lg:col-span-1" data-aos="fade-left">
          <div class="sticky top-6">
            <div class="bg-white/90 backdrop-blur-sm rounded-2xl shadow-xl p-6 border border-white/20">
              <h2 class="text-xl font-bold text-gray-800 mb-6 flex items-center">
                <svg class="w-6 h-6 mr-3 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                </svg>
                Tóm tắt đơn hàng
              </h2>
              
              <!-- Order Details -->
              <div class="space-y-4 mb-6">
                <div class="order-row">
                  <span class="order-label">Tạm tính ({{ totalItems }} sản phẩm)</span>
                  <span class="order-value">{{ formatPrice(subTotal) }}</span>
                </div>
                <div class="order-row">
                  <span class="order-label">Phí vận chuyển</span>
                  <span v-if="finalShippingFee > 0" class="order-value text-red-600 font-semibold">{{ formatPrice(finalShippingFee) }}</span>
                  <span v-else class="order-value text-green-600 font-semibold">Miễn phí</span>
                </div>
                <div v-if="totalDiscount > 0" class="order-row">
                  <span class="order-label">Giảm giá sản phẩm</span>
                  <span class="order-value text-green-600 font-semibold">-{{ formatPrice(totalDiscount) }}</span>
                </div>
                <div v-if="appliedDiscount > 0" class="order-row">
                  <span class="order-label">Mã giảm giá</span>
                  <span class="order-value text-green-600 font-semibold">-{{ formatPrice(appliedDiscount) }}</span>
                </div>
                <div class="border-t border-gray-200 pt-4">
                  <div class="order-row text-lg font-bold">
                    <span class="text-gray-800">Tổng cộng</span>
                    <span class="text-purple-600">{{ formatPrice(total) }}</span>
                  </div>
                </div>
              </div>

              <!-- Discount Code -->
              <div class="discount-section">
                <label class="discount-label">
                  <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.99 1.99 0 013 12V7a4 4 0 014-4z"></path>
                  </svg>
                  Mã giảm giá
                </label>
                <div class="discount-input-container">
                  <input
                    v-model="discountCode"
                    type="text"
                    placeholder="Nhập mã giảm giá"
                    class="discount-input"
                  />
                  <button
                    @click="applyDiscount"
                    :disabled="!discountCode.trim()"
                    class="discount-btn"
                  >
                    Áp dụng
                  </button>
                </div>
                <p v-if="discountMessage" :class="['discount-message', discountSuccess ? 'success' : 'error']">
                  {{ discountMessage }}
                </p>
              </div>

              <!-- Checkout Button -->
              <router-link
                to="/checkout"
                class="checkout-btn"
                @click.native="prepareCheckout"
              >
                <svg class="w-6 h-6 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
                Tiến hành thanh toán
              </router-link>

              <!-- Security Badge -->
              <div class="security-badge">
                <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                </svg>
                Thanh toán bảo mật SSL
              </div>
            </div>
          </div>
        </div>
      </div>

          <!-- Continue Shopping -->
          <div class="mt-8 text-center" data-aos="fade-up">
            <router-link
              to="/products"
              class="inline-flex items-center px-6 py-3 text-purple-600 hover:text-purple-700 font-semibold bg-white/80 backdrop-blur-sm rounded-full hover:bg-white transition-all duration-300 shadow-md hover:shadow-lg"
            >
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
              </svg>
              Tiếp tục mua sắm
            </router-link>
          </div>
        </div>

    <!-- Success/Error Popup -->
    <div
      v-if="showPopup"
      class="notification-popup"
      :class="popupSuccess ? 'success' : 'error'"
    >
      <div class="notification-content">
        <svg v-if="popupSuccess" class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
        <svg v-else class="notification-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
        <span class="notification-text">{{ popupMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCartStore } from '../stores/cart'
import { getProductImage } from '../utils/productImage'

const cartStore = useCartStore()

// State: lưu id các sản phẩm được chọn
const selectedIds = ref([])

onMounted(() => {
  cartStore.loadFromStorage()
  // Mặc định chọn hết sản phẩm
  selectedIds.value = cartStore.items.map(item => item.id)
})

// Chọn/bỏ chọn từng sản phẩm
function toggleSelect(id) {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter(i => i !== id)
  } else {
    selectedIds.value.push(id)
  }
}
// Chọn/bỏ chọn tất cả
function toggleSelectAll() {
  if (selectedIds.value.length === cartStore.items.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = cartStore.items.map(item => item.id)
  }
}

// Danh sách sản phẩm được chọn
const selectedItems = computed(() => cartStore.items.filter(item => selectedIds.value.includes(item.id)))

// Reactive data
const discountCode = ref('')
const discountMessage = ref('')
const discountSuccess = ref(false)
const showPopup = ref(false)
const popupMessage = ref('')
const popupSuccess = ref(false)

// Computed properties
const subTotal = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    const qty = Number(item.quantity) || 1
    const price = Number(item.price) || 0
    return sum + (price * qty)
  }, 0)
})

const totalDiscount = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    if (item.oldPrice) {
      const qty = Number(item.quantity) || 1
      const oldPrice = Number(item.oldPrice) || 0
      const price = Number(item.price) || 0
      return sum + ((oldPrice - price) * qty)
    }
    return sum
  }, 0)
})

const appliedDiscount = computed(() => {
  if (!discountSuccess.value) return 0
  const code = discountCode.value.trim().toUpperCase()
  const validCodes = {
    'TINEE10': 0.1,
    'WELCOME20': 0.2,
    'FREESHIP': 0
  }
  const discountRate = validCodes[code] || 0
  return subTotal.value * discountRate
})

const shippingFee = computed(() => {
  const hasShippingProducts = selectedItems.value.some(item => item.id === 1 || item.id === 3)
  if (hasShippingProducts && subTotal.value <= 299000) {
    return 20000
  }
  return 0
})

const finalShippingFee = computed(() => {
  // Nếu có mã FREESHIP thì miễn phí vận chuyển
  if (discountCode.value.trim().toUpperCase() === 'FREESHIP' && discountSuccess.value) {
    return 0
  }
  return shippingFee.value
})

const total = computed(() => {
  return subTotal.value + finalShippingFee.value - appliedDiscount.value
})

const totalItems = computed(() => {
  return selectedItems.value.reduce((sum, item) => {
    const qty = Number(item.quantity) || 1
    return sum + qty
  }, 0)
})

// Methods
const formatPrice = (price) => {
  return price.toLocaleString('vi-VN') + '₫'
}

const increaseQuantity = (id) => {
  const item = cartStore.items.find(item => item.id === id)
  if (item) {
    const currentQty = Number(item.quantity) || 1
    cartStore.updateQuantity(id, currentQty + 1)
    resetDiscount() // Reset discount khi thay đổi số lượng
    showNotification('Đã cập nhật số lượng sản phẩm', true)
  }
}

const decreaseQuantity = (id) => {
  const item = cartStore.items.find(item => item.id === id)
  if (item) {
    const currentQty = Number(item.quantity) || 1
    if (currentQty > 1) {
      cartStore.updateQuantity(id, currentQty - 1)
      resetDiscount() // Reset discount khi thay đổi số lượng
      showNotification('Đã cập nhật số lượng sản phẩm', true)
    }
  }
}

const removeItem = (id) => {
  const item = cartStore.items.find(item => item.id === id)
  if (item) {
    cartStore.removeItem(id)
    // Cập nhật selectedIds khi xóa item
    selectedIds.value = selectedIds.value.filter(itemId => itemId !== id)
    resetDiscount() // Reset discount khi xóa item
    showNotification(`Đã xóa "${item.name}" khỏi giỏ hàng!`, false)
  }
}

const applyDiscount = () => {
  const code = discountCode.value.trim().toUpperCase()
  
  // Mock discount codes
  const validCodes = {
    'TINEE10': { discount: 0.1, message: 'Giảm 10% cho đơn hàng' },
    'WELCOME20': { discount: 0.2, message: 'Giảm 20% cho khách hàng mới' },
    'FREESHIP': { discount: 0, message: 'Miễn phí vận chuyển (đã áp dụng)' }
  }
  
  if (validCodes[code]) {
    discountMessage.value = validCodes[code].message
    discountSuccess.value = true
    showNotification('Mã giảm giá đã được áp dụng!', true)
  } else {
    discountMessage.value = 'Mã giảm giá không hợp lệ'
    discountSuccess.value = false
    showNotification('Mã giảm giá không hợp lệ', false)
  }
}

// Reset discount when items change
const resetDiscount = () => {
  discountCode.value = ''
  discountMessage.value = ''
  discountSuccess.value = false
}

const showNotification = (message, success = true) => {
  popupMessage.value = message
  popupSuccess.value = success
  showPopup.value = true
  setTimeout(() => {
    showPopup.value = false
  }, 3000)
}

function prepareCheckout() {
  // Lưu các sản phẩm được chọn vào localStorage
  localStorage.setItem('checkoutSelectedItems', JSON.stringify(selectedItems.value))
}

// Lifecycle
onMounted(async () => {
  cartStore.loadFromStorage()
  // Cập nhật giá sản phẩm từ API
  await cartStore.updateProductPrices()
})
</script> 

<style scoped>
/* Cart Item Styles */
.cart-item-wrapper {
  padding: 2rem;
  transition: all 0.3s ease;
}

.cart-item-wrapper:hover {
  background: linear-gradient(135deg, #fdf2f8 0%, #f3e8ff 100%);
}

.cart-item-container {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.cart-item-image {
  flex-shrink: 0;
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 1rem;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, rgba(236, 72, 153, 0.8), rgba(139, 92, 246, 0.8));
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-wrapper:hover .product-image {
  transform: scale(1.1);
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.overlay-content {
  transform: translateY(10px);
  transition: transform 0.3s ease;
}

.image-wrapper:hover .overlay-content {
  transform: translateY(0);
}

.cart-item-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.product-description {
  color: #6b7280;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.features-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.feature-tag {
  background: linear-gradient(45deg, #fdf2f8, #f3e8ff);
  color: #be185d;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid #f9a8d4;
}

.price-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.current-price {
  font-size: 1.5rem;
  font-weight: 800;
  background: linear-gradient(45deg, #ec4899, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.old-price {
  color: #9ca3af;
  text-decoration: line-through;
  font-size: 0.875rem;
}

.discount-badge {
  background: linear-gradient(45deg, #10b981, #059669);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  font-weight: 600;
}

.controls-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.quantity-label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.quantity-controls {
  display: flex;
  align-items: center;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quantity-btn {
  padding: 0.75rem;
  color: #6b7280;
  transition: all 0.3s ease;
  background: transparent;
  border: none;
  cursor: pointer;
}

.quantity-btn:hover:not(:disabled) {
  background: linear-gradient(45deg, #fdf2f8, #f3e8ff);
  color: #ec4899;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-display {
  padding: 0.75rem 1rem;
  font-weight: 700;
  color: #111827;
  min-width: 3rem;
  text-align: center;
  background: #f9fafb;
}

.remove-btn {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #ef4444;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 0.75rem;
  font-weight: 600;
  transition: all 0.3s ease;
  cursor: pointer;
}

.remove-btn:hover {
  background: #fee2e2;
  color: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
}

.item-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 0.75rem;
  border: 1px solid #e2e8f0;
}

.total-label {
  font-weight: 600;
  color: #64748b;
}

.total-amount {
  font-size: 1.25rem;
  font-weight: 800;
  background: linear-gradient(45deg, #ec4899, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Order Summary Styles */
.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
}

.order-label {
  color: #6b7280;
  font-weight: 500;
}

.order-value {
  font-weight: 600;
  color: #111827;
}

.discount-section {
  margin-bottom: 2rem;
}

.discount-label {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.75rem;
}

.discount-input-container {
  display: flex;
  gap: 0.5rem;
}

.discount-input {
  flex: 1;
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.75rem;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.discount-input:focus {
  outline: none;
  border-color: #ec4899;
  box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.1);
}

.discount-btn {
  padding: 0.875rem 1.5rem;
  background: linear-gradient(45deg, #ec4899, #8b5cf6);
  color: white;
  border: none;
  border-radius: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.discount-btn:hover:not(:disabled) {
  background: linear-gradient(45deg, #db2777, #7c3aed);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(236, 72, 153, 0.3);
}

.discount-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.discount-message {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.discount-message.success {
  color: #059669;
}

.discount-message.error {
  color: #dc2626;
}

.checkout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 1rem 1.5rem;
  background: linear-gradient(45deg, #ec4899, #8b5cf6);
  color: white;
  font-weight: 700;
  font-size: 1.125rem;
  border-radius: 0.75rem;
  text-decoration: none;
  transition: all 0.3s ease;
  margin-bottom: 1rem;
}

.checkout-btn:hover {
  background: linear-gradient(45deg, #db2777, #7c3aed);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(236, 72, 153, 0.4);
}

.security-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Notification Popup */
.notification-popup {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 50;
  padding: 1rem 1.5rem;
  border-radius: 0.75rem;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  max-width: 20rem;
  animation: slideIn 0.3s ease;
}

.notification-popup.success {
  background: linear-gradient(45deg, #10b981, #059669);
  color: white;
}

.notification-popup.error {
  background: linear-gradient(45deg, #ef4444, #dc2626);
  color: white;
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.notification-icon {
  width: 1.25rem;
  height: 1.25rem;
  flex-shrink: 0;
}

.notification-text {
  font-weight: 600;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Mobile Responsive Styles */
@media (max-width: 768px) {
  .cart-item-container {
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }
  
  .cart-item-image {
    align-self: center;
  }
  
  .product-image {
    width: 100px;
    height: 100px;
  }
  
  .cart-item-info {
    width: 100%;
    text-align: center;
  }
  
  .product-name {
    font-size: 1.125rem;
  }
  
  .features-container {
    justify-content: center;
  }
  
  .price-container {
    justify-content: center;
  }
  
  .controls-container {
    flex-direction: column;
    gap: 1rem;
  }
  
  .quantity-controls {
    width: 100%;
    max-width: 200px;
  }
  
  .remove-btn {
    width: 100%;
    max-width: 200px;
    justify-content: center;
  }
  
  .discount-input-container {
    flex-direction: column;
  }
  
  .discount-btn {
    width: 100%;
  }
  
  .notification-popup {
    top: 1rem;
    right: 1rem;
    left: 1rem;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .cart-item-wrapper {
    padding: 1rem;
  }
  
  .product-image {
    width: 80px;
    height: 80px;
  }
  
  .product-name {
    font-size: 1rem;
  }
  
  .current-price {
    font-size: 1.25rem;
  }
  
  .quantity-btn {
    padding: 0.5rem;
  }
  
  .quantity-display {
    padding: 0.5rem 0.75rem;
  }
  
  .remove-btn {
    padding: 0.5rem 0.75rem;
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.cart-item-wrapper {
  animation: fadeIn 0.6s ease;
}
</style> 