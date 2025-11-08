<template>
  <div>
    <!-- Hero Section - Outside container -->
    <section class="hero-section relative w-full overflow-hidden">
      <!-- Background Image -->
      <img 
        src="/images/me/banner.jpg" 
        alt="HealthCare Banner" 
        class="w-full h-auto object-cover"
        @error="handleBannerError"
      />
      
      <!-- Content Overlay -->
      <div class="absolute inset-0 flex items-center justify-center">
        <div class="text-center" data-aos="fade-up">
          <!-- <div class="logo-container mb-3 md:mb-6 lg:mb-8">
            <div class="circle-logo bg-white rounded-full w-10 h-10 sm:w-12 sm:h-12 md:w-20 md:h-20 lg:w-24 lg:h-24 mx-auto flex items-center justify-center shadow-lg">
              <img src="/images/logo/logo.png" alt="Logo" class="w-8 h-8 sm:w-10 sm:h-10 md:w-12 md:h-12 lg:w-14 lg:h-14 object-contain transition-transform hover:scale-105" />
            </div>
          </div> -->
          <h3 class="text-lg sm:text-xl md:text-3xl lg:text-4xl font-bold text-white mb-2 md:mb-4 lg:mb-4" style="text-shadow: 0 2px 4px rgba(0,0,0,0.1);">
            TRIẾT LÝ DƯỠNG SINH
          </h3>
          <p class="text-xs sm:text-sm md:text-2xl lg:text-4xl text-teal-500 font-medium tracking-wider leading-relaxed transform hover:scale-105 transition-all duration-300 ease-in-out drop-shadow-sm px-4" style="font-family: 'Great Vibes', cursive;">
            <span class="text-white text-sm sm:text-base md:text-2xl lg:text-4xl">Dưỡng sinh - lối sống thuận tự nhiên từ ngàn xưa</span>
          </p>
        </div>
      </div>
    </section>

    <div class="healthcare-page" style="background-color: #fefce8;">

    <!-- Story Section -->
    <section class="story-section py-12">
      <div class="container mx-auto px-4">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-16" data-aos="fade-up">
            <h2 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">TRIẾT LÝ DƯỠNG SINH</h2>
            <div class="w-24 h-1 bg-green-500 mx-auto"></div>
          </div>
          
          <div class="mx-auto text-gray-700 leading-relaxed text-base md:text-lg" data-aos="fade-up" data-aos-delay="200">
            <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-8 mb-4">Dưỡng sinh - lối sống thuận tự nhiên từ ngàn xưa</h4>
            
            <div class="mb-6 text-base md:text-lg">
              Từ hơn hai ngàn năm trước, trong nền y học cổ phương Đông, dưỡng sinh đã hình thành như một lối sống thấm đẫm triết lý âm dương, ngũ hành với niềm tin rằng: muốn thân vững, tâm phải yên; muốn sống khỏe phải thuận theo đất trời. Toàn bộ triết lý dưỡng sinh Đông phương cổ đại đều xoay quanh một điều cốt lõi: <strong>sức khỏe không nằm ở thứ tìm bên ngoài, mà ở cách ta sống mỗi ngày.</strong>
            </div>
            
            <div class="mb-6 text-base md:text-lg">
              Mỗi hành động trong ngày, mỗi lựa chọn nhỏ đều là một phần của dưỡng sinh: ăn uống điều độ, sinh hoạt theo mùa, làm việc có chừng, giữ tâm yên, lòng thanh.
            </div>
            
            <div class="mb-6 text-base md:text-lg">
              Và trong lối sống ấy, <strong>thực phẩm không chỉ là cái ăn</strong>, mà là cái nuôi: nuôi thân, nuôi khí, điều hòa tạng phủ. Mỗi loại hạt, mỗi loại đậu đều mang một tính chất riêng: ấm hay mát, bổ khí hay dưỡng huyết, dưỡng nhan để nâng đỡ cơ thể theo từng mùa, từng thể trạng, từng nhịp sống.
            </div>
            
            <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-8 mb-4">Dưỡng sinh trong nếp sống người Việt</h4>
            
            <div class="mb-6 text-base md:text-lg">
              Trước cả khi được gọi tên, <strong>dưỡng sinh đã là một phần tự nhiên trong đời sống người Việt</strong>, âm thầm, giản dị nhưng bền bỉ: ăn theo mùa, giữ ấm khi trở gió, dùng gừng nướng lúc cảm hàn, cháo tía tô khi sốt, nước đỗ đen giải nhiệt, hạt sen giúp ngủ ngon… Những điều tưởng chừng bình dị ấy, chính là dưỡng sinh, nuôi thân, an tâm, thuận tự nhiên.
            </div>
            
            <h4 class="text-xl md:text-2xl font-bold text-gray-800 mt-8 mb-4">Gieo lại một nếp sống lành từ những điều tưởng chừng đã cũ</h4>
            
            <div class="mb-6 text-base md:text-lg">
              Từ nền tảng dưỡng sinh truyền thống, Thi Yên chọn kết hợp cùng những phương pháp hiện đại nhằm giữ trọn dưỡng chất, đồng thời phù hợp hơn với nhịp sống ngày nay.
            </div>
            
            <div class="mb-6 text-base md:text-lg">
              Chúng tôi tin rằng, truyền thống không nằm lại trong quá khứ. Nó sống động khi bước vào đời sống hôm nay trong hình thức mới, nhưng vẫn giữ linh hồn cũ.
            </div>
            
            <div class="text-base md:text-lg">
              <strong>Dưỡng sinh không phải là triết lý cao siêu, cũng không chỉ dành riêng cho người lớn tuổi.</strong> Đó là lựa chọn sống lành mà ai cũng có thể bắt đầu từ những điều rất giản dị: ăn một bữa sáng đủ đầy, uống 1 cốc nước ấm, ít đường, chọn thực phẩm nguyên bản, không chế biến quá đà. 
            </div>
            
            <div class="text-base md:text-lg">
              Chỉ cần vậy ta đã gieo lại cho mình một nếp sống lành - vững bền, mộc mạc và đủ để lan tỏa.
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Values Section -->
    <section class="core-values-section py-8">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16" data-aos="fade-up">
          <h3 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">NGUYÊN TẮC DƯỠNG SINH</h3>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>
        
        <div class="grid md:grid-cols-3 gap-8">
          <!-- Value 1 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="100">
              <div class="text-center mb-6">
                <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                  <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                  <img src="/images/icons/tu-nhien.png" alt="Thuận tự nhiên" class="w-32 h-32 object-contain relative z-10" />
                </div>
                <h3 class="text-xl font-bold text-gray-800 mb-4">Thuận tự nhiên</h3>
              </div>
              <div class="text-gray-700 leading-relaxed text-base md:text-lg">
                Sống theo nhịp điệu tự nhiên, ăn theo mùa, nghỉ ngơi đúng giờ, lắng nghe cơ thể và tâm hồn để điều chỉnh lối sống phù hợp.
              </div>
            </div>

          <!-- Value 2 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="200">
              <div class="text-center mb-6">
                <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                  <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                  <img src="/images/icons/can-bang.png" alt="Điều hòa âm dương" class="w-32 h-32 object-contain relative z-10" />
                </div>
                <h3 class="text-xl font-bold text-gray-800 mb-4">Điều hòa âm dương</h3>
              </div>
              <div class="text-gray-700 leading-relaxed text-base md:text-lg">
                Cân bằng giữa động và tĩnh, làm việc và nghỉ ngơi, thực phẩm ấm và mát để giữ hài hòa trong cơ thể.
              </div>
            </div>

          <!-- Value 3 -->
          <div class="value-card bg-white rounded-lg shadow-lg p-8" data-aos="fade-up" data-aos-delay="300">
            <div class="text-center mb-6">
              <div class="relative w-32 h-32 mx-auto mb-4 flex items-center justify-center">
                <div class="w-24 h-24 bg-green-50 rounded-full absolute"></div>
                <img src="/images/icons/tien-loi.png" alt="Giản dị bền vững" class="w-32 h-32 object-contain relative z-10" />
              </div>
              <h3 class="text-xl font-bold text-gray-800 mb-4">Giản dị bền vững</h3>
            </div>
            <div class="text-gray-700 leading-relaxed text-base md:text-lg">
              Chọn lối sống đơn giản, không cầu kỳ phức tạp, tập trung vào những điều cốt lõi và duy trì lâu dài.
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Nutritional Values Section -->
    <section class="nutrition-section py-8">
      <div class="container mx-auto px-4">
        <div class="text-center mb-16" data-aos="fade-up">
          <h3 class="text-3xl md:text-4xl font-bold text-green-600 mb-4">DƯỠNG SINH QUA THỰC PHẨM</h3>
          <div class="w-24 h-1 bg-green-500 mx-auto"></div>
        </div>
        
        <div class="grid md:grid-cols-5 gap-6">
          <!-- Value 1 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="100">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🌾</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Ăn theo mùa</h3>
            <p class="text-sm text-gray-600">Chọn thực phẩm phù hợp</p>
          </div>

          <!-- Value 2 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="200">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">⚖️</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Điều độ</h3>
            <p class="text-sm text-gray-600">Không quá nhiều, không quá ít</p>
          </div>

          <!-- Value 3 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="300">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🌿</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Nguyên bản</h3>
            <p class="text-sm text-gray-600">Ít chế biến, giữ nguyên chất</p>
          </div>

          <!-- Value 4 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="400">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🍵</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Ấm bụng</h3>
            <p class="text-sm text-gray-600">Thực phẩm ấm, dễ tiêu hóa</p>
          </div>

          <!-- Value 5 -->
          <div class="nutrition-card text-center" data-aos="zoom-in" data-aos-delay="500">
            <div class="nutrition-icon w-20 h-20 bg-green-100 rounded-full mx-auto mb-4 flex items-center justify-center">
              <span class="text-3xl">🕐</span>
            </div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">Đúng giờ</h3>
            <p class="text-sm text-gray-600">Ăn uống theo nhịp sinh học</p>
          </div>
        </div>
      </div>
    </section>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HealthCare',
  methods: {
    handleBannerError(event) {
      console.log('Banner image failed to load, using fallback background')
      // Hide the image and show fallback background
      event.target.style.display = 'none'
    }
  },
  mounted() {
    // Initialize AOS if available
    if (typeof AOS !== 'undefined') {
      AOS.refresh()
    }
  }
}
</script>

<style scoped>
.healthcare-page {
  overflow-x: hidden;
}

/* Hero section improvements */
.hero-section {
  position: relative;
  width: 100vw;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  background-color: #fefce8; /* Fallback background */
}

/* Ensure banner image displays properly */
.hero-section img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
  object-position: center;
}

/* Ensure text is always readable */
.hero-section h1,
.hero-section p {
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* Smooth transitions for responsive elements */
.circle-logo,
.hero-section h1,
.hero-section p {
  transition: all 0.3s ease;
}

.value-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.value-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.nutrition-card {
  transition: transform 0.3s ease;
}

.nutrition-card:hover {
  transform: scale(1.05);
}

.nutrition-icon {
  transition: all 0.3s ease;
}

.nutrition-card:hover .nutrition-icon {
  background-color: #f97316;
  transform: rotate(5deg);
}

.circle-logo {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* Responsive adjustments for hero section */
@media (max-width: 640px) {
  .logo-container {
    margin-bottom: 0.5rem !important;
  }
  
  .circle-logo {
    width: 3rem !important;
    height: 3rem !important;
  }
  
  .circle-logo img {
    width: 1.5rem !important;
    height: 1.5rem !important;
  }
  
  h3 {
    font-size: 1.125rem !important;
    margin-bottom: 0.375rem !important;
    line-height: 1.2 !important;
  }
  
  p {
    font-size: 0.6875rem !important;
    line-height: 1.3 !important;
    padding: 0 0.75rem !important;
  }
}

@media (min-width: 641px) and (max-width: 768px) {
  .logo-container {
    margin-bottom: 0.75rem !important;
  }
  
  .circle-logo {
    width: 3.5rem !important;
    height: 3.5rem !important;
  }
  
  .circle-logo img {
    width: 2rem !important;
    height: 2rem !important;
  }
  
  h3 {
    font-size: 1.375rem !important;
    margin-bottom: 0.5rem !important;
  }
  
  p {
    font-size: 0.8125rem !important;
    line-height: 1.4 !important;
  }
}

@media (min-width: 769px) and (max-width: 1023px) {
  .logo-container {
    margin-bottom: 2rem !important;
  }
  
  .circle-logo {
    width: 8rem !important;
    height: 8rem !important;
  }
  
  .circle-logo img {
    width: 7rem !important;
    height: 7rem !important;
  }
  
  h3 {
    font-size: 3.5rem !important;
    margin-bottom: 1.5rem !important;
  }
  
  p {
    font-size: 1.75rem !important;
    line-height: 1.6 !important;
  }
}

@media (min-width: 1024px) {
  /* Desktop styles if needed */
}

/* Hide scrollbar */
::-webkit-scrollbar {
  display: none;
}

/* For Firefox */
* {
  scrollbar-width: none;
}

/* For IE and Edge */
* {
  -ms-overflow-style: none;
}
</style> 