<template>
  <AdminLayout>
    <div class="max-w-6xl mx-auto">
      <div class="flex items-start justify-between mb-4 gap-3 flex-wrap">
        <div>
          <h1 class="text-2xl font-bold text-green-700">Đánh giá sản phẩm</h1>
          <p class="text-sm text-gray-500">Khách gửi đánh giá ở trang sản phẩm; chỉ đánh giá "Đã duyệt" hiện công khai và được tính vào số sao.</p>
        </div>
        <button type="button" class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm" @click="openCreate">
          + Thêm đánh giá
        </button>
      </div>

      <!-- Filters -->
      <div class="bg-white rounded-xl shadow p-3 mb-4 flex flex-col lg:flex-row lg:items-center gap-3">
        <label class="flex items-center gap-2 text-sm">
          <span class="text-gray-600 shrink-0">Sản phẩm</span>
          <select v-model="filters.productId" class="border rounded-lg px-2 py-1.5 text-sm min-w-0 flex-1 lg:w-72" @change="applyFilters">
            <option value="">Tất cả sản phẩm</option>
            <option v-for="p in products" :key="p.id" :value="String(p.id)">#{{ p.id }} {{ p.name }}</option>
          </select>
        </label>
        <div class="flex flex-wrap gap-1 lg:ml-auto lg:justify-end">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            type="button"
            class="px-3 py-1.5 rounded-lg text-sm font-semibold whitespace-nowrap border"
            :class="filters.status === tab.value ? 'bg-green-600 text-white border-green-600' : 'bg-white text-gray-700 hover:bg-gray-50'"
            @click="setStatus(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.value" class="ml-1 text-xs opacity-80">({{ counts[tab.value] || 0 }})</span>
          </button>
        </div>
      </div>

      <p v-if="message" class="mb-3 text-sm" :class="messageError ? 'text-red-600' : 'text-green-600'">{{ message }}</p>

      <div v-if="loading" class="py-10 text-center text-gray-500">Đang tải...</div>
      <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded">
        {{ error }}
        <button type="button" class="ml-2 underline" @click="load">Thử lại</button>
      </div>

      <template v-else>
        <div v-if="!items.length" class="bg-white rounded-xl shadow p-8 text-center text-gray-500 text-sm">
          Không có đánh giá nào{{ filters.status ? ` ở trạng thái "${statusLabel(filters.status)}"` : '' }}.
        </div>

        <div v-else class="space-y-3">
          <article v-for="r in items" :key="r.id" class="bg-white rounded-xl shadow p-4">
            <div class="flex flex-wrap items-center gap-2 mb-2">
              <span class="px-2 py-0.5 rounded-full text-xs font-semibold" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs bg-gray-100 text-gray-600">{{ sourceLabel(r.source) }}</span>
              <router-link :to="`/products/${r.productId}`" target="_blank" class="text-xs text-gray-500 hover:text-green-700 truncate max-w-full">
                #{{ r.productId }} {{ r.productName }} ↗
              </router-link>
              <span class="text-xs text-gray-400 md:ml-auto">{{ formatDateTime(r.createdAt) }}</span>
            </div>

            <div class="flex items-center gap-2 mb-1">
              <span class="font-semibold text-gray-800 text-sm">{{ r.authorName }}</span>
              <span class="text-yellow-400 text-sm tracking-tight" :aria-label="`${r.rating} sao`">
                {{ '★'.repeat(r.rating) }}<span class="text-gray-300">{{ '★'.repeat(5 - r.rating) }}</span>
              </span>
            </div>
            <p class="text-sm text-gray-700 whitespace-pre-line break-words">{{ r.content }}</p>
            <div v-if="r.images?.length" class="flex gap-2 flex-wrap mt-2">
              <a v-for="(img, i) in r.images" :key="i" :href="resolveCmsImage(img)" target="_blank" rel="noopener">
                <img :src="resolveCmsImage(img)" loading="lazy" class="w-16 h-16 object-cover rounded border" :alt="`Ảnh ${i + 1}`" />
              </a>
            </div>

            <div class="flex flex-wrap gap-2 mt-3 pt-3 border-t">
              <button v-if="r.status !== 'APPROVED'" type="button" :disabled="busyId === r.id" class="px-3 py-1.5 rounded-lg text-sm font-semibold bg-green-600 text-white disabled:opacity-50" @click="changeStatus(r, 'APPROVED')">Duyệt</button>
              <button v-if="r.status !== 'HIDDEN'" type="button" :disabled="busyId === r.id" class="px-3 py-1.5 rounded-lg text-sm font-semibold border bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50" @click="changeStatus(r, 'HIDDEN')">Ẩn</button>
              <button type="button" :disabled="busyId === r.id" class="px-3 py-1.5 rounded-lg text-sm font-semibold border bg-white text-gray-700 hover:bg-gray-50 disabled:opacity-50" @click="openEdit(r)">Sửa</button>
              <button type="button" :disabled="busyId === r.id" class="px-3 py-1.5 rounded-lg text-sm font-semibold text-red-600 hover:bg-red-50 disabled:opacity-50 sm:ml-auto" @click="remove(r)">Xoá</button>
            </div>
          </article>
        </div>

        <div v-if="totalPages > 1" class="flex items-center justify-center gap-3 mt-4 text-sm">
          <button type="button" class="px-3 py-1.5 border rounded-lg bg-white disabled:opacity-40" :disabled="page === 0" @click="goPage(page - 1)">‹ Trước</button>
          <span class="text-gray-600">Trang {{ page + 1 }} / {{ totalPages }}</span>
          <button type="button" class="px-3 py-1.5 border rounded-lg bg-white disabled:opacity-40" :disabled="page >= totalPages - 1" @click="goPage(page + 1)">Sau ›</button>
        </div>
      </template>
    </div>

    <!-- Create / edit dialog -->
    <div v-if="editor.open" class="fixed inset-0 bg-black/50 z-50 flex items-end sm:items-center justify-center sm:p-4" @click.self="closeEditor">
      <form class="bg-white w-full sm:max-w-xl rounded-t-xl sm:rounded-xl shadow-2xl max-h-[92vh] overflow-y-auto" @submit.prevent="save">
        <div class="flex items-center justify-between p-4 border-b sticky top-0 bg-white">
          <h2 class="text-lg font-bold text-gray-900">{{ editor.id ? 'Sửa đánh giá' : 'Thêm đánh giá' }}</h2>
          <button type="button" class="text-gray-400 hover:text-gray-600 text-2xl leading-none" aria-label="Đóng" @click="closeEditor">×</button>
        </div>

        <div class="p-4 space-y-4">
          <label class="block">
            <span class="block text-sm font-semibold text-gray-700 mb-1">Sản phẩm *</span>
            <select v-model="editor.form.productId" :disabled="!!editor.id" required class="w-full border rounded-lg px-3 py-2 text-sm disabled:bg-gray-100">
              <option value="" disabled>Chọn sản phẩm</option>
              <option v-for="p in products" :key="p.id" :value="String(p.id)">#{{ p.id }} {{ p.name }}</option>
            </select>
          </label>

          <label class="block">
            <span class="block text-sm font-semibold text-gray-700 mb-1">Tên người đánh giá *</span>
            <input v-model="editor.form.authorName" type="text" required maxlength="100" class="w-full border rounded-lg px-3 py-2 text-sm" />
          </label>

          <div>
            <span class="block text-sm font-semibold text-gray-700 mb-1">Số sao *</span>
            <div class="flex items-center gap-1">
              <button v-for="i in 5" :key="i" type="button" class="text-3xl leading-none" :class="i <= editor.form.rating ? 'text-yellow-400' : 'text-gray-300'" :aria-label="`${i} sao`" @click="editor.form.rating = i">★</button>
              <span class="ml-2 text-sm text-gray-600">{{ editor.form.rating }}/5</span>
            </div>
          </div>

          <label class="block">
            <span class="block text-sm font-semibold text-gray-700 mb-1">Nội dung *</span>
            <textarea v-model="editor.form.content" required maxlength="2000" rows="4" class="w-full border rounded-lg px-3 py-2 text-sm resize-y"></textarea>
            <span class="text-xs text-gray-400">{{ editor.form.content.length }}/2000</span>
          </label>

          <div>
            <span class="block text-sm font-semibold text-gray-700 mb-1">Ảnh (tối đa {{ MAX_IMAGES }}, cắt vuông 800×800)</span>
            <div class="flex gap-2 flex-wrap items-center">
              <div v-for="(img, i) in editor.form.images" :key="img" class="relative">
                <img :src="resolveCmsImage(img)" class="w-20 h-20 object-cover rounded border" :alt="`Ảnh ${i + 1}`" />
                <button type="button" class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 text-xs" aria-label="Bỏ ảnh" @click="editor.form.images.splice(i, 1)">×</button>
              </div>
              <label
                v-if="editor.form.images.length < MAX_IMAGES"
                class="w-20 h-20 border-2 border-dashed rounded flex items-center justify-center text-xs text-gray-500 text-center cursor-pointer hover:bg-gray-50"
                :class="{ 'opacity-50 pointer-events-none': uploading || !editor.form.productId }"
              >
                {{ uploading ? 'Đang tải...' : '+ Ảnh' }}
                <input type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="uploadImages" />
              </label>
            </div>
            <p v-if="!editor.form.productId" class="text-xs text-gray-400 mt-1">Chọn sản phẩm trước khi thêm ảnh.</p>
          </div>

          <label class="block">
            <span class="block text-sm font-semibold text-gray-700 mb-1">Trạng thái</span>
            <select v-model="editor.form.status" class="w-full border rounded-lg px-3 py-2 text-sm">
              <option value="APPROVED">Đã duyệt (hiện công khai)</option>
              <option value="PENDING">Chờ duyệt</option>
              <option value="HIDDEN">Đã ẩn</option>
            </select>
          </label>

          <p v-if="editor.error" class="text-sm text-red-600">{{ editor.error }}</p>
        </div>

        <div class="flex justify-end gap-2 p-4 border-t sticky bottom-0 bg-white">
          <button type="button" class="px-4 py-2 border rounded-lg text-sm font-semibold" @click="closeEditor">Huỷ</button>
          <button type="submit" :disabled="editor.saving || uploading" class="px-4 py-2 bg-green-600 text-white rounded-lg text-sm font-semibold disabled:opacity-50">
            {{ editor.saving ? 'Đang lưu...' : 'Lưu' }}
          </button>
        </div>
      </form>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import { reviewService } from '../services/review.service.js'
import { contentService } from '../services/content.service.js'
import { productManagementAPI } from '@/utils/api.js'
import { resolveCmsImage } from '../content/homeDefaults.js'
import { processImageForSlot } from '../utils/imageSlotPipeline.js'

const MAX_IMAGES = 4
const PAGE_SIZE = 20

const route = useRoute()
const router = useRouter()

const products = ref([])
const items = ref([])
const counts = ref({})
const page = ref(0)
const totalPages = ref(0)
const loading = ref(true)
const error = ref('')
const message = ref('')
const messageError = ref(false)
const busyId = ref(null)
const uploading = ref(false)

// Filters live in the URL (?productId=&status=) so links from the product page and reloads keep them
const filters = reactive({
  productId: route.query.productId ? String(route.query.productId) : '',
  status: route.query.status !== undefined ? String(route.query.status) : 'PENDING',
})

const tabs = computed(() => [
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'HIDDEN', label: 'Đã ẩn' },
  { value: '', label: `Tất cả (${Object.values(counts.value).reduce((a, b) => a + b, 0)})` },
])

const STATUS_LABELS = { PENDING: 'Chờ duyệt', APPROVED: 'Đã duyệt', HIDDEN: 'Đã ẩn' }
const statusLabel = (s) => STATUS_LABELS[s] || s
const statusClass = (s) =>
  ({ PENDING: 'bg-yellow-100 text-yellow-800', APPROVED: 'bg-green-100 text-green-700', HIDDEN: 'bg-gray-200 text-gray-600' })[s] ||
  'bg-gray-100 text-gray-600'
const sourceLabel = (s) => ({ CUSTOMER: 'Khách gửi', ADMIN: 'Admin thêm', SEED: 'Mặc định cũ' })[s] || s

function formatDateTime(value) {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

function flash(text, isError = false) {
  message.value = text
  messageError.value = isError
}

async function loadProducts() {
  try {
    const data = await productManagementAPI.getProducts()
    const list = Array.isArray(data) ? data : data?.data || []
    products.value = list.filter((p) => !p.deleted).sort((a, b) => a.id - b.id)
  } catch (_) {
    products.value = []
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await reviewService.getAdminReviews({
      productId: filters.productId || undefined,
      status: filters.status || undefined,
      page: page.value,
      size: PAGE_SIZE,
    })
    items.value = data?.items || []
    counts.value = data?.statusCounts || {}
    totalPages.value = data?.totalPages || 0
  } catch (e) {
    error.value = e.message || 'Không tải được đánh giá'
  } finally {
    loading.value = false
  }
}

function syncQuery() {
  const query = {}
  if (filters.productId) query.productId = filters.productId
  if (filters.status !== 'PENDING') query.status = filters.status
  router.replace({ query })
}

function applyFilters() {
  page.value = 0
  syncQuery()
  load()
}

function setStatus(value) {
  filters.status = value
  applyFilters()
}

function goPage(next) {
  page.value = next
  load()
}

async function changeStatus(review, status) {
  busyId.value = review.id
  try {
    await reviewService.updateReviewStatus(review.id, status)
    flash(`Đã ${status === 'APPROVED' ? 'duyệt' : 'ẩn'} đánh giá của ${review.authorName}.`)
    await load()
  } catch (e) {
    flash(e.message || 'Cập nhật thất bại', true)
  } finally {
    busyId.value = null
  }
}

async function remove(review) {
  if (!window.confirm(`Xoá vĩnh viễn đánh giá của "${review.authorName}"? Không thể hoàn tác. Muốn giữ lại thì dùng "Ẩn".`)) return
  busyId.value = review.id
  try {
    await reviewService.deleteReview(review.id)
    flash('Đã xoá đánh giá.')
    if (items.value.length === 1 && page.value > 0) page.value -= 1
    await load()
  } catch (e) {
    flash(e.message || 'Xoá thất bại', true)
  } finally {
    busyId.value = null
  }
}

// ----- editor -----
const emptyForm = () => ({
  productId: filters.productId || '',
  authorName: '',
  rating: 5,
  content: '',
  images: [],
  status: 'APPROVED',
  updatedAt: null,
})
const editor = reactive({ open: false, id: null, saving: false, error: '', form: emptyForm() })

function openCreate() {
  Object.assign(editor, { open: true, id: null, saving: false, error: '', form: emptyForm() })
}

function openEdit(review) {
  Object.assign(editor, {
    open: true,
    id: review.id,
    saving: false,
    error: '',
    form: {
      productId: String(review.productId),
      authorName: review.authorName,
      rating: review.rating,
      content: review.content,
      images: [...(review.images || [])],
      status: review.status,
      updatedAt: review.updatedAt,
    },
  })
}

function closeEditor() {
  if (editor.saving || uploading.value) return
  editor.open = false
}

async function uploadImages(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  const room = MAX_IMAGES - editor.form.images.length
  if (files.length > room) editor.error = `Chỉ thêm được ${room} ảnh nữa.`
  uploading.value = true
  try {
    for (const file of files.slice(0, Math.max(room, 0))) {
      const processed = await processImageForSlot(file, 'review_image')
      const path = await contentService.uploadCmsImage(processed.file, 'review_image', 'review', editor.form.productId)
      editor.form.images.push(path)
    }
  } catch (e) {
    editor.error = e.message || 'Tải ảnh thất bại'
  } finally {
    uploading.value = false
  }
}

async function save() {
  editor.saving = true
  editor.error = ''
  const payload = {
    productId: Number(editor.form.productId),
    authorName: editor.form.authorName.trim(),
    rating: editor.form.rating,
    content: editor.form.content.trim(),
    images: editor.form.images,
    status: editor.form.status,
    updatedAt: editor.form.updatedAt,
  }
  try {
    if (editor.id) {
      await reviewService.updateReview(editor.id, payload)
      flash('Đã lưu đánh giá.')
    } else {
      await reviewService.createReview(payload)
      flash('Đã thêm đánh giá.')
    }
    editor.open = false
    await load()
  } catch (e) {
    editor.error = e.message || 'Lưu thất bại'
  } finally {
    editor.saving = false
  }
}

// Reacts to sidebar/product-page links that change ?productId= while this page is open
watch(
  () => route.query.productId,
  (value) => {
    const next = value ? String(value) : ''
    if (next !== filters.productId) {
      filters.productId = next
      page.value = 0
      load()
    }
  }
)

onMounted(() => {
  loadProducts()
  load()
})
</script>
