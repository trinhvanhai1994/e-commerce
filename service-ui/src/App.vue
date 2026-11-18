<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from './stores/cart'
import { navigateToMainDomain, navigateToSubdomain, shouldStayOnSubdomain, shouldChangeDomain } from './utils/domainUtils'
import { getImageUrlFromApi } from './utils/imageUtils.js'
import { contentService } from './services/content.service.js'
import { resolveSlotImage, DEFAULT_IMAGE_PATHS } from './content/homeDefaults.js'
import SiteFooter from './components/SiteFooter.vue'
import AuthModal from './components/auth/AuthModal.vue'
import { useAuthStore } from './stores/auth.js'

const route = useRoute()
const router = useRouter()
const siteLogoUseDefault = ref(true)
const siteLogoPath = ref(null)
// null → SiteFooter uses its packaged defaults (same text as before CMS)
const footerContent = ref(null)
const footerEnabled = ref(true)
const siteLogoUrl = computed(() =>
  resolveSlotImage({
    useDefault: siteLogoUseDefault.value,
    path: siteLogoPath.value,
    defaultPath: DEFAULT_IMAGE_PATHS.header_logo.imageDesktop,
  })
)
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const openSubMenu = ref(false)
const showScrollTop = ref(false)
const showSearch = ref(false)
const searchQuery = ref("")

const cartStore = useCartStore()
const showCartPopup = ref(false)
const coupon = ref("")

const navigation = ref([
  { name: 'Trang Chủ', href: '/', current: false },
  
  { name: 'Câu chuyện Thi Yên', href: '/me', current: false },
  { name: 'Sản phẩm của chúng tôi', href: '/products', current: false },
  { name: 'Blog chăm sóc cá nhân', href: '/blog', current: false },
  { name: 'Liên hệ', href: '/contact', current: false },
])

const headerMenu = [
  { name: 'Trang Chủ', href: '/' },
  { name: 'Câu chuyện Thi Yên', href: '/me' },
  { name: 'Triết lý dưỡng sinh', href: '/healthcare' },
  { name: 'Sản Phẩm', href: '/products' },
  { name: 'Blogs', href: '/blog' },
]

// Mục menu active cả ở trang con (/products/:id, /blog/:slug)
const isMenuActive = (href) =>
  href === '/' ? route.path === '/' : route.path === href || route.path.startsWith(`${href}/`)

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
  
  // Check if this is a product page with special domain (ID 1, 2, 3, or 4)
  const productMatch = pathWithoutQuery.match(/^\/products\/([1234])/)
  
  if (productMatch) {
    // This is a special product, navigate to subdomain
    const productId = parseInt(productMatch[1], 10)
    const domainChanged = navigateToSubdomain(path, productId)
    if (!domainChanged) {
      router.push(path).catch(() => {}) // Ignore navigation errors
    }
    return
  }
  
  // For all other navigation, check if we need to switch domain
  const currentHost = window.location.hostname
  const subdomainPrefixes = ['botnguhacmeden', 'botngusachongdau']
  
  // If currently on any product subdomain and navigating to non-product page, switch to main domain
  for (const prefix of subdomainPrefixes) {
    if (currentHost.startsWith(`${prefix}.`)) {
      const mainDomain = currentHost.substring(prefix.length + 1)
      const newUrl = `${window.location.protocol}//${mainDomain}${path}`
      window.location.href = newUrl
      return
    }
  }
  
  // Otherwise, use router navigation directly
  // Use replace: false to allow browser back button
  // So sánh cả query để /products?search=... vẫn điều hướng khi đang ở /products
  if (route.fullPath !== path) {
    router.push(path).catch((err) => {
      // Ignore navigation errors (e.g., navigating to same route)
      if (err.name !== 'NavigationDuplicated') {
        console.error('Navigation error:', err)
      }
    })
  }
}

async function loadSiteLogo() {
  try {
    const data = await contentService.getHomepage()
    const footerSection = data?.sections?.footer
    if (footerSection) {
      footerEnabled.value = footerSection.enabled !== false
      footerContent.value = footerSection.extraJson || null
    }
    const logoSection = data?.sections?.header_logo
    if (!logoSection) return
    siteLogoUseDefault.value = logoSection.useDefaultDesktop !== false
    siteLogoPath.value = logoSection.imageDesktop || null
  } catch (_) {
    siteLogoUseDefault.value = true
    siteLogoPath.value = null
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  window.addEventListener('click', closeAccountMenuOnOutsideClick)
  authStore.init()
  cartStore.loadFromStorage()
  loadSiteLogo()
  
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

// Khung tìm kiếm dùng chung PC + mobile: tìm theo tên sản phẩm ở trang /products?search=
function openSearch() {
  searchQuery.value = route.path === '/products' ? String(route.query.search || '') : ''
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

// Từ khóa sản phẩm chủ chốt hiện dưới ô tìm kiếm để chọn nhanh
const quickSearchKeywords = ['Mè đen', 'Hồng đậu']

function quickSearch(keyword) {
  searchQuery.value = keyword
  submitSearch()
}

function submitSearch() {
  const q = searchQuery.value.trim()
  if (!q) return
  closeSearch()
  handleNavigation(`/products?search=${encodeURIComponent(q)}`, null)
}

function handleSearchKey(e) {
  if (e.key === 'Escape') closeSearch()
}

function closeMobileMenu() {
  isMobileMenuOpen.value = false
}

function removeFromCart(item) {
  cartStore.removeItem(item.id)
}

// Customer account (task-003): header button opens the login popup, or a menu once logged in
const authStore = useAuthStore()
const showAccountMenu = ref(false)
const accountMenuRoot = ref(null)

function onAccountButton() {
  if (authStore.isLoggedIn) showAccountMenu.value = !showAccountMenu.value
  else authStore.openModal('login')
}

function goToAccount(tab, e) {
  showAccountMenu.value = false
  closeMobileMenu()
  handleNavigation(tab ? `/tai-khoan?tab=${tab}` : '/tai-khoan', e)
}

async function logoutCustomer() {
  showAccountMenu.value = false
  closeMobileMenu()
  await authStore.logout()
  if (route.path.startsWith('/tai-khoan')) router.push('/')
}

function closeAccountMenuOnOutsideClick(event) {
  if (showAccountMenu.value && accountMenuRoot.value && !accountMenuRoot.value.contains(event.target)) {
    showAccountMenu.value = false
  }
}

onUnmounted(() => {
  window.removeEventListener('click', closeAccountMenuOnOutsideClick)
})
</script>

<template>
  <div class="min-h-screen flex flex-col bg-yellow-50">
    <!-- Facebook Pixel noscript -->
    <noscript>
      <img height="1" width="1" style="display:none"
        src="https://www.facebook.com/tr?id=822351806811750&ev=PageView&noscript=1"
      />
    </noscript>
    <!-- Customer login/register popup (stores/auth.js opens it from any page) -->
    <AuthModal />
    <router-view v-slot="{ Component, route }">
      <transition name="fade" mode="out-in">
        <div v-if="route.path.startsWith('/admin')" :key="route.path">
          <component :is="Component" />
        </div>
        <div v-else :key="route.path">
          <!-- Header chung toàn site: logo trên, tên thương hiệu dưới, menu ngang bên phải, tìm kiếm/giỏ hàng bên phải -->
          <header class="w-full border-b border-gray-100 sticky top-0 z-40 py-1 md:py-2" style="background-color: #eef1c5;">
            <div class="max-w-7xl mx-auto px-4">
              <!-- Responsive header: PC logo | menu | search+cart phải; Mobile: logo trái, search/cart giữa, hamburger phải -->
              <div class="flex items-center min-h-[42px] py-0">
                <!-- Logo + Brand: luôn sát trái -->
                <a href="#" @click="handleLogoClick" class="flex items-center justify-center header-brand select-none cursor-pointer flex-shrink-0 no-underline" style="text-decoration: none;">
                  <img v-if="siteLogoUrl" :src="siteLogoUrl" alt="Logo" class="w-16 h-16 md:w-20 md:h-20 object-contain transition-transform hover:scale-105" />
                </a>
                <!-- Menu PC only: căn giữa, flex-1 để menu chiếm không gian giữa, ẩn trên mobile -->
                <nav class="header-menu hidden md:flex gap-6 items-center flex-1 justify-center">
                  <a
                    v-for="item in headerMenu"
                    :key="item.href"
                    :href="item.href"
                    @click="(e) => { handleNavigation(item.href, e); closeMobileMenu(); }"
                    :class="['text-black font-bold', { 'is-active': isMenuActive(item.href) }]"
                    :aria-current="isMenuActive(item.href) ? 'page' : undefined"
                  >{{ item.name }}</a>
                </nav>
                <!-- Cụm icon: search, cart, hamburger. PC: icon sát phải, mobile: icon sát phải, không bị ảnh hưởng bởi flex-1 -->
                <div class="flex items-center gap-2 md:gap-3 min-w-[100px] ml-auto md:ml-0 order-2 md:order-none">
                  <!-- Mobile: chỉ icon search, PC: input search -->
                  <button type="button" class="search-trigger" @click="openSearch" aria-label="Tìm kiếm sản phẩm" :aria-expanded="showSearch">
                    <svg class="w-5 h-5 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                      <circle cx="11" cy="11" r="8" stroke-width="2"/>
                      <path d="M21 21l-4.35-4.35" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <span class="hidden md:inline">Tìm kiếm</span>
                  </button>
                  <!-- Tài khoản khách: chưa đăng nhập thì mở popup, đã đăng nhập thì mở menu -->
                  <div ref="accountMenuRoot" class="relative flex-shrink-0 order-1">
                    <button
                      type="button"
                      class="flex items-center gap-1 p-1 text-gray-700 hover:text-green-600"
                      :aria-label="authStore.isLoggedIn ? 'Tài khoản của ' + authStore.displayName : 'Đăng nhập'"
                      :aria-expanded="authStore.isLoggedIn ? showAccountMenu : undefined"
                      @click="onAccountButton"
                    >
                      <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <span class="hidden lg:inline text-sm font-semibold max-w-[120px] truncate">
                        {{ authStore.isLoggedIn ? authStore.displayName : 'Đăng nhập' }}
                      </span>
                    </button>
                    <div
                      v-if="authStore.isLoggedIn && showAccountMenu"
                      class="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-100 py-2 z-50"
                      role="menu"
                    >
                      <div class="px-4 py-2 border-b border-gray-100">
                        <div class="text-sm font-semibold text-gray-800 truncate">{{ authStore.displayName }}</div>
                        <div class="text-xs text-gray-500">{{ authStore.customer?.phone }}</div>
                      </div>
                      <a href="#" role="menuitem" class="block px-4 py-2 text-sm hover:bg-green-50" @click="(e) => goToAccount('', e)">Tài khoản của tôi</a>
                      <a href="#" role="menuitem" class="block px-4 py-2 text-sm hover:bg-green-50" @click="(e) => goToAccount('orders', e)">Đơn hàng của tôi</a>
                      <a href="#" role="menuitem" class="block px-4 py-2 text-sm hover:bg-green-50" @click="(e) => goToAccount('password', e)">Đổi mật khẩu</a>
                      <button type="button" role="menuitem" class="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50" @click="logoutCustomer">Đăng xuất</button>
                    </div>
                  </div>
                  <a href="#" @click="(e) => handleNavigation('/cart', e)" class="relative flex-shrink-0 flex items-center justify-center order-2">
                    <svg class="w-7 h-7 text-gray-700 hover:text-green-600 transition" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2 9m13-9l2 9m-5-9V6a2 2 0 10-4 0v7" />
                    </svg>
                    <span v-if="getTotalItems() > 0" class="absolute -top-1 -right-1 bg-green-500 text-white text-xs rounded-full px-1">{{ getTotalItems() }}</span>
                  </a>
                  <button class="ml-1 md:hidden p-2 flex-shrink-0 flex items-center justify-center order-3" @click="isMobileMenuOpen = !isMobileMenuOpen" aria-label="Open menu">
                    <svg class="w-7 h-7 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                  </button>
                </div>
              </div>
              <!-- Khung tìm kiếm (PC + mobile): rơi xuống từ đầu trang, cùng tông với header -->
              <transition name="fade">
                <div v-if="showSearch" class="search-backdrop fixed inset-0 z-50 flex items-start justify-center px-4" @click.self="closeSearch">
                  <form class="search-panel w-full max-w-xl mt-4 md:mt-24" role="search" @submit.prevent="submitSearch">
                    <div class="search-field">
                      <svg class="w-5 h-5 search-field-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="11" cy="11" r="8" stroke-width="2"/>
                        <path d="M21 21l-4.35-4.35" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                      <label for="search-input" class="sr-only">Tìm kiếm sản phẩm</label>
                      <input
                        id="search-input"
                        v-model="searchQuery"
                        type="search"
                        autocomplete="off"
                        enterkeyhint="search"
                        placeholder="Tìm sản phẩm..."
                        class="flex-1 min-w-0 bg-transparent text-base"
                        @keydown="handleSearchKey"
                      />
                      <button v-if="searchQuery" type="button" class="search-clear" aria-label="Xoá nội dung" @click="searchQuery = ''">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" aria-hidden="true">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                      <button type="submit" class="search-submit" :disabled="!searchQuery.trim()">Tìm</button>
                    </div>
                    <div class="search-quick" aria-label="Từ khóa phổ biến">
                      <span class="search-quick-label">Tìm nhanh:</span>
                      <button
                        v-for="keyword in quickSearchKeywords"
                        :key="keyword"
                        type="button"
                        class="search-chip"
                        @click="quickSearch(keyword)"
                      >
                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                          <circle cx="11" cy="11" r="8" stroke-width="2"/>
                          <path d="M21 21l-4.35-4.35" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        {{ keyword }}
                      </button>
                    </div>
                    <div class="search-hint">
                      <span>Nhấn Enter để tìm</span>
                      <button type="button" class="search-close" @click="closeSearch">Đóng <kbd class="hidden md:inline">Esc</kbd></button>
                    </div>
                  </form>
                </div>
              </transition>
              <!-- Mobile menu overlay -->
              <transition name="fade">
                <div v-if="isMobileMenuOpen" class="mobile-drawer-backdrop fixed inset-0 z-50 md:hidden" @click.self="closeMobileMenu">
                  <aside class="mobile-drawer absolute top-0 left-0 h-full w-[82%] max-w-xs flex flex-col animate-slideIn" role="dialog" aria-modal="true" aria-label="Menu điều hướng">
                    <div class="mobile-drawer-head flex items-center justify-between px-4 py-2">
                      <img v-if="siteLogoUrl" :src="siteLogoUrl" alt="Logo" class="w-14 h-14 object-contain" />
                      <button class="mobile-drawer-close" @click="closeMobileMenu" aria-label="Đóng menu">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                    <nav class="flex-1 overflow-y-auto px-3 py-4 flex flex-col gap-1">
                      <a
                        v-for="item in headerMenu"
                        :key="item.href"
                        :href="item.href"
                        @click="(e) => { handleNavigation(item.href, e); closeMobileMenu(); }"
                        :class="['mobile-menu-link', { 'is-active': isMenuActive(item.href) }]"
                        :aria-current="isMenuActive(item.href) ? 'page' : undefined"
                      >
                        <span>{{ item.name }}</span>
                        <svg class="mobile-menu-chevron w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" aria-hidden="true">
                          <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                        </svg>
                      </a>
                    </nav>
                    <div class="mobile-drawer-foot px-4 py-4">
                      <!-- Tài khoản khách (task-003) -->
                      <div class="mb-3 flex flex-col gap-1 text-sm">
                        <template v-if="authStore.isLoggedIn">
                          <div class="text-gray-500">Xin chào, <span class="font-semibold text-gray-800">{{ authStore.displayName }}</span></div>
                          <a href="/tai-khoan" class="py-1 font-semibold" @click="(e) => goToAccount('', e)">Tài khoản của tôi</a>
                          <a href="/tai-khoan?tab=orders" class="py-1 font-semibold" @click="(e) => goToAccount('orders', e)">Đơn hàng của tôi</a>
                          <button type="button" class="py-1 font-semibold text-left text-red-600" @click="logoutCustomer">Đăng xuất</button>
                        </template>
                        <button v-else type="button" class="py-2 font-bold text-left text-green-700" @click="closeMobileMenu(); authStore.openModal('login')">Đăng nhập / Đăng ký</button>
                      </div>
                      <a href="/cart" @click="(e) => { handleNavigation('/cart', e); closeMobileMenu(); }" class="mobile-drawer-cart">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2 9m13-9l2 9m-5-9V6a2 2 0 10-4 0v7" />
                        </svg>
                        <span>Giỏ hàng</span>
                        <span v-if="getTotalItems() > 0" class="mobile-drawer-badge">{{ getTotalItems() }}</span>
                      </a>
                    </div>
                  </aside>
                </div>
              </transition>
            </div>
          </header>
          <!-- Nút scroll to top -->
          <button
            v-if="showScrollTop"
            @click="scrollToTop"
            class="fixed bottom-24 md:bottom-6 right-4 z-[9999] bg-green-500 hover:bg-green-600 text-white rounded-full shadow-lg p-3 transition-all duration-200 flex items-center justify-center"
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
              class="bg-white rounded-lg p-2 md:p-6 w-full max-w-sm md:max-w-5xl mx-0 md:mx-4 overflow-y-auto max-h-[90vh] flex flex-col md:grid md:grid-cols-3 gap-2 md:gap-8"
              @click.stop
            >
              <!-- Cart List -->
              <div class="md:col-span-2 flex-1 flex flex-col">
                <!-- Mobile: card list -->
                <div class="space-y-3 md:hidden flex-1 overflow-y-auto">
                  <div v-for="item in cartStore.items" :key="item.id" class="bg-gray-50 rounded-lg p-3 flex gap-3 items-center relative">
                    <img :src="item.image" :alt="item.name" class="w-14 h-14 object-cover rounded" />
                    <div class="flex-1">
                      <div class="font-semibold text-sm text-blue-900 mb-1">{{ item.name }}</div>
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-bold">{{ formatPrice(item.price) }}</span>
                        <span class="text-xs text-gray-400">x{{ item.quantity }}</span>
                      </div>
                      <div class="flex items-center border rounded-full w-max">
                        <button @click="decrement(item)" class="px-2 py-1 text-base text-gray-500 hover:text-green-500">-</button>
                        <span class="px-2 font-semibold">{{ item.quantity }}</span>
                        <button @click="increment(item)" class="px-2 py-1 text-base text-gray-500 hover:text-green-500">+</button>
                      </div>
                    </div>
                    <button @click="remove(item)" class="absolute top-2 right-2 text-gray-400 hover:text-green-500 text-lg">&times;</button>
                  </div>
                </div>
                <!-- PC: table list -->
                <div class="hidden md:block flex-1 overflow-y-auto">
                  <table class="w-full">
                    <thead class="bg-gray-50">
                      <tr>
                        <th class="text-left p-3 font-semibold text-gray-700">Sản phẩm</th>
                        <th class="text-center p-3 font-semibold text-gray-700">Giá</th>
                        <th class="text-center p-3 font-semibold text-gray-700">Số lượng</th>
                        <th class="text-center p-3 font-semibold text-gray-700">Tổng</th>
                        <th class="text-center p-3 font-semibold text-gray-700"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="item in cartStore.items" :key="item.id" class="border-b border-gray-100">
                        <td class="p-3">
                          <div class="flex items-center gap-3">
                            <img :src="item.image" :alt="item.name" class="w-12 h-12 object-cover rounded" />
                            <div>
                              <div class="font-semibold text-gray-900">{{ item.name }}</div>
                              <div class="text-sm text-gray-500">{{ item.category }}</div>
                            </div>
                          </div>
                        </td>
                        <td class="p-3 text-center font-semibold">{{ formatPrice(item.price) }}</td>
                        <td class="p-3 text-center">
                          <div class="flex items-center justify-center gap-2">
                            <button @click="decrement(item)" class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-500 hover:text-green-500 hover:border-green-300">-</button>
                            <span class="font-semibold w-8 text-center">{{ item.quantity }}</span>
                            <button @click="increment(item)" class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-500 hover:text-green-500 hover:border-green-300">+</button>
                          </div>
                        </td>
                        <td class="p-3 text-center font-bold text-green-600">{{ formatPrice(item.price * item.quantity) }}</td>
                        <td class="p-3 text-center">
                          <button @click="remove(item)" class="text-gray-400 hover:text-red-500">
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
                <div class="bg-gray-50 rounded-lg p-4 space-y-3">
                  <h3 class="font-bold text-lg text-gray-900">Tổng đơn hàng</h3>
                  <div class="space-y-2">
                    <div class="flex justify-between">
                      <span class="text-gray-600">Tạm tính:</span>
                      <span class="font-semibold">{{ formatPrice(cartsubTotal()) }}</span>
                    </div>
                    <div class="flex justify-between">
                      <span class="text-gray-600">Phí vận chuyển:</span>
                      <span class="font-semibold">{{ formatPrice(cartShippingFee()) }}</span>
                    </div>
                    <div class="border-t pt-2">
                      <div class="flex justify-between">
                        <span class="font-bold text-lg">Tổng cộng:</span>
                        <span class="font-bold text-lg text-green-600">{{ formatPrice(cartsubTotal() + cartShippingFee()) }}</span>
                      </div>
                    </div>
                  </div>
                  <router-link to="/checkout" class="w-full bg-green-500 hover:bg-green-600 text-white font-bold py-3 px-4 rounded-lg transition-all duration-200 text-center block">
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
          <!-- Footer (CMS section "footer", defaults in content/homeDefaults.js) -->
          <SiteFooter v-if="footerEnabled" :content="footerContent" :logo-url="siteLogoUrl" />
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
/*
 * Menu header: chữ đổi sang xanh đậm thương hiệu + vạch chỉ báo bo tròn dưới chữ.
 * Không dùng nền riêng cho item để giữ nguyên nền header (#eef1c5).
 * Hover: vạch mờ, ngắn mọc từ giữa. Active: vạch đầy đủ, đậm. Màu #2f6b2f đạt tương phản AA trên nền header.
 */
.header-menu {
  --menu-accent: #2f6b2f;
}
.header-menu a {
  position: relative;
  padding: 6px 2px;
  font-weight: 600;
  font-size: 1rem;
  color: #1f2a1f;
  transition: color 0.2s ease;
}
.header-menu a::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -2px;
  height: 3px;
  border-radius: 9999px;
  background-color: var(--menu-accent);
  opacity: 0;
  transform: scaleX(0);
  transition: transform 0.25s ease, opacity 0.25s ease;
}
.header-menu a:hover {
  color: var(--menu-accent);
}
.header-menu a:hover::after {
  opacity: 0.35;
  transform: scaleX(0.5);
}
.header-menu a.is-active {
  color: var(--menu-accent);
}
.header-menu a.is-active::after {
  opacity: 1;
  transform: scaleX(1);
}
.header-menu a:focus-visible {
  outline: 2px solid var(--menu-accent);
  outline-offset: 4px;
  border-radius: 4px;
}
@media (prefers-reduced-motion: reduce) {
  .header-menu a,
  .header-menu a::after {
    transition: none;
  }
}

/*
 * Drawer menu mobile: cùng bảng màu với header (#eef1c5) và nền trang (kem),
 * mục đang xem dùng xanh đậm #2f6b2f giống vạch active trên PC.
 */
.mobile-drawer-backdrop {
  background-color: rgba(31, 42, 31, 0.45);
}
.mobile-drawer {
  background-color: #fbfbef;
  box-shadow: 8px 0 24px rgba(31, 42, 31, 0.18);
}
.mobile-drawer-head {
  background-color: #eef1c5;
  border-bottom: 1px solid rgba(47, 107, 47, 0.15);
}
.mobile-drawer-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 9999px;
  color: #1f2a1f;
  transition: background-color 0.2s;
}
.mobile-drawer-close:hover,
.mobile-drawer-close:focus-visible {
  background-color: rgba(47, 107, 47, 0.1);
  outline: none;
}
.mobile-menu-link {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.8rem 1rem;
  border-radius: 12px;
  font-size: 1.05rem;
  font-weight: 600;
  color: #1f2a1f;
  transition: color 0.2s, background-color 0.2s;
}
.mobile-menu-chevron {
  color: rgba(31, 42, 31, 0.3);
  transition: color 0.2s, transform 0.2s;
}
.mobile-menu-link:hover,
.mobile-menu-link:focus-visible {
  background-color: rgba(47, 107, 47, 0.06);
  outline: none;
}
.mobile-menu-link.is-active {
  color: #2f6b2f;
  background-color: #eef1c5;
}
.mobile-menu-link.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 22%;
  bottom: 22%;
  width: 4px;
  border-radius: 9999px;
  background-color: #2f6b2f;
}
.mobile-menu-link.is-active .mobile-menu-chevron {
  color: #2f6b2f;
  transform: translateX(2px);
}
.mobile-drawer-foot {
  border-top: 1px solid rgba(47, 107, 47, 0.15);
}
.mobile-drawer-cart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem 1rem;
  border-radius: 9999px;
  font-weight: 600;
  color: #fff;
  background-color: #2f6b2f;
  transition: background-color 0.2s;
}
.mobile-drawer-cart:hover {
  background-color: #275a27;
}
.mobile-drawer-badge {
  min-width: 1.4rem;
  padding: 0 0.4rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  line-height: 1.4rem;
  text-align: center;
  color: #2f6b2f;
  background-color: #eef1c5;
}
.header-brand span {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.1;
  margin-top: 0;
}
/*
 * Tìm kiếm: nút mở trong header (PC: pill có chữ, mobile: icon tròn) và khung tìm kiếm dùng chung.
 * Bảng màu giống menu: nền header #eef1c5, nền kem #fbfbef, xanh đậm #2f6b2f.
 */
.search-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  width: 40px;
  height: 40px;
  border-radius: 9999px;
  color: #1f2a1f;
  transition: background-color 0.2s, color 0.2s, border-color 0.2s;
}
.search-trigger:hover,
.search-trigger:focus-visible {
  color: #2f6b2f;
  background-color: rgba(47, 107, 47, 0.08);
  outline: none;
}
@media (min-width: 768px) {
  .search-trigger {
    width: auto;
    height: 36px;
    padding: 0 0.9rem;
    font-size: 0.875rem;
    font-weight: 600;
    color: rgba(31, 42, 31, 0.7);
    background-color: rgba(255, 255, 255, 0.6);
    border: 1px solid rgba(47, 107, 47, 0.2);
  }
  .search-trigger:hover,
  .search-trigger:focus-visible {
    background-color: #fff;
    border-color: rgba(47, 107, 47, 0.45);
  }
}
.search-backdrop {
  background-color: rgba(31, 42, 31, 0.45);
}
.search-panel {
  padding: 0.75rem;
  border-radius: 20px;
  background-color: #fbfbef;
  box-shadow: 0 16px 40px rgba(31, 42, 31, 0.25);
  animation: searchDrop 0.22s cubic-bezier(.4, 0, .2, 1);
}
.search-field {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.35rem 0.35rem 0.9rem;
  border-radius: 9999px;
  background-color: #fff;
  border: 1.5px solid rgba(47, 107, 47, 0.25);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-field:focus-within {
  border-color: #2f6b2f;
  box-shadow: 0 0 0 3px rgba(47, 107, 47, 0.15);
}
.search-field-icon {
  flex-shrink: 0;
  color: #2f6b2f;
}
.search-field input::-webkit-search-cancel-button {
  -webkit-appearance: none;
}
.search-field input::placeholder {
  color: rgba(31, 42, 31, 0.45);
}
.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 9999px;
  color: rgba(31, 42, 31, 0.5);
  background-color: rgba(31, 42, 31, 0.06);
}
.search-clear:hover {
  color: #1f2a1f;
  background-color: rgba(31, 42, 31, 0.12);
}
.search-submit {
  flex-shrink: 0;
  padding: 0.5rem 1.1rem;
  border-radius: 9999px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
  background-color: #2f6b2f;
  transition: background-color 0.2s, opacity 0.2s;
}
.search-submit:hover:not(:disabled) {
  background-color: #275a27;
}
.search-submit:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.search-quick {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 0.25rem 0;
}
.search-quick-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: rgba(31, 42, 31, 0.6);
}
.search-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.85rem;
  border-radius: 9999px;
  font-size: 0.85rem;
  font-weight: 600;
  color: #2f6b2f;
  background-color: #eef1c5;
  border: 1px solid rgba(47, 107, 47, 0.2);
  transition: background-color 0.2s, color 0.2s, border-color 0.2s;
}
.search-chip:hover,
.search-chip:focus-visible {
  color: #fff;
  background-color: #2f6b2f;
  border-color: #2f6b2f;
  outline: none;
}
.search-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0.5rem 0;
  font-size: 0.8rem;
  color: rgba(31, 42, 31, 0.55);
}
.search-close {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-weight: 600;
  color: #2f6b2f;
}
.search-close kbd {
  padding: 0 0.35rem;
  border-radius: 4px;
  font-family: inherit;
  font-size: 0.7rem;
  border: 1px solid rgba(47, 107, 47, 0.3);
}
@keyframes searchDrop {
  from { transform: translateY(-12px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
@media (prefers-reduced-motion: reduce) {
  .search-panel {
    animation: none;
  }
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