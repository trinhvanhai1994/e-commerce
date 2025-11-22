import { createRouter, createWebHistory } from 'vue-router'
// Import admin service for authentication check (must be before router creation)
import adminService from '../services/admin.service.js'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/blog',
    name: 'Blog',
    component: () => import('../views/Blog.vue')
  },
  {
    path: '/blog/:slug',
    name: 'BlogDetail',
    component: () => import('../views/ArticleDetail.vue')
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('../views/Contact.vue')
  },

  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue')
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('../views/Checkout.vue')
  },
  {
    path: '/order-success/:orderId?',
    name: 'OrderSuccess',
    component: () => import('../views/OrderSuccess.vue')
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue')
  },
  {
    path: '/me',
    name: 'me',
    component: () => import('../views/Me.vue')
  },
      {
      path: '/healthcare',
      name: 'HealthCare',
      component: () => import('../views/HealthCare.vue')
    },
  {
    path: '/privacy',
    name: 'Privacy',
    component: () => import('../views/Privacy.vue')
  },
  {
    path: '/returns',
    name: 'Returns',
    component: () => import('../views/Returns.vue')
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('../views/Payment.vue')
  },
  {
    path: '/terms',
    name: 'Terms',
    component: () => import('../views/Terms.vue')
  },
  {
    path: '/faq',
    name: 'FAQ',
    component: () => import('../views/FAQ.vue')
  },
  {
    path: '/maps',
    name: 'Maps',
    component: () => import('../views/MapView.vue')
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/AdminDashboard.vue'),
    beforeEnter: (to, from, next) => {
      if (!adminService.isAuthenticated()) {
        next({ path: '/admin/login', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLogin.vue')
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('../views/AdminOrders.vue'),
    beforeEnter: (to, from, next) => {
      if (!adminService.isAuthenticated()) {
        next({ path: '/admin/login', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    }
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('../views/AdminProducts.vue'),
    beforeEnter: (to, from, next) => {
      if (!adminService.isAuthenticated()) {
        next({ path: '/admin/login', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    }
  },
  {
    path: '/admin/products/:id',
    name: 'AdminProductDetail',
    component: () => import('../views/AdminProductDetail.vue'),
    beforeEnter: (to, from, next) => {
      if (!adminService.isAuthenticated()) {
        next({ path: '/admin/login', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/AdminUsers.vue'),
    beforeEnter: (to, from, next) => {
      if (!adminService.isAuthenticated()) {
        next({ path: '/admin/login', query: { redirect: to.fullPath } })
      } else {
        next()
      }
    }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// Router guard for authentication - BẮT BUỘC LOGIN CHO TẤT CẢ ADMIN ROUTES
router.beforeEach((to, from, next) => {
  const isAdminRoute = to.path.startsWith('/admin')
  const isLoginPage = to.path === '/admin/login'
  
  // Check authentication status - STRICT CHECK
  const isAuthenticated = adminService.isAuthenticated()
  
  // Nếu đã logged in và cố truy cập login page, redirect về dashboard
  if (isLoginPage && isAuthenticated) {
    next({ path: '/admin/orders' })
    return
  }
  
  // Cho phép truy cập login page nếu chưa authenticated
  if (isLoginPage && !isAuthenticated) {
    next()
    return
  }
  
  // Cho phép truy cập các route khác (không phải admin)
  // Admin routes sẽ được bảo vệ bởi beforeEnter guard
  next()
})

export default router 