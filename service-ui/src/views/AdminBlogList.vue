<template>
  <AdminLayout>
    <div class="max-w-6xl mx-auto">
      <div class="flex items-center justify-between mb-4 gap-3 flex-wrap">
        <div>
          <h1 class="text-2xl font-bold text-green-700">Bài viết</h1>
          <p class="text-sm text-gray-500">Bài "Đã xuất bản" hiện ở trang Blog. Tối đa 3 bài được chọn hiện thêm ở Trang chủ.</p>
        </div>
        <div class="flex items-center gap-2">
          <a href="/blog" target="_blank" rel="noopener" class="px-3 py-1.5 text-sm rounded-lg border bg-white font-semibold text-gray-700 hover:bg-gray-50">
            Mở trang Blog ↗
          </a>
          <router-link to="/admin/posts/new" class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm">
            + Tạo bài viết
          </router-link>
        </div>
      </div>

      <div v-if="loading" class="py-10 text-center text-gray-500">Đang tải...</div>
      <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded">{{ error }}</div>

      <template v-else>
        <p v-if="message" class="mb-3 text-sm" :class="messageError ? 'text-red-600' : 'text-green-600'">{{ message }}</p>

        <!-- Home slots -->
        <section class="bg-white rounded-xl shadow p-4 mb-4">
          <div class="flex items-center justify-between mb-3">
            <h2 class="text-sm font-bold text-gray-700 uppercase tracking-wide">Trang chủ hiển thị</h2>
            <span class="text-xs text-gray-500">Khối "Bài viết nổi bật", từ trái sang phải</span>
          </div>
          <div class="grid sm:grid-cols-3 gap-3">
            <div v-for="cell in homeSlots" :key="cell.no" class="border rounded-lg p-2 flex gap-3 items-center" :class="cell.post ? '' : 'border-dashed'">
              <span class="w-7 h-7 shrink-0 rounded-full text-sm font-bold flex items-center justify-center" :class="cell.post ? 'bg-green-600 text-white' : 'bg-gray-100 text-gray-400'">{{ cell.no }}</span>
              <template v-if="cell.post">
                <img v-if="cover(cell.post)" :src="cover(cell.post)" alt="" class="w-16 aspect-[16/10] object-cover rounded shrink-0" />
                <div v-else class="w-16 aspect-[16/10] rounded bg-gray-100 shrink-0"></div>
                <div class="min-w-0">
                  <router-link :to="`/admin/posts/${cell.post.id}`" class="text-sm font-semibold text-gray-800 hover:text-green-700 line-clamp-2">{{ cell.post.title }}</router-link>
                  <span v-if="cell.post.status !== 'PUBLISHED'" class="text-[11px] text-amber-700">Nháp, chưa hiện trên Trang chủ</span>
                </div>
              </template>
              <span v-else class="text-sm text-gray-400">Trống</span>
            </div>
          </div>
          <p v-if="!homeSlots.some((c) => c.post && c.post.status === 'PUBLISHED')" class="mt-2 text-xs text-gray-500">
            Chưa có bài nào hiện trên Trang chủ nên khối "Bài viết nổi bật" đang ẩn.
          </p>
        </section>

        <!-- Post list -->
        <div class="bg-white rounded-xl shadow overflow-hidden">
          <div v-if="!sortedPosts.length" class="p-8 text-center text-gray-500 text-sm">Chưa có bài viết nào.</div>

          <!-- Desktop table -->
          <table v-if="sortedPosts.length" class="hidden md:table w-full text-sm">
            <thead class="bg-gray-50 text-left text-xs uppercase text-gray-500">
              <tr>
                <th class="p-3 w-24"></th>
                <th class="p-3">Bài viết</th>
                <th class="p-3">Trạng thái</th>
                <th class="p-3">Trang chủ</th>
                <th class="p-3">Cập nhật</th>
                <th class="p-3"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="post in sortedPosts" :key="post.id" class="border-t hover:bg-gray-50/60">
                <td class="p-3">
                  <img v-if="cover(post)" :src="cover(post)" alt="" class="w-20 aspect-[16/10] object-cover rounded" />
                  <div v-else class="w-20 aspect-[16/10] rounded bg-gray-100"></div>
                </td>
                <td class="p-3">
                  <router-link :to="`/admin/posts/${post.id}`" class="font-semibold text-gray-800 hover:text-green-700 line-clamp-2">{{ post.title }}</router-link>
                  <div class="text-xs text-gray-400">/blog/{{ post.slug }}</div>
                </td>
                <td class="p-3">
                  <span class="px-2 py-0.5 rounded-full text-xs font-semibold whitespace-nowrap" :class="statusClass(post)">{{ statusLabel(post) }}</span>
                </td>
                <td class="p-3 whitespace-nowrap">
                  <span v-if="post.showOnHome && post.homeOrder" class="text-xs font-semibold text-green-700">Vị trí {{ post.homeOrder }}</span>
                  <span v-else class="text-gray-300">Không</span>
                </td>
                <td class="p-3 text-xs text-gray-500 whitespace-nowrap">{{ formatDateTime(post.updatedAt) }}</td>
                <td class="p-3 text-right whitespace-nowrap space-x-3">
                  <router-link :to="`/admin/posts/${post.id}`" class="text-blue-600 font-semibold">Sửa</router-link>
                  <a v-if="post.status === 'PUBLISHED'" :href="`/blog/${post.slug}`" target="_blank" rel="noopener" class="text-gray-600 font-semibold hover:text-green-700">Xem trên web ↗</a>
                  <button type="button" class="text-red-600 font-semibold disabled:opacity-40" :disabled="removingId === post.id" @click="remove(post)">Xoá</button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Mobile cards -->
          <div class="md:hidden divide-y">
            <div v-for="post in sortedPosts" :key="post.id" class="p-3 flex gap-3">
              <img v-if="cover(post)" :src="cover(post)" alt="" class="w-20 aspect-[16/10] object-cover rounded shrink-0" />
              <div v-else class="w-20 aspect-[16/10] rounded bg-gray-100 shrink-0"></div>
              <div class="min-w-0 flex-1">
                <router-link :to="`/admin/posts/${post.id}`" class="font-semibold text-sm text-gray-800 line-clamp-2">{{ post.title }}</router-link>
                <div class="text-xs text-gray-400 truncate">/blog/{{ post.slug }}</div>
                <div class="flex flex-wrap items-center gap-2 mt-1 text-xs">
                  <span class="px-2 py-0.5 rounded-full font-semibold" :class="statusClass(post)">{{ statusLabel(post) }}</span>
                  <span v-if="post.showOnHome && post.homeOrder" class="font-semibold text-green-700">Trang chủ: vị trí {{ post.homeOrder }}</span>
                  <span class="text-gray-400">{{ formatDateTime(post.updatedAt) }}</span>
                </div>
                <div class="flex gap-3 mt-2 text-sm">
                  <router-link :to="`/admin/posts/${post.id}`" class="text-blue-600 font-semibold">Sửa</router-link>
                  <a v-if="post.status === 'PUBLISHED'" :href="`/blog/${post.slug}`" target="_blank" rel="noopener" class="text-gray-600 font-semibold">Xem trên web ↗</a>
                  <button type="button" class="text-red-600 font-semibold disabled:opacity-40" :disabled="removingId === post.id" @click="remove(post)">Xoá</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminLayout from './AdminLayout.vue'
import { contentService } from '../services/content.service.js'
import { resolveCmsImage } from '../content/homeDefaults.js'

const posts = ref([])
const loading = ref(true)
const error = ref('')
const message = ref('')
const messageError = ref(false)
const removingId = ref(null)

const cover = (post) => resolveCmsImage(post.coverImage || '')

const sortedPosts = computed(() =>
  [...posts.value].sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
)

const homeSlots = computed(() =>
  [1, 2, 3].map((no) => ({
    no,
    post: posts.value.find((p) => p.showOnHome && p.homeOrder === no) || null,
  }))
)

const statusLabel = (post) => (post.status === 'PUBLISHED' ? 'Đã xuất bản' : 'Nháp')
const statusClass = (post) =>
  post.status === 'PUBLISHED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-800'

function formatDateTime(value) {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    posts.value = (await contentService.getAdminPosts()) || []
  } catch (e) {
    error.value = e.message || 'Không tải được bài viết'
  } finally {
    loading.value = false
  }
}

async function remove(post) {
  const extra = post.showOnHome ? ' Bài cũng sẽ bị gỡ khỏi Trang chủ.' : ''
  if (!window.confirm(`Xoá bài "${post.title}"? Bài sẽ bị ẩn khỏi trang web và danh sách này.${extra}`)) return
  removingId.value = post.id
  message.value = ''
  messageError.value = false
  try {
    await contentService.softDeletePost(post.id)
    message.value = `Đã xoá "${post.title}".`
    await load()
  } catch (e) {
    messageError.value = true
    message.value = e.message || 'Xoá thất bại'
  } finally {
    removingId.value = null
  }
}

onMounted(load)
</script>
