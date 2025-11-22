<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="bg-white rounded-xl shadow-lg p-8 w-full max-w-sm">
      <div class="text-center mb-6">
        <img :src="getImageUrlFromApi('/images/logo/logo.png')" alt="Thi Yên Logo" class="w-16 h-16 mx-auto mb-4 object-contain" />
        <h2 class="text-2xl font-bold text-green-700">Đăng nhập Admin</h2>
      </div>
      
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-gray-700 font-semibold mb-1">Tên đăng nhập</label>
          <input 
            v-model="username" 
            type="text"
            class="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-green-500" 
            autocomplete="username"
            required
            :disabled="loading"
          />
        </div>
        <div>
          <label class="block text-gray-700 font-semibold mb-1">Mật khẩu</label>
          <input 
            v-model="password" 
            type="password" 
            class="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-green-500" 
            autocomplete="current-password"
            required
            :disabled="loading"
          />
        </div>
        
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
          {{ error }}
        </div>
        
        <button 
          type="submit" 
          :disabled="loading || !username || !password" 
          class="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-white font-bold py-2 rounded-lg transition"
        >
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
import { getImageUrlFromApi } from '../utils/imageUtils.js'

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const router = useRouter()

async function handleLogin() {
  // Validation
  if (!username.value.trim() || !password.value.trim()) {
    error.value = 'Vui lòng nhập đầy đủ thông tin!'
    return
  }

  loading.value = true
  error.value = ''
  
  try {
    // Call login API - ServiceApiAdapter will extract data from ApiResponse
    const loginResponse = await adminService.login({
      username: username.value.trim(),
      password: password.value,
    })
    
    // Response format: { token, user: { id, name, email, role } }
    if (!loginResponse || !loginResponse.token) {
      throw new Error('Không nhận được token từ server')
    }
    
    // Store authentication data
    localStorage.setItem('authToken', loginResponse.token)
    
    if (loginResponse.user) {
      localStorage.setItem('adminUser', JSON.stringify(loginResponse.user))
    }
    
    // Clear password from memory (security best practice)
    password.value = ''
    
    // Redirect to admin dashboard
    router.push('/admin/orders')
  } catch (err) {
    // Clear password on error (security)
    password.value = ''
    error.value = err.message || 'Sai tài khoản hoặc mật khẩu!'
  } finally {
    loading.value = false
  }
}
</script> 