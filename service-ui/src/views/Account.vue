<template>
  <div class="max-w-4xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold text-green-700 mb-4">Tài khoản của tôi</h1>

    <!-- Chưa đăng nhập -->
    <div v-if="!auth.isLoggedIn" class="bg-white rounded-xl shadow p-6 text-center">
      <p class="text-gray-700 mb-4">Vui lòng đăng nhập để xem thông tin tài khoản và đơn hàng.</p>
      <button type="button" class="px-5 py-2 bg-green-600 text-white rounded-lg font-semibold" @click="auth.openModal('login')">
        Đăng nhập / Đăng ký
      </button>
    </div>

    <template v-else>
      <div class="flex gap-1 mb-4 border-b border-gray-200 overflow-x-auto" role="tablist">
        <button
          v-for="t in tabs"
          :key="t.value"
          type="button"
          role="tab"
          :aria-selected="tab === t.value"
          class="px-4 py-2 text-sm font-semibold whitespace-nowrap border-b-2 -mb-px"
          :class="tab === t.value ? 'border-green-600 text-green-700' : 'border-transparent text-gray-600 hover:text-gray-800'"
          @click="setTab(t.value)"
        >
          {{ t.label }}
        </button>
      </div>

      <p v-if="message" class="mb-4 text-sm rounded-lg px-3 py-2" :class="messageError ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-800'" role="status">
        {{ message }}
      </p>

      <!-- Thông tin -->
      <section v-if="tab === 'profile'" class="bg-white rounded-xl shadow p-5">
        <div v-if="loadingProfile" class="text-sm text-gray-500">Đang tải...</div>
        <div v-else-if="profileLoadError" class="text-sm text-gray-600">
          {{ profileLoadError }}
          <button type="button" class="ml-1 text-green-700 font-semibold hover:underline" @click="loadProfile">Thử lại</button>
        </div>
        <form v-else class="grid grid-cols-1 md:grid-cols-2 gap-4" novalidate @submit.prevent="saveProfile">
          <div class="md:col-span-2">
            <label class="block text-sm font-semibold text-gray-700 mb-1">Số điện thoại (tên đăng nhập)</label>
            <input :value="profile.phone" disabled class="w-full border border-gray-200 bg-gray-50 rounded-lg px-3 py-2 text-gray-600" />
            <p class="text-xs text-gray-500 mt-1">Cần đổi số điện thoại? Vui lòng liên hệ hotline của Thi Yên.</p>
          </div>
          <div>
            <label for="acc-name" class="block text-sm font-semibold text-gray-700 mb-1">Họ và tên *</label>
            <input id="acc-name" v-model="profile.fullName" maxlength="100" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="acc-email" class="block text-sm font-semibold text-gray-700 mb-1">Email</label>
            <input id="acc-email" v-model.trim="profile.email" type="email" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="acc-birth" class="block text-sm font-semibold text-gray-700 mb-1">Ngày sinh</label>
            <input id="acc-birth" v-model="profile.birthDate" type="date" :max="today" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="acc-gender" class="block text-sm font-semibold text-gray-700 mb-1">Giới tính</label>
            <select id="acc-gender" v-model="profile.gender" class="w-full border border-gray-300 rounded-lg px-3 py-2">
              <option value="">Không chọn</option>
              <option value="FEMALE">Nữ</option>
              <option value="MALE">Nam</option>
              <option value="OTHER">Khác</option>
            </select>
          </div>

          <h2 class="md:col-span-2 font-bold text-gray-800 mt-2">Địa chỉ giao hàng mặc định</h2>
          <p class="md:col-span-2 -mt-3 text-xs text-gray-500">Dùng để tự điền khi đặt hàng.</p>
          <div>
            <label for="acc-recipient" class="block text-sm font-semibold text-gray-700 mb-1">Tên người nhận</label>
            <input id="acc-recipient" v-model="profile.addressRecipient" maxlength="100" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="acc-rphone" class="block text-sm font-semibold text-gray-700 mb-1">Số điện thoại nhận hàng</label>
            <input id="acc-rphone" v-model.trim="profile.addressPhone" type="tel" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="acc-province" class="block text-sm font-semibold text-gray-700 mb-1">Tỉnh/Thành phố</label>
            <select id="acc-province" v-model="profile.provinceCode" class="w-full border border-gray-300 rounded-lg px-3 py-2" @change="onProvinceChange">
              <option value="">Chọn tỉnh/thành phố</option>
              <option v-for="p in provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
            </select>
          </div>
          <div>
            <label for="acc-district" class="block text-sm font-semibold text-gray-700 mb-1">Quận/Huyện</label>
            <select id="acc-district" v-model="profile.districtCode" :disabled="!profile.provinceCode" class="w-full border border-gray-300 rounded-lg px-3 py-2" @change="onDistrictChange">
              <option value="">Chọn quận/huyện</option>
              <option v-for="d in districts" :key="d.code" :value="d.code">{{ d.name }}</option>
            </select>
          </div>
          <div>
            <label for="acc-ward" class="block text-sm font-semibold text-gray-700 mb-1">Phường/Xã</label>
            <select id="acc-ward" v-model="profile.wardCode" :disabled="!profile.districtCode" class="w-full border border-gray-300 rounded-lg px-3 py-2">
              <option value="">Chọn phường/xã</option>
              <option v-for="w in wards" :key="w.code" :value="w.code">{{ w.name }}</option>
            </select>
          </div>
          <div class="md:col-span-2">
            <label for="acc-address" class="block text-sm font-semibold text-gray-700 mb-1">Địa chỉ (số nhà, đường)</label>
            <input id="acc-address" v-model="profile.addressLine" maxlength="500" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div class="md:col-span-2 flex justify-end">
            <button type="submit" :disabled="saving" class="px-5 py-2 bg-green-600 text-white rounded-lg font-semibold disabled:opacity-50">
              {{ saving ? 'Đang lưu...' : 'Lưu thông tin' }}
            </button>
          </div>
        </form>
      </section>

      <!-- Đổi mật khẩu -->
      <section v-else-if="tab === 'password'" class="bg-white rounded-xl shadow p-5 max-w-md">
        <form class="space-y-4" novalidate @submit.prevent="savePassword">
          <div>
            <label for="pw-current" class="block text-sm font-semibold text-gray-700 mb-1">Mật khẩu hiện tại</label>
            <input id="pw-current" v-model="password.current" type="password" autocomplete="current-password" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <div>
            <label for="pw-new" class="block text-sm font-semibold text-gray-700 mb-1">Mật khẩu mới</label>
            <input id="pw-new" v-model="password.next" type="password" autocomplete="new-password" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
            <p class="text-xs text-gray-500 mt-1">Từ 8 ký tự, có cả chữ và số. Đổi xong, các thiết bị khác sẽ bị đăng xuất.</p>
          </div>
          <div>
            <label for="pw-confirm" class="block text-sm font-semibold text-gray-700 mb-1">Nhập lại mật khẩu mới</label>
            <input id="pw-confirm" v-model="password.confirm" type="password" autocomplete="new-password" class="w-full border border-gray-300 rounded-lg px-3 py-2" />
          </div>
          <button type="submit" :disabled="saving" class="w-full py-2 bg-green-600 text-white rounded-lg font-semibold disabled:opacity-50">
            {{ saving ? 'Đang lưu...' : 'Đổi mật khẩu' }}
          </button>
        </form>
      </section>

      <!-- Đơn hàng -->
      <section v-else class="bg-white rounded-xl shadow p-5">
        <div v-if="loadingOrders" class="text-sm text-gray-500">Đang tải đơn hàng...</div>
        <div v-else-if="ordersError" class="text-sm text-gray-600">
          {{ ordersError }}
          <button type="button" class="ml-1 text-green-700 font-semibold hover:underline" @click="loadOrders">Thử lại</button>
        </div>
        <div v-else-if="orders.length === 0" class="text-sm text-gray-600">
          Bạn chưa có đơn hàng nào. Đơn đặt khi đã đăng nhập sẽ hiện ở đây.
        </div>
        <ul v-else class="divide-y divide-gray-100">
          <li v-for="o in orders" :key="o.orderId" class="py-3 flex flex-col md:flex-row md:items-center md:justify-between gap-1">
            <div>
              <div class="font-semibold text-gray-800">Đơn #{{ o.orderId }}</div>
              <div class="text-xs text-gray-500">{{ formatDate(o.createdAt) }} · {{ itemSummary(o) }}</div>
            </div>
            <div class="flex items-center gap-3">
              <span class="text-sm font-semibold text-gray-800">{{ formatPrice(o.total) }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-700">{{ getStatusText(o.status) }}</span>
            </div>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import { customerService } from '@/services/customer.service.js'
import locationService from '@/services/location.service.js'
import { getStatusText } from '@/constants/orderStatus.js'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const tabs = [
  { value: 'profile', label: 'Thông tin' },
  { value: 'password', label: 'Đổi mật khẩu' },
  { value: 'orders', label: 'Đơn hàng' },
]
const tab = computed(() => (tabs.some((t) => t.value === route.query.tab) ? route.query.tab : 'profile'))
const today = new Date().toISOString().slice(0, 10)

const message = ref('')
const messageError = ref(false)
const saving = ref(false)

const profile = reactive({})
const loadingProfile = ref(false)
const profileLoadError = ref('')
const provinces = ref([])
const districts = ref([])
const wards = ref([])

const password = reactive({ current: '', next: '', confirm: '' })

const orders = ref([])
const loadingOrders = ref(false)
const ordersError = ref('')

function flash(text, isError = false) {
  message.value = text
  messageError.value = isError
}

function setTab(value) {
  flash('')
  router.replace({ query: value === 'profile' ? {} : { tab: value } })
}

async function loadProfile() {
  loadingProfile.value = true
  profileLoadError.value = ''
  try {
    const data = await auth.authorized((token) => customerService.getProfile(token))
    Object.assign(profile, emptyToBlank(data))
    auth.setCustomer(data)
    await loadAddressLists()
  } catch (e) {
    profileLoadError.value = e.status === 401 ? 'Vui lòng đăng nhập lại.' : (e.message || 'Không tải được thông tin.')
  } finally {
    loadingProfile.value = false
  }
}

function emptyToBlank(data) {
  const out = { ...data }
  ;['email', 'birthDate', 'gender', 'addressRecipient', 'addressPhone', 'addressLine', 'provinceCode', 'districtCode', 'wardCode']
    .forEach((k) => { if (out[k] == null) out[k] = '' })
  return out
}

async function loadAddressLists() {
  try {
    if (provinces.value.length === 0) provinces.value = await locationService.getProvinces()
    districts.value = profile.provinceCode ? await locationService.getDistricts(profile.provinceCode) : []
    wards.value = profile.districtCode ? await locationService.getWards(profile.districtCode) : []
  } catch {
    // Address lists are optional here; the rest of the form still works
  }
}

async function onProvinceChange() {
  profile.districtCode = ''
  profile.wardCode = ''
  wards.value = []
  districts.value = profile.provinceCode ? await locationService.getDistricts(profile.provinceCode) : []
}

async function onDistrictChange() {
  profile.wardCode = ''
  wards.value = profile.districtCode ? await locationService.getWards(profile.districtCode) : []
}

async function saveProfile() {
  if (!profile.fullName || !profile.fullName.trim()) {
    flash('Vui lòng nhập họ tên', true)
    return
  }
  saving.value = true
  flash('')
  try {
    const payload = {
      fullName: profile.fullName.trim(),
      email: profile.email || null,
      birthDate: profile.birthDate || null,
      gender: profile.gender || null,
      addressRecipient: profile.addressRecipient || null,
      addressPhone: profile.addressPhone || null,
      addressLine: profile.addressLine || null,
      provinceCode: profile.provinceCode || null,
      districtCode: profile.districtCode || null,
      wardCode: profile.wardCode || null,
      updatedAt: profile.updatedAt,
    }
    const { data, message: msg } = await auth.authorized((token) => customerService.updateProfile(token, payload))
    Object.assign(profile, emptyToBlank(data))
    auth.setCustomer(data)
    flash(msg || 'Đã lưu thông tin')
  } catch (e) {
    flash(e.message || 'Lưu thất bại', true)
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  if (!password.current || !password.next) {
    flash('Vui lòng nhập đủ mật khẩu hiện tại và mật khẩu mới', true)
    return
  }
  if (password.next !== password.confirm) {
    flash('Mật khẩu nhập lại không khớp', true)
    return
  }
  saving.value = true
  flash('')
  try {
    const { data, message: msg } = await auth.authorized((token) =>
      customerService.changePassword(token, { currentPassword: password.current, newPassword: password.next }))
    // The server ended every session and returned a new one for this device
    auth.applySession(data)
    Object.assign(password, { current: '', next: '', confirm: '' })
    flash(msg || 'Đã đổi mật khẩu')
  } catch (e) {
    flash(e.message || 'Đổi mật khẩu thất bại', true)
  } finally {
    saving.value = false
  }
}

async function loadOrders() {
  loadingOrders.value = true
  ordersError.value = ''
  try {
    orders.value = await auth.authorized((token) => customerService.getMyOrders(token))
  } catch (e) {
    ordersError.value = e.status === 401 ? 'Vui lòng đăng nhập lại.' : (e.message || 'Không tải được đơn hàng.')
  } finally {
    loadingOrders.value = false
  }
}

function loadTab() {
  if (!auth.isLoggedIn) return
  if (tab.value === 'profile') loadProfile()
  else if (tab.value === 'orders') loadOrders()
}

const formatPrice = (val) => Number(val || 0).toLocaleString('vi-VN') + '₫'
function formatDate(value) {
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch {
    return value || ''
  }
}
function itemSummary(order) {
  const items = order.items || []
  const count = items.reduce((n, i) => n + (i.quantity || 0), 0)
  return items.length ? `${items[0].name}${items.length > 1 ? ` và ${items.length - 1} sản phẩm khác` : ''} (${count} món)` : ''
}

onMounted(() => {
  if (!auth.isLoggedIn) auth.openModal('login', 'Đăng nhập để xem tài khoản của bạn.')
  loadTab()
})
watch(tab, loadTab)
// Logged in from the popup on this page, or logged out in another tab
watch(() => auth.isLoggedIn, (now) => {
  if (now) loadTab()
})
</script>
