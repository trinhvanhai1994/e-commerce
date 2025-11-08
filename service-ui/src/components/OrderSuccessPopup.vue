<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  orderId: {
    type: String,
    required: true
  },
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])
const router = useRouter()

function continueShopping() {
  emit('close')
  router.push('/products')
}

function viewOrderDetails() {
  emit('close')
  router.push(`/order-success/${props.orderId}`)
}

function closePopup() {
  emit('close')
}
</script>

<template>
  <transition name="fade">
    <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999] p-4" @click.self="closePopup">
      <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full mx-4 transform transition-all duration-300 scale-100" @click.stop>
        <!-- Success Icon -->
        <div class="text-center pt-8 pb-4">
          <div class="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800 mb-2">Đặt hàng thành công!</h2>
          <p class="text-gray-600 mb-1">Cảm ơn bạn đã tin tưởng Thi Yên</p>
          <p class="text-sm text-gray-500">Mã đơn hàng: <span class="font-semibold text-green-600">#{{ orderId }}</span></p>
        </div>

        <!-- Message -->
        <div class="px-8 pb-6">
          <div class="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <div class="text-sm">
                <p class="font-semibold text-yellow-800 mb-1">Thông báo quan trọng:</p>
                <p class="text-yellow-700">Chúng tôi sẽ liên hệ với bạn trong vòng 24 giờ để xác nhận đơn hàng và thông tin giao hàng.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="px-8 pb-8 space-y-3">
          <button @click="viewOrderDetails" class="w-full bg-green-500 text-white py-3 rounded-full font-bold hover:bg-green-600 transition-all duration-200 flex items-center justify-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
            Xem chi tiết đơn hàng
          </button>
          <button @click="continueShopping" class="w-full bg-gray-100 text-gray-700 py-3 rounded-full font-bold hover:bg-gray-200 transition-all duration-200 flex items-center justify-center gap-2">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.4 9.4m0 0L17 22"/>
            </svg>
            Tiếp tục mua hàng
          </button>
        </div>

        <!-- Close button -->
        <button @click="closePopup" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-active .bg-white,
.fade-leave-active .bg-white {
  transition: transform 0.3s ease;
}

.fade-enter-from .bg-white {
  transform: scale(0.9);
}

.fade-leave-to .bg-white {
  transform: scale(0.9);
}
</style> 