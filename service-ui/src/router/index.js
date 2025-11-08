import { createRouter, createWebHistory } from 'vue-router'

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
    component: () => import('../views/AdminDashboard.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLogin.vue')
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: () => import('../views/AdminOrders.vue')
  },
  {
    path: '/admin/products',
    name: 'AdminProducts',
    component: () => import('../views/AdminProducts.vue')
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/AdminUsers.vue')
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// Simple router guard
router.beforeEach((to, from, next) => {
  console.log('🔄 Router navigation:', {
    from: from.path,
    to: to.path,
    name: to.name
  })
  
  next()
})

router.afterEach((to, from) => {
  console.log('✅ Router navigation completed:', {
    from: from.path,
    to: to.path,
    name: to.name
  })
})

export default router 