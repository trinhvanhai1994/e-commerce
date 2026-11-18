<template>
  <AdminLayout>
    <div class="max-w-4xl mx-auto">
      <div class="flex items-start justify-between mb-4 gap-3 flex-wrap">
        <div>
          <h1 class="text-2xl font-bold text-green-700">Danh mục sản phẩm</h1>
          <p class="text-sm text-gray-500">
            Danh mục dùng khi tạo, sửa sản phẩm và làm nút lọc ở trang Sản phẩm. Danh mục "Ẩn" vẫn chọn được cho sản phẩm
            nhưng không hiện nút lọc. Chỉ xoá được danh mục không còn sản phẩm.
          </p>
        </div>
        <button type="button" class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm" @click="openCreate">
          + Thêm danh mục
        </button>
      </div>

      <p v-if="message" class="mb-3 text-sm" :class="messageError ? 'text-red-600' : 'text-green-600'" role="status">{{ message }}</p>

      <div v-if="loading" class="py-10 text-center text-gray-500">Đang tải...</div>
      <div v-else-if="loadError" class="py-10 text-center">
        <p class="text-red-600 mb-3">{{ loadError }}</p>
        <button type="button" class="px-4 py-2 border rounded-lg text-sm font-semibold" @click="load">Thử lại</button>
      </div>
      <div v-else-if="!categories.length" class="py-10 text-center text-gray-500">Chưa có danh mục nào.</div>

      <div v-else class="bg-white rounded-xl shadow overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-green-50 text-green-800">
            <tr>
              <th class="px-3 py-2 text-left font-bold w-16">Thứ tự</th>
              <th class="px-3 py-2 text-left font-bold">Tên danh mục</th>
              <th class="px-3 py-2 text-left font-bold hidden sm:table-cell">Mã</th>
              <th class="px-3 py-2 text-center font-bold">Sản phẩm</th>
              <th class="px-3 py-2 text-center font-bold">Trang khách</th>
              <th class="px-3 py-2 text-right font-bold">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="cat in categories" :key="cat.id" class="border-t">
              <td class="px-3 py-2 text-gray-600">{{ cat.sortOrder }}</td>
              <td class="px-3 py-2 font-semibold text-gray-800">{{ cat.name }}</td>
              <td class="px-3 py-2 hidden sm:table-cell"><code class="text-xs text-gray-500">{{ cat.code }}</code></td>
              <td class="px-3 py-2 text-center">{{ cat.productCount ?? 0 }}</td>
              <td class="px-3 py-2 text-center">
                <span
                  class="inline-block px-2 py-0.5 rounded-full text-xs font-semibold"
                  :class="cat.visible ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'"
                >{{ cat.visible ? 'Hiện' : 'Ẩn' }}</span>
              </td>
              <td class="px-3 py-2 text-right whitespace-nowrap">
                <button type="button" class="px-2 py-1 text-blue-600 hover:underline" @click="openEdit(cat)">Sửa</button>
                <button
                  type="button"
                  class="px-2 py-1 text-red-600 hover:underline disabled:text-gray-300 disabled:no-underline disabled:cursor-not-allowed"
                  :disabled="cat.productCount > 0 || busyId === cat.id"
                  :title="cat.productCount > 0 ? 'Còn sản phẩm trong danh mục, chuyển sản phẩm sang danh mục khác trước' : 'Xoá danh mục'"
                  @click="remove(cat)"
                >Xoá</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create / edit dialog -->
    <div
      v-if="showForm"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      @click.self="closeForm"
      @keydown.esc="closeForm"
    >
      <form
        class="bg-white rounded-xl shadow-xl w-full max-w-md p-5"
        role="dialog"
        aria-modal="true"
        aria-labelledby="category-form-title"
        @submit.prevent="save"
      >
        <h2 id="category-form-title" class="text-lg font-bold text-green-700 mb-4">
          {{ editing ? 'Sửa danh mục' : 'Thêm danh mục' }}
        </h2>

        <label class="block mb-3">
          <span class="block font-semibold mb-1 text-gray-700">Tên danh mục <span class="text-red-500">*</span></span>
          <input
            ref="nameInput"
            v-model="form.name"
            type="text"
            maxlength="100"
            required
            class="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
            placeholder="Ví dụ: Trà thảo mộc"
          />
          <span class="block text-xs text-gray-500 mt-1">
            Mã: <code>{{ editing ? editing.code : (codePreview || '...') }}</code>
            {{ editing ? '(không đổi được)' : '(tự sinh từ tên, không đổi sau khi tạo)' }}
          </span>
        </label>

        <label class="block mb-3">
          <span class="block font-semibold mb-1 text-gray-700">Thứ tự hiển thị</span>
          <input
            v-model.number="form.sortOrder"
            type="number"
            min="0"
            max="9999"
            class="w-full border rounded-lg px-3 py-2 focus:ring-2 focus:ring-green-500 focus:border-transparent"
            placeholder="Để trống: xếp cuối"
          />
        </label>

        <label class="flex items-center gap-2 mb-4 text-gray-700">
          <input v-model="form.visible" type="checkbox" class="rounded text-green-600 focus:ring-green-500" />
          Hiện nút lọc ở trang Sản phẩm
        </label>

        <p v-if="formError" class="mb-3 text-sm text-red-600" role="alert">{{ formError }}</p>

        <div class="flex justify-end gap-2">
          <button type="button" class="px-4 py-2 border rounded-lg text-sm font-semibold" @click="closeForm">Huỷ</button>
          <button
            type="submit"
            class="px-4 py-2 bg-green-600 text-white rounded-lg text-sm font-semibold disabled:opacity-60"
            :disabled="saving || !form.name.trim()"
          >{{ saving ? 'Đang lưu...' : 'Lưu' }}</button>
        </div>
      </form>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import AdminLayout from './AdminLayout.vue'
import { categoryService } from '../services/category.service.js'

const categories = ref([])
const loading = ref(true)
const loadError = ref('')
const message = ref('')
const messageError = ref(false)
const busyId = ref(null)

const showForm = ref(false)
const editing = ref(null)
const saving = ref(false)
const formError = ref('')
const nameInput = ref(null)
const form = reactive({ name: '', sortOrder: null, visible: true })

// Mirrors ProductCategoryService.generateCode (the server decides; this is only a preview)
const codePreview = computed(() =>
  form.name
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .normalize('NFD')
    .replace(/\p{M}+/gu, '')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 50)
)

function notify(text, isError = false) {
  message.value = text
  messageError.value = isError
}

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    categories.value = await categoryService.getAdminCategories()
  } catch (err) {
    loadError.value = err.message || 'Không tải được danh mục'
  } finally {
    loading.value = false
  }
}

async function openForm(cat) {
  editing.value = cat
  form.name = cat ? cat.name : ''
  form.sortOrder = cat ? cat.sortOrder : null
  form.visible = cat ? cat.visible : true
  formError.value = ''
  showForm.value = true
  await nextTick()
  nameInput.value?.focus()
}

const openCreate = () => openForm(null)
const openEdit = (cat) => openForm(cat)

function closeForm() {
  if (saving.value) return
  showForm.value = false
}

async function save() {
  saving.value = true
  formError.value = ''
  const payload = {
    name: form.name.trim(),
    sortOrder: form.sortOrder === '' || form.sortOrder === null ? null : Number(form.sortOrder),
    visible: form.visible,
  }
  try {
    if (editing.value) {
      await categoryService.updateCategory(editing.value.id, { ...payload, updatedAt: editing.value.updatedAt })
      notify(`Đã cập nhật danh mục "${payload.name}"`)
    } else {
      const created = await categoryService.createCategory(payload)
      notify(`Đã thêm danh mục "${created.name}" (mã ${created.code})`)
    }
    saving.value = false
    showForm.value = false
    await load()
  } catch (err) {
    formError.value = err.message || 'Lưu không thành công'
    saving.value = false
  }
}

async function remove(cat) {
  if (!window.confirm(`Xoá danh mục "${cat.name}"? Thao tác này không hoàn tác được.`)) return
  busyId.value = cat.id
  try {
    await categoryService.deleteCategory(cat.id)
    notify(`Đã xoá danh mục "${cat.name}"`)
    await load()
  } catch (err) {
    notify(err.message || 'Xoá không thành công', true)
  } finally {
    busyId.value = null
  }
}

onMounted(load)
</script>
