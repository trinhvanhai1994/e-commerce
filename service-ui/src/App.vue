<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from './stores/cart'
import { useThemeStore } from './stores/theme'
import ThemeToggle from './components/ThemeToggle.vue'
import { navigateToMainDomain, navigateToSubdomain, shouldStayOnSubdomain, shouldChangeDomain } from './utils/domainUtils'

const route = useRoute()
const router = useRouter()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const openSubMenu = ref(false)
const showScrollTop = ref(false)
const showSearch = ref(false)
const searchQuery = ref("")
const showMobileSearch = ref(false)

const cartStore = useCartStore()
const themeStore = useThemeStore()
const showCartPopup = ref(false)
const coupon = ref("")

const navigation = ref([
  { name: 'Trang Chủ', href: '/', current: false },
  
  { name: 'Câu chuyện Thi Yên', href: '/me', current: false },
  { name: 'Sản phẩm của chúng tôi', href: '/products', current: false },
  { name: 'Blog chăm sóc cá nhân', href: '/blog', current: false },
  { name: 'Liên hệ', href: '/contact', current: false },
])

// Watch route changes to update current state
watch(
  () => route.path,
  (newPath) => {
    navigation.value = navigation.value.map(item => ({
      ...item,
      current: item.href === newPath
    }))
  },
  { immediate: true }
)

const storeName = computed(() => import.meta.env.VITE_APP_STORE_NAME || 'Huyen Store')
const storeDescription = computed(() => import.meta.env.VITE_APP_STORE_DESCRIPTION || 'Your trusted source for quality products')
const contactEmail = computed(() => import.meta.env.VITE_APP_CONTACT_EMAIL || 'contact@yourstore.com')
const contactPhone = computed(() => import.meta.env.VITE_APP_CONTACT_PHONE || '(123) 456-7890')

const handleScroll = () => {
  isScrolled.value = window.scrollY > 0
  showScrollTop.value = window.scrollY > 200
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const handleLogoClick = (e) => {
  e.preventDefault()
  const path = '/'
  const domainChanged = navigateToMainDomain(path)
  if (!domainChanged) {
    if (route.path !== path) {
      router.push(path)
    }
    scrollToTop()
  }
}

// Function to handle navigation with domain switching
const handleNavigation = (path, e) => {
  // Prevent default link behavior
  if (e) {
    e.preventDefault()
    e.stopPropagation()
  }
  
  // Extract path without query string for matching
  const pathWithoutQuery = path.split('?')[0]
  
  // Check if this is a product page with special domain (ID 1 or 2)
  const productMatch = pathWithoutQuery.match(/^\/products\/([12])/)
  
  if (productMatch) {
    // This is a special product, navigate to subdomain
    const domainChanged = navigateToSubdomain(path)
    if (!domainChanged) {
      router.push(path).catch(() => {}) // Ignore navigation errors
    }
    return
  }
  
  // For all other navigation, check if we need to switch domain
  const currentHost = window.location.hostname
  const subdomainPrefix = 'botnguhacmeden'
  
  // If currently on subdomain and navigating to non-product page, switch to main domain
  if (currentHost.startsWith(`${subdomainPrefix}.`)) {
    const mainDomain = currentHost.substring(subdomainPrefix.length + 1)
    const newUrl = `${window.location.protocol}//${mainDomain}${path}`
    window.location.href = newUrl
    return
  }
  
  // Otherwise, use router navigation directly
  // Use replace: false to allow browser back button
  if (route.path !== pathWithoutQuery) {
    router.push(path).catch((err) => {
      // Ignore navigation errors (e.g., navigating to same route)
      if (err.name !== 'NavigationDuplicated') {
        console.error('Navigation error:', err)
      }
    })
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  cartStore.loadFromStorage()
  themeStore.applyTheme() // Ensure theme is applied on mount
  
  // Facebook Pixel Code
  !function(f,b,e,v,n,t,s)
  {if(f.fbq)return;n=f.fbq=function(){n.callMethod?
  n.callMethod.apply(n,arguments):n.queue.push(arguments)};
  if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';
  n.queue=[];t=b.createElement(e);t.async=!0;
  t.src=v;s=b.getElementsByTagName(e)[0];
  s.parentNode.insertBefore(t,s)}(window, document,'script',
  'https://connect.facebook.net/en_US/fbevents.js');
  fbq('init', '822351806811750');
  fbq('track', 'PageView');
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// Lưu cart vào localStorage mỗi khi thay đổi
watch(cartStore.items, (newCart) => {
  localStorage.setItem('cartItems', JSON.stringify(newCart))
}, { deep: true })

const showCart = () => {
  showCartPopup.value = true
}
const closeCart = () => {
  showCartPopup.value = false
}
const getTotalItems = () => {
  return cartStore.items.reduce((total, item) => total + item.quantity, 0)
}
const cartsubTotal = () => {
  return cartStore.items.reduce((total, item) => total + item.price * item.quantity, 0)
}

const cartShippingFee = () => {
  // Thêm phí ship 20.000đ cho sản phẩm id = 1 hoặc 3 nếu đơn hàng dưới hoặc bằng 299.000đ
  const hasShippingProducts = cartStore.items.some(item => item.id === 1 || item.id === 3)
  if (hasShippingProducts && cartsubTotal() <= 299000) {
    return 20000
  }
  return 0
}

const cartTotal = () => {
  return cartsubTotal() + cartShippingFee()
}
const cartOriginalTotal = () => {
  return cartStore.items.reduce((total, item) => total + (item.originalPrice || 0) * item.quantity, 0)
}

const navigationWithoutProduct = computed(() => navigation.value.filter(item => item.name !== 'Sản phẩm của chúng tôi'))

function formatPrice(val) {
  return val.toLocaleString('vi-VN') + '₫'
}

function increment(item) {
  cartStore.updateQuantity(item.id, item.quantity + 1)
}

function decrement(item) {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.id, item.quantity - 1)
  }
}

function remove(item) {
  cartStore.removeItem(item.id)
}

function updateCart() {
  cartStore.saveToStorage()
}

function proceedToCheckout() {
  showCartPopup.value = false
  handleNavigation('/checkout', null)
}

function openSearch() {
  showSearch.value = true
  setTimeout(() => {
    const input = document.getElementById('search-input')
    if (input) input.focus()
  }, 50)
}

function closeSearch() {
  showSearch.value = false
  searchQuery.value = ""
}

function handleSearchKey(e) {
  if (e.key === 'Enter' && searchQuery.value.trim()) {
    handleNavigation(`/products?search=${encodeURIComponent(searchQuery.value.trim())}`, null)
    closeSearch()
  } else if (e.key === 'Escape') {
    closeSearch()
  }
}

function closeMobileMenu() {
  isMobileMenuOpen.value = false
}

function openMobileSearch() {
  showMobileSearch.value = true
  setTimeout(() => {
    const input = document.getElementById('mobile-search-input')
    if (input) input.focus()
  }, 50)
}

function closeMobileSearch() {
  showMobileSearch.value = false
}

function removeFromCart(item) {
  cartStore.removeItem(item.id)
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-yellow-50 dark:bg-gray-900 transition-colors duration-300">
    <!-- Facebook Pixel noscript -->
    <noscript>
      <img height="1" width="1" style="display:none"
        src="https://www.facebook.com/tr?id=822351806811750&ev=PageView&noscript=1"
      />
    </noscript>
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in">
        <div v-if="route.path.startsWith('/admin')" :key="route.path">
          <component :is="Component" />
        </div>
        <div v-else :key="route.path">
          <!-- Header chung toàn site: logo trên, tên thương hiệu dưới, menu ngang bên phải, tìm kiếm/giỏ hàng bên phải -->
          <header class="w-full border-b border-gray-100 dark:border-gray-700 sticky top-0 z-40 py-1 md:py-2 transition-colors duration-300" style="background-color: #eef1c5;" :style="{ backgroundColor: themeStore.isDark ? '#1f2937' : '#eef1c5' }">
            <div class="max-w-7xl mx-auto px-4">
              <!-- Responsive header: PC logo | menu | search+cart phải; Mobile: logo trái, search/cart giữa, hamburger phải -->
              <div class="flex items-center min-h-[42px] py-0">
                <!-- Logo + Brand: luôn sát trái -->
                <a href="#" @click="handleLogoClick" class="flex items-center justify-center header-brand select-none cursor-pointer flex-shrink-0 no-underline" style="text-decoration: none;">
                  <img src="/images/logo/logo.png" alt="Logo" class="w-16 h-16 md:w-20 md:h-20 object-contain transition-transform hover:scale-105" />
                </a>
                <!-- Menu PC only: căn giữa, flex-1 để menu chiếm không gian giữa, ẩn trên mobile -->
                <nav class="header-menu hidden md:flex gap-6 items-center flex-1 justify-center">
                  <a href="#" @click="(e) => { handleNavigation('/', e); closeMobileMenu(); }" class="text-black dark:text-gray-100 font-bold transition-colors duration-300">Trang Chủ</a>
                  <a href="#" @click="(e) => { handleNavigation('/me', e); closeMobileMenu(); }" class="text-black dark:text-gray-100 font-bold transition-colors duration-300">Câu chuyện Thi Yên</a>
                  <a href="#" @click="(e) => { handleNavigation('/healthcare', e); closeMobileMenu(); }" class="text-black dark:text-gray-100 font-bold transition-colors duration-300">Triết lý dưỡng sinh</a>
                  <a href="#" @click="(e) => { handleNavigation('/products', e); closeMobileMenu(); }" class="text-black dark:text-gray-100 font-bold transition-colors duration-300">Sản Phẩm</a>
                  <a href="#" @click="(e) => { handleNavigation('/blog', e); closeMobileMenu(); }" class="text-black dark:text-gray-100 font-bold transition-colors duration-300">Blogs</a>
                </nav>
                <!-- Cụm icon: search, cart, hamburger. PC: icon sát phải, mobile: icon sát phải, không bị ảnh hưởng bởi flex-1 -->
                <div class="flex items-center gap-2 md:gap-3 min-w-[100px] ml-auto md:ml-0 order-2 md:order-none">
                  <!-- Theme Toggle -->
                  <ThemeToggle />
                  <!-- Mobile: chỉ icon search, PC: input search -->
                  <button class="md:hidden p-2 flex items-center justify-center" @click="openMobileSearch" aria-label="Tìm kiếm">
                    <svg class="w-6 h-6 text-gray-700 dark:text-gray-300 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <circle cx="11" cy="11" r="8" stroke-width="2"/>
                      <path d="M21 21l-4.35-4.35" stroke-width="2"/>
                    </svg>
                  </button>
                  <div class="hidden md:block relative w-[150px]">
                    <input type="text" placeholder="Tìm kiếm" class="border border-gray-300 dark:border-gray-600 rounded-full px-3 py-1 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-200 dark:focus:ring-green-600 w-full transition-colors duration-300" />
                    <svg class="absolute right-2 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <circle cx="11" cy="11" r="8" stroke-width="2"/>
                      <path d="M21 21l-4.35-4.35" stroke-width="2"/>
                    </svg>
                  </div>
                  <a href="#" @click="(e) => handleNavigation('/cart', e)" class="relative flex-shrink-0 flex items-center justify-center order-2">
                    <svg class="w-7 h-7 text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2 9m13-9l2 9m-5-9V6a2 2 0 10-4 0v7" />
                    </svg>
                    <span v-if="getTotalItems() > 0" class="absolute -top-1 -right-1 bg-green-500 text-white text-xs rounded-full px-1">{{ getTotalItems() }}</span>
                  </a>
                  <button class="ml-1 md:hidden p-2 flex-shrink-0 flex items-center justify-center order-3" @click="isMobileMenuOpen = !isMobileMenuOpen" aria-label="Open menu">
                    <svg class="w-7 h-7 text-gray-700 dark:text-gray-300 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                  </button>
                </div>
              </div>
              <!-- Mobile search overlay -->
              <transition name="fade">
                <div v-if="showMobileSearch" class="fixed inset-0 z-50 bg-black bg-opacity-40 flex items-start justify-center md:hidden" @click.self="closeMobileSearch">
                  <div class="bg-white dark:bg-gray-800 rounded-full mt-6 px-4 py-2 flex items-center gap-2 w-[90vw] max-w-xs shadow-lg transition-colors duration-300">
                    <input id="mobile-search-input" type="text" placeholder="Tìm kiếm..." class="flex-1 border-none outline-none text-base bg-transparent text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400" />
                    <button @click="closeMobileSearch" class="text-gray-400 dark:text-gray-500 hover:text-green-500 dark:hover:text-green-400 text-lg transition-colors duration-300">&times;</button>
                  </div>
                </div>
              </transition>
              <!-- Mobile menu overlay -->
              <transition name="slide-down">
                <div v-if="isMobileMenuOpen" class="fixed inset-0 z-50 bg-black bg-opacity-30 md:hidden" @click.self="closeMobileMenu">
                  <div class="absolute top-0 left-0 w-3/4 max-w-xs h-full bg-white dark:bg-gray-800 shadow-lg p-6 flex flex-col gap-4 animate-slideIn transition-colors duration-300">
                    <a href="#" @click="(e) => { handleNavigation('/', e); closeMobileMenu(); }" class="py-2 font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Trang Chủ</a>
                    <a href="#" @click="(e) => { handleNavigation('/me', e); closeMobileMenu(); }" class="py-2 font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Câu chuyện Thi Yên</a>
                    <a href="#" @click="(e) => { handleNavigation('/healthcare', e); closeMobileMenu(); }" class="py-2 font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Triết lý dưỡng sinh</a>
                    <a href="#" @click="(e) => { handleNavigation('/products', e); closeMobileMenu(); }" class="py-2 font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Sản Phẩm</a>
                    <a href="#" @click="(e) => { handleNavigation('/blog', e); closeMobileMenu(); }" class="py-2 font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Blogs</a>
                  </div>
                </div>
              </transition>
            </div>
          </header>
          <!-- Nút scroll to top -->
          <button
            v-if="showScrollTop"
            @click="scrollToTop"
            class="fixed bottom-24 md:bottom-6 right-4 z-[9999] bg-green-500 hover:bg-green-600 dark:bg-green-600 dark:hover:bg-green-700 text-white rounded-full shadow-lg p-3 transition-all duration-200 flex items-center justify-center"
            aria-label="Scroll to top"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
            </svg>
          </button>
          <!-- Cart Popup -->
          <div
            v-if="showCartPopup"
            class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]"
            @click="closeCart"
          >
            <div
              class="bg-white dark:bg-gray-800 rounded-lg p-2 md:p-6 w-full max-w-sm md:max-w-5xl mx-0 md:mx-4 overflow-y-auto max-h-[90vh] flex flex-col md:grid md:grid-cols-3 gap-2 md:gap-8 transition-colors duration-300"
              @click.stop
            >
              <!-- Cart List -->
              <div class="md:col-span-2 flex-1 flex flex-col">
                <!-- Mobile: card list -->
                <div class="space-y-3 md:hidden flex-1 overflow-y-auto">
                  <div v-for="item in cartStore.items" :key="item.id" class="bg-gray-50 dark:bg-gray-700 rounded-lg p-3 flex gap-3 items-center relative transition-colors duration-300">
                    <img :src="item.image" :alt="item.name" class="w-14 h-14 object-cover rounded" />
                    <div class="flex-1">
                      <div class="font-semibold text-sm text-blue-900 dark:text-blue-200 mb-1 transition-colors duration-300">{{ item.name }}</div>
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-bold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ formatPrice(item.price) }}</span>
                        <span class="text-xs text-gray-400 dark:text-gray-500 transition-colors duration-300">x{{ item.quantity }}</span>
                      </div>
                      <div class="flex items-center border border-gray-300 dark:border-gray-600 rounded-full w-max transition-colors duration-300">
                        <button @click="decrement(item)" class="px-2 py-1 text-base text-gray-500 dark:text-gray-400 hover:text-green-500 dark:hover:text-green-400 transition-colors duration-300">-</button>
                        <span class="px-2 font-semibold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ item.quantity }}</span>
                        <button @click="increment(item)" class="px-2 py-1 text-base text-gray-500 dark:text-gray-400 hover:text-green-500 dark:hover:text-green-400 transition-colors duration-300">+</button>
                      </div>
                    </div>
                    <button @click="remove(item)" class="absolute top-2 right-2 text-gray-400 dark:text-gray-500 hover:text-green-500 dark:hover:text-green-400 text-lg transition-colors duration-300">&times;</button>
                  </div>
                </div>
                <!-- PC: table list -->
                <div class="hidden md:block flex-1 overflow-y-auto">
                  <table class="w-full">
                    <thead class="bg-gray-50 dark:bg-gray-700 transition-colors duration-300">
                      <tr>
                        <th class="text-left p-3 font-semibold text-gray-700 dark:text-gray-200 transition-colors duration-300">Sản phẩm</th>
                        <th class="text-center p-3 font-semibold text-gray-700 dark:text-gray-200 transition-colors duration-300">Giá</th>
                        <th class="text-center p-3 font-semibold text-gray-700 dark:text-gray-200 transition-colors duration-300">Số lượng</th>
                        <th class="text-center p-3 font-semibold text-gray-700 dark:text-gray-200 transition-colors duration-300">Tổng</th>
                        <th class="text-center p-3 font-semibold text-gray-700 dark:text-gray-200 transition-colors duration-300"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="item in cartStore.items" :key="item.id" class="border-b border-gray-100 dark:border-gray-700 transition-colors duration-300">
                        <td class="p-3">
                          <div class="flex items-center gap-3">
                            <img :src="item.image" :alt="item.name" class="w-12 h-12 object-cover rounded" />
                            <div>
                              <div class="font-semibold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ item.name }}</div>
                              <div class="text-sm text-gray-500 dark:text-gray-400 transition-colors duration-300">{{ item.category }}</div>
                            </div>
                          </div>
                        </td>
                        <td class="p-3 text-center font-semibold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ formatPrice(item.price) }}</td>
                        <td class="p-3 text-center">
                          <div class="flex items-center justify-center gap-2">
                            <button @click="decrement(item)" class="w-8 h-8 rounded-full border border-gray-300 dark:border-gray-600 flex items-center justify-center text-gray-500 dark:text-gray-400 hover:text-green-500 dark:hover:text-green-400 hover:border-green-300 dark:hover:border-green-500 transition-colors duration-300">-</button>
                            <span class="font-semibold w-8 text-center text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ item.quantity }}</span>
                            <button @click="increment(item)" class="w-8 h-8 rounded-full border border-gray-300 dark:border-gray-600 flex items-center justify-center text-gray-500 dark:text-gray-400 hover:text-green-500 dark:hover:text-green-400 hover:border-green-300 dark:hover:border-green-500 transition-colors duration-300">+</button>
                          </div>
                        </td>
                        <td class="p-3 text-center font-bold text-green-600 dark:text-green-400 transition-colors duration-300">{{ formatPrice(item.price * item.quantity) }}</td>
                        <td class="p-3 text-center">
                          <button @click="remove(item)" class="text-gray-400 dark:text-gray-500 hover:text-red-500 dark:hover:text-red-400 transition-colors duration-300">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
              <!-- Cart Summary -->
              <div class="md:col-span-1 flex flex-col">
                <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4 space-y-3 transition-colors duration-300">
                  <h3 class="font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Tổng đơn hàng</h3>
                  <div class="space-y-2">
                    <div class="flex justify-between">
                      <span class="text-gray-600 dark:text-gray-300 transition-colors duration-300">Tạm tính:</span>
                      <span class="font-semibold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ formatPrice(cartsubTotal()) }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-gray-600 dark:text-gray-300 transition-colors duration-300">Phí vận chuyển:</span>
                      <span class="font-semibold text-gray-900 dark:text-gray-100 transition-colors duration-300">{{ formatPrice(cartShippingFee()) }}</span>
                    </div>
                    <div class="border-t border-gray-300 dark:border-gray-600 pt-2 transition-colors duration-300">
                      <div class="flex justify-between">
                        <span class="font-bold text-lg text-gray-900 dark:text-gray-100 transition-colors duration-300">Tổng cộng:</span>
                        <span class="font-bold text-lg text-green-600 dark:text-green-400 transition-colors duration-300">{{ formatPrice(cartsubTotal() + cartShippingFee()) }}</span>
                      </div>
                    </div>
                  </div>
                  <router-link to="/checkout" class="w-full bg-green-500 hover:bg-green-600 dark:bg-green-600 dark:hover:bg-green-700 text-white font-bold py-3 px-4 rounded-lg transition-all duration-200 text-center block">
                    Thanh toán
                  </router-link>
                </div>
              </div>
            </div>
          </div>
          <!-- Main Content with padding for fixed header -->
          <main class="flex-1">
            <div class="mx-auto max-w-7xl sm:px-6 lg:px-8">
              <component :is="Component" />
            </div>
          </main>
          <!-- Footer -->
          <footer class="bg-gradient-to-br from-yellow-50 via-white to-yellow-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 font-sans border-t border-yellow-200 dark:border-gray-700 mt-4 text-black dark:text-gray-100 relative overflow-hidden transition-colors duration-300">
            <!-- Background decoration -->
            <div class="absolute inset-0 bg-gradient-to-r from-transparent via-yellow-100/20 to-transparent dark:via-gray-800/20 transition-colors duration-300"></div>
            
            <div class="max-w-7xl mx-auto px-4 py-8 relative z-10">
              <!-- Main footer content -->
              <div class="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
                <!-- Logo + Info -->
                <div class="text-center bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50 dark:border-gray-700/50 transition-colors duration-300">
                  <img src="/images/logo/logo.png" alt="Logo" class="h-20 w-auto mb-4 object-contain mx-auto drop-shadow-md" />
                  <div class="space-y-3 text-gray-700 dark:text-gray-300 transition-colors duration-300">
                    <div class="flex items-center justify-center gap-2">
                      <svg class="w-5 h-5 text-green-600 dark:text-green-400 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
                      </svg>
                      <span class="text-sm">Số 4.18 Khai Sơn Town, KĐT Khai Sơn City,<br />Phường Bồ Đề, Thành phố Hà Nội, Việt Nam</span>
                    </div>
                    <div class="flex items-center justify-center gap-2">
                      <svg class="w-5 h-5 text-green-600 dark:text-green-400 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                      </svg>
                      <span class="font-bold text-green-700 dark:text-green-400 transition-colors duration-300">0396860584</span>
                    </div>
                    <div class="flex items-center justify-center gap-2">
                      <svg class="w-5 h-5 text-green-600 dark:text-green-400 transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                      </svg>
                      <span class="text-sm">thiyen.vietnam@gmail.com</span>
                    </div>
                  </div>
                </div>
                
                <!-- Quy định & chính sách -->
                <div class="text-center bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50 dark:border-gray-700/50 transition-colors duration-300">
                  <div class="text-xl font-bold mb-4 text-green-700 dark:text-green-400 flex items-center justify-center gap-2 transition-colors duration-300">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                    QUY ĐỊNH VÀ CHÍNH SÁCH
                  </div>
                  <ul class="space-y-3">
                    <li>
                      <router-link to="/terms" class="text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-all duration-300 hover:bg-green-50 dark:hover:bg-gray-700 px-3 py-2 rounded-lg block">
                        Điều khoản sử dụng
                      </router-link>
                    </li>
                    <li>
                      <router-link to="/privacy" class="text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-all duration-300 hover:bg-green-50 dark:hover:bg-gray-700 px-3 py-2 rounded-lg block">
                        Chính sách bảo mật
                      </router-link>
                    </li>
                    <li>
                      <router-link to="/returns" class="text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-all duration-300 hover:bg-green-50 dark:hover:bg-gray-700 px-3 py-2 rounded-lg block">
                        Chính sách đổi trả
                      </router-link>
                    </li>
                    <li>
                      <router-link to="/payment" class="text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-all duration-300 hover:bg-green-50 dark:hover:bg-gray-700 px-3 py-2 rounded-lg block">
                        Chính sách thanh toán
                      </router-link>
                    </li>
                  </ul>
                </div>
                
                <!-- Thông tin liên hệ -->
                <div class="text-center bg-white/60 dark:bg-gray-800/60 backdrop-blur-sm rounded-2xl p-6 shadow-lg border border-yellow-200/50 dark:border-gray-700/50 transition-colors duration-300">
                  <div class="text-xl font-bold mb-4 text-green-700 dark:text-green-400 flex items-center justify-center gap-2 transition-colors duration-300">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                    </svg>
                    THÔNG TIN LIÊN HỆ
                  </div>
                  <div class="space-y-3">
                    <div class="bg-green-100 dark:bg-green-900/30 rounded-lg p-3 transition-colors duration-300">
                      <div class="font-bold text-green-800 dark:text-green-300 text-lg transition-colors duration-300">HOTLINE: 0396860584</div>
                      <div class="text-sm text-green-700 dark:text-green-400 transition-colors duration-300">(Thứ 2 - Thứ 7 (8h - 17h))</div>
                    </div>
                    <div>
                      <router-link to="/faq" class="text-gray-700 dark:text-gray-300 hover:text-green-600 dark:hover:text-green-400 transition-all duration-300 hover:bg-green-50 dark:hover:bg-gray-700 px-3 py-2 rounded-lg block">
                        Các câu hỏi thường gặp
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Social Media Section -->
              <div class="text-center mb-8">
                <div class="flex items-center justify-center gap-2 mb-4">
                  <div class="w-6 h-6 bg-green-600 dark:bg-green-500 rounded-full flex items-center justify-center transition-colors duration-300">
                    <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                    </svg>
                  </div>
                  <span class="text-green-600 dark:text-green-400 font-medium transition-colors duration-300">Kết nối với Thi Yên tại</span>
                </div>
                
                <div class="flex flex-wrap justify-center gap-3">
                  <!-- Facebook -->
                  <a href="#" class="bg-white dark:bg-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200 dark:border-gray-700">
                    <svg class="w-5 h-5 text-blue-600 dark:text-blue-400 transition-colors duration-300" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                    </svg>
                    <span class="font-medium text-gray-700 dark:text-gray-300 transition-colors duration-300">FACEBOOK</span>
                  </a>
                  
                  <!-- TikTok -->
                  <a href="#" class="bg-white dark:bg-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200 dark:border-gray-700">
                    <svg class="w-5 h-5 text-black dark:text-gray-300 transition-colors duration-300" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07z"/>
                    </svg>
                    <span class="font-medium text-gray-700 dark:text-gray-300 transition-colors duration-300">TIKTOK</span>
                  </a>
                  
                  <!-- YouTube -->
                  <a href="#" class="bg-white dark:bg-gray-800 rounded-lg px-4 py-2 flex items-center gap-2 shadow-md hover:shadow-lg transition-all duration-300 border border-gray-200 dark:border-gray-700">
                    <svg class="w-5 h-5 text-red-600 dark:text-red-400 transition-colors duration-300" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
                    </svg>
                    <span class="font-medium text-gray-700 dark:text-gray-300 transition-colors duration-300">YOUTUBE</span>
                  </a>
                </div>
              </div>
              
              <!-- Bottom footer -->
              <div class="border-t border-yellow-200 dark:border-gray-700 pt-8 transition-colors duration-300">
                <div class="text-center">
                  <p class="text-gray-600 dark:text-gray-400 text-sm transition-colors duration-300">&copy; 2024 Thi Yên. Tất cả quyền được bảo lưu.</p>
                </div>
              </div>
            </div>
          </footer>
        </div>
      </transition>
    </router-view>
  </div>
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Add smooth scroll behavior */
html {
  scroll-behavior: smooth;
}

/* Navigation hover effects */
.router-link-active {
  position: relative;
}

.router-link-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #0284c7;
  transform: scaleX(1);
  transition: transform 0.3s ease;
}

.slide-left-enter-active, .slide-left-leave-active { transition: transform 0.3s cubic-bezier(.4,0,.2,1), opacity 0.2s; }
.slide-left-enter-from { transform: translateX(-100%); opacity: 0; }
.slide-left-enter-to { transform: translateX(0); opacity: 1; }
.slide-left-leave-from { transform: translateX(0); opacity: 1; }
.slide-left-leave-to { transform: translateX(-100%); opacity: 0; }
.slide-down-enter-active, .slide-down-leave-active { transition: transform 0.3s cubic-bezier(.4,0,.2,1), opacity 0.2s; }
.slide-down-enter-from { transform: translateY(-100%); opacity: 0; }
.slide-down-enter-to { transform: translateY(0); opacity: 1; }
.slide-down-leave-from { transform: translateY(0); opacity: 1; }
.slide-down-leave-to { transform: translateY(-100%); opacity: 0; }

/* Header chỉnh đẹp, cân đối */
.header-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 160px;
  text-decoration: none;
}
.header-menu {
  display: flex;
  gap: 2.5rem;
  align-items: center;
}
@media (max-width: 900px) {
  .header-menu {
    gap: 1.2rem;
  }
  .header-brand span {
    font-size: 1.2rem;
  }
}
@media (max-width: 600px) {
  .header-menu {
    gap: 0.5rem;
  }
  .header-brand {
    min-width: 100px;
  }
}
.header-menu a {
  padding: 4px 0;
  font-weight: 600;
  font-size: 1rem;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s, background 0.2s;
}
.header-menu a:hover {
  color: #0dd906;
  background: #fffbe6;
  border-bottom: 2px solid #1ab63c;
  border-radius: 2px;
}
.header-brand span {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.1;
  margin-top: 0;
}
.header-search {
  min-width: 120px;
}

@keyframes slideIn {
  from { transform: translateX(-100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
.animate-slideIn {
  animation: slideIn 0.25s cubic-bezier(.4,0,.2,1);
}

@media (max-width: 767px) {
  .header-menu {
    display: none !important;
  }
  .header-brand {
    min-width: 0;
  }
}

/* Remove all underlines from logo link */
.header-brand,
.header-brand:hover,
.header-brand:focus,
.header-brand:active,
.header-brand:visited {
  text-decoration: none !important;
  border-bottom: none !important;
  box-shadow: none !important;
}

.header-brand::after,
.header-brand:hover::after,
.header-brand:focus::after,
.header-brand:active::after {
  display: none !important;
}
</style>

<style>
input#search-input,
input#search-input:focus {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}
</style>