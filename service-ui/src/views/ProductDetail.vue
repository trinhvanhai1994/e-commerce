<template>
  <div v-if="product" class="min-h-screen bg-gray-50 dark:bg-gray-900 pb-2 transition-colors duration-300">
    <!-- Breadcrumb -->
    <div class="max-w-7xl mx-auto px-4 py-2">
      <nav class="text-sm text-gray-600 dark:text-gray-400 transition-colors duration-300">
        <router-link to="/" class="hover:text-green-600 dark:hover:text-green-400 transition-colors duration-300">Trang chủ</router-link>
        <span class="mx-2">/</span>
        <span class="text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ product.name }}</span>
      </nav>
    </div>
    <div class="max-w-7xl mx-auto px-1 py-1 md:px-4 md:py-2">
        <!-- Main Product Section -->
        <div class="bg-white dark:bg-gray-800 rounded-none shadow-none md:rounded-lg md:shadow-lg overflow-hidden transition-colors duration-300">
          <div class="grid grid-cols-1 xl:grid-cols-2 gap-1 md:gap-8 p-1 md:p-6">
                      <!-- Left Column - Product Images -->
            <div class="flex flex-col lg:flex-row gap-1 md:gap-4">
            <!-- Vertical Thumbnail Gallery - Hidden on mobile -->
            <div class="hidden lg:flex flex-col space-y-3 w-20 xl:w-24">
              <!-- Gallery Header -->
              <div class="text-center">
                <div class="bg-gradient-to-r from-green-500 to-green-600 text-white px-2 xl:px-3 py-1 rounded-full text-xs font-semibold shadow-lg">
                  {{ galleryImages.length }} ảnh
                </div>
              </div>
              
              <!-- Scrollable Gallery Container -->
              <div class="flex flex-col space-y-2 max-h-80 xl:max-h-96 overflow-y-auto custom-scrollbar pr-1">
                <div
                  v-for="(image, index) in galleryImages"
                  :key="index"
                  @click="selectImage(image, index)"
                  :class="[
                    'relative aspect-square bg-gradient-to-br from-white to-gray-50 dark:from-gray-800 dark:to-gray-700 rounded-xl overflow-hidden cursor-pointer transition-all duration-300 group border-2',
                    currentImageIndex === index
                      ? 'border-green-500 dark:border-green-400 shadow-lg transform scale-105 ring-2 ring-green-200 dark:ring-green-800'
                      : 'border-gray-200 dark:border-gray-600 hover:border-green-300 dark:hover:border-green-500 hover:shadow-md hover:scale-102'
                  ]"
                >
                  <img
                    :src="image"
                    :alt="`${product.name} ${index + 1}`"
                    class="w-full h-full object-cover transition-all duration-300 group-hover:scale-110"
                    @error="handleImageError"
                    :key="`thumb-${index}-${imageReloadKey}`"
                  />
                  
                  <!-- Overlay -->
                  <div class="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                  
                  <!-- Selected indicator with animation -->
                  <div v-if="currentImageIndex === index" class="absolute top-1.5 right-1.5 bg-green-500 text-white rounded-full p-1 shadow-lg animate-bounce">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"></path>
                    </svg>
                  </div>
                  
                  <!-- Image number with better styling -->
                  <div class="absolute bottom-1.5 left-1.5 bg-black/70 backdrop-blur-sm text-white text-xs px-2 py-0.5 rounded-full font-medium">
                    {{ index + 1 }}
                  </div>
                  
                  <!-- Hover effect overlay -->
                  <div class="absolute inset-0 border-2 border-green-400 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                  
                  <!-- Play icon for selected image -->
                  <div v-if="currentImageIndex === index" class="absolute inset-0 flex items-center justify-center">
                    <div class="bg-green-500/80 backdrop-blur-sm rounded-full p-1 opacity-0 group-hover:opacity-100 transition-all duration-300">
                      <svg class="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"></path>
                        <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"></path>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Main Image Carousel -->
            <div class="flex-1 relative main-carousel">
              <div class="relative overflow-hidden rounded-none md:rounded-lg shadow-none md:shadow-lg bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-700 p-0 md:p-4 group transition-colors duration-300">
                <!-- Carousel Container with touch support -->
                <div 
                  class="relative h-[400px] md:h-64 lg:h-80 overflow-hidden rounded-none md:rounded-lg touch-pan-x aspect-square"
                  @touchstart="handleTouchStart"
                  @touchmove="handleTouchMove"
                  @touchend="handleTouchEnd"
                >
                  <div 
                    class="flex transition-transform duration-500 ease-in-out h-full"
                    :style="{ transform: `translateX(-${currentImageIndex * 100}%)` }"
                  >
                    <div
                      v-for="(image, index) in galleryImages"
                      :key="index"
                      class="w-full flex-shrink-0 relative"
                    >
                      <img
                        :src="image"
                        :alt="`${product.name} ${index + 1}`"
                        class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                        @error="handleImageError"
                        :key="`main-${index}-${imageReloadKey}`"
                      />
                    </div>
                  </div>
                </div>
                
                <!-- Navigation Arrows - Hidden on mobile -->
                <button
                  @click="previousImage"
                  class="hidden md:block absolute left-2 top-1/2 -translate-y-1/2 bg-white/80 backdrop-blur-sm hover:bg-white text-gray-700 hover:text-gray-900 rounded-full p-2 shadow-lg transition-all duration-200 opacity-0 group-hover:opacity-100 hover:scale-110"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
                  </svg>
                </button>
                
                <button
                  @click="nextImage"
                  class="hidden md:block absolute right-2 top-1/2 -translate-y-1/2 bg-white/80 backdrop-blur-sm hover:bg-white text-gray-700 hover:text-gray-900 rounded-full p-2 shadow-lg transition-all duration-200 opacity-0 group-hover:opacity-100 hover:scale-110"
                >
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                  </svg>
                </button>
                
                <!-- Overlay gradient -->
                <div class="absolute inset-0 bg-gradient-to-t from-black/5 to-transparent rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"></div>
                
                <!-- Banners dưới chân ảnh, nằm ngoài carousel -->
                <div class="flex justify-between items-end px-0 py-1 mt-1 gap-1">
                  <!-- Flash Sale Banner -->
                  <div class="bg-gradient-to-r from-orange-500 to-red-600 text-white rounded-lg shadow-lg overflow-hidden banner-flash-sale flex-1 mr-1 max-w-full min-w-0">
                    <div class="flex items-center px-2 sm:px-3 py-1 relative">
                      <div class="flex-1 min-w-0">
                        <div class="flex items-baseline gap-1">
                          <span class="text-xs font-bold truncate">{{ formatPrice(product.price) }}</span>
                          <span class="text-[10px] sm:text-xs line-through opacity-70">{{ formatPrice(product.oldPrice) }}</span>
                        </div>
                        <div class="bg-red-600 text-white text-xs px-1 sm:px-2 py-0.5 rounded mt-1 w-fit animate-pulse">
                          Tiết kiệm tới {{ flashSaleDiscountPercent }}%
                        </div>
                      </div>
                      <div class="text-right ml-1 sm:ml-2 flex-shrink-0">
                        <div class="text-xs font-bold">Flash Sale</div>
                        <div class="text-xs opacity-90 hidden sm:block">Kết thúc sau</div>
                        <div class="text-xs font-mono bg-white/20 px-1 py-0.5 rounded mt-1 whitespace-nowrap">
                          {{ formatFlashSaleTime(flashSaleLeft) }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <!-- XTRA Banner -->
                  <div class="bg-gradient-to-r from-teal-500 to-blue-500 text-white rounded-lg shadow-lg overflow-hidden banner-xtra flex items-center px-1 sm:px-2 py-1 flex-none min-w-[80px] sm:min-w-[110px] max-w-[35%] sm:max-w-[38%]">
                    <div class="text-sm sm:text-lg font-bold mr-1 sm:mr-2">XTRA</div>
                    <div class="text-xs">
                      <div>Shipping</div>
                      <div>Voucher</div>
                    </div>
                  </div>
                </div>

                <!-- Original Discount Badge -->
                <div v-if="product.discount" class="absolute top-16 md:top-20 left-1 md:left-6 bg-gradient-to-r from-red-500 to-red-600 text-white px-1 md:px-4 py-0.5 md:py-2 rounded-full text-xs font-bold shadow-lg animate-pulse">
                  <span class="flex items-center gap-1">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    -{{ product.discount }}%
                  </span>
                </div>
                
                <div v-if="product.isNew" class="absolute top-1 md:top-6 right-1 md:right-6 bg-gradient-to-r from-green-500 to-green-600 text-white px-1 md:px-4 py-0.5 md:py-2 rounded-full text-xs font-bold shadow-lg">
                  <span class="flex items-center gap-1">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3z"></path>
                    </svg>
                    Mới
                  </span>
                </div>

                <!-- Auto-play indicator - Hidden on mobile -->
                <div class="hidden md:block absolute bottom-4 right-4 bg-white/80 backdrop-blur-sm rounded-full p-2 opacity-0 group-hover:opacity-100 transition-all duration-300">
                  <button @click="toggleAutoPlay" class="text-gray-600 hover:text-gray-900">
                    <svg v-if="isAutoPlaying" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zM13 8a1 1 0 012 0v4a1 1 0 11-2 0V8z"></path>
                    </svg>
                    <svg v-else class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"></path>
                    </svg>
                  </button>
                </div>

                <!-- Swipe indicator on mobile - Hidden to save space -->
                <div class="hidden md:block absolute bottom-2 right-2 bg-white/80 backdrop-blur-sm rounded-full px-2 py-1 text-xs text-gray-600 flex items-center gap-1">
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16l-4-4m0 0l4-4m-4 4h18"></path>
                  </svg>
                  <span>Vuốt</span>
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"></path>
                  </svg>
                </div>
              </div>

              <!-- Dots indicator - Smaller on mobile -->
              <div class="flex justify-center mt-2 md:mt-8 space-x-1 md:space-x-2">
                <button
                  v-for="(image, index) in galleryImages"
                  :key="index"
                  @click="selectImage(image, index)"
                  :class="[
                    'w-1.5 h-1.5 md:w-2 md:h-2 rounded-full transition-all duration-300',
                    currentImageIndex === index
                      ? 'bg-green-500 w-4 md:w-6'
                      : 'bg-gray-300 hover:bg-gray-400'
                  ]"
                ></button>
              </div>

              <!-- Additional Product Images - Vertical Layout for Desktop -->
              <div class="hidden xl:flex flex-col gap-3 mt-16">
                <div
                  v-for="(image, idx) in galleryImages.slice(1, 6)"
                  :key="'gallery-xl-' + idx"
                  class="relative group cursor-pointer overflow-hidden rounded-lg aspect-square"
                >
                  <img
                    :src="image"
                    :alt="`${product.name} chi tiết ${idx + 2}`"
                    class="w-full h-full object-contain transition-transform duration-300 group-hover:scale-105"
                    @error="handleImageError"
                  />
                  <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition-all duration-300"></div>
                </div>
                <div v-if="galleryImages.length <= 1" class="text-center text-gray-400 text-xs py-6">
                  Không có ảnh chi tiết
                </div>
              </div>
              
            </div>
          </div>

          <!-- Right Column - Product Details -->
          <div class="space-y-4 md:space-y-6 lg:pl-4">
            <!-- Product Title and Rating -->
            <div>
              <h1 class="text-base md:text-lg lg:text-xl font-bold text-gray-900 dark:text-gray-100 mb-2 transition-colors duration-300">{{ product.name }}</h1>
              <div class="flex items-center gap-2 mb-3">
                <div class="flex items-center">
                  <span class="text-base md:text-lg font-bold text-gray-900 dark:text-gray-100 mr-2 transition-colors duration-300">5</span>
                  <div class="flex text-yellow-400 dark:text-yellow-500 transition-colors duration-300">
                    <svg v-for="i in 5" :key="i" class="w-3 h-3 fill-current" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                  </div>
                  <span class="ml-2 text-xs text-gray-600 dark:text-gray-400 transition-colors duration-300">5 trên tổng 5 sao (dựa trên {{ product.reviewCount || 32 }} đánh giá)</span>
                </div>
              </div>
            </div>

            <!-- Price Section -->
            <div class="space-y-2">
              <div class="flex items-center gap-2">
                <span class="text-lg md:text-xl font-bold text-red-500 dark:text-red-400 transition-colors duration-300">{{ formatPrice(product.price) }}</span>
                <span v-if="product.oldPrice > product.price" class="text-sm text-gray-500 dark:text-gray-400 line-through transition-colors duration-300">{{ formatPrice(product.oldPrice) }}</span>
                <div v-if="product.discount > 0" class="bg-red-500 text-white px-2 py-1 rounded-full text-xs font-bold">
                  -{{ product.discount }}%
                </div>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center gap-1">
                <span v-if="product.discount > 0" class="text-xs text-green-600 font-semibold">
                  • Tiết kiệm {{ formatPrice(product.oldPrice - product.price) }}
                </span>
              </div>
              <!-- Phân loại sản phẩm: 4 sản phẩm chính -->
              <div class="mt-2">
                <div class="font-bold text-sm mb-2">Sản Phẩm:
                  <span class="font-normal">{{ product.name }}</span>
                </div>
                <div class="flex gap-2 flex-wrap">
                  <button
                    v-for="item in products"
                    :key="item.id"
                    @click="navigateToProduct(item.id)"
                    :class="[
                      'px-4 py-2 rounded-lg border text-sm transition',
                      selectedProductId === Number(item.id)
                        ? 'bg-green-100 border-green-500 text-green-700 font-bold shadow'
                        : 'bg-white dark:bg-gray-700 border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-green-50 dark:hover:bg-green-900/30 hover:border-green-300 dark:hover:border-green-500'
                    ]"
                  >
                    {{ item.name }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Product Description -->
            <div class="text-gray-700 dark:text-gray-300 leading-relaxed transition-colors duration-300">
              <p class="mb-3 text-xs">{{ product.shortDesc }}</p>
              <p class="mb-3 text-xs">{{ product.benefits }}</p>
            </div>

            <!-- Shipping Info -->
            <div class="space-y-3">
              <div class="flex items-start gap-3">
                <div class="bg-green-100 rounded-full p-2 flex-shrink-0">
                  <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                </div>
                <div>
                  <span class="font-semibold text-gray-900 dark:text-gray-100 text-xs md:text-sm transition-colors duration-300">Cam kết:</span>
                  <span class="text-gray-700 dark:text-gray-300 text-xs md:text-sm font-extrabold transition-colors duration-300"> 100% NGUYÊN LIỆU TỰ NHIÊN</span>
                </div>
              </div>

              <div class="flex items-start gap-3">
                <div class="bg-green-100 rounded-full p-2 flex-shrink-0">
                  <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
                  </svg>
                </div>
                <div>
                  <span class="font-semibold text-gray-900 dark:text-gray-100 text-xs md:text-sm transition-colors duration-300">Phí ship:</span>
                  <span class="text-gray-700 dark:text-gray-300 text-xs md:text-sm font-extrabold transition-colors duration-300"> 20.000₫ cho đơn 1 sản phẩm.</span>
                  <span class="text-gray-700 dark:text-gray-300 text-xs md:text-sm font-extrabold transition-colors duration-300"> Miễn phí vận chuyển khi mua 2 sản phẩm</span>
                </div>
              </div>

              <div class="flex items-start gap-3">
                <div class="bg-green-100 rounded-full p-2 flex-shrink-0">
                  <svg class="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <div>
                  <span class="font-semibold text-gray-900 dark:text-gray-100 text-xs md:text-sm transition-colors duration-300">Thời gian giao hàng:</span>
                  <span class="text-gray-700 dark:text-gray-300 text-xs md:text-sm font-extrabold transition-colors duration-300"> Hà Nội, TP.HCM: 1-3 ngày | Các tỉnh khác: 2-4 ngày</span>
                </div>
              </div>
            </div>
            
            <!-- Info Bar (di chuyển xuống đây) -->
            <div class="flex items-center gap-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-md px-3 py-2 mb-3 text-xs font-medium text-gray-800 dark:text-gray-200 shadow-sm transition-colors duration-300">
              <span v-for="(item, idx) in infoItems" :key="item.label" class="flex items-center">
                <button @click="openInfoPopup(idx)" class="flex items-center gap-1 text-yellow-700 hover:underline focus:outline-none">
                  <span v-if="item.icon === 'payment'">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="2" y="6" width="20" height="12" rx="2" fill="#fde68a"/><path d="M2 10h20" stroke="#fbbf24"/><rect x="6" y="14" width="4" height="2" rx="1" fill="#fbbf24"/></svg>
                  </span>
                  <span v-else-if="item.icon === 'cancel'">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="4" fill="#fde68a"/><path d="M9 9l6 6m0-6l-6 6" stroke="#fbbf24"/></svg>
                  </span>
                  <span v-else-if="item.icon === 'support'">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="#fde68a"/><path d="M12 16v-1m0-4a2 2 0 100 4 2 2 0 000-4zm0-4v2" stroke="#fbbf24"/></svg>
                  </span>
                  {{ item.label }}
                </button>
                <span v-if="idx < infoItems.length - 1" class="mx-2 text-gray-300">|</span>
              </span>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col sm:flex-row gap-3 pt-3">
              <button
                @click="addToCart"
                class="flex-1 bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-3 md:px-4 rounded-lg transition-all duration-200 text-xs md:text-sm flex items-center justify-center gap-2 btn-hover-scale"
              >
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5 6m0 0h9m-9 0V19a2 2 0 002 2h7a2 2 0 002-2v-4.5M9 17h6"></path>
                </svg>
                Thêm vào giỏ hàng
              </button>
              <button
                @click="buyNow"
                class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 md:px-6 rounded-full transition-all duration-200 text-xs md:text-sm flex items-center justify-center gap-2 btn-hover-scale shadow-lg"
              >
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                </svg>
                MUA NGAY
              </button>
            </div>

            <!-- Customer Reviews -->
            <div class="bg-white dark:bg-gray-800 rounded-lg p-4 mt-2 shadow-md transition-colors duration-300">
              <div class="flex items-center justify-between mb-2">
                <div class="font-bold text-green-500 text-base md:text-2xl">Đánh giá của khách hàng ({{ allReviews.length }})</div>
                <div class="flex gap-2">
                  <button @click="showAddReviewForm = true" class="px-3 py-1.5 bg-green-500 text-white text-xs font-semibold rounded-lg hover:bg-green-600 transition-colors">
                    Viết đánh giá
                  </button>
                  <button v-if="!showAllReviews && allReviews && allReviews.length > 6" @click="showAllReviews = true" class="text-blue-500 text-xs font-semibold hover:underline">Xem thêm &gt;</button>
                  <button v-if="showAllReviews" @click="showAllReviews = false" class="text-gray-500 text-xs font-semibold hover:underline">Thu gọn &lt;</button>
                </div>
              </div>
              <div class="flex items-center mb-3">
                <span class="text-yellow-500 text-2xl font-bold mr-2">{{ averageRating }}</span>
                <div class="flex text-yellow-400 mr-2">
                  <svg v-for="i in 5" :key="i" class="w-5 h-5 fill-current" :class="i > Math.round(averageRating) ? 'text-gray-300' : ''" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
              </div>
              <div v-for="review in displayedReviews" :key="review.id" class="border-b border-gray-100 pb-4 mb-4 last:mb-0 last:pb-0 last:border-0">
                <div class="flex items-center mb-2">
                  <div class="w-8 h-8 rounded-full bg-yellow-100 flex items-center justify-center font-bold text-yellow-700 mr-2 text-base">
                    {{ review.author.charAt(0).toUpperCase() }}
                  </div>
                  <span class="font-semibold text-gray-800 text-sm mr-2">{{ review.author }}</span>
                  <div class="flex text-yellow-400 ml-1">
                    <svg v-for="i in 5" :key="i" class="w-4 h-4 fill-current" :class="i > review.rating ? 'text-gray-300' : ''" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                    </svg>
                  </div>
                  <button v-if="!review.isDefault" @click="confirmDeleteReview(review.id)" class="ml-auto text-red-500 text-xs hover:text-red-700">Xóa</button>
                </div>
                <div class="text-gray-800 text-sm mb-2">{{ review.content }}</div>
                <div class="flex gap-2 flex-wrap">
                  <img v-for="(img, idx) in review.images" :key="idx" :src="img" class="w-16 h-16 object-cover rounded-md border border-gray-200" :alt="`Ảnh đánh giá ${idx+1}`" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Popup Form Thêm Đánh Giá -->
      <div v-if="showAddReviewForm" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" @click.self="closeAddReviewForm">
        <div class="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
          <!-- Header -->
          <div class="flex items-center justify-between p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
            <h2 class="text-xl font-bold text-gray-900">Viết đánh giá</h2>
            <button @click="closeAddReviewForm" class="text-gray-400 hover:text-gray-600 transition-colors">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>

          <!-- Form Body -->
          <form @submit.prevent="submitReview" class="p-6 space-y-4">
            <!-- Tên khách hàng -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Tên của bạn *</label>
              <input
                v-model="newReviewForm.author"
                type="text"
                required
                placeholder="Nhập tên của bạn"
                class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-green-500"
              />
            </div>

            <!-- Rating -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Đánh giá *</label>
              <div class="flex items-center gap-2">
                <button
                  v-for="i in 5"
                  :key="i"
                  type="button"
                  @click="newReviewForm.rating = i"
                  class="transition-transform hover:scale-110"
                >
                  <svg class="w-8 h-8" :class="i <= newReviewForm.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </button>
                <span class="ml-2 text-sm text-gray-600">{{ newReviewForm.rating }}/5 sao</span>
              </div>
            </div>

            <!-- Nội dung đánh giá -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Nội dung đánh giá *</label>
              <textarea
                v-model="newReviewForm.content"
                required
                rows="4"
                placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
                class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-green-500 resize-none"
              ></textarea>
            </div>

            <!-- Upload ảnh -->
            <div>
              <label class="block text-sm font-semibold text-gray-700 mb-2">Ảnh đánh giá (tùy chọn)</label>
              <div class="space-y-3">
                <div class="flex items-center gap-2">
                  <input
                    ref="imageInput"
                    type="file"
                    multiple
                    accept="image/*"
                    @change="handleImageUpload"
                    class="hidden"
                  />
                  <button
                    type="button"
                    @click="$refs.imageInput?.click()"
                    class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm font-medium"
                  >
                    Chọn ảnh
                  </button>
                  <span class="text-xs text-gray-500">Tối đa 5 ảnh, mỗi ảnh tối đa 5MB</span>
                </div>

                <!-- Preview ảnh đã chọn -->
                <div v-if="newReviewForm.imagePreviews.length > 0" class="grid grid-cols-4 gap-2">
                  <div v-for="(preview, idx) in newReviewForm.imagePreviews" :key="idx" class="relative group">
                    <img :src="preview" alt="Preview" class="w-full h-24 object-cover rounded-lg border border-gray-200" />
                    <button
                      type="button"
                      @click="removeImagePreview(idx)"
                      class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-xs"
                    >
                      ×
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Buttons -->
            <div class="flex justify-end gap-3 pt-4 border-t border-gray-200">
              <button
                type="button"
                @click="closeAddReviewForm"
                class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
              >
                Hủy
              </button>
              <button
                type="submit"
                :disabled="submittingReview"
                class="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
              </button>
            </div>
          </form>
        </div>
      </div>

              <!-- Product Details Table -->
        <div class="space-y-6">
          <div class="grid grid-cols-1 gap-6">
            <div class="product-detail-section">
              <h3 v-if="product.id === 5" class="section-title">1. BỘT NGŨ HẮC MÈ ĐEN</h3>
              <h3 class="section-title">CÔNG DỤNG</h3>
              <ul class="benefits-list">
                <li v-if="product.id === 1 || product.id === 2">• Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo </li>
                <li v-if="product.id === 1 || product.id === 2">• Dinh dưỡng cho tóc chắc khỏe, đen mượt từ 5 loại hạt đen giàu dưỡng chất</li>
                <li v-if="product.id === 1 || product.id === 2">• Bổ huyết, bồi bổ sức khỏe: kết hợp đa dạng nhóm thực phẩm từ hạt, ngũ cốc và quả, cung cấp 4 nhóm dưỡng chất thiết yếu, hỗ trợ tuần hoàn máu, bổ sung dưỡng chất cần thiết giúp cơ thể duy trì thể trạng</li>
                <li v-if="product.id === 1 || product.id === 2">• Dinh dưỡng cho giấc ngủ, giúp cơ thể thư giãn, dễ đi vào giấc ngủ, ngủ ngon và ngủ sâu hơn.</li>
                <li v-if="product.id === 3 || product.id === 4">• Giàu chất xơ tự nhiên từ thực vật, giúp duy trì chế độ ăn lành mạnh.</li>
                
                <li v-if="product.id === 3 || product.id === 4">• Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo, phù hợp cho chế độ ăn lành mạnh.</li>
                <li v-if="product.id === 3 || product.id === 4">• Bồi bổ và nuôi dưỡng khí huyết: Củ dền đỏ giàu sắt và folate, kèm hơn 25 vi chất cần thiết cho cơ thể. Kết hợp cùng táo đỏ, đậu đỏ, diêm mạch đỏ, hạt sen đỏ tạo nên nguồn dinh dưỡng giúp cơ thể thêm khỏe khoắn, giảm cảm giác mệt mỏi, điều khí, cải thiện lưu thông máu, góp phần hỗ trợ tuần hoàn và duy trì năng lượng cho cơ thể.</li>
                <li v-if="product.id === 3 || product.id === 4">• Dưỡng nhan thuần thực vật: nuôi dưỡng làn da hồng hào, nhuận sắc, rạng rỡ từ bên trong.</li>
                <li v-if="product.id === 3 || product.id === 4">• Dinh dưỡng cho giấc ngủ, giúp cơ thể thư thái và dễ chìm vào giấc ngủ sâu hơn.</li>

                <li v-if="product.id === 52">• Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo </li>
                <li v-if="product.id === 52">• Dinh dưỡng cho tóc chắc khỏe, đen mượt từ 5 loại hạt đen giàu dưỡng chất</li>
                <li v-if="product.id === 52">• Bổ huyết, bồi bổ sức khỏe: kết hợp đa dạng nhóm thực phẩm từ hạt, ngũ cốc và quả, cung cấp 4 nhóm dưỡng chất thiết yếu, hỗ trợ tuần hoàn máu, bổ sung dưỡng chất cần thiết giúp cơ thể duy trì thể trạng</li>
                <li v-if="product.id === 52">• Dinh dưỡng cho giấc ngủ, giúp cơ thể thư giãn, dễ đi vào giấc ngủ, ngủ ngon và ngủ sâu hơn.</li>
                <li v-if="product.id === 52">• Giàu chất xơ tự nhiên từ thực vật, giúp duy trì chế độ ăn lành mạnh.</li>
              </ul>

              <h3 v-if="product.id === 52" class="section-title">2. BỘT NGŨ SẮC HỒNG ĐẬU</h3>
              <h3 v-if="product.id === 52" class="section-title">CÔNG DỤNG</h3>
              <ul class="benefits-list">
                <li v-if="product.id === 52">• Thay thế bữa sáng, bữa phụ, tiện lợi giàu dinh dưỡng ít calo, phù hợp cho chế độ ăn lành mạnh. </li>
                <li v-if="product.id === 52">• Bồi bổ và nuôi dưỡng khí huyết: Củ dền đỏ giàu sắt và folate, kèm hơn 25 vi chất cần thiết cho cơ thể. Kết hợp cùng táo đỏ, đậu đỏ, diêm mạch đỏ, hạt sen đỏ tạo nên nguồn dinh dưỡng giúp cơ thể thêm khỏe khoắn, giảm cảm giác mệt mỏi, điều khí, cải thiện lưu thông máu, góp phần hỗ trợ tuần hoàn và duy trì năng lượng cho cơ thể.</li>
                <li v-if="product.id === 52">• Dưỡng nhan thuần thực vật: nuôi dưỡng làn da hồng hào, nhuận sắc, rạng rỡ từ bên trong.</li>
                <li v-if="product.id === 52">• Dinh dưỡng cho giấc ngủ, giúp cơ thể thư thái và dễ chìm vào giấc ngủ sâu hơn.</li>
              </ul>
            </div>

            <div class="product-detail-section">
              <h3 class="section-title">ĐỐI TƯỢNG PHÙ HỢP</h3>
              <p class="section-subtitle">Phù hợp với người lớn và trẻ em trên 03 tuổi</p>
              <ul class="benefits-list">
                <li v-if="product.id === 1 || product.id === 2">• Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng</li>
                <li v-if="product.id === 1 || product.id === 2">• Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu</li>
                <li v-if="product.id === 1 || product.id === 2">• Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe</li>
                <li v-if="product.id === 1 || product.id === 2">• Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng</li>
                
                <li v-if="product.id === 3 || product.id === 4">• Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng</li>
                <li v-if="product.id === 3 || product.id === 4">• Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu</li>
                <li v-if="product.id === 3 || product.id === 4">• Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe</li>
                <li v-if="product.id === 3 || product.id === 4">• Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng</li>

                <li v-if="product.id === 52">• Người ăn chay, người đang ăn kiêng, kiểm soát cân nặng</li>
                <li v-if="product.id === 52">• Người lớn tuổi, cần bổ sung dưỡng chất nhẹ, dễ tiêu</li>
                <li v-if="product.id === 52">• Phụ nữ sau sinh, người cần dưỡng huyết, ngủ ngon, tóc khỏe</li>
                <li v-if="product.id === 52">• Dân văn phòng bận rộn, không kịp nấu nhưng vẫn muốn ăn lành mạnh, bổ dưỡng</li>
              </ul>
            </div>

            <div class="product-detail-section">
              <h3 class="section-title">CÔNG NGHỆ VƯỢT TRỘI TỐI ƯU DƯỠNG CHẤT</h3>
              <ul class="benefits-list">
                <li v-if="product.id === 1 || product.id === 2">• Công nghệ nén khí & bơm nitơ: Kéo dài thời gian bảo quản, giữ nguyên hương vị, màu sắc, dưỡng chất.</li>
                <li v-if="product.id === 1 || product.id === 2">• Nghiền Nano siêu mịn, không lợn cợn, dễ uống</li>
                <li v-if="product.id === 1 || product.id === 2">• Hấp nhiệt thông minh: Giữ nguyên hương vị và dưỡng chất tự nhiên</li>
                
                <li v-if="product.id === 3 || product.id === 4">• Công nghệ nén khí & bơm nitơ: Kéo dài thời gian bảo quản, giữ nguyên hương vị, màu sắc, dưỡng chất.</li>
                <li v-if="product.id === 3 || product.id === 4">• Nghiền Nano siêu mịn, không lợn cợn, dễ uống</li>
                <li v-if="product.id === 3 || product.id === 4">• Hấp nhiệt thông minh: Giữ nguyên hương vị và dưỡng chất tự nhiên</li>

                <li v-if="product.id === 52">• Công nghệ nén khí & bơm nitơ: Kéo dài thời gian bảo quản, giữ nguyên hương vị, màu sắc, dưỡng chất.</li>
                <li v-if="product.id === 52">• Nghiền Nano siêu mịn, không lợn cợn, dễ uống</li>
                <li v-if="product.id === 52">• Hấp nhiệt thông minh: Giữ nguyên hương vị và dưỡng chất tự nhiên</li>
              </ul>
            </div>

            <div class="product-detail-section">
              <h3 class="section-title">HƯỚNG DẪN BẢO QUẢN</h3>
              <p class="section-content">Bảo quản ở nơi thoáng mát, tránh ánh nắng trực tiếp chiếu vào sản phẩm. Sản phẩm đã mở nắp cần sử dụng trong vòng 4 tuần.</p>
            </div>

            <div v-if="product.specifications" class="product-detail-section">
              <h3 class="section-title">HẠN SỬ DỤNG</h3>
              <p v-if="product.id === 1 || product.id === 2" class="section-content">10 tháng từ ngày sản xuất</p>
              <p v-if="product.id === 3 || product.id === 4" class="section-content">8 tháng từ ngày sản xuất</p>
              <p v-if="product.id === 52" class="section-content">8 tháng từ ngày sản xuất. Nên dùng sau khi mở nắp trong vòng 4 tuần</p>
            </div>
           
          </div>
        </div>

         <!-- Ảnh liên quan đến sản phẩm -->
     <div class="bg-white rounded-none shadow-none md:rounded-lg md:shadow-lg p-2 md:p-4 mt-2 md:mt-4 md:hidden">
       <!-- Video giới thiệu sản phẩm -->
       <video
         class="w-full max-w-2xl mx-auto rounded-lg mb-4"
         controls
         poster="/images/products/details/black/1.png"
       >
         <source src="/video/intro.mp4" type="video/mp4" />
         Trình duyệt của bạn không hỗ trợ video.
       </video>
       <div class="flex flex-col gap-3">
         <div
           v-for="(image, idx) in relatedImages"
           :key="'related-m-' + idx"
           class="relative group cursor-pointer"
         >
           <img
             :src="image"
             :alt="`${product.name} liên quan ${idx + 1}`"
             class="w-full h-100 object-cover rounded-lg border border-gray-200 shadow-sm group-hover:scale-105 transition-transform duration-200"
             @error="handleImageError"
             :key="`related-m-${idx}-${imageReloadKey}`"
           />
           <div class="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition-opacity rounded-lg"></div>
         </div>
         <div v-if="relatedImages.length === 0" class="text-center text-gray-400 text-xs py-6">
           Không có ảnh liên quan
         </div>
       </div>
     </div>

     <!-- Ảnh liên quan (desktop) -->
     <!-- <div class="hidden md:block mt-2">
       <div class="grid grid-cols-6 gap-4">
         <div
           v-for="(image, idx) in relatedImages"
           :key="'related-' + idx"
           class="relative group cursor-pointer transition-all duration-200 rounded-xl shadow-md border border-gray-200 hover:shadow-xl hover:scale-105 hover:z-10 overflow-hidden"
           style="background: linear-gradient(135deg, #f9fafb 60%, #fef3c7 100%);"
         >
           <img
             :src="image"
             :alt="`${product.name} liên quan ${idx + 1}`"
             class="w-full h-40 object-cover rounded-xl border border-gray-200 shadow-sm group-hover:scale-105 transition-transform duration-200"
             @error="handleImageError"
             :key="`related-${idx}-${imageReloadKey}`"
           />
           <div class="absolute inset-0 bg-black/10 opacity-0 group-hover:opacity-100 transition-opacity rounded-xl"></div>
         </div>
         <div v-if="relatedImages.length === 0" class="text-center text-gray-400 text-xs py-6 col-span-6">
           Không có ảnh liên quan
         </div>
       </div>
     </div> -->
    </div>

    <!-- Product Features with Images -->
    <div class="bg-gradient-to-br from-green-50 to-yellow-50 rounded-lg shadow-lg p-4 mt-2">
        <h2 class="text-sm md:text-base font-bold text-gray-900 mb-4 text-center">Đặc điểm nổi bật</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- Feature 1 -->
          <div class="flex items-start space-x-3 bg-white/60 backdrop-blur-sm rounded-xl p-3 hover:bg-white/80 transition-all duration-300">
            <div class="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <div>
              <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">100% Tự nhiên</h3>
              <p class="text-xs text-gray-600">Sản phẩm được làm từ nguyên liệu tự nhiên, không chất bảo quản, an toàn cho sức khỏe.</p>
            </div>
          </div>
          
          <!-- Feature 2 -->
          <div class="flex items-start space-x-3 bg-white/60 backdrop-blur-sm rounded-xl p-3 hover:bg-white/80 transition-all duration-300">
            <div class="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
            </div>
            <div>
              <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Giàu dinh dưỡng</h3>
              <p class="text-xs text-gray-600">Cung cấp đầy đủ vitamin, khoáng chất và protein thiết yếu cho cơ thể.</p>
            </div>
          </div>
          
          <!-- Feature 3 -->
          <div class="flex items-start space-x-3 bg-white/60 backdrop-blur-sm rounded-xl p-3 hover:bg-white/80 transition-all duration-300">
            <div class="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <div>
              <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Tiện lợi</h3>
              <p class="text-xs text-gray-600">Dễ dàng pha chế, phù hợp cho lối sống bận rộn, có thể dùng mọi lúc mọi nơi.</p>
            </div>
          </div>
          
          <!-- Feature 4 -->
          <div class="flex items-start space-x-3 bg-white/60 backdrop-blur-sm rounded-xl p-3 hover:bg-white/80 transition-all duration-300">
            <div class="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-purple-500 to-purple-600 rounded-full flex items-center justify-center">
              <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
              </svg>
            </div>
            <div>
              <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Chất lượng cao</h3>
              <p class="text-xs text-gray-600">Được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Usage Instructions with Visual -->
      <div class="bg-white rounded-lg shadow-lg p-4 mt-1">
        <h2 class="text-sm md:text-base font-bold text-gray-900 mb-4 text-center">Hướng dẫn sử dụng</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-2">
          <!-- Step 1 -->
          <div class="text-center group">
            <div class="relative mx-auto w-24 h-24 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform duration-300">
              <div class="absolute -top-1 -right-1 bg-green-500 text-white rounded-full w-4 h-4 flex items-center justify-center text-xs font-bold">1</div>
              <img src="/images/icons/Step1.png" alt="Step 1" class="w-24 h-24 object-contain" />
            </div>
            <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Cho bột vào cốc</h3>
            <p class="text-gray-600 text-xs">Cho 2-3 thìa bột vào cốc sạch</p>
          </div>
          
          <!-- Step 2 -->
          <div class="text-center group">
            <div class="relative mx-auto w-24 h-24 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform duration-300">
              <div class="absolute -top-1 -right-1 bg-green-500 text-white rounded-full w-4 h-4 flex items-center justify-center text-xs font-bold">2</div>
              <img src="/images/icons/Step2.png" alt="Step 2" class="w-24 h-24 object-contain" />
            </div>
            <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Pha với nước ấm</h3>
            <p class="text-gray-600 text-xs">Thêm 200ml nước ấm hoặc sữa</p>
          </div>
          
          <!-- Step 3 -->
          <div class="text-center group">
            <div class="relative mx-auto w-24 h-24 flex items-center justify-center mb-2 group-hover:scale-110 transition-transform duration-300">
              <div class="absolute -top-1 -right-1 bg-green-500 text-white rounded-full w-4 h-4 flex items-center justify-center text-xs font-bold">3</div>
              <img src="/images/icons/Step3.png" alt="Step 3" class="w-24 h-24 object-contain" />
            </div>
            <h3 class="text-xs md:text-sm font-semibold text-gray-900 mb-1">Khuấy đều & thưởng thức</h3>
            <p class="text-gray-600 text-xs">Khuấy đều và thưởng thức ngay</p>
          </div>
        </div>
      </div>

    <!-- Mobile Sticky Bottom Bar -->
    <div class="md:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-lg z-50">
      <div class="flex items-center px-2 py-2">
        <!-- Left Section - Message Icon -->
        <div class="flex items-center gap-3 mr-3">
          <!-- Message Icon -->
          <button @click="openMessage" class="flex flex-col items-center gap-1">
            <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
            <span class="text-xs text-gray-600">Nhắn tin</span>
          </button>
        </div>

        <!-- Right Section - Two Buttons -->
        <div class="flex-1 flex gap-2">
          <!-- Add to Cart Button -->
          <button
            @click="addToCart"
            class="w-24 bg-white border border-green-500 text-green-500 font-bold py-2 px-2 rounded-lg transition-all duration-200 flex items-center justify-center gap-1 text-xs"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5 6m0 0h9m-9 0V19a2 2 0 002 2h7a2 2 0 002-2v-4.5M9 17h6"/>
            </svg>
            Thêm vào giỏ hàng
          </button>
          
          <!-- Buy Now Button -->
          <button
            @click="buyNow"
            class="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 rounded-full transition-all duration-200 flex items-center justify-center gap-1 text-xs shadow-lg"
          >
            MUA NGAY
          </button>
        </div>
      </div>
    </div>

    <!-- Social Proof Popup - Small notification at bottom left -->
    <div v-if="showSocialProof" class="fixed bottom-24 md:bottom-4 left-4 z-50 max-w-sm">
      <div class="bg-white rounded-lg shadow-lg border border-gray-200 overflow-hidden cursor-pointer hover:shadow-xl transition-all duration-200" @click="navigateToSocialProofProduct">
        <!-- Close button -->
        <button @click.stop="closeSocialProof" class="absolute top-2 right-2 text-gray-400 hover:text-gray-600 transition-colors z-10">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>

        <div class="flex">
          <!-- Left side - Product Image -->
          <div class="relative w-20 h-20 bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center">
            <img
              :src="socialProofData.productImage"
              :alt="socialProofData.productName"
              class="w-16 h-16 object-contain"
            />
            <!-- COMBO badge -->
            <div class="absolute -top-1 -left-1 bg-red-500 text-white text-xs px-1 py-0.5 rounded-sm font-bold">
              COMBO
            </div>
            <!-- Heart icons in background -->
            <div class="absolute inset-0 opacity-10">
              <svg class="w-3 h-3 text-green-400 absolute top-1 left-2" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
              <svg class="w-2 h-2 text-green-400 absolute bottom-2 right-1" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
              </svg>
            </div>
          </div>

          <!-- Right side - Text content -->
          <div class="flex-1 p-3 pr-8">
            <h4 class="font-bold text-gray-900 text-sm leading-tight mb-1">
              {{ socialProofData.productName }}
            </h4>
            <p class="text-gray-600 text-xs">
              Khách hàng <span class="font-semibold">{{ socialProofData.customerName }}</span> tại <span class="font-semibold">{{ socialProofData.location }}</span> vừa mua sản phẩm cách đây <span class="font-semibold text-green-500">{{ socialProofData.timeAgo }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Success Popup -->
    <div
      v-if="showPopup"
      class="fixed top-6 right-6 z-50 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg animate-bounce max-w-sm"
    >
      <div class="flex items-center">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
        {{ popupMessage }}
      </div>
    </div>

    <!-- Popup -->
    <div v-if="showInfoPopup" class="fixed inset-0 bg-black/30 z-50 flex items-center justify-center">
      <div class="bg-white rounded-xl shadow-2xl p-0 max-w-md w-full relative animate-bounce-in overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-center border-b border-gray-100 py-3 bg-white relative">
          <span class="absolute left-4 top-1/2 -translate-y-1/2">
            <svg class="w-6 h-6 text-yellow-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 18v-1a3 3 0 013-3h0a3 3 0 013 3v1" stroke="#fbbf24"/><circle cx="12" cy="11" r="4" stroke="#fbbf24"/><circle cx="12" cy="12" r="10" stroke="#fbbf24"/></svg>
          </span>
          <span class="font-bold text-gray-900 text-lg">Tự tin mua sắm</span>
          <button @click="closeInfoPopup" class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>
        <!-- Body -->
        <div class="bg-yellow-50 px-5 py-4">
          <!-- Thanh toán bảo mật -->
          <div class="flex items-start gap-3 mb-4">
            <span class="mt-1">
              <svg class="w-6 h-6" fill="#fde68a" stroke="#fbbf24" stroke-width="2" viewBox="0 0 24 24"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M2 10h20"/><rect x="6" y="14" width="4" height="2" rx="1" fill="#fbbf24"/></svg>
            </span>
            <div class="flex-1">
              <div class="font-bold text-gray-900 mb-1">Thanh toán bảo mật</div>
              <div class="text-gray-700 text-sm mb-2">Chúng tôi sử dụng các phương thức thanh toán bảo mật và tuân theo Tiêu chuẩn bảo mật dữ liệu ngành thẻ thanh toán (PCI DSS). Tiêu chuẩn này mã hóa thông tin của bạn trong quá trình truyền tải. Chúng tôi không chia sẻ thông tin thẻ tín dụng của bạn với nhà bán hàng, cũng như không bán thông tin của bạn cho người khác.</div>
                <div class="flex items-center gap-2 mb-2">
                  <!-- Momo -->
                  <svg class="h-6 w-6" viewBox="0 0 32 32"><rect width="32" height="32" rx="8" fill="#A50064"/><text x="16" y="22" text-anchor="middle" font-size="13" fill="#fff" font-family="Arial,Helvetica,sans-serif">momo</text></svg>
                  <!-- ZaloPay -->
                  <svg class="h-6 w-6" viewBox="0 0 32 32"><rect width="32" height="32" rx="8" fill="#00AEFF"/><text x="16" y="22" text-anchor="middle" font-size="11" fill="#fff" font-family="Arial,Helvetica,sans-serif">ZaloPay</text></svg>
                  <!-- VNPay -->
                  <svg class="h-6 w-6" viewBox="0 0 32 32"><rect width="32" height="32" rx="8" fill="#0060AF"/><text x="16" y="22" text-anchor="middle" font-size="11" fill="#fff" font-family="Arial,Helvetica,sans-serif">VNPay</text></svg>
                  <!-- ATM -->
                  <svg class="h-6 w-6" viewBox="0 0 32 32"><rect width="32" height="32" rx="8" fill="#FBBF24"/><text x="16" y="22" text-anchor="middle" font-size="12" fill="#fff" font-family="Arial,Helvetica,sans-serif">ATM</text></svg>
                </div>
                <div class="text-xs text-gray-500">Để biết thông tin về cách chúng tôi sử dụng dữ liệu cá nhân của bạn, vui lòng xem <a href="#" class="text-blue-500 underline">Chính sách Quyền riêng tư</a>.</div>
            </div>
          </div>
          <!-- Hủy đơn dễ dàng -->
          <div class="flex items-start gap-3 mb-4">
            <span class="mt-1">
              <svg class="w-6 h-6" fill="#fde68a" stroke="#fbbf24" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="4"/><path d="M9 9l6 6m0-6l-6 6"/></svg>
            </span>
            <div class="flex-1">
              <div class="font-bold text-gray-900 mb-1">Hủy đơn dễ dàng</div>
              <div class="text-gray-700 text-sm">Có thể hủy đơn hàng ngay lập tức mà không cần lý do trước khi mặt hàng chuyển sang trạng thái "Sẵn sàng vận chuyển" (trước khi vào hình thức vận chuyển).</div>
            </div>
          </div>
          <!-- Đội ngũ hỗ trợ -->
          <div class="flex items-start gap-3 mb-2">
            <span class="mt-1">
              <svg class="w-6 h-6" fill="#fde68a" stroke="#fbbf24" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 16v-1m0-4a2 2 0 100 4 2 2 0 000-4zm0-4v2"/></svg>
            </span>
            <div class="flex-1">
              <div class="font-bold text-gray-900 mb-1">Đội ngũ hỗ trợ của Thi Yên</div>
              <div class="text-gray-700 text-sm">Đội ngũ CSKH của Thi Yên luôn sẵn sàng hỗ trợ bạn 24/7 qua hotline, chat hoặc email để đảm bảo bạn có trải nghiệm mua sắm tuyệt vời.</div>
            </div>
          </div>
        </div>
        <!-- Footer -->
        <div class="text-xs text-gray-400 px-5 py-2 border-t border-gray-100 bg-white">Điều Khoản Sử Dụng và Bán Hàng của Thi Yên</div>
      </div>
    </div>

    <!-- Zalo Floating Button -->
    <div
      class="fixed z-50 right-4 zalo-container"
      :style="{ top: '66%' }"
      style="transform: translateY(-50%);"
    >
      <a
        href="https://zalo.me/0396860584"
        target="_blank"
        rel="noopener noreferrer"
        class="block zalo-float-btn rounded-full p-2"
        title="Chat Zalo với Thi Yên"
      >
        <!-- Logo Zalo thương hiệu -->
        <svg viewBox="0 0 48 48" class="w-12 h-12 zalo-logo">
          <ellipse cx="24" cy="24" rx="20" ry="18" fill="#008fe5"/>
          <text x="24" y="29" text-anchor="middle" fill="#fff" font-size="18" font-family="Arial Rounded MT Bold,Arial,Helvetica,sans-serif" font-weight="bold">Zalo</text>
        </svg>
        
        <!-- Pulse ring effect -->
        <div class="zalo-pulse-ring"></div>
        
        <!-- Floating notification dot -->
        <div class="zalo-notification-dot">
          <div class="zalo-notification-pulse"></div>
        </div>
      </a>
    </div>

    <!-- Popup chọn sản phẩm khi mua ngay -->
    <div v-if="showBuyNowPopup" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-2xl shadow-2xl max-w-lg w-full relative overflow-hidden">
        <!-- Header với branding -->
        <div class="flex items-center justify-between p-6 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <!-- Logo Thi Yên -->
            <div class="flex items-center gap-2">
              <img src="/images/logo/logo.png" alt="Thi Yên" class="w-8 h-8 object-contain" />
            </div>
            <!-- Badges -->
            <div class="flex gap-1">
              <div class="flex items-center gap-1 bg-green-100 text-green-700 px-1.5 py-0.5 rounded-full text-xs">
                <svg class="w-2.5 h-2.5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"></path>
                </svg>
                CHÍNH HÃNG
              </div>
              <div class="flex items-center gap-1 bg-blue-100 text-blue-700 px-1.5 py-0.5 rounded-full text-xs">
                <svg class="w-2.5 h-2.5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
                </svg>
                CAM KẾT
              </div>
            </div>
          </div>
          <button @click="closeBuyNowPopup" class="text-gray-400 hover:text-gray-600 transition-colors">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <!-- Product Information -->
        <div class="p-6">
          <div class="flex gap-4 mb-6">
            <!-- Product Image -->
            <div class="w-24 h-24 bg-gray-100 rounded-lg overflow-hidden flex-shrink-0">
              <img 
                :src="getSelectedProductImage" 
                :alt="getSelectedProductName"
                class="w-full h-full object-cover"
              />
            </div>
            <!-- Product Details -->
            <div class="flex-1">
              <div class="text-red-600 text-lg font-bold mb-1">
                {{ formatPrice(getSelectedProductPrice) }}
              </div>
              <div class="text-gray-500 line-through text-xs mb-1">
                {{ formatPrice(getSelectedProductOldPrice) }}
              </div>
              <div class="text-xs text-gray-600">
                Kho: {{ getSelectedProductStock }}
              </div>
            </div>
          </div>

          <!-- Combo Selection -->
          <div class="mb-6">
            <h3 class="font-bold text-base mb-2">Combo</h3>
            <div class="grid grid-cols-1 gap-3">
              <button
                v-for="item in relatedBuyNowProducts"
                :key="item.id"
                @click="selectedBuyNowProductId = item.id"
                :class="[
                  'flex items-center gap-3 p-3 rounded-lg border-2 transition-all',
                  selectedBuyNowProductId === item.id
                    ? 'border-red-500 bg-red-50'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="w-12 h-12 bg-gray-100 rounded overflow-hidden flex-shrink-0">
                  <img :src="item.image" :alt="item.name" class="w-full h-full object-cover" />
                </div>
                <div class="flex-1 text-left">
                  <div :class="[
                    'text-xs font-medium',
                    selectedBuyNowProductId === item.id ? 'text-red-600' : 'text-gray-800'
                  ]">
                    {{ item.name }}
                  </div>
                </div>
              </button>
            </div>
          </div>

          <!-- Quantity Selector -->
          <div class="mb-6">
            <label class="block text-xs font-medium text-gray-700 mb-2">Số lượng</label>
            <div class="flex items-center border border-gray-300 rounded-lg w-fit">
              <button 
                @click="buyNowQuantity = Math.max(1, buyNowQuantity - 1)"
                class="px-3 py-2 text-gray-500 hover:bg-gray-100 transition-colors"
              >
                -
              </button>
              <input 
                v-model="buyNowQuantity"
                type="number"
                min="1"
                class="w-16 text-center border-none focus:outline-none"
              />
              <button 
                @click="buyNowQuantity++"
                class="px-3 py-2 text-gray-500 hover:bg-gray-100 transition-colors"
              >
                +
              </button>
            </div>
          </div>

          <!-- Buy Now Button với Freeship Info bên trong -->
          <div class="space-y-3">
            <button
              @click="confirmBuyNow"
              class="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-full transition-colors shadow-lg relative overflow-hidden"
            >
              <!-- Main text -->
              <div class="text-base md:text-lg mb-1 drop-shadow-lg">MUA NGAY</div>
              
              <!-- Freeship Info bên trong button -->
              <div class="flex items-center justify-center gap-1 text-xs font-medium opacity-90">
                <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"></path>
                  <path d="M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1V5a1 1 0 00-1-1H3zM14 7a1 1 0 00-1 1v6.05A2.5 2.5 0 0115.95 16H17a1 1 0 001-1V8a1 1 0 00-1-1h-3z"></path>
                </svg>
                <span>Freeship đơn từ 2 sản phẩm</span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="min-h-screen flex items-center justify-center">
    <span class="text-gray-400 text-lg">Đang tải sản phẩm...</span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { productAPI, getImageUrlWithCacheBusting } from '@/utils/api.js'
import { getProductImage, getProductGallery } from '../utils/productImage'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// Computed property để kiểm tra development mode
const isDevelopment = computed(() => import.meta.env.DEV)

// State
const products = ref([])
const product = ref(null)
const loading = ref(false)
const error = ref('')
const isComponentMounted = ref(false)

// Lấy tất cả sản phẩm
async function fetchProducts() {
  if (!isComponentMounted.value) return
  loading.value = true
  try {
    const data = await productAPI.getProducts()
    if (isComponentMounted.value) {
      products.value = Array.isArray(data) ? data : (data.data || [])
    }
  } catch (e) {
    if (isComponentMounted.value) {
      error.value = 'Không lấy được danh sách sản phẩm.'
    }
  } finally {
    if (isComponentMounted.value) {
      loading.value = false
    }
  }
}
// Lấy chi tiết sản phẩm theo id
async function fetchProductById(id) {
  if (!isComponentMounted.value) return
  loading.value = true
  try {
    const data = await productAPI.getProduct(id)
    if (isComponentMounted.value) {
      product.value = data.data || data
    }
  } catch (e) {
    if (isComponentMounted.value) {
      error.value = 'Không lấy được chi tiết sản phẩm.'
    }
  } finally {
    if (isComponentMounted.value) {
      loading.value = false
    }
  }
}

const selectedProductId = computed(() => Number(product.value?.id))

// Thay thế các chỗ dùng productData
const relatedProducts = computed(() => {
  if (!products.value.length || !product.value) return []
  // Lấy các sản phẩm cùng loại (ví dụ: id 1,2 hoặc 3,4)
  if (product.value.id === 1 || product.value.id === 2) {
    return products.value.filter(p => p.id === 1 || p.id === 2)
  } else if (product.value.id === 3 || product.value.id === 4) {
    return products.value.filter(p => p.id === 3 || p.id === 4)
  }
  return []
})

const allProductSwitch = computed(() => {
  if (!products.value.length || !product.value) return []
  // Lấy nhóm sản phẩm cùng loại (id 1,2 hoặc 3,4), loại bỏ sản phẩm hiện tại
  if (product.value.id === 1 || product.value.id === 2) {
    return products.value.filter(p => (p.id === 1 || p.id === 2) && p.id !== product.value.id)
  } else if (product.value.id === 3 || product.value.id === 4) {
    return products.value.filter(p => (p.id === 3 || p.id === 4) && p.id !== product.value.id)
  }
  return products.value.filter(p => p.id !== product.value.id)
})

// Ảnh liên quan từ product.gallery
const relatedImages = computed(() => {
  if (!product.value) return []
  // Sử dụng utility function để lấy gallery
  return getProductGallery(product.value.id).slice(0, 6) // Lấy tối đa 6 ảnh
})

// Cập nhật ảnh sản phẩm khi load dữ liệu
function updateProductImages() {
  if (product.value) {
    product.value.image = getProductImage(product.value.id)
    product.value.gallery = getProductGallery(product.value.id)
  }
  if (products.value.length > 0) {
    products.value.forEach(p => {
      p.image = getProductImage(p.id)
    })
  }
}

// Load dữ liệu khi vào trang hoặc đổi id
async function loadData() {
  await fetchProducts()
  const id = parseInt(route.params.id) || (products.value[0]?.id || 1)
  await fetchProductById(id)
  updateProductImages()
}

// Xử lý lỗi ảnh
function handleImageError(event) {
  console.log('Image load error:', event.target.src)
  // Thay thế ảnh lỗi bằng ảnh placeholder
  event.target.src = '/images/products/details/black/1.png'
  event.target.onerror = null // Tránh loop vô hạn
}

// Debug function để kiểm tra trạng thái ảnh
function debugImageStatus() {
  console.log('Current product:', product.value)
  console.log('Gallery images:', galleryImages.value)
  console.log('Related images:', relatedImages.value)
  console.log('Image reload key:', imageReloadKey.value)
}

// Single onMounted hook to avoid conflicts
onMounted(() => {
  isComponentMounted.value = true
  loadReviewsFromStorage() // Load reviews từ localStorage khi component mount
  loadData()
  startFlashSaleCountdown()
  if (isAutoPlaying.value) {
    startAutoPlay()
  }
  
  // Thêm event listener để reload ảnh khi tab được focus
  window.addEventListener('focus', handleWindowFocus)
  
  // Thêm event listener để reload ảnh khi visibility change
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  isComponentMounted.value = false
  if (flashSaleInterval) clearInterval(flashSaleInterval)
  if (autoPlayInterval) clearInterval(autoPlayInterval)
  
  // Clean up event listeners
  window.removeEventListener('focus', handleWindowFocus)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

watch(() => route.params.id, async () => {
  await fetchProductById(route.params.id)
  updateProductImages()
})

// ... Giữ lại các logic khác, thay productData thành products.value hoặc product.value tương ứng

function formatPrice(val) {
  if (typeof val !== 'number') return ''
  return val.toLocaleString('vi-VN') + '₫'
}

function formatFlashSaleTime(sec) {
  const h = Math.floor(sec / 3600).toString().padStart(2, '0')
  const m = Math.floor((sec % 3600) / 60).toString().padStart(2, '0')
  const s = (sec % 60).toString().padStart(2, '0')
  return `${h} : ${m} : ${s}`
}



// Cải thiện galleryImages computed
const galleryImages = computed(() => {
  if (!product.value) return []
  return getProductGallery(product.value.id)
})

// Reviews mặc định (5 reviews)
const defaultReviews = [
  {
    id: 1,
    author: 'Thu Quyên',
    rating: 5,
    content: 'Giao hàng nhanh, hàng chính hãng, đóng gói bọc xốp cẩn thận, Vị thơm mùi mè, pha nhanh, dễ pha, hợp với người bận rộn như mình.',
    images: [
      '/images/review/comment1/5f70f3785c7bd025896a1.jpg',
      '/images/review/comment1/cba003a9acaa20f479bb2.jpg'
    ],
    isDefault: true
  },
  {
    id: 2,
    author: 'Mai Trân',
    rating: 5,
    content: 'Mẫu mã và chất lượng OK ạ. Shop tư vấn nhiệt tình. Hàng đóng gói cẩn thận. Sẽ ủng hộ Shop lâu dài',
    images: [
      '/images/review/comment2/55604d6ae2696e3737789.jpg',
      '/images/review/comment2/c59aea924591c9cf908010.jpg'
    ],
    isDefault: true
  },
  {
    id: 3,
    author: 'Lê Lan',
    rating: 5,
    content: 'Shop giao hàng nhanh, đóng gói cẩn thận, bột mè đen mịn, dễ pha, vị mè đen thơm, dễ uống',
    images: [
      '/images/review/comment3/ee0af18a5f89d3d78a9814.jpg'
    ],
    isDefault: true
  },
  {
    id: 4,
    author: 'Quyền Thu',
    rating: 5,
    content: 'Bột ngũ hắc thơm mùi mè đen, vị ngon béo ngậy. Bé nhà m cũng thích, chiều đi học về cứ làm cốc là ấm bụng.',
    images: [
      '/images/review/comment4/fbcebb48154b9915c05a13.jpg'
    ],
    isDefault: true
  },
  {
    id: 5,
    author: 'Lê Hà',
    rating: 5,
    content: 'cũng dùng thử mấy loại rồi mà chỉ thấy toàn mùi đỗ đen, nay mới thấy loại này ngon và rõ mùi vị của mè đen. Nghe nói tốt lắm, thử dùng xem sao',
    images: [
      '/images/review/comment5/1f973fb291b11def44a018.jpg',
      '/images/review/comment5/f19088b626b5aaebf3a417.jpg'
    ],
    isDefault: true
  }
]

// Reviews từ localStorage
const storedReviews = ref([])

// Key cho localStorage
const STORAGE_KEY = 'product_reviews'

// Load reviews từ localStorage
function loadReviewsFromStorage() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      storedReviews.value = JSON.parse(stored)
    } else {
      storedReviews.value = []
    }
  } catch (error) {
    console.error('Error loading reviews from localStorage:', error)
    storedReviews.value = []
  }
}

// Lưu reviews vào localStorage
function saveReviewsToStorage() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(storedReviews.value))
  } catch (error) {
    console.error('Error saving reviews to localStorage:', error)
  }
}

// Thêm review mới
function addReview(review) {
  const newReview = {
    ...review,
    id: Date.now(), // Tạo ID duy nhất
    isDefault: false
  }
  storedReviews.value.push(newReview)
  saveReviewsToStorage()
}

// Cập nhật review
function updateReview(reviewId, updatedReview) {
  const index = storedReviews.value.findIndex(r => r.id === reviewId)
  if (index !== -1) {
    storedReviews.value[index] = {
      ...updatedReview,
      id: reviewId,
      isDefault: false
    }
    saveReviewsToStorage()
    return true
  }
  return false
}

// Xóa review (chỉ xóa reviews từ localStorage, không xóa reviews mặc định)
function deleteReview(reviewId) {
  const index = storedReviews.value.findIndex(r => r.id === reviewId)
  if (index !== -1) {
    storedReviews.value.splice(index, 1)
    saveReviewsToStorage()
    return true
  }
  return false
}

// Xác nhận trước khi xóa review
function confirmDeleteReview(reviewId) {
  if (confirm('Bạn có chắc muốn xóa đánh giá này?')) {
    deleteReview(reviewId)
    popupMessage.value = 'Đã xóa đánh giá thành công!'
    showPopup.value = true
    setTimeout(() => {
      showPopup.value = false
    }, 2000)
  }
}

// Load reviews sẽ được gọi trong onMounted

// Form thêm review
const showAddReviewForm = ref(false)
const submittingReview = ref(false)
const imageInput = ref(null)
const newReviewForm = ref({
  author: '',
  rating: 5,
  content: '',
  imagePreviews: []
})

// Xử lý upload ảnh
function handleImageUpload(event) {
  const files = Array.from(event.target.files || [])
  if (files.length === 0) return

  // Giới hạn tối đa 5 ảnh
  const remainingSlots = 5 - newReviewForm.value.imagePreviews.length
  const filesToProcess = files.slice(0, remainingSlots)

  filesToProcess.forEach(file => {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      alert('Vui lòng chọn file ảnh hợp lệ')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert(`File ${file.name} không được lớn hơn 5MB`)
      return
    }

    // Convert to base64
    const reader = new FileReader()
    reader.onload = (e) => {
      newReviewForm.value.imagePreviews.push(e.target.result)
    }
    reader.onerror = () => {
      alert('Có lỗi xảy ra khi đọc file')
    }
    reader.readAsDataURL(file)
  })

  // Reset input để có thể chọn lại cùng file
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

// Xóa ảnh preview
function removeImagePreview(index) {
  newReviewForm.value.imagePreviews.splice(index, 1)
}

// Đóng form
function closeAddReviewForm() {
  showAddReviewForm.value = false
  // Reset form
  newReviewForm.value = {
    author: '',
    rating: 5,
    content: '',
    imagePreviews: []
  }
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

// Submit review
function submitReview() {
  if (!newReviewForm.value.author || !newReviewForm.value.content) {
    alert('Vui lòng điền đầy đủ thông tin')
    return
  }

  submittingReview.value = true

  try {
    // Tạo review object
    const review = {
      author: newReviewForm.value.author,
      rating: newReviewForm.value.rating,
      content: newReviewForm.value.content,
      images: [...newReviewForm.value.imagePreviews] // Lưu base64 images
    }

    // Thêm review
    addReview(review)

    // Hiển thị thông báo thành công
    popupMessage.value = 'Đánh giá của bạn đã được gửi thành công!'
    showPopup.value = true
    setTimeout(() => {
      showPopup.value = false
    }, 3000)

    // Đóng form
    closeAddReviewForm()
  } catch (error) {
    console.error('Error submitting review:', error)
    alert('Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.')
  } finally {
    submittingReview.value = false
  }
}

// ... existing code ...
// Popup state
const showPopup = ref(false)
const popupMessage = ref('')

function addToCart() {
  if (product.value) {
    cartStore.addToCart(product.value, 1)
    popupMessage.value = `Đã thêm "${product.value?.name || 'sản phẩm'}" vào giỏ hàng!`
    showPopup.value = true
    setTimeout(() => {
      showPopup.value = false
    }, 3000)
  }
}

function buyNow() {
  showBuyNowPopup.value = true
  selectedBuyNowProductId.value = product.value.id
  buyNowQuantity.value = 1
}

const showBuyNowPopup = ref(false)
const selectedBuyNowProductId = ref(null)
const buyNowQuantity = ref(1)

function confirmBuyNow() {
  if (!selectedBuyNowProductId.value) return
  const selectedProduct = products.value.find(p => p.id === selectedBuyNowProductId.value)
  if (selectedProduct) {
    localStorage.removeItem('checkoutSelectedItems')
    localStorage.setItem('checkoutSingleItem', JSON.stringify({ ...selectedProduct, quantity: buyNowQuantity.value }))
    showBuyNowPopup.value = false
    router.push('/checkout')
  }
}
function closeBuyNowPopup() {
  showBuyNowPopup.value = false
  selectedBuyNowProductId.value = null
  buyNowQuantity.value = 1
}

const relatedBuyNowProducts = computed(() => {
  if (!products.value.length || !product.value) return []
  if (product.value.id === 1 || product.value.id === 2) {
    return products.value.filter(p => p.id === 1 || p.id === 2)
  } else if (product.value.id === 3 || product.value.id === 4) {
    return products.value.filter(p => p.id === 3 || p.id === 4)
  }
  return []
})

// Sản phẩm được chọn trong popup mua ngay
const selectedBuyNowProduct = computed(() => {
  if (!selectedBuyNowProductId.value) return null
  return products.value.find(p => p.id === selectedBuyNowProductId.value)
})

// Computed properties for selected product details
const getSelectedProductImage = computed(() => {
  return selectedBuyNowProduct.value?.image || '/images/products/details/black/1.png'
})

const getSelectedProductName = computed(() => {
  return selectedBuyNowProduct.value?.name || 'Sản phẩm'
})

const getSelectedProductPrice = computed(() => {
  return selectedBuyNowProduct.value?.price || 0
})

const getSelectedProductOldPrice = computed(() => {
  return selectedBuyNowProduct.value?.oldPrice || 0
})

const getSelectedProductStock = computed(() => {
  return selectedBuyNowProduct.value?.stock || 100
})

const showSocialProof = ref(false)

const currentImageIndex = ref(0)

const flashSaleDiscountPercent = computed(() => {
  if (!product.value || !product.value.oldPrice || !product.value.price || product.value.oldPrice <= product.value.price) return 0
  return Math.floor((product.value.oldPrice - product.value.price) / product.value.oldPrice * 100)
})

const flashSaleLeft = ref(0)

const isAutoPlaying = ref(true)

const showAllReviews = ref(false)

// Kết hợp reviews mặc định và reviews từ localStorage
const allReviews = computed(() => {
  return [...defaultReviews, ...storedReviews.value]
})

const averageRating = computed(() => {
  if (!allReviews.value.length) return 0
  const total = allReviews.value.reduce((sum, r) => sum + (r.rating || 0), 0)
  return (total / allReviews.value.length).toFixed(1)
})

const displayedReviews = computed(() => showAllReviews.value ? allReviews.value : allReviews.value.slice(0, 6))

const infoItems = [
  {
    icon: 'payment',
    label: 'Thanh toán bảo mật',
    detail: 'Mọi giao dịch đều được mã hóa và bảo vệ bởi hệ thống bảo mật hiện đại. Bạn có thể yên tâm khi thanh toán tại cửa hàng.'
  },
  {
    icon: 'cancel',
    label: 'Hủy đơn dễ dàng',
    detail: 'Bạn có thể hủy đơn hàng trong vòng 2 giờ sau khi đặt mà không mất phí. Đơn hàng sẽ được hoàn tiền nhanh chóng.'
  },
  {
    icon: 'support',
    label: 'Đội ngũ hỗ trợ của Thi Yên',
    detail: 'Đội ngũ CSKH của Thi Yên luôn sẵn sàng hỗ trợ bạn 24/7 qua hotline, chat hoặc email.'
  }
]

const showInfoPopup = ref(false)
const infoPopupContent = ref('')
function openInfoPopup(idx) {
  infoPopupContent.value = infoItems[idx].detail
  showInfoPopup.value = true
}
function closeInfoPopup() {
  showInfoPopup.value = false
}

function maskName(name) {
  if (!name) return ''
  return name.split(' ').map(word => word.charAt(0)).join(' ')
}

// Flash Sale countdown
const minFlashSale = 14 * 3600 + 53 * 60 + 21 // 14:53:21 in seconds = 53601
const maxFlashSale = 15 * 3600 + 45 * 60 + 17 // 15:45:17 in seconds = 56717
let flashSaleInterval = null

function randomFlashSaleTime() {
  // Random từ 10 đến 30 phút (600 - 1800 giây) cho đơn giản
  return Math.floor(Math.random() * (1800 - 600 + 1)) + 600
}

function startFlashSaleCountdown() {
  if (flashSaleInterval) clearInterval(flashSaleInterval)
  flashSaleLeft.value = randomFlashSaleTime()
  flashSaleInterval = setInterval(() => {
    // Check if component is still mounted before updating state
    if (!isComponentMounted.value) {
      clearInterval(flashSaleInterval)
      return
    }
    
    if (flashSaleLeft.value > 0) {
      flashSaleLeft.value--
    } else {
      flashSaleLeft.value = randomFlashSaleTime()
    }
  }, 1000)
}

// Image gallery functions
function selectImage(image, index) {
  selectedImage.value = image
  currentImageIndex.value = index
}

function nextImage() {
  if (currentImageIndex.value < galleryImages.value.length - 1) {
    currentImageIndex.value++
  } else {
    currentImageIndex.value = 0
  }
}

function previousImage() {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  } else {
    currentImageIndex.value = galleryImages.value.length - 1
  }
}

// Touch events for mobile
const touchStartX = ref(0)
const touchEndX = ref(0)

function handleTouchStart(e) {
  touchStartX.value = e.touches[0].clientX
}

function handleTouchMove(e) {
  touchEndX.value = e.touches[0].clientX
}

function handleTouchEnd() {
  const swipeThreshold = 50
  const diff = touchStartX.value - touchEndX.value
  
  if (Math.abs(diff) > swipeThreshold) {
    if (diff > 0) {
      nextImage()
    } else {
      previousImage()
    }
  }
}

// Auto play functions
function toggleAutoPlay() {
  isAutoPlaying.value = !isAutoPlaying.value
  if (isAutoPlaying.value) {
    startAutoPlay()
  } else {
    stopAutoPlay()
  }
}

function startAutoPlay() {
  if (autoPlayInterval) clearInterval(autoPlayInterval)
  autoPlayInterval = setInterval(() => {
    // Check if component is still mounted before updating state
    if (!isComponentMounted.value) {
      clearInterval(autoPlayInterval)
      return
    }
    nextImage()
  }, 3000)
}

function stopAutoPlay() {
  if (autoPlayInterval) clearInterval(autoPlayInterval)
}

// Social proof functions
function openMessage() {
  window.open('https://zalo.me/0396860584', '_blank')
}

import { navigateToSubdomain, navigateToMainDomain } from '../utils/domainUtils'

function navigateToProduct(id) {
  const path = `/products/${id}`
  
  // Check if this is a special product (ID 1 or 2)
  if (id === 1 || id === 2) {
    const domainChanged = navigateToSubdomain(path)
    if (!domainChanged) {
      router.push(path)
    }
  } else {
    // Regular product, use main domain
    const domainChanged = navigateToMainDomain(path)
    if (!domainChanged) {
      router.push(path)
    }
  }
}

function navigateToSocialProofProduct() {
  if (socialProofData.value && socialProofData.value.productId) {
    const path = `/products/${socialProofData.value.productId}`
    const productId = socialProofData.value.productId
    
    // Check if this is a special product (ID 1 or 2)
    if (productId === 1 || productId === 2) {
      const domainChanged = navigateToSubdomain(path)
      if (!domainChanged) {
        router.push(path)
      }
    } else {
      // Regular product, use main domain
      const domainChanged = navigateToMainDomain(path)
      if (!domainChanged) {
        router.push(path)
      }
    }
  }
}

function closeSocialProof() {
  showSocialProof.value = false
}

// Initialize auto play
let autoPlayInterval = null
const selectedImage = ref('')
const socialProofData = ref({
  productId: 1,
  productName: 'BỘT NGŨ HẮC MÈ ĐEN',
  productImage: '/images/products/me-den.jpg',
  customerName: 'Nguyễn Thị A',
  location: 'Hà Nội',
  timeAgo: '2 phút trước'
})



// Thêm ref để force reload ảnh
const imageReloadKey = ref(Date.now())

// Thêm function để reload ảnh khi cần
function reloadImages() {
  // Check if component is still mounted before updating state
  if (!isComponentMounted.value) return
  
  // Force reload bằng cách thay đổi key
  const currentTime = Date.now()
  // Trigger reactivity bằng cách thay đổi một ref
  imageReloadKey.value = currentTime
}

// Named functions for event listeners to enable proper cleanup
function handleWindowFocus() {
  setTimeout(() => {
    reloadImages()
  }, 100)
}

function handleVisibilityChange() {
  if (!document.hidden) {
    setTimeout(() => {
      reloadImages()
    }, 100)
  }
}
</script>

<!--
- Thay các chỗ dùng productData[ID] => product.value
- Thay Object.values(productData) => products.value
- Thay relatedBuyNowProducts => relatedProducts
- Khi xác nhận mua ngay, lấy sản phẩm từ products.value theo id
- Các nút chuyển sản phẩm, popup, ... đều dùng products.value
-->

<style scoped>
/* Custom styles for the product detail page */

/* Image loading improvements */
img {
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  backface-visibility: hidden;
  transform: translateZ(0);
  -webkit-font-smoothing: antialiased;
}

/* Force image reload on mobile */
@media (max-width: 768px) {
  img {
    will-change: transform;
  }
}

/* Zalo Floating Button Animations */
.zalo-container {
  animation: zaloFloat 3s ease-in-out infinite;
}

.zalo-float-btn {
  position: relative;
  background: linear-gradient(135deg, #008fe5, #0066cc);
  box-shadow: 0 8px 25px rgba(0, 143, 229, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible;
}

.zalo-float-btn:hover {
  transform: scale(1.1) translateY(-2px);
  box-shadow: 0 12px 35px rgba(0, 143, 229, 0.4);
  background: linear-gradient(135deg, #0099ff, #0077e6);
}

.zalo-float-btn:active {
  transform: scale(0.95);
}

/* Zalo Logo Animation */
.zalo-logo {
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.zalo-float-btn:hover .zalo-logo {
  transform: rotate(5deg) scale(1.05);
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

/* Pulse Ring Effect */
.zalo-pulse-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border: 1px solid #008fe5;
  border-radius: 50%;
  opacity: 0;
  animation: zaloPulse 2s ease-out infinite;
}

.zalo-pulse-ring::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border: 1px solid #008fe5;
  border-radius: 50%;
  opacity: 0;
  animation: zaloPulse 2s ease-out infinite 0.5s;
}

/* Notification Dot */
.zalo-notification-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  width: 16px;
  height: 16px;
  background: #ff4757;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: zaloNotificationBounce 2s ease-in-out infinite;
}

.zalo-notification-pulse {
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  animation: zaloNotificationPulse 1.5s ease-in-out infinite;
}

/* Keyframe Animations */
@keyframes zaloFloat {
  0%, 100% {
    transform: translateY(-50%) translateX(0);
  }
  50% {
    transform: translateY(-50%) translateX(-5px);
  }
}

@keyframes zaloPulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

@keyframes zaloNotificationBounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes zaloNotificationPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.8);
  }
}

/* Hover Effects */
.zalo-float-btn:hover .zalo-pulse-ring {
  animation-duration: 1s;
}

.zalo-float-btn:hover .zalo-notification-dot {
  animation-duration: 1s;
}

/* Mobile Responsive */
@media (max-width: 768px) {
  .zalo-container {
    right: 2rem;
  }
  
  .zalo-float-btn {
    padding: 0.75rem;
  }
  
  .zalo-logo {
    width: 2.5rem;
    height: 2.5rem;
  }
  
  .zalo-notification-dot {
    width: 14px;
    height: 14px;
    top: -2px;
    right: -2px;
  }
  
  .zalo-notification-pulse {
    width: 5px;
    height: 5px;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .zalo-float-btn {
    box-shadow: 0 8px 25px rgba(0, 143, 229, 0.4);
  }
  
  .zalo-float-btn:hover {
    box-shadow: 0 12px 35px rgba(0, 143, 229, 0.5);
  }
}

/* Product Detail Section Styling */
.product-detail-section {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-left: 4px solid #10b981;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.product-detail-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 2px;
  flex-shrink: 0;
}

.section-subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1rem;
  font-weight: 500;
  font-style: italic;
}

.section-content {
  font-size: 0.875rem;
  color: #374151;
  line-height: 1.6;
  margin-bottom: 0;
}

.benefits-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefits-list li {
  position: relative;
  padding: 0.75rem 0 0.75rem 2rem;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  color: #374151;
  line-height: 1.6;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  border-left: 3px solid #10b981;
  transition: all 0.2s ease;
}

.benefits-list li:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.1);
}

.benefits-list li::before {
  content: '✓';
  position: absolute;
  left: 0.75rem;
  top: 0.75rem;
  color: #10b981;
  font-weight: bold;
  font-size: 0.875rem;
}

.benefits-list li:last-child {
  margin-bottom: 0;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .product-detail-section {
    padding: 1rem;
    margin-bottom: 1rem;
  }
  
  .section-title {
    font-size: 0.875rem;
  }
  
  .benefits-list li {
    padding: 0.5rem 0 0.5rem 1.5rem;
    font-size: 0.8125rem;
  }
  
  .benefits-list li::before {
    left: 0.5rem;
    top: 0.5rem;
  }
}
</style>