<template>
  <aside class="w-64 min-h-screen bg-white border-r border-gray-100 flex flex-col py-6">
    <nav class="flex-1">
      <ul class="space-y-2">
        <li>
          <router-link to="/admin" class="flex items-center gap-2 px-6 py-3 text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path === '/admin' || $route.path === '/admin/' }">
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="($route.path === '/admin' || $route.path === '/admin/') ? 'bg-green-500' : 'bg-gray-300'"></span>
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
            Dashboard
          </router-link>
        </li>
        <li>
          <router-link to="/admin/orders" class="flex items-center gap-2 px-6 py-3 text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/orders') }">
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/orders') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Đơn Hàng
          </router-link>
        </li>
        <li>
          <router-link to="/admin/products" class="flex items-center gap-2 px-6 py-3 text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/products') }">
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/products') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Sản Phẩm
          </router-link>
        </li>
        <li>
          <router-link to="/admin/users" class="flex items-center gap-2 px-6 py-3 text-lg font-semibold rounded-l-full transition hover:bg-green-50"
            :class="{ 'text-green-600 bg-green-50': $route.path.startsWith('/admin/users') }">
            <span class="inline-block w-2 h-2 rounded-full mr-2" :class="$route.path.startsWith('/admin/users') ? 'bg-green-500' : 'bg-gray-300'"></span>
            Khách Hàng
          </router-link>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script> 