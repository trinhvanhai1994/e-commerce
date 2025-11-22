<template>
  <div class="min-h-screen" style="background-color: #fefce8;">
    <!-- Banner Section: PC, Tablet & Mobile -->
    <section class="hero-section relative w-full overflow-hidden" data-aos="fade-down">
      <!-- Banner mobile & tablet -->
      <img :src="getImageUrlFromApi('/images/banner-mobile.png')" alt="Banner mobile" class="w-full object-contain drop-shadow-xl block lg:hidden cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(getImageUrlFromApi('/images/banner-mobile.png'), 'Banner mobile')" />
      <!-- Banner desktop (chỉ hiển thị từ lg trở lên) -->
      <img :src="getImageUrlFromApi('/images/banner.png')" alt="Banner PC" class="w-full h-auto object-cover drop-shadow-xl hidden lg:block cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(getImageUrlFromApi('/images/banner.png'), 'Banner PC')" />
    </section>

    <!-- GIỮ TRỌN DƯỠNG CHẤT & HƯƠNG VỊ NGUYÊN BẢN -->
    <section class="max-w-7xl mx-auto w-full px-4 md:px-4 py-8 grid grid-cols-1 md:grid-cols-5 gap-8 items-center">
      <div class="md:col-span-3 w-full" data-aos="fade-right">
        <h2 class="font-bold text-xl md:text-2xl mb-2 gradient-text-green">GIỮ TRỌN DƯỠNG CHẤT<br />VÀ HƯƠNG VỊ NGUYÊN BẢN</h2>
        <div class="text-gray-700 mb-4">
          Một bữa ăn nhẹ, nhưng đủ đầy.<br />
          Một cách chăm sóc bản thân, nhưng không cầu kỳ.<br />
          Một thói quen nhỏ, nhưng nuôi dưỡng cơ thể mỗi ngày.<br /><br />
          Với Thi Yên, giá trị của một sản phẩm tốt không chỉ nằm ở công thức, mà bắt đầu từ sự chắt lọc cẩn thận từng nguyên liệu, giữ lại những gì nguyên bản,  trọn vị và thật sự nuôi dưỡng cơ thể.<br /><br />
          Mỗi sản phẩm là sự giao thoa giữa công thức dưỡng sinh cổ truyền thuần thực vật và tư duy dinh dưỡng hiện đại tạo nên những bữa ăn tiện lợi, phù hợp với nhịp sống ngày nay.
        </div>
        <router-link to="/me" class="inline-block bg-green-500 text-white font-bold rounded-full px-6 py-2 shadow hover:bg-green-600 transition-all duration-300 hover:scale-105">Khám phá câu chuyện Thi Yên</router-link>
      </div>
      <div class="md:col-span-2 flex justify-center w-full" data-aos="fade-left">
        <img :src="getImageUrlFromApi('/images/baner-second.png')" alt="Hướng dẫn sử dụng Mè Đen" 
            class="w-full max-w-lg h-auto object-contain rounded-2xl cursor-pointer hover:opacity-90 transition-opacity duration-300" 
            @click="openImageModal(getImageUrlFromApi('/images/baner-second.png'), 'Hướng dẫn sử dụng Mè Đen')" />
      </div>
    </section>

    <!-- SẢN PHẨM BÁN CHẠY (Best Sellers) dạng grid 4 cột -->
    <section class="max-w-7xl mx-auto px-4 py-8">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">SẢN PHẨM BÁN CHẠY</h2>
      
      <!-- Loading state -->
      <div v-if="isLoading" class="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
        <div v-for="i in 4" :key="i" class="bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 overflow-hidden animate-pulse">
          <div class="w-full h-48 md:h-64 bg-gray-200"></div>
          <div class="p-4 md:p-6 space-y-3">
            <div class="h-4 bg-gray-200 rounded"></div>
            <div class="h-3 bg-gray-200 rounded w-3/4"></div>
            <div class="h-3 bg-gray-200 rounded w-1/2"></div>
          </div>
        </div>
      </div>
      
      <!-- Products grid -->
      <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-6">
        <div 
          v-for="(product, index) in bestSellers" 
          :key="product.id" 
          :data-aos="'fade-up'" 
          :data-aos-delay="index * 100" 
          class="product-card bg-white/60 backdrop-blur-sm rounded-2xl shadow-lg border border-green-200/50 overflow-hidden hover:shadow-xl transition-shadow duration-300 flex flex-col h-full"
        >
          <!-- Product Image -->
          <div class="relative cursor-pointer flex-shrink-0" @click="openProductDetail(product)">
            <img
              :src="getProductImage(product)"
              :alt="product.name"
              class="w-full h-48 md:h-64 object-cover hover:scale-105 transition-transform duration-300"
              @error="handleImageError"
              loading="lazy"
            />
            <div v-if="getProductDiscount(product) > 0" class="absolute top-2 left-2 bg-red-500 text-white px-2 py-1 rounded-full text-xs font-bold">
              -{{ getProductDiscount(product) }}%
            </div>
            <!-- Loading placeholder -->
            <div v-if="!product.image" class="w-full h-48 md:h-64 bg-gray-200 flex items-center justify-center">
              <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
              </svg>
            </div>
          </div>

          <!-- Product Info -->
          <div class="p-4 md:p-6 flex flex-col flex-1">
            <!-- Category and Rating -->
            <div class="flex items-center justify-between mb-2">
              <span class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full font-medium">
                {{ product.category === 'me-den' ? 'Mè đen' : 'Hồng đậu' }}
              </span>
              <div class="flex items-center">
                <div class="flex text-yellow-400">
                  <svg v-for="i in 5" :key="i" class="w-3 h-3 md:w-4 md:h-4 fill-current" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span class="ml-1 text-xs text-gray-600">({{ product.rating }})</span>
              </div>
            </div>

            <!-- Product Name -->
            <h3 class="font-bold text-gray-900 mb-2 text-sm md:text-base line-clamp-2">{{ product.name }}</h3>

            <!-- Price and Stock -->
            <div class="mt-auto">
              <div class="flex items-center gap-2 mb-2">
                <span class="text-lg md:text-xl font-bold text-green-600">{{ formatPrice(product.price) }}</span>
                <span v-if="product.oldPrice > product.price" class="text-sm text-gray-500 line-through">{{ formatPrice(product.oldPrice) }}</span>
              </div>
              <p class="text-xs text-gray-500 mb-3">Còn {{ product.stock }} sản phẩm</p>

              <!-- Action Buttons -->
              <div class="flex gap-2">
                <button
                  @click="openProductDetail(product)"
                  class="flex-1 bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-3 rounded-lg transition-all duration-200 text-xs md:text-sm flex items-center justify-center"
                >
                  Xem chi tiết
                </button>
                <button
                  @click="addToCartDirect(product)"
                  class="flex-1 bg-green-100 hover:bg-green-200 text-green-700 font-bold py-2 px-3 rounded-lg transition-all duration-200 text-xs md:text-sm flex items-center justify-center gap-1"
                >
                  <svg class="w-3 h-3 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5 6m0 0h9m-9 0V19a2 2 0 002 2h7a2 2 0 002-2v-4.5M9 17h6"/>
                  </svg>
                  Thêm vào giỏ
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- TIÊU CHUẨN CHẤT LƯỢNG -->
    <section class="max-w-5xl mx-auto px-4 py-8">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">TIÊU CHUẨN CHẤT LƯỢNG</h2>
      <div class="flex flex-col gap-4 md:gap-6">
        <div class="flex flex-row items-center gap-3 md:gap-4 quality-card" data-aos="fade-right">
          <img :src="getImageUrlFromApi('/images/iso.png')" alt="ISO" class="w-12 h-12 md:w-16 md:h-16 object-contain quality-icon cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(getImageUrlFromApi('/images/iso.png'), 'TIÊU CHUẨN ISO 22000:2018')" />
          <div>
            <div class="font-bold text-sm md:text-base mb-1">TIÊU CHUẨN ISO 22000:2018</div>
            <div class="text-gray-700 text-xs md:text-sm">Nhà máy sản xuất có hệ thống quản lý tốt an toàn vệ sinh thực phẩm và đảm bảo cung cấp các sản phẩm thực phẩm an toàn, chất lượng cho người tiêu dùng.</div>
          </div>
        </div>
        <div class="flex flex-row items-center gap-3 md:gap-4 quality-card" data-aos="fade-left">
          <img :src="getImageUrlFromApi('/images/gmp.png')" alt="GMP" class="w-12 h-12 md:w-16 md:h-16 object-contain quality-icon cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(getImageUrlFromApi('/images/gmp.png'), 'CHỨNG NHẬN GMP – Codex')" />
          <div>
            <div class="font-bold text-sm md:text-base mb-1">Chứng nhận GMP Codex (TCVN 5603:2023)</div>
            <div class="text-gray-700 text-xs md:text-sm">Do TQC cấp theo lĩnh vực thực phẩm.<br />Nhà máy đảm bảo toàn bộ quy trình sản xuất tuân thủ Thực hành sản xuất tốt (Good Manufacturing Practice - GMP)</div>
          </div>
        </div>
      </div>
    </section>

    <!-- DINH DƯỠNG THUẦN THỰC VẬT -->
    <section class="max-w-8xl mx-auto px-4 py-8" data-aos="fade-up">
      <!-- Four Home Image -->
      <div class="flex justify-center mb-8" data-aos="fade-up" data-aos-delay="100">
        <img 
          :src="getImageUrlFromApi('/images/four-home.png')" 
          alt="Four Home" 
          class="w-full max-w-4xl h-auto object-contain cursor-pointer hover:opacity-90 transition-opacity duration-300" 
          @click="openImageModal(getImageUrlFromApi('/images/four-home.png'), 'Four Home')"
        />
      </div>
      
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG</h2>
      <!-- Desktop: ảnh vuông to, ẩn trên mobile -->
      <div class="hidden md:grid md:grid-cols-4 md:gap-6">
        <img
          v-for="(img, index) in images"
          :key="'d' + index"
          :src="img.src"
          :alt="img.alt"
          class="w-full aspect-square object-contain bg-gray-50 rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
          @click="openImageModal(img.src, img.alt)"
        />
      </div>

      <!-- Mobile: swiper slider, ẩn trên desktop -->
      <div class="md:hidden">
        <swiper
          :modules="swiperModules"
          :slides-per-view="2.5"
          space-between="12"
          :loop="shouldLoop"
          :autoplay="{
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true
          }"
          :speed="800"
          :grab-cursor="true"
          :centered-slides="false"
          :breakpoints="{
            320: {
              slidesPerView: 2.2,
              spaceBetween: 8
            },
            480: {
              slidesPerView: 2.5,
              spaceBetween: 12
            }
          }"
        >
          <swiper-slide v-for="(img, index) in images" :key="'m' + index">
            <img
              :src="img.src"
              :alt="img.alt"
              class="w-full h-40 object-contain rounded-xl shadow hover:shadow-lg transition-shadow duration-300 cursor-pointer hover:opacity-90 transition-opacity duration-300"
              @click="openImageModal(img.src, img.alt)"
            />
          </swiper-slide>
        </swiper>
      </div>
    </section>

    <!-- BÀI VIẾT NỔI BẬT -->
    <section class="max-w-7xl mx-auto px-4 py-4">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">BÀI VIẾT NỔI BẬT</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-8">
        <div v-for="(post, index) in blogPosts" :key="post.id" :data-aos="'fade-up'" :data-aos-delay="index * 150" class="bg-white rounded-xl shadow p-3 md:p-4 flex flex-col items-center blog-card">
          <!-- Ảnh với 2 tùy chọn: click để phóng to hoặc click để xem bài viết -->
          <div class="relative w-full">
            <img :src="post.image" :alt="post.title" class="w-full h-32 md:h-40 object-cover rounded mb-3 cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(post.image, post.title)" />
            <!-- Nút xem bài viết trên ảnh -->
            <div class="absolute inset-0 bg-black bg-opacity-0 hover:bg-opacity-20 transition-all duration-300 rounded mb-3 flex items-center justify-center">
              <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug } }" class="opacity-0 hover:opacity-100 transition-opacity duration-300 bg-white text-green-600 px-3 py-1 rounded-full text-xs font-semibold">
                Xem bài viết
              </router-link>
            </div>
          </div>
          <!-- Tiêu đề và mô tả có thể click để xem bài viết -->
          <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug } }" class="block w-full">
            <div class="font-bold text-green-700 text-sm md:text-base mb-1 text-center hover:text-green-600 transition-colors duration-200 cursor-pointer">{{ post.title }}</div>
          </router-link>
          <router-link :to="{ name: 'BlogDetail', params: { slug: post.slug } }" class="block w-full">
            <div class="text-gray-600 text-xs md:text-sm text-center hover:text-gray-800 transition-colors duration-200 cursor-pointer">{{ post.excerpt }}</div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- NÚT NỔI BÊN PHẢI -->
    <div 
      class="fixed right-3 md:right-6 z-50 flex flex-col items-center gap-3 md:gap-4"
      style="top:66%;transform:translateY(-50%);"
      data-aos="fade-left" data-aos-delay="1000"
    >
      <a href="https://m.me/yourpage" target="_blank" rel="noopener" class="bg-blue-500 rounded-full w-12 h-12 md:w-14 md:h-14 flex items-center justify-center shadow-lg hover:bg-blue-600 transition-all duration-300 hover:scale-110 floating-btn">
        <svg class="w-6 h-6 md:w-7 md:h-7 text-white" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2C6.477 2 2 6.016 2 11.01c0 2.49 1.01 4.77 2.77 6.53V22l2.52-1.39c1.13.31 2.33.48 3.71.48 5.523 0 10-4.016 10-9.01S17.523 2 12 2zm.25 13.5l-2.25-2.4-4.25 2.4 5.5-6 2.25 2.4 4.25-2.4-5.5 6z"/>
        </svg>
      </a>
      <a href="https://zalo.me/yourzalo" target="_blank" rel="noopener" class="bg-green-500 rounded-full w-12 h-12 md:w-14 md:h-14 flex items-center justify-center shadow-lg hover:bg-green-600 transition-all duration-300 font-bold text-sm md:text-lg text-white hover:scale-110 floating-btn">ZALO</a>
    </div>

    <!-- Success Popup -->
    <div
      v-if="showPopup"
      class="fixed top-3 right-3 md:top-6 md:right-6 z-50 bg-green-500 text-white px-4 py-2 md:px-6 md:py-3 rounded-lg shadow-lg animate-bounce max-w-xs md:max-w-sm"
    >
      <div class="flex items-center">
        <svg class="w-4 h-4 md:w-5 md:h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
        </svg>
        <span class="text-sm md:text-base">{{ popupMessage }}</span>
      </div>
    </div>

    <!-- Image Zoom Modal -->
    <div
      v-if="showImageModal"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-black bg-opacity-90 backdrop-blur-sm"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center p-4">
        <!-- Close Button -->
        <button
          @click="closeImageModal"
          class="absolute top-4 right-4 z-10 bg-white rounded-full p-3 shadow-lg hover:bg-gray-100 transition-colors duration-200"
        >
          <svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
        
        <!-- Zoomed Image Container -->
        <div class="relative max-w-[90vw] max-h-[90vh] flex items-center justify-center">
          <img
            :src="zoomedImageSrc"
            :alt="zoomedImageAlt"
            class="max-w-full max-h-full object-contain rounded-lg shadow-2xl zoom-image"
            @click.stop
          />
        </div>
        
        <!-- Image Info -->
        <div v-if="zoomedImageAlt" class="absolute bottom-4 left-4 right-4 bg-black bg-opacity-70 text-white p-3 rounded-lg">
          <p class="text-sm md:text-base text-center">{{ zoomedImageAlt }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../components/ProductCard.vue'
import { useCartStore } from '../stores/cart'
import { productAPI } from '@/utils/api.js'
import { getProductImage } from '../utils/productImage'
import { getImageUrlFromApi } from '../utils/imageUtils.js'
import { getProductDiscount } from '../utils/productUtils.js'
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay } from 'swiper/modules';
import 'swiper/css';

const router = useRouter()
const cartStore = useCartStore()

// Swiper modules
const swiperModules = [Autoplay]

const showQuantityPopup = ref(false)
const selectedBundle = ref(null)
const quantity = ref(1)
const showCartPopup = ref(false)
const showProductDetail = ref(false)
const productDetail = ref({})
const showPopup = ref(false)
const popupMessage = ref("")
const isLoading = ref(false)
const showImageModal = ref(false)
const zoomedImageSrc = ref("")
const zoomedImageAlt = ref("")

const images = [
  { src: getImageUrlFromApi('/images/struct/1.png'), alt: 'Thêm mô tả 1' },
  { src: getImageUrlFromApi('/images/struct/2.png'), alt: 'Thêm mô tả 2' },
  { src: getImageUrlFromApi('/images/struct/3.png'), alt: 'Thêm mô tả 3' },
  { src: getImageUrlFromApi('/images/struct/4.png'), alt: 'Thêm mô tả 4' },
];

// Computed property để tự động tắt loop nếu không đủ slides
// Loop cần ít nhất 2 * slidesPerView slides để hoạt động đúng
const shouldLoop = computed(() => {
  const minSlidesForLoop = 2 * 2.5 // 2 * slidesPerView
  return images.length >= minSlidesForLoop
})

// Featured product data
const featuredProduct = ref({
  id: 1,
  name: 'Premium Product',
  description: 'Experience the ultimate quality with our flagship product. Designed for excellence and built to last.',
  price: 299.99,
  image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=60',
  features: [
    'Premium Quality Materials',
    'Advanced Technology',
    'Lifetime Warranty',
    'Free Shipping'
  ]
})

// Product reviews data
const productReviews = ref([
  {
    id: 1,
    title: 'Why Our Premium Product is Worth Every Penny',
    excerpt: 'An in-depth review of our flagship product, exploring its features, benefits, and real-world applications.',
    author: 'John Smith',
    date: 'March 15, 2024',
    image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=60',
    category: 'Product Review',
    readTime: '5 min read'
  },
  {
    id: 2,
    title: 'Comparing Our Bundles: Which One is Right for You?',
    excerpt: 'A comprehensive comparison of our different product bundles to help you make the right choice.',
    author: 'Sarah Johnson',
    date: 'March 12, 2024',
    image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=60',
    category: 'Comparison',
    readTime: '7 min read'
  },
  {
    id: 3,
    title: 'Customer Success Stories: Real Results with Our Products',
    excerpt: 'Read about how our customers are achieving their goals with our premium products.',
    author: 'Mike Wilson',
    date: 'March 10, 2024',
    image: 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&auto=format&fit=crop&q=60',
    category: 'Success Stories',
    readTime: '6 min read'
  }
])

import { navigateToSubdomain, navigateToMainDomain } from '../utils/domainUtils'

const navigateToProduct = (id) => {
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

const openProductDetail = (product) => {
  const path = `/products/${product.id}`
  
  // Check if this is a special product (ID 1 or 2)
  if (product.id === 1 || product.id === 2) {
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

const navigateToBundle = (id) => {
  const path = `/bundles/${id}`
  const domainChanged = navigateToMainDomain(path)
  if (!domainChanged) {
    router.push(path)
  }
}

const navigateToBlog = (slug) => {
  const path = `/blog/${slug}`
  const domainChanged = navigateToMainDomain(path)
  if (!domainChanged) {
    router.push(path)
  }
}

const openQuantityPopup = (bundle) => {
  selectedBundle.value = bundle
  quantity.value = 1
  showQuantityPopup.value = true
}

const addToCartDirect = (product) => {
  cartStore.addToCart(product, 1)
  popupMessage.value = `Đã thêm "${product.name}" vào giỏ hàng!`
  showPopup.value = true
  setTimeout(() => {
    showPopup.value = false
  }, 3000)
}

const closeQuantityPopup = () => {
  showQuantityPopup.value = false
  selectedBundle.value = null
  quantity.value = 1
}

const incrementQuantity = () => {
  quantity.value++
}

const decrementQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  if (selectedBundle.value) {
    cartStore.addToCart(selectedBundle.value, quantity.value)
    closeQuantityPopup()
  }
}

const getTotalItems = () => {
  return cartStore.items.reduce((total, item) => total + item.quantity, 0)
}

const openCartPopup = () => {
  showCartPopup.value = true
}

const closeCartPopup = () => {
  showCartPopup.value = false
}

const cartTotal = () => {
  return cartStore.items.reduce((total, item) => total + item.price * item.quantity, 0)
}

const cartOriginalTotal = () => {
  return cartStore.items.reduce((total, item) => total + (item.originalPrice || 0) * item.quantity, 0)
}

// Khôi phục cart từ localStorage khi load trang
onMounted(() => {
  const savedCart = localStorage.getItem('cartItems')
  if (savedCart) {
    try {
      cartStore.items = JSON.parse(savedCart)
    } catch (e) {
      cartStore.items = []
    }
  }
  
  // Refresh AOS when component mounts
  nextTick(() => {
    if (window.AOS) {
      window.AOS.refresh()
    }
  })
})

// Lưu cart vào localStorage mỗi khi thay đổi
watch(() => cartStore.items, (newCart) => {
  localStorage.setItem('cartItems', JSON.stringify(newCart))
}, { deep: true })

// Dữ liệu mẫu best sellers
const bestSellers = ref([])

// Dữ liệu mẫu blog
const blogPosts = ref([
  {
    id: 1,
    title: 'Dinh Dưỡng Đen Nguyên Bản Từ 5 Loại Hạt Trong Bột Ngũ Hắc Mè Đen',
    excerpt: 'Bột ngũ hắc mè đen Thi Yên là bữa ăn thay thế dinh dưỡng từ 5 loại hạt đen nguyên bản, phù hợp với lối sống lành mạnh và hiện đại. Dưỡng sinh dễ dàng cho cơ thể khỏe mạnh.',
    image: getImageUrlFromApi('/images/blogs/blog1/avatar1.png'),
    slug: 'blog1'
  },
  {
    id: 2,
    title: 'Hiểu Cơ Bản Về Dưỡng Sinh: Cân Bằng Âm Dương & Thuận Tự Nhiên',
    excerpt: 'Khám phá triết lý dưỡng sinh của Thi Yên, kết hợp tinh hoa dưỡng sinh cổ truyền và dinh dưỡng hiện đại. Mỗi sản phẩm là bữa ăn tiện lợi, cân bằng âm dương, thuận theo ngũ hành, nuôi dưỡng cơ thể, giấc ngủ, dưỡng nhan và hỗ trợ sức khỏe toàn diện',
    image: getImageUrlFromApi('/images/blogs/blog2/avatar2.png'),
    slug: 'blog2'
  },
  {
    id: 3,
    title: 'Dưỡng Sinh Hiện Đại Là Gì? Vì Sao Người Trẻ Nên Nuôi Cơ Thể Từ Gốc Mỗi Ngày?',
    excerpt: 'Khám phá dưỡng sinh hiện đại với Thi Yên: Bột ngũ hắc mè đen và bột ngũ sắc hồng đậu giúp nuôi dưỡng cơ thể từ bên trong, cải thiện giấc ngủ, dưỡng nhan, dinh dưỡng cho tóc. Sản phẩm thuần tự nhiên, tiện lợi, phù hợp cho người trẻ và lối sống lành mạnh.',
    image: getImageUrlFromApi('/images/blogs/blog3/avatar3.png'),
    slug: 'blog3'
  }
])

// Danh mục sản phẩm nổi bật
const categories = ref([
  {
    id: 1,
    name: 'Thực phẩm bổ dưỡng',
    icon: getImageUrlFromApi('/images/products/me-den.jpg'),
    desc: 'Mè đen và hồng đậu tự nhiên, bổ dưỡng.',
    link: '/products?cat=food'
  }
])

// Feedback khách hàng (ảnh mẫu)
const feedbackImages = [
  getImageUrlFromApi('/images/review/koc1.jpg'),
  getImageUrlFromApi('/images/review/koc2.jpg'),
  getImageUrlFromApi('/images/review/koc3.jpg'),
  getImageUrlFromApi('/images/review/koc4.jpg'),
]

async function fetchProducts() {
  isLoading.value = true
  try {
    // Get featured products (top 4 by priority) for homepage
    const data = await productAPI.getFeaturedProducts()
    // Nếu API trả về mảng, lấy luôn, nếu trả về {data: [...]}, lấy data
    const products = Array.isArray(data) ? data : (data.data || [])
    
    // Xử lý dữ liệu sản phẩm từ API
    const processedProducts = products.map(product => ({
      ...product,
      // Đảm bảo có đầy đủ các trường cần thiết
      id: product.id || 1,
      name: product.name || 'Sản phẩm',
      price: product.price || 299000,
      oldPrice: product.oldPrice || 390000,
      // Sử dụng mainImage từ API, fallback về getProductImage
      image: product.mainImage ? getProductImage(product) : getProductImage(product.id),
      shortDesc: product.shortDesc || 'Mô tả sản phẩm...',
      category: product.category || 'me-den',
      rating: product.rating || 5,
      stock: product.stock || 100,
      discount: product.discount || Math.floor(((product.oldPrice || 390000) - (product.price || 299000)) / (product.oldPrice || 390000) * 100),
      priority: product.priority || 999
    }))
    
    // Backend đã sort theo priority và limit 4 sản phẩm
    // Lấy tối đa 4 sản phẩm đầu tiên (đã được sort theo priority từ backend)
    bestSellers.value = processedProducts.slice(0, 4)
    
    console.log('Products loaded from API:', processedProducts)
  } catch (e) {
    console.error('Error fetching products:', e)
    // Fallback data nếu API fail
    bestSellers.value = [
      {
        id: 1,
        name: 'BỘT NGŨ HẮC MÈ ĐEN',
        price: 299000,
        oldPrice: 390000,
        image: getProductImage(1),
        shortDesc: 'Bột Ngũ Hắc Mè Đen là bữa ăn thay thế tiện lợi...',
        category: 'me-den',
        rating: 5,
        stock: 100,
        discount: 23
      },
      {
        id: 2,
        name: 'COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN',
        price: 499000,
        oldPrice: 780000,
        image: getProductImage(2),
        shortDesc: 'Combo tiết kiệm cho gia đình...',
        category: 'combo',
        rating: 5,
        stock: 50,
        discount: 36
      },
      {
        id: 3,
        name: 'BỘT NGŨ SẮC HỒNG ĐẬU',
        price: 299000,
        oldPrice: 390000,
        image: getProductImage(3),
        shortDesc: 'Bột Ngũ Sắc Hồng Đậu là bữa ăn thay thế tiện lợi...',
        category: 'hong-dau',
        rating: 5,
        stock: 80,
        discount: 23
      },
      {
        id: 4,
        name: 'COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU',
        price: 499000,
        oldPrice: 780000,
        image: getProductImage(4),
        shortDesc: 'Combo tiết kiệm cho gia đình...',
        category: 'combo',
        rating: 5,
        stock: 40,
        discount: 36
      },
      {
        id: 5,
        name: 'COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)',
        price: 499000,
        oldPrice: 780000,
        image: getProductImage(5),
        shortDesc: 'Combo tiết kiệm cho gia đình...',
        category: 'combo',
        rating: 5,
        stock: 40,
        discount: 36
      }
    ]
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchProducts()

  // Add keyboard event listener
  document.addEventListener('keydown', handleKeydown)

  // Refresh AOS when component mounts
  nextTick(() => {
    if (window.AOS) {
      window.AOS.refresh()
    }
  })
})

// Cleanup event listener on unmount
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  // Restore body scroll in case component unmounts with modal open
  document.body.style.overflow = 'auto'
})

// Format price function
const formatPrice = (price) => {
  return price.toLocaleString() + '₫'
}

// Xử lý lỗi ảnh
const handleImageError = (event) => {
  // Thay thế ảnh lỗi bằng ảnh placeholder
  event.target.src = getImageUrlFromApi('/images/products/details/black/1.png')
  event.target.onerror = null // Tránh loop vô hạn
}

// Xử lý URL ảnh từ API
const getImageUrl = (imagePath) => {
  if (!imagePath) {
    console.log('No image path provided, using default')
    return getImageUrlFromApi('/images/products/details/black/1.png')
  }
  
  console.log('Using image URL:', imagePath)
  return imagePath
}

// Image zoom functions
const openImageModal = (src, alt = '') => {
  // Đảm bảo ảnh được load trước khi hiển thị
  const img = new Image()
  img.onload = () => {
    zoomedImageSrc.value = src
    zoomedImageAlt.value = alt
    showImageModal.value = true
    // Prevent body scroll when modal is open
    document.body.style.overflow = 'hidden'
  }
  img.onerror = () => {
    // Fallback nếu ảnh không load được
    zoomedImageSrc.value = src
    zoomedImageAlt.value = alt
    showImageModal.value = true
    document.body.style.overflow = 'hidden'
  }
  img.src = src
}

const closeImageModal = () => {
  showImageModal.value = false
  zoomedImageSrc.value = ""
  zoomedImageAlt.value = ""
  // Restore body scroll
  document.body.style.overflow = 'auto'
}

// Handle keyboard events
const handleKeydown = (event) => {
  if (event.key === 'Escape' && showImageModal.value) {
    closeImageModal()
  }
}
</script>

<style scoped>

@media (min-width: 768px) {
  .swiper-container {
    display: none !important;
  }
}

/* Swiper mobile optimizations */
.md\\:hidden .swiper {
  overflow: visible;
}

.md\\:hidden .swiper-slide {
  transition: transform 0.3s ease;
}

.md\\:hidden .swiper-slide:hover {
  transform: scale(1.02);
}

/* Ensure smooth autoplay */
.md\\:hidden .swiper-wrapper {
  transition-timing-function: ease-in-out;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
input#search-input:focus {
  outline: none !important;
}
.cursor-grabbing {
  cursor: grabbing !important;
  user-select: none;
}
/* Ẩn thanh cuộn ngang */
.hide-scrollbar {
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.hide-scrollbar::-webkit-scrollbar {
  display: none; /* Chrome/Safari/Webkit */
}

/* Animation styles */
.product-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.quality-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  padding: 1rem;
  border-radius: 0.75rem;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.quality-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
}

.quality-icon {
  transition: transform 0.3s ease;
}

.quality-card:hover .quality-icon {
  transform: scale(1.1) rotate(5deg);
}

.nutrition-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.nutrition-card:hover {
  transform: scale(1.02);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.blog-card {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.blog-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

/* Blog card specific styles */
.blog-card .relative:hover .absolute {
  background-color: rgba(0, 0, 0, 0.1);
}

.blog-card .relative .absolute a {
  transform: translateY(0);
  transition: all 0.3s ease;
}

.blog-card .relative:hover .absolute a {
  opacity: 1;
  transform: translateY(-2px);
}

/* Hover effects for blog text links */
.blog-card a:hover div {
  transform: translateY(-1px);
}

.floating-btn {
  animation: float 3s ease-in-out infinite;
}

.floating-btn:nth-child(1) {
  animation-delay: 0s;
}

.floating-btn:nth-child(2) {
  animation-delay: 0.5s;
}

.floating-btn:nth-child(3) {
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* Pulse animation for important elements */
.pulse-on-hover:hover {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* Fade in animation */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fadeIn 0.6s ease-out;
}

/* Slide in animations */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.slide-in-left {
  animation: slideInLeft 0.6s ease-out;
}

.slide-in-right {
  animation: slideInRight 0.6s ease-out;
}

/* Smooth transitions for all interactive elements */
* {
  transition: all 0.3s ease;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #ec4899;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #db2777;
}

/* Gradient text effect */
.gradient-text {
  background: linear-gradient(45deg, #ec4899, #f97316);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Green gradient text effect */
.gradient-text-green {
  background: linear-gradient(45deg, #059669, #10b981);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Image hover effects */
img {
  transition: transform 0.3s ease, filter 0.3s ease;
}

img:hover {
  transform: scale(1.02);
  filter: brightness(1.1);
}

/* Button hover effects */
button {
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

/* Section spacing and background effects */
section {
  position: relative;
  overflow: hidden;
}

section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 0%, rgba(236, 72, 153, 0.02) 50%, transparent 100%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

section:hover::before {
  opacity: 1;
}

.animate-bounce {
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0,0,0);
  }
  40%, 43% {
    transform: translate3d(0,-30px,0);
  }
  70% {
    transform: translate3d(0,-15px,0);
  }
  90% {
    transform: translate3d(0,-4px,0);
  }
}

/* Image Zoom Modal Styles */
.zoom-image {
  animation: zoomIn 0.3s ease-out;
  transform-origin: center;
  /* Đảm bảo tất cả ảnh có kích thước đồng nhất */
  width: auto;
  height: auto;
  max-width: 90vw;
  max-height: 90vh;
  object-fit: contain;
  object-position: center;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Modal backdrop animation */
.fixed.inset-0 {
  animation: fadeIn 0.3s ease-out;
}

/* Close button hover effect */
.absolute.top-4.right-4 button:hover {
  transform: scale(1.1);
  background-color: #f3f4f6;
}

/* Image hover effects for clickable images */
img[class*="cursor-pointer"]:hover {
  transform: scale(1.02);
  filter: brightness(1.05);
}

/* Smooth transitions for all clickable images */
img[class*="cursor-pointer"] {
  transition: all 0.3s ease;
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

/* Banner responsive adjustments */
@media (max-width: 1023px) {
  /* Tablet và mobile sử dụng banner-mobile */
  .w-full.object-cover {
    height: auto; /* Tự động điều chỉnh chiều cao theo tỷ lệ ảnh */
    object-fit: contain; /* Hiển thị full ảnh không bị cắt */
    object-position: center;
    background-color: transparent; /* Bỏ background màu */
  }
}

@media (max-width: 768px) {
  /* Mobile specific adjustments */
  .w-full.object-cover {
    height: auto; /* Tự động điều chỉnh chiều cao theo tỷ lệ ảnh */
    object-fit: contain; /* Hiển thị full ảnh không bị cắt */
    object-position: center;
    background-color: transparent; /* Bỏ background màu */
  }
  
  /* Modal responsive adjustments */
  .zoom-image {
    max-width: 95vw;
    max-height: 85vh;
  }
  
  .absolute.top-4.right-4 {
    top: 1rem;
    right: 1rem;
  }
  
  .absolute.top-4.right-4 button {
    padding: 0.75rem;
  }
  
  .absolute.top-4.right-4 svg {
    width: 1.25rem;
    height: 1.25rem;
  }
  
  .absolute.bottom-4 {
    bottom: 1rem;
    left: 1rem;
    right: 1rem;
  }
}

/* Tablet specific adjustments */
@media (min-width: 769px) and (max-width: 1023px) {
  /* Tablet sử dụng banner-mobile với height tối ưu */
  .w-full.object-cover {
    height: auto; /* Tự động điều chỉnh chiều cao theo tỷ lệ ảnh */
    object-fit: contain; /* Hiển thị full ảnh không bị cắt */
    object-position: center;
    background-color: transparent; /* Bỏ background màu */
  }
  
  /* Tablet grid images - hiển thị full ảnh không cắt */
  .hidden.md\\:grid img {
    object-fit: contain !important;
    object-position: center;
    background-color: #f9fafb;
    padding: 0.5rem;
  }
  
  .zoom-image {
    max-width: 85vw;
    max-height: 80vh;
  }
}

/* Desktop responsive adjustments for modal */
@media (min-width: 1024px) {
  /* Desktop banner - đảm bảo hiển thị đầy đủ */
  .hero-section img.hidden.lg\\:block {
    height: auto !important;
    min-height: 400px;
    max-height: 600px;
    object-fit: cover;
    object-position: center top; /* Ưu tiên hiển thị phần trên của ảnh */
  }
  
  /* Desktop grid images - có thể dùng object-cover hoặc contain tùy ý */
  .hidden.md\\:grid img {
    object-fit: contain;
    object-position: center;
    background-color: #f9fafb;
    padding: 0.5rem;
  }
  
  .zoom-image {
    max-width: 80vw;
    max-height: 80vh;
  }
}

/* Đảm bảo ảnh luôn hiển thị đẹp với aspect ratio */
.zoom-image {
  border-radius: 0.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

/* Loading state cho ảnh */
.zoom-image[src=""] {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style> 