<template>
  <Teleport to="body">
    <transition name="fade">
      <div
        v-if="auth.modalOpen"
        class="auth-backdrop fixed inset-0 z-[10000] flex items-stretch md:items-center justify-center md:p-4"
        @click.self="close"
      >
        <div
          ref="dialog"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
          class="auth-dialog w-full md:max-w-md md:rounded-3xl flex flex-col max-h-full overflow-y-auto"
          @keydown.esc="close"
          @keydown.tab="trapFocus"
        >
          <!-- Đầu popup: cùng màu header site -->
          <div class="auth-head relative px-6 pt-6 pb-5">
            <button type="button" class="auth-close" aria-label="Đóng" @click="close">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <h2 :id="titleId" class="auth-title">{{ title }}</h2>
            <p class="auth-subtitle">{{ subtitle }}</p>

            <!-- Chuyển nhanh Đăng nhập / Đăng ký -->
            <div v-if="auth.modalView !== 'forgot'" class="auth-tabs" role="group" aria-label="Chọn đăng nhập hoặc đăng ký">
              <button
                type="button"
                :class="['auth-tab', { 'is-active': auth.modalView === 'login' }]"
                :aria-pressed="auth.modalView === 'login'"
                @click="auth.modalView !== 'login' && switchView('login')"
              >Đăng nhập</button>
              <button
                type="button"
                :class="['auth-tab', { 'is-active': auth.modalView === 'register' }]"
                :aria-pressed="auth.modalView === 'register'"
                @click="auth.modalView !== 'register' && switchView('register')"
              >Đăng ký</button>
            </div>
          </div>

          <div class="px-6 py-6 space-y-4">
            <p v-if="auth.modalReason" class="auth-notice">{{ auth.modalReason }}</p>
            <p v-if="formError" class="auth-alert" role="alert">{{ formError }}</p>

            <!-- Đăng nhập -->
            <form v-if="auth.modalView === 'login'" class="space-y-4" novalidate @submit.prevent="submitLogin">
              <div>
                <label for="auth-login-phone" class="auth-label">Số điện thoại</label>
                <input
                  id="auth-login-phone"
                  ref="firstInput"
                  v-model.trim="login.phone"
                  type="tel"
                  inputmode="tel"
                  autocomplete="tel"
                  placeholder="0912345678"
                  :class="['auth-input', { 'has-error': errors.phone }]"
                />
                <p v-if="errors.phone" class="auth-error">{{ errors.phone }}</p>
              </div>
              <div>
                <div class="flex items-center justify-between mb-1.5">
                  <label for="auth-login-password" class="auth-label !mb-0">Mật khẩu</label>
                  <button type="button" class="auth-link text-sm" @click="switchView('forgot')">Quên mật khẩu?</button>
                </div>
                <div class="relative">
                  <input
                    id="auth-login-password"
                    v-model="login.password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    :class="['auth-input pr-16', { 'has-error': errors.password }]"
                  />
                  <button type="button" class="auth-toggle" @click="showPassword = !showPassword">
                    {{ showPassword ? 'Ẩn' : 'Hiện' }}
                  </button>
                </div>
                <p v-if="errors.password" class="auth-error">{{ errors.password }}</p>
              </div>
              <button type="submit" :disabled="submitting" class="auth-btn">
                {{ submitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}
              </button>
              <p class="auth-foot">
                Chưa có tài khoản?
                <button type="button" class="auth-link font-semibold" @click="switchView('register')">Đăng ký ngay</button>
              </p>
            </form>

            <!-- Đăng ký -->
            <form v-else-if="auth.modalView === 'register'" class="space-y-4" novalidate @submit.prevent="submitRegister">
              <!-- Honeypot: hidden from people, bots fill it -->
              <input v-model="register.website" type="text" name="website" tabindex="-1" autocomplete="off" aria-hidden="true" class="hidden" />
              <div>
                <label for="auth-reg-name" class="auth-label">Họ và tên *</label>
                <input
                  id="auth-reg-name"
                  ref="firstInput"
                  v-model="register.fullName"
                  type="text"
                  maxlength="100"
                  autocomplete="name"
                  :class="['auth-input', { 'has-error': errors.fullName }]"
                />
                <p v-if="errors.fullName" class="auth-error">{{ errors.fullName }}</p>
              </div>
              <div>
                <label for="auth-reg-phone" class="auth-label">Số điện thoại * <span class="auth-label-note">(dùng để đăng nhập)</span></label>
                <input
                  id="auth-reg-phone"
                  v-model.trim="register.phone"
                  type="tel"
                  inputmode="tel"
                  autocomplete="tel"
                  placeholder="0912345678"
                  :class="['auth-input', { 'has-error': errors.phone }]"
                />
                <p v-if="errors.phone" class="auth-error">{{ errors.phone }}</p>
              </div>
              <div>
                <label for="auth-reg-email" class="auth-label">Email <span class="auth-label-note">(không bắt buộc)</span></label>
                <input
                  id="auth-reg-email"
                  v-model.trim="register.email"
                  type="email"
                  autocomplete="email"
                  :class="['auth-input', { 'has-error': errors.email }]"
                />
                <p v-if="errors.email" class="auth-error">{{ errors.email }}</p>
              </div>
              <div>
                <label for="auth-reg-password" class="auth-label">Mật khẩu *</label>
                <div class="relative">
                  <input
                    id="auth-reg-password"
                    v-model="register.password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    :class="['auth-input pr-16', { 'has-error': errors.password }]"
                  />
                  <button type="button" class="auth-toggle" @click="showPassword = !showPassword">
                    {{ showPassword ? 'Ẩn' : 'Hiện' }}
                  </button>
                </div>
                <p :class="errors.password ? 'auth-error' : 'auth-hint'">
                  {{ errors.password || 'Từ 8 ký tự, có cả chữ và số.' }}
                </p>
              </div>
              <div>
                <label for="auth-reg-confirm" class="auth-label">Nhập lại mật khẩu *</label>
                <input
                  id="auth-reg-confirm"
                  v-model="register.confirm"
                  :type="showPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  :class="['auth-input', { 'has-error': errors.confirm }]"
                />
                <p v-if="errors.confirm" class="auth-error">{{ errors.confirm }}</p>
              </div>
              <div>
                <label class="flex items-start gap-2 text-sm auth-text">
                  <input v-model="register.consent" type="checkbox" class="auth-checkbox mt-0.5" />
                  <span>
                    Tôi đồng ý với
                    <router-link to="/terms" target="_blank" class="auth-link underline">Điều khoản sử dụng</router-link>
                    và
                    <router-link to="/privacy" target="_blank" class="auth-link underline">Chính sách bảo mật</router-link>
                    của Thi Yên.
                  </span>
                </label>
                <p v-if="errors.consent" class="auth-error">{{ errors.consent }}</p>
              </div>
              <button type="submit" :disabled="submitting" class="auth-btn">
                {{ submitting ? 'Đang đăng ký...' : 'Đăng ký' }}
              </button>
              <p class="auth-foot">
                Đã có tài khoản?
                <button type="button" class="auth-link font-semibold" @click="switchView('login')">Đăng nhập</button>
              </p>
            </form>

            <!-- Quên mật khẩu: chưa có OTP ở phiên bản này (task-003 R6), admin đặt lại qua hotline -->
            <div v-else class="space-y-4">
              <p class="auth-notice">
                Hiện chưa hỗ trợ tự đặt lại mật khẩu. Vui lòng liên hệ hotline hoặc Zalo của Thi Yên, cung cấp số điện thoại
                đã đăng ký và mã đơn hàng gần nhất (nếu có) để được cấp mật khẩu tạm.
              </p>
              <router-link ref="firstInput" to="/contact" class="auth-btn block text-center" @click="close">
                Xem thông tin liên hệ
              </router-link>
              <button type="button" class="auth-link w-full text-sm font-semibold" @click="switchView('login')">← Quay lại đăng nhập</button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.js'

const auth = useAuthStore()
const titleId = 'auth-modal-title'
const dialog = ref(null)
const firstInput = ref(null)

const login = reactive({ phone: '', password: '' })
const register = reactive({ fullName: '', phone: '', email: '', password: '', confirm: '', consent: false, website: '' })
const errors = reactive({})
const formError = ref('')
const submitting = ref(false)
const showPassword = ref(false)

const title = computed(() => ({ login: 'Đăng nhập', register: 'Đăng ký tài khoản', forgot: 'Quên mật khẩu' })[auth.modalView])
const subtitle = computed(() => ({
  login: 'Chào mừng bạn quay lại với Thi Yên',
  register: 'Tạo tài khoản để theo dõi đơn hàng dễ dàng hơn',
  forgot: 'Thi Yên sẽ hỗ trợ bạn lấy lại tài khoản',
})[auth.modalView])

// Same rules as the server (PhoneNumbers, CustomerAuthService.validatePassword); the server stays the authority
const PHONE_RE = /^0[35789][0-9]{8}$/
function normalizePhone(raw) {
  let s = (raw || '').replace(/[\s.\-()]/g, '')
  if (s.startsWith('+84')) s = '0' + s.slice(3)
  else if (s.startsWith('84') && s.length === 11) s = '0' + s.slice(2)
  return PHONE_RE.test(s) ? s : null
}

function passwordProblem(password, phone) {
  if (!password || password.length < 8 || password.length > 64) return 'Mật khẩu từ 8 đến 64 ký tự'
  if (!/\p{L}/u.test(password) || !/\d/.test(password)) return 'Mật khẩu phải có cả chữ và số'
  if (phone && password.includes(phone)) return 'Mật khẩu không được chứa số điện thoại'
  return ''
}

function resetErrors() {
  Object.keys(errors).forEach((k) => delete errors[k])
  formError.value = ''
}

function switchView(view) {
  resetErrors()
  showPassword.value = false
  auth.modalView = view
  // Keep the phone typed in one form when switching to the other
  if (view === 'register' && login.phone && !register.phone) register.phone = login.phone
  if (view === 'login' && register.phone && !login.phone) login.phone = register.phone
  focusFirst()
}

function close() {
  if (submitting.value) return
  auth.closeModal()
}

async function submitLogin() {
  resetErrors()
  const phone = normalizePhone(login.phone)
  if (!phone) errors.phone = 'Số điện thoại không hợp lệ (ví dụ 0912345678)'
  if (!login.password) errors.password = 'Vui lòng nhập mật khẩu'
  if (Object.keys(errors).length) return
  submitting.value = true
  try {
    await auth.login({ phone, password: login.password })
    login.password = ''
  } catch (e) {
    formError.value = e.message
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  resetErrors()
  const phone = normalizePhone(register.phone)
  if (!register.fullName.trim()) errors.fullName = 'Vui lòng nhập họ tên'
  if (!phone) errors.phone = 'Số điện thoại không hợp lệ (ví dụ 0912345678)'
  if (register.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(register.email)) errors.email = 'Email không hợp lệ'
  const pwd = passwordProblem(register.password, phone)
  if (pwd) errors.password = pwd
  if (register.confirm !== register.password) errors.confirm = 'Mật khẩu nhập lại không khớp'
  if (!register.consent) errors.consent = 'Vui lòng đồng ý Điều khoản sử dụng và Chính sách bảo mật'
  if (Object.keys(errors).length) return
  submitting.value = true
  try {
    await auth.register({
      phone,
      fullName: register.fullName.trim(),
      email: register.email || null,
      password: register.password,
      consent: true,
      website: register.website,
    })
    Object.assign(register, { fullName: '', phone: '', email: '', password: '', confirm: '', consent: false, website: '' })
  } catch (e) {
    formError.value = e.message
  } finally {
    submitting.value = false
  }
}

function focusFirst() {
  nextTick(() => {
    const el = firstInput.value?.$el || firstInput.value
    el?.focus?.()
  })
}

// Keep Tab inside the dialog
function trapFocus(event) {
  const nodes = dialog.value?.querySelectorAll('a[href], button:not([disabled]), input:not([type="hidden"]):not(.hidden), [tabindex]:not([tabindex="-1"])')
  if (!nodes || nodes.length === 0) return
  const first = nodes[0]
  const last = nodes[nodes.length - 1]
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

watch(() => auth.modalOpen, (open) => {
  if (open) {
    resetErrors()
    showPassword.value = false
    focusFirst()
  }
})
</script>

<style scoped>
/*
 * Bảng màu chung của site: nền header #eef1c5, nền kem #fbfbef,
 * xanh đậm thương hiệu #2f6b2f (giống menu, drawer mobile, khung tìm kiếm).
 */
.auth-backdrop {
  background-color: rgba(31, 42, 31, 0.45);
}
.auth-dialog {
  background-color: #fbfbef;
  box-shadow: 0 24px 60px rgba(31, 42, 31, 0.3);
  color: #1f2a1f;
}
.auth-head {
  background-color: #eef1c5;
  border-bottom: 1px solid rgba(47, 107, 47, 0.15);
}
.auth-close {
  position: absolute;
  top: 0.85rem;
  right: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 9999px;
  color: rgba(31, 42, 31, 0.6);
  transition: background-color 0.2s, color 0.2s;
}
.auth-close:hover,
.auth-close:focus-visible {
  color: #1f2a1f;
  background-color: rgba(47, 107, 47, 0.12);
  outline: none;
}
.auth-title {
  font-size: 1.5rem;
  font-weight: 800;
  line-height: 1.2;
  color: #2f6b2f;
}
.auth-subtitle {
  margin-top: 0.25rem;
  font-size: 0.9rem;
  color: rgba(31, 42, 31, 0.65);
}
.auth-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px;
  margin-top: 1.1rem;
  padding: 4px;
  border-radius: 9999px;
  background-color: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(47, 107, 47, 0.15);
}
.auth-tab {
  padding: 0.5rem 0;
  border-radius: 9999px;
  font-size: 0.9rem;
  font-weight: 600;
  color: rgba(31, 42, 31, 0.65);
  transition: background-color 0.2s, color 0.2s, box-shadow 0.2s;
}
.auth-tab:hover {
  color: #2f6b2f;
}
.auth-tab.is-active {
  color: #fff;
  background-color: #2f6b2f;
  box-shadow: 0 2px 6px rgba(47, 107, 47, 0.3);
}
.auth-tab:focus-visible {
  outline: 2px solid #2f6b2f;
  outline-offset: 2px;
}
.auth-label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2a1f;
}
.auth-label-note {
  font-weight: 400;
  color: rgba(31, 42, 31, 0.55);
}
.auth-input {
  width: 100%;
  padding: 0.65rem 0.9rem;
  border-radius: 12px;
  background-color: #fff;
  border: 1.5px solid rgba(47, 107, 47, 0.22);
  color: #1f2a1f;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.auth-input::placeholder {
  color: rgba(31, 42, 31, 0.35);
}
.auth-input:focus {
  outline: none;
  border-color: #2f6b2f;
  box-shadow: 0 0 0 3px rgba(47, 107, 47, 0.15);
}
.auth-input.has-error {
  border-color: #dc2626;
}
.auth-input.has-error:focus {
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.15);
}
.auth-toggle {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  padding: 0.25rem 0.6rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #2f6b2f;
}
.auth-toggle:hover {
  background-color: rgba(47, 107, 47, 0.08);
}
.auth-error {
  margin-top: 0.3rem;
  font-size: 0.75rem;
  color: #dc2626;
}
.auth-hint {
  margin-top: 0.3rem;
  font-size: 0.75rem;
  color: rgba(31, 42, 31, 0.55);
}
.auth-notice {
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  font-size: 0.875rem;
  color: #2f6b2f;
  background-color: #eef1c5;
}
.auth-alert {
  padding: 0.65rem 0.85rem;
  border-radius: 12px;
  font-size: 0.875rem;
  color: #b91c1c;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
}
.auth-btn {
  width: 100%;
  padding: 0.75rem 1rem;
  border-radius: 9999px;
  font-weight: 700;
  color: #fff;
  background-color: #2f6b2f;
  box-shadow: 0 4px 12px rgba(47, 107, 47, 0.25);
  transition: background-color 0.2s, opacity 0.2s;
}
.auth-btn:hover:not(:disabled) {
  background-color: #275a27;
}
.auth-btn:focus-visible {
  outline: 2px solid #2f6b2f;
  outline-offset: 3px;
}
.auth-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.auth-link {
  color: #2f6b2f;
}
.auth-link:hover {
  text-decoration: underline;
}
.auth-text {
  color: rgba(31, 42, 31, 0.8);
}
.auth-foot {
  font-size: 0.875rem;
  text-align: center;
  color: rgba(31, 42, 31, 0.65);
}
.auth-checkbox {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
  /* @tailwindcss/forms tô ô đã chọn bằng currentColor */
  color: #2f6b2f;
  border-radius: 4px;
  border-color: rgba(47, 107, 47, 0.4);
}
.auth-checkbox:focus {
  --tw-ring-color: rgba(47, 107, 47, 0.3);
}
</style>
