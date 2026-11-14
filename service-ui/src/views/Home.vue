<template>
  <div class="min-h-screen" style="background-color: #fefce8;">
    <!-- Banner Section: PC, Tablet & Mobile -->
    <section v-if="sectionEnabled('hero')" class="hero-section relative w-full overflow-hidden" data-aos="fade-down">
      <img v-if="heroMobileUrl" :src="heroMobileUrl" alt="Banner mobile" class="w-full object-contain drop-shadow-xl block lg:hidden cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(heroMobileUrl, 'Banner mobile')" />
      <img v-if="heroDesktopUrl" :src="heroDesktopUrl" alt="Banner PC" class="w-full h-auto object-cover drop-shadow-xl hidden lg:block cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(heroDesktopUrl, 'Banner PC')" />
    </section>

    <section v-if="sectionEnabled('story')" class="max-w-7xl mx-auto w-full px-4 md:px-4 py-8 grid grid-cols-1 md:grid-cols-5 gap-8 items-center">
      <div class="md:col-span-3 w-full" data-aos="fade-right">
        <h2 class="font-bold text-xl md:text-2xl mb-2 gradient-text-green whitespace-pre-line">{{ sections.story?.title }}</h2>
        <div class="text-gray-700 mb-4 whitespace-pre-line">{{ sections.story?.body }}</div>
        <component
          :is="isInternalLink(storyCtaLink) ? 'router-link' : 'a'"
          v-bind="linkAttrs(storyCtaLink)"
          class="inline-block bg-green-500 text-white font-bold rounded-full px-6 py-2 shadow hover:bg-green-600 transition-all duration-300 hover:scale-105"
        >{{ sections.story?.ctaText || 'Khám phá câu chuyện Thi Yên' }}</component>
      </div>
      <div class="md:col-span-2 flex justify-center w-full" data-aos="fade-left">
        <img v-if="storyImageUrl" :src="storyImageUrl" alt="Hướng dẫn sử dụng Mè Đen" 
            class="w-full max-w-lg h-auto object-contain rounded-2xl cursor-pointer hover:opacity-90 transition-opacity duration-300" 
            @click="openImageModal(storyImageUrl, 'Hướng dẫn sử dụng Mè Đen')" />
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

    <section v-if="sectionEnabled('quality')" class="max-w-5xl mx-auto px-4 py-8">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">{{ sections.quality?.title || 'TIÊU CHUẨN CHẤT LƯỢNG' }}</h2>
      <div class="flex flex-col gap-4 md:gap-6">
        <div
          v-for="(item, qIdx) in qualityItems"
          :key="qIdx"
          class="flex flex-row items-center gap-3 md:gap-4 quality-card"
          :data-aos="qIdx % 2 === 0 ? 'fade-right' : 'fade-left'"
        >
          <img v-if="item.iconUrl" :src="item.iconUrl" :alt="item.title" class="w-12 h-12 md:w-16 md:h-16 object-contain quality-icon cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(item.iconUrl, item.title)" />
          <div>
            <div class="font-bold text-sm md:text-base mb-1">{{ item.title }}</div>
            <div class="text-gray-700 text-xs md:text-sm whitespace-pre-line">{{ item.body }}</div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="sectionEnabled('meal_gallery')" class="max-w-8xl mx-auto px-4 py-8" data-aos="fade-up">
      <div class="flex justify-center mb-8" data-aos="fade-up" data-aos-delay="100">
        <img 
          v-if="mealBannerUrl"
          :src="mealBannerUrl" 
          alt="Four Home" 
          class="w-full max-w-4xl h-auto object-contain cursor-pointer hover:opacity-90 transition-opacity duration-300" 
          @click="openImageModal(mealBannerUrl, 'Four Home')"
        />
      </div>
      
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">{{ mealTitle }}</h2>
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
      <div class="md:hidden" v-if="images && images.length > 0">
        <ImageSwiper
          :images="images"
          :slides-per-view="2.5"
          :space-between="12"
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
          @image-click="openImageModal"
        />
      </div>
    </section>

    <!-- BÀI VIẾT NỔI BẬT -->
    <section v-if="blogPosts.length" class="max-w-7xl mx-auto px-4 py-4">
      <h2 class="text-2xl md:text-3xl font-bold text-green-600 mb-8 text-center gradient-text-green" data-aos="fade-up">BÀI VIẾT NỔI BẬT</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-8">
        <div v-for="(post, index) in blogPosts" :key="post.id" :data-aos="'fade-up'" :data-aos-delay="index * 150" class="bg-white rounded-xl shadow p-3 md:p-4 flex flex-col items-center blog-card">
          <!-- Ảnh với 2 tùy chọn: click để phóng to hoặc click để xem bài viết -->
          <div class="relative w-full">
            <img :src="postCover(post)" :alt="post.title" class="w-full h-32 md:h-40 object-cover rounded mb-3 cursor-pointer hover:opacity-90 transition-opacity duration-300" @click="openImageModal(postCover(post), post.title)" />
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
      <a v-if="sectionEnabled('floating_chat')" :href="messengerUrl" target="_blank" rel="noopener" class="bg-blue-500 rounded-full w-12 h-12 md:w-14 md:h-14 flex items-center justify-center shadow-lg hover:bg-blue-600 transition-all duration-300 hover:scale-110 floating-btn">
        <svg class="w-6 h-6 md:w-7 md:h-7 text-white" fill="currentColor" viewBox="0 0 24 24">
          <path d="M12 2C6.477 2 2 6.016 2 11.01c0 2.49 1.01 4.77 2.77 6.53V22l2.52-1.39c1.13.31 2.33.48 3.71.48 5.523 0 10-4.016 10-9.01S17.523 2 12 2zm.25 13.5l-2.25-2.4-4.25 2.4 5.5-6 2.25 2.4 4.25-2.4-5.5 6z"/>
        </svg>
      </a>
      <a v-if="sectionEnabled('floating_chat')" :href="zaloUrl" target="_blank" rel="noopener" class="bg-green-500 rounded-full w-12 h-12 md:w-14 md:h-14 flex items-center justify-center shadow-lg hover:bg-green-600 transition-all duration-300 font-bold text-sm md:text-lg text-white hover:scale-110 floating-btn">ZALO</a>
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
import ImageSwiper from '../components/ImageSwiper.vue'
import { useCartStore } from '../stores/cart'
import { productAPI } from '@/utils/api.js'
import { getProductImage } from '../utils/productImage'
import { getImageUrlFromApi } from '../utils/imageUtils.js'
import { getProductDiscount } from '../utils/productUtils.js'
import { contentService } from '../services/content.service.js'
import { defaultHomepage, resolveCmsImage, resolveSlotImage, DEFAULT_IMAGE_PATHS } from '../content/homeDefaults.js'
import { isInternalLink, linkAttrs } from '../utils/links.js'

const router = useRouter()
const cartStore = useCartStore()

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

const sections = ref({ ...defaultHomepage.sections })
const storyCtaLink = computed(() => sections.value.story?.ctaLink || '/me')
// Featured posts come only from the API: a packaged copy could show posts the admin has edited or unpublished
const blogPosts = ref([])

const cmsImg = (path) => resolveCmsImage(path)
const sectionEnabled = (key) => sections.value[key]?.enabled !== false

const heroDesktopUrl = computed(() =>
  resolveSlotImage({
    useDefault: sections.value.hero?.useDefaultDesktop,
    path: sections.value.hero?.imageDesktop,
    defaultPath: DEFAULT_IMAGE_PATHS.hero.imageDesktop,
  })
)
const heroMobileUrl = computed(() =>
  resolveSlotImage({
    useDefault: sections.value.hero?.useDefaultMobile,
    path: sections.value.hero?.imageMobile,
    defaultPath: DEFAULT_IMAGE_PATHS.hero.imageMobile,
  })
)
const storyImageUrl = computed(() =>
  resolveSlotImage({
    useDefault: sections.value.story?.useDefaultDesktop,
    path: sections.value.story?.imageDesktop,
    defaultPath: DEFAULT_IMAGE_PATHS.story.imageDesktop,
  })
)

const qualityItems = computed(() => {
  const items = sections.value.quality?.extraJson?.items || []
  return items.map((item, idx) => ({
    ...item,
    iconUrl: resolveSlotImage({
      useDefault: item.useDefaultIcon,
      path: item.icon,
      defaultPath: DEFAULT_IMAGE_PATHS.quality.icons[idx],
    }),
  }))
})

const mealBannerUrl = computed(() => {
  const extra = sections.value.meal_gallery?.extraJson || {}
  return resolveSlotImage({
    useDefault: extra.useDefaultBanner,
    path: extra.banner,
    defaultPath: DEFAULT_IMAGE_PATHS.meal_gallery.banner,
  })
})
const mealTitle = computed(() => sections.value.meal_gallery?.extraJson?.title || 'BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG')
const images = computed(() => {
  const tiles = sections.value.meal_gallery?.extraJson?.tiles || []
  return tiles
    .map((t, idx) => ({
      src: resolveSlotImage({
        useDefault: t.useDefault,
        path: t.src,
        defaultPath: DEFAULT_IMAGE_PATHS.meal_gallery.tiles[idx],
      }),
      alt: t.alt || '',
    }))
    .filter((t) => !!t.src)
})
const messengerUrl = computed(() => sections.value.floating_chat?.extraJson?.messengerUrl || 'https://m.me/yourpage')
const zaloUrl = computed(() => sections.value.floating_chat?.extraJson?.zaloUrl || 'https://zalo.me/yourzalo')
const postCover = (post) => cmsImg(post.coverImage || post.image)

const shouldLoop = computed(() => {
  const minSlidesForLoop = 2 * 2.5
  return images.value.length >= minSlidesForLoop
})

async function fetchHomepageContent() {
  try {
    const data = await contentService.getHomepage()
    if (data?.sections) {
      sections.value = { ...defaultHomepage.sections, ...data.sections }
    }
    if (Array.isArray(data?.featuredPosts)) {
      blogPosts.value = data.featuredPosts
    }
  } catch (e) {
    console.warn('CMS homepage fallback:', e.message)
    sections.value = { ...defaultHomepage.sections }
    blogPosts.value = []
  }
}

import { navigateToSubdomain, navigateToMainDomain } from '../utils/domainUtils'

const navigateToProduct = (id) => {
  const path = `/products/${id}`
  
  // Check if this is a special product (ID 1, 2, 3, or 4)
  if (id === 1 || id === 2 || id === 3 || id === 4) {
    const domainChanged = navigateToSubdomain(path, id)
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
  
  // Check if this is a special product (ID 1, 2, 3, or 4)
  if (product.id === 1 || product.id === 2 || product.id === 3 || product.id === 4) {
    const domainChanged = navigateToSubdomain(path, product.id)
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
      // mainImage → gallery[0] → fallback ID (xem getProductImage)
      image: getProductImage(product),
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
  fetchHomepageContent()
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
    return getImageUrlFromApi('/images/products/details/black/1.png')
  }
  
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