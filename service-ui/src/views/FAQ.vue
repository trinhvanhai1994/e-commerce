<template>
  <div class="min-h-screen bg-yellow-50">
    <!-- Content Section -->
    <div class="max-w-4xl mx-auto px-4 py-12">
      <div class="mb-8">
        <h1 class="text-3xl md:text-4xl font-bold mb-4 text-green-700">Câu hỏi thường gặp</h1>
        <p class="text-gray-600 text-lg">Giải đáp các thắc mắc phổ biến về sản phẩm và dịch vụ</p>
      </div>
      
      <div class="space-y-6">
        <!-- FAQ Item -->
        <div 
          v-for="(faq, index) in faqs" 
          :key="index"
          class="bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 overflow-hidden"
        >
          <button
            @click="toggleFAQ(index)"
            class="w-full px-6 py-4 text-left flex justify-between items-center hover:bg-green-50/50 transition-colors duration-200"
          >
            <h3 class="text-lg font-semibold text-green-700">{{ faq.question }}</h3>
            <svg 
              :class="['w-6 h-6 text-green-500 transition-transform duration-200', { 'rotate-180': faq.isOpen }]"
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>
          <div 
            v-show="faq.isOpen"
            class="px-6 pb-4 text-gray-700 leading-relaxed bg-white/30 backdrop-blur-sm"
          >
            <div v-html="faq.answer"></div>
          </div>
        </div>
      </div>

      <!-- Contact Support -->
      <div class="mt-12 p-6 bg-gradient-to-r from-green-50 to-green-100 rounded-xl border border-green-200 shadow-md">
        <h3 class="text-lg font-semibold text-green-700 mb-2 flex items-center gap-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
          </svg>
          Không tìm thấy câu trả lời?
        </h3>
        <p class="text-green-700 mb-3">
          Liên hệ với chúng tôi để được hỗ trợ trực tiếp:
        </p>
        <div class="flex flex-col sm:flex-row gap-4">
          <div class="flex items-center gap-2">
            <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
            </svg>
            <span class="text-green-700 font-semibold">HOTLINE: 0396860584</span>
          </div>
          <div class="flex items-center gap-2">
            <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
            </svg>
            <span class="text-green-700 font-semibold">thiyen.vietnam@gmail.com</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const faqs = ref([
  {
    question: "Sản phẩm của Thi Yên có nguồn gốc từ đâu?",
    answer: "Tất cả sản phẩm của Thi Yên đều được sản xuất từ nguyên liệu thuần tự nhiên, không chất bảo quản, được chọn lọc kỹ lưỡng từ các vùng nguyên liệu chất lượng cao tại Việt Nam.",
    isOpen: false
  },
  {
    question: "Làm thế nào để bảo quản sản phẩm đúng cách?",
    answer: "Sản phẩm nên được bảo quản ở nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp. Sau khi mở nắp, nên sử dụng trong vòng 30 ngày để đảm bảo chất lượng tốt nhất.",
    isOpen: false
  },
  {
    question: "Sản phẩm có phù hợp với trẻ em không?",
    answer: "Sản phẩm của Thi Yên hoàn toàn tự nhiên và an toàn cho trẻ em từ 3 tuổi trở lên. Tuy nhiên, bạn nên tham khảo ý kiến bác sĩ trước khi sử dụng cho trẻ nhỏ.",
    isOpen: false
  },
  {
    question: "Thời gian giao hàng là bao lâu?",
    answer: "Thời gian giao hàng thường từ 2-5 ngày làm việc tùy theo khu vực. Đối với khu vực nội thành Hà Nội và TP.HCM, thời gian giao hàng từ 1-2 ngày.",
    isOpen: false
  },
  {
    question: "Làm thế nào để kiểm tra tính xác thực của sản phẩm?",
    answer: "Mỗi sản phẩm đều có tem chống hàng giả và QR code để kiểm tra. Bạn có thể quét mã QR hoặc liên hệ hotline để xác minh tính xác thực của sản phẩm.",
    isOpen: false
  }
])

const toggleFAQ = (index) => {
  faqs.value[index].isOpen = !faqs.value[index].isOpen
}
</script>

<style scoped>
/* Custom styles for FAQ animations */
</style> 