<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="bg-white rounded-xl shadow-lg p-8 w-full max-w-sm">
      <h2 class="text-2xl font-bold text-green-700 mb-6 text-center">Đăng nhập Admin</h2>
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-gray-700 font-semibold mb-1">Username</label>
          <input v-model="username" class="w-full border rounded px-3 py-2" autocomplete="username" />
        </div>
        <div>
          <label class="block text-gray-700 font-semibold mb-1">Password</label>
          <input v-model="password" type="password" class="w-full border rounded px-3 py-2" autocomplete="current-password" />
        </div>
        <div v-if="error" class="text-red-500 text-sm">{{ error }}</div>
        <button type="submit" :disabled="loading" class="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-bold py-2 rounded-lg transition">
          {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import adminService from '../services/admin.service.js'

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const router = useRouter()

async function handleLogin() {
  if (!username.value || !password.value) {
    error.value = 'Vui lòng nhập đầy đủ thông tin!'
    return
  }

  loading.value = true
  error.value = ''
  
  try {
    const response = await adminService.login({
      username: username.value,
      password: password.value,
    })
    
    if (response.success || response.token) {
      // Store auth token if available
      if (response.token) {
        localStorage.setItem('authToken', response.token)
      }
      
      // Store admin info
      localStorage.setItem('isAdmin', '1')
      if (response.user) {
        localStorage.setItem('adminUser', JSON.stringify(response.user))
      }
      
      router.push('/admin/orders')
    } else {
      error.value = response.message || 'Sai tài khoản hoặc mật khẩu!'
    }
  } catch (err) {
    error.value = err.message || 'Sai tài khoản hoặc mật khẩu!'
  } finally {
    loading.value = false
  }
}
</script> 