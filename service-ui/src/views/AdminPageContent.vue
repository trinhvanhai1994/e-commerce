<template>
  <AdminLayout>
    <div class="max-w-7xl mx-auto">
      <!-- Top bar -->
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div>
          <h1 class="text-2xl font-bold text-green-700">{{ page.title }}</h1>
          <p class="text-sm text-gray-500">Bấm vào một khối trên bản đồ để sửa đúng vị trí đó trên trang {{ page.path }}. Mỗi khối lưu riêng.</p>
        </div>
        <div class="flex items-center gap-2">
          <div class="inline-flex rounded-lg border bg-white p-0.5 text-sm">
            <button
              v-for="d in devices"
              :key="d.key"
              type="button"
              class="px-3 py-1 rounded-md font-semibold"
              :class="device === d.key ? 'bg-green-600 text-white' : 'text-gray-600'"
              @click="device = d.key"
            >{{ d.label }}</button>
          </div>
          <a :href="page.path" target="_blank" rel="noopener" class="px-3 py-1.5 text-sm rounded-lg border bg-white font-semibold text-gray-700 hover:bg-gray-50">
            Mở trang ↗
          </a>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10 text-gray-500">Đang tải...</div>
      <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded mb-4">{{ error }}</div>

      <div v-else class="grid lg:grid-cols-12 gap-4 items-start">
        <!-- ============ LEFT: page map ============ -->
        <div class="lg:col-span-5 lg:sticky lg:top-20">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-sm font-bold text-gray-700 uppercase tracking-wide">Bản đồ trang</h2>
            <div class="flex items-center gap-3 text-[11px] text-gray-500">
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-amber-500"></span>Chưa lưu</span>
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-gray-400"></span>Đang ẩn</span>
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-blue-400"></span>Cố định / trang khác</span>
            </div>
          </div>

          <div class="bg-gray-200 rounded-2xl p-3 lg:max-h-[calc(100vh-11rem)] lg:overflow-y-auto">
            <div
              class="relative mx-auto rounded-xl shadow-inner overflow-hidden transition-all duration-300"
              style="background-color: #fefce8;"
              :class="device === 'mobile' ? 'max-w-[260px]' : 'w-full'"
            >
              <button
                v-for="block in mapBlocks"
                :key="block.key"
                type="button"
                class="group relative block w-full text-left transition"
                :class="[
                  activeKey === block.key ? 'ring-4 ring-green-500 ring-inset z-10' : 'hover:ring-2 hover:ring-green-300 hover:ring-inset',
                  isHidden(block.key) ? 'opacity-40' : '',
                ]"
                @click="select(block.key)"
              >
                <span
                  class="absolute top-1 left-1 z-20 flex items-center gap-1 text-[10px] font-bold px-1.5 py-0.5 rounded"
                  :class="activeKey === block.key ? 'bg-green-600 text-white' : block.locked ? 'bg-blue-100 text-blue-800' : 'bg-white/90 text-gray-700 shadow'"
                >
                  <span>{{ block.no }}</span><span>{{ block.label }}</span>
                  <span v-if="isDirty(block.key)" class="w-1.5 h-1.5 rounded-full bg-amber-500"></span>
                  <span v-if="isHidden(block.key)" class="ml-0.5 font-semibold">· ẩn</span>
                </span>

                <!-- hero -->
                <div v-if="block.type === 'hero'" class="relative bg-gray-100">
                  <img v-if="previewImage(block.key, ex(block.key))" :src="previewImage(block.key, ex(block.key))" alt="" class="w-full aspect-[2560/1030] object-cover" />
                  <div class="absolute inset-0 flex flex-col items-center justify-center text-center px-2">
                    <div class="text-[11px] font-bold text-white drop-shadow">{{ ex(block.key).title }}</div>
                    <div class="text-[9px] text-white drop-shadow italic">{{ ex(block.key).subtitle }}</div>
                  </div>
                </div>

                <!-- paragraphs -->
                <div v-else-if="block.type === 'paragraphs'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1">{{ ex(block.key).title }}</div>
                  <div v-for="(p, i) in (ex(block.key).paragraphs || []).slice(0, 3)" :key="i" class="text-[8px] text-gray-600 line-clamp-2 mb-0.5">{{ plain(p) }}</div>
                </div>

                <!-- feature -->
                <div v-else-if="block.type === 'feature'" class="p-3 pt-6 flex gap-2" :class="[device === 'mobile' ? 'flex-col' : '', block.imageLeft && device !== 'mobile' ? 'flex-row-reverse' : '']">
                  <div class="flex-1 min-w-0">
                    <div class="text-[9px] font-bold text-gray-800 mb-0.5">{{ ex(block.key).title }}</div>
                    <div v-for="(p, i) in (ex(block.key).paragraphs || []).slice(0, 2)" :key="i" class="text-[8px] text-gray-600 line-clamp-2">{{ plain(p) }}</div>
                  </div>
                  <img v-if="previewImage(block.key, ex(block.key))" :src="previewImage(block.key, ex(block.key))" alt="" class="w-16 h-16 object-cover rounded shrink-0" />
                </div>

                <!-- mission -->
                <div v-else-if="block.type === 'mission'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1">{{ ex(block.key).title }}</div>
                  <div class="bg-gray-50 rounded p-1.5 text-[8px] text-gray-600">
                    <div class="line-clamp-2">{{ plain(ex(block.key).intro) }}</div>
                    <div class="font-bold text-gray-800 mt-0.5">{{ ex(block.key).commitTitle }}</div>
                    <div v-for="(it, i) in ex(block.key).items || []" :key="i" class="truncate">• {{ it }}</div>
                  </div>
                </div>

                <!-- cards with icon image -->
                <div v-else-if="block.type === 'cards'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">{{ ex(block.key).title }}</div>
                  <div class="grid gap-1.5" :class="device === 'mobile' ? 'grid-cols-1' : 'grid-cols-3'">
                    <div v-for="(it, i) in ex(block.key).items || []" :key="i" class="bg-white rounded shadow-sm p-1.5 text-center">
                      <img v-if="previewImage(block.key, it, i)" :src="previewImage(block.key, it, i)" alt="" class="w-7 h-7 mx-auto object-contain" />
                      <div class="text-[8px] font-bold truncate">{{ it.title }}</div>
                      <div class="text-[7px] text-gray-500 line-clamp-2">{{ it.body }}</div>
                    </div>
                  </div>
                </div>

                <!-- highlights (emoji) -->
                <div v-else-if="block.type === 'highlights'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">{{ ex(block.key).title }}</div>
                  <div class="grid gap-1" :class="device === 'mobile' ? 'grid-cols-2' : 'grid-cols-5'">
                    <div v-for="(it, i) in ex(block.key).items || []" :key="i" class="text-center">
                      <div class="w-6 h-6 mx-auto rounded-full bg-green-100 flex items-center justify-center text-[11px]">{{ it.icon }}</div>
                      <div class="text-[7px] font-semibold leading-tight mt-0.5">{{ it.title }}</div>
                    </div>
                  </div>
                </div>

                <!-- article -->
                <div v-else-if="block.type === 'article'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1">{{ ex(block.key).title }}</div>
                  <template v-for="(b, i) in (ex(block.key).blocks || []).slice(0, 8)" :key="i">
                    <div v-if="b.type === 'h4'" class="text-[9px] font-bold text-gray-800 mt-1">{{ b.text }}</div>
                    <div v-else class="text-[8px] text-gray-600 line-clamp-1">{{ plain(b.text) }}</div>
                  </template>
                  <div v-if="(ex(block.key).blocks || []).length > 8" class="text-[8px] text-gray-400">…</div>
                </div>

                <!-- locked (rendered from code) -->
                <div v-else class="p-3 pt-6 bg-blue-50/50">
                  <div class="text-[10px] font-bold text-center text-green-700">{{ block.previewTitle }}</div>
                  <div class="grid grid-cols-3 gap-1 mt-1.5">
                    <div v-for="i in 3" :key="i" class="h-6 rounded bg-white border border-dashed border-blue-200"></div>
                  </div>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- ============ RIGHT: editor ============ -->
        <div class="lg:col-span-7">
          <div v-if="activeBlock" class="bg-white rounded-xl shadow p-4 md:p-6 space-y-4">
            <div class="flex flex-wrap items-start justify-between gap-3 border-b pb-3">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-7 h-7 rounded-full bg-green-600 text-white text-sm font-bold flex items-center justify-center">{{ activeBlock.no }}</span>
                  <h2 class="text-lg font-bold text-gray-800">{{ activeBlock.label }}</h2>
                  <span v-if="isDirty(activeKey)" class="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 font-semibold">Chưa lưu</span>
                </div>
                <p class="text-xs text-gray-500 mt-1">{{ positionText(activeBlock) }}</p>
              </div>
              <label v-if="d && !activeBlock.locked" class="flex items-center gap-2 text-sm font-semibold cursor-pointer select-none">
                <input v-model="d.enabled" type="checkbox" class="w-4 h-4" />
                Hiển thị trên trang
              </label>
            </div>

            <!-- locked -->
            <div v-if="activeBlock.locked" class="space-y-3">
              <p class="text-sm text-gray-700">{{ activeBlock.lockedText }}</p>
              <router-link v-if="activeBlock.linkTo" :to="activeBlock.linkTo" class="inline-block px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold text-sm">
                {{ activeBlock.linkLabel }} →
              </router-link>
            </div>

            <div v-else-if="!d" class="bg-amber-50 text-amber-800 text-sm p-3 rounded">
              Khối này chưa có trong cơ sở dữ liệu. Khởi động lại service-api để seed tạo khối với nội dung đang hiển thị, sau đó tải lại trang.
            </div>

            <template v-else>
              <p v-if="hasParagraphs(activeBlock.type)" class="text-xs text-gray-500 bg-gray-50 rounded px-3 py-2">
                Mẹo: bọc chữ trong <code>**hai dấu sao**</code> để in đậm.
              </p>

              <!-- common title -->
              <label class="block text-sm font-semibold">{{ activeBlock.type === 'hero' ? 'Tiêu đề trên banner' : 'Tiêu đề khối' }}
                <input v-model="d.extra.title" class="mt-1 w-full border rounded-lg px-3 py-2" />
              </label>

              <!-- hero -->
              <template v-if="activeBlock.type === 'hero'">
                <label class="block text-sm font-semibold">Dòng phụ (chữ nghiêng)
                  <input v-model="d.extra.subtitle" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <CmsImageUploader
                  v-model="d.extra.image"
                  v-model:use-default="d.extra.useDefaultImage"
                  :default-src="cmsImg(pageDefaultImage(pageKey, activeKey))"
                  slot-key="page_banner"
                  entity-type="cms"
                  :entity-id="`${pageKey}-hero`"
                  label="Ảnh banner"
                />
              </template>

              <!-- paragraphs / feature -->
              <template v-if="activeBlock.type === 'paragraphs' || activeBlock.type === 'feature'">
                <div class="space-y-2">
                  <div class="text-sm font-semibold">Các đoạn văn</div>
                  <div v-for="(p, idx) in d.extra.paragraphs" :key="idx" class="flex gap-2">
                    <textarea v-model="d.extra.paragraphs[idx]" rows="3" class="flex-1 border rounded-lg px-3 py-2 text-sm" />
                    <ListControls :list="d.extra.paragraphs" :index="idx" />
                  </div>
                  <button type="button" class="text-xs border border-dashed rounded px-3 py-1 text-green-700" @click="d.extra.paragraphs.push('')">+ Thêm đoạn</button>
                </div>
              </template>
              <template v-if="activeBlock.type === 'feature'">
                <div class="grid md:grid-cols-2 gap-3 items-start">
                  <CmsImageUploader
                    v-model="d.extra.image"
                    v-model:use-default="d.extra.useDefaultImage"
                    :default-src="cmsImg(pageDefaultImage(pageKey, activeKey))"
                    slot-key="page_feature"
                    entity-type="cms"
                    :entity-id="`${pageKey}-${activeKey}`"
                    :label="activeBlock.imageLeft ? 'Ảnh bên trái' : 'Ảnh bên phải'"
                  />
                  <label class="block text-sm font-semibold">Mô tả ảnh
                    <input v-model="d.extra.imageAlt" class="mt-1 w-full border rounded-lg px-3 py-2" />
                  </label>
                </div>
              </template>

              <!-- mission -->
              <template v-if="activeBlock.type === 'mission'">
                <label class="block text-sm font-semibold">Đoạn mở đầu
                  <textarea v-model="d.extra.intro" rows="3" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <label class="block text-sm font-semibold">Tiêu đề danh sách
                  <input v-model="d.extra.commitTitle" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <div class="space-y-2">
                  <div class="text-sm font-semibold">Các cam kết</div>
                  <div v-for="(it, idx) in d.extra.items" :key="idx" class="flex gap-2 items-center">
                    <span class="text-gray-400">•</span>
                    <input v-model="d.extra.items[idx]" class="flex-1 border rounded-lg px-3 py-1.5 text-sm" />
                    <ListControls :list="d.extra.items" :index="idx" />
                  </div>
                  <button type="button" class="text-xs border border-dashed rounded px-3 py-1 text-green-700" @click="d.extra.items.push('')">+ Thêm cam kết</button>
                </div>
              </template>

              <!-- cards -->
              <template v-if="activeBlock.type === 'cards'">
                <div v-for="(it, idx) in d.extra.items" :key="idx" class="border rounded-lg p-3 grid md:grid-cols-3 gap-3">
                  <CmsImageUploader
                    v-model="it.image"
                    v-model:use-default="it.useDefaultImage"
                    :default-src="cmsImg(pageDefaultImage(pageKey, activeKey, idx))"
                    slot-key="value_icon"
                    entity-type="cms"
                    :entity-id="`${pageKey}-${activeKey}-${idx}`"
                    :label="`Biểu tượng ${idx + 1}`"
                  />
                  <div class="md:col-span-2 space-y-2">
                    <div class="flex gap-2">
                      <input v-model="it.title" class="flex-1 border rounded px-3 py-2 text-sm font-semibold" placeholder="Tiêu đề thẻ" />
                      <ListControls :list="d.extra.items" :index="idx" />
                    </div>
                    <textarea v-model="it.body" rows="4" class="w-full border rounded px-3 py-2 text-sm" placeholder="Mô tả" />
                  </div>
                </div>
                <button
                  v-if="d.extra.items.length < 6"
                  type="button"
                  class="text-xs border border-dashed rounded px-3 py-1 text-green-700"
                  @click="d.extra.items.push({ title: '', body: '', image: '', useDefaultImage: true })"
                >+ Thêm thẻ</button>
              </template>

              <!-- highlights -->
              <template v-if="activeBlock.type === 'highlights'">
                <div class="space-y-2">
                  <div v-for="(it, idx) in d.extra.items" :key="idx" class="grid grid-cols-12 gap-2 items-center">
                    <input v-model="it.icon" class="col-span-2 border rounded px-2 py-1.5 text-center text-lg" placeholder="🌿" />
                    <input v-model="it.title" class="col-span-4 border rounded px-2 py-1.5 text-sm font-semibold" placeholder="Tiêu đề" />
                    <input v-model="it.subtitle" class="col-span-4 border rounded px-2 py-1.5 text-sm" placeholder="Mô tả ngắn" />
                    <div class="col-span-2"><ListControls :list="d.extra.items" :index="idx" /></div>
                  </div>
                  <button
                    v-if="d.extra.items.length < 8"
                    type="button"
                    class="text-xs border border-dashed rounded px-3 py-1 text-green-700"
                    @click="d.extra.items.push({ icon: '🌿', title: '', subtitle: '' })"
                  >+ Thêm mục</button>
                  <p class="text-[11px] text-gray-400">Biểu tượng là emoji (dán trực tiếp, vd 🌾 ⚖️ 🍵).</p>
                </div>
              </template>

              <!-- article -->
              <template v-if="activeBlock.type === 'article'">
                <div class="space-y-2">
                  <div v-for="(b, idx) in d.extra.blocks" :key="idx" class="border rounded-lg p-2" :class="b.type === 'h4' ? 'bg-green-50/50' : ''">
                    <div class="flex items-center gap-2 mb-1">
                      <select v-model="b.type" class="border rounded px-2 py-1 text-xs">
                        <option value="h4">Tiêu đề nhỏ</option>
                        <option value="p">Đoạn văn</option>
                      </select>
                      <div class="ml-auto"><ListControls :list="d.extra.blocks" :index="idx" /></div>
                    </div>
                    <input v-if="b.type === 'h4'" v-model="b.text" class="w-full border rounded px-3 py-1.5 text-sm font-bold" />
                    <textarea v-else v-model="b.text" rows="3" class="w-full border rounded px-3 py-2 text-sm" />
                  </div>
                  <div class="flex gap-2">
                    <button type="button" class="text-xs border border-dashed rounded px-3 py-1 text-green-700" @click="d.extra.blocks.push({ type: 'h4', text: '' })">+ Tiêu đề nhỏ</button>
                    <button type="button" class="text-xs border border-dashed rounded px-3 py-1 text-green-700" @click="d.extra.blocks.push({ type: 'p', text: '' })">+ Đoạn văn</button>
                  </div>
                </div>
              </template>

              <!-- actions -->
              <div class="flex flex-wrap items-center justify-end gap-2 pt-3 border-t">
                <span v-if="activeSection?.updatedAt" class="text-xs text-gray-400 mr-auto">
                  Lưu lần cuối {{ formatDate(activeSection.updatedAt) }}<template v-if="activeSection.updatedBy"> bởi {{ activeSection.updatedBy }}</template>
                </span>
                <button type="button" class="px-4 py-2 border rounded-lg text-sm disabled:opacity-40" :disabled="saving || !isDirty(activeKey)" @click="revert(activeKey)">
                  Hoàn tác
                </button>
                <button
                  type="button"
                  class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm disabled:opacity-50"
                  :disabled="saving || !isDirty(activeKey)"
                  @click="save(activeKey)"
                >
                  {{ saving ? 'Đang lưu...' : 'Lưu khối này' }}
                </button>
              </div>
              <p class="text-xs text-gray-400 text-right">Sau khi lưu, trang cập nhật trong khoảng 30 giây.</p>
            </template>

            <p v-if="message" class="text-sm" :class="messageError ? 'text-red-600' : 'text-green-600'">{{ message }}</p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, defineComponent, h, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import CmsImageUploader from '../components/cms/CmsImageUploader.vue'
import { contentService } from '../services/content.service.js'
import { resolveCmsImage, resolveSlotImage } from '../content/homeDefaults.js'
import { PAGE_DEFAULTS, pageDefaultImage } from '../content/pageDefaults.js'

const props = defineProps({
  pageKey: { type: String, required: true },
})

/** Blocks in the exact order they render on the public page. `locked` blocks stay in code. */
const PAGES = {
  story: {
    title: 'Câu chuyện Thi Yên',
    path: '/me',
    blocks: [
      { key: 'hero', label: 'Banner', type: 'hero' },
      { key: 'intro', label: 'Mở đầu', type: 'paragraphs' },
      { key: 'feature_1', label: 'Khối ảnh bên phải', type: 'feature' },
      { key: 'feature_2', label: 'Khối ảnh bên trái', type: 'feature', imageLeft: true },
      { key: 'vision', label: 'Tầm nhìn', type: 'paragraphs' },
      { key: 'mission', label: 'Sứ mệnh', type: 'mission' },
      { key: 'values', label: 'Giá trị cốt lõi', type: 'cards' },
      { key: 'nutrition', label: 'Dinh dưỡng', type: 'highlights' },
      {
        key: '__ingredients', label: 'Thành phần dinh dưỡng', locked: true, previewTitle: 'Thành phần dinh dưỡng',
        lockedText: 'Khối tab thành phần, thông tin và bảng dinh dưỡng sản phẩm hiện nằm cố định trong code trang.',
      },
      {
        key: '__products', label: 'Sản phẩm nổi bật', locked: true, previewTitle: 'Sản phẩm nổi bật',
        lockedText: 'Khối giới thiệu 2 sản phẩm và lời kêu gọi mua hàng hiện nằm cố định trong code trang.',
        linkTo: '/admin/products', linkLabel: 'Quản lý sản phẩm',
      },
      {
        key: '__gallery', label: 'Gallery bữa ăn', locked: true, previewTitle: 'BỮA ĂN THAY THẾ THƠM NGON, BỔ DƯỠNG',
        lockedText: 'Gallery ảnh bữa ăn ở cuối trang hiện nằm cố định trong code trang.',
      },
    ],
  },
  philosophy: {
    title: 'Triết lý dưỡng sinh',
    path: '/healthcare',
    blocks: [
      { key: 'hero', label: 'Banner', type: 'hero' },
      { key: 'article', label: 'Bài viết triết lý', type: 'article' },
      { key: 'principles', label: 'Nguyên tắc dưỡng sinh', type: 'cards' },
      { key: 'food', label: 'Dưỡng sinh qua thực phẩm', type: 'highlights' },
    ],
  },
}

/** ▲ ▼ ✕ controls for an item of an array. */
const ListControls = defineComponent({
  props: { list: { type: Array, required: true }, index: { type: Number, required: true } },
  setup(p) {
    const btn = (label, title, onClick, disabled, extraClass = '') =>
      h('button', { type: 'button', title, class: `px-1 text-xs disabled:opacity-30 ${extraClass}`, disabled, onClick }, label)
    const move = (delta) => {
      const target = p.index + delta
      const [item] = p.list.splice(p.index, 1)
      p.list.splice(target, 0, item)
    }
    return () =>
      h('div', { class: 'flex items-center gap-0.5 shrink-0' }, [
        btn('▲', 'Lên', () => move(-1), p.index === 0, 'text-gray-600'),
        btn('▼', 'Xuống', () => move(1), p.index === p.list.length - 1, 'text-gray-600'),
        btn('✕', 'Xoá', () => p.list.splice(p.index, 1), false, 'text-red-600'),
      ])
  },
})

const devices = [
  { key: 'desktop', label: 'Máy tính' },
  { key: 'mobile', label: 'Điện thoại' },
]

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const message = ref('')
const messageError = ref(false)
const device = ref('desktop')
const activeKey = ref('hero')
const sections = ref([])
const drafts = reactive({})
const snapshots = reactive({})

const page = computed(() => PAGES[props.pageKey] || { title: 'Trang', path: '/', blocks: [] })
const mapBlocks = computed(() => page.value.blocks.map((b, i) => ({ ...b, no: i + 1 })))
const activeBlock = computed(() => mapBlocks.value.find((b) => b.key === activeKey.value))
const activeSection = computed(() => sections.value.find((s) => s.sectionKey === activeKey.value))
const d = computed(() => drafts[activeKey.value])

const clone = (v) => JSON.parse(JSON.stringify(v ?? null))
const cmsImg = (path) => resolveCmsImage(path || '')
const plain = (text) => String(text ?? '').replace(/\*\*/g, '')
const hasParagraphs = (type) => ['paragraphs', 'feature', 'mission', 'article'].includes(type)

/** Extra of a block for the map: the draft when loaded, else the packaged default. */
const ex = (key) => drafts[key]?.extra || PAGE_DEFAULTS[props.pageKey]?.[key]?.extraJson || {}

function previewImage(sectionKey, obj, index = null) {
  return resolveSlotImage({
    useDefault: obj?.useDefaultImage,
    path: obj?.image,
    defaultPath: pageDefaultImage(props.pageKey, sectionKey, index),
  })
}

function normalize(section) {
  const block = page.value.blocks.find((b) => b.key === section.sectionKey)
  const extra = clone(section.extraJson || {})
  const withImage = (o) => ({ ...o, image: o.image || '', useDefaultImage: o.useDefaultImage !== false })
  if (block?.type === 'hero' || block?.type === 'feature') Object.assign(extra, withImage(extra))
  if (['paragraphs', 'feature'].includes(block?.type)) extra.paragraphs = extra.paragraphs || []
  if (block?.type === 'mission') extra.items = extra.items || []
  if (block?.type === 'cards') extra.items = (extra.items || []).map(withImage)
  if (block?.type === 'highlights') extra.items = extra.items || []
  if (block?.type === 'article') extra.blocks = extra.blocks || []
  return { enabled: section.enabled !== false, extra }
}

/** Images: "Dùng ảnh mặc định" clears the stored path; empty strings become null. */
function buildExtra(extra) {
  const fix = (o) => (o && 'useDefaultImage' in o ? { ...o, image: o.useDefaultImage !== false ? null : o.image || null } : o)
  const out = fix(clone(extra))
  if (Array.isArray(out.items)) out.items = out.items.map((it) => (typeof it === 'object' ? fix(it) : it))
  if (Array.isArray(out.paragraphs)) out.paragraphs = out.paragraphs.filter((p) => String(p).trim())
  if (Array.isArray(out.blocks)) out.blocks = out.blocks.filter((b) => String(b.text || '').trim())
  return out
}

function resetDraft(section) {
  const draft = normalize(section)
  drafts[section.sectionKey] = draft
  snapshots[section.sectionKey] = JSON.stringify(draft)
}

const isDirty = (key) => !!drafts[key] && JSON.stringify(drafts[key]) !== snapshots[key]
const anyDirty = computed(() => Object.keys(drafts).some(isDirty))
const isHidden = (key) => drafts[key]?.enabled === false

function revert(key) {
  if (snapshots[key]) drafts[key] = JSON.parse(snapshots[key])
}

function select(key) {
  activeKey.value = key
  message.value = ''
}

function positionText(block) {
  const list = mapBlocks.value
  const prev = list[block.no - 2]
  const next = list[block.no]
  if (!prev) return `Vị trí ${block.no}: đầu trang.`
  if (!next) return `Vị trí ${block.no}: cuối trang, sau "${prev.label}".`
  return `Vị trí ${block.no}: nằm giữa "${prev.label}" và "${next.label}".`
}

function formatDate(value) {
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

async function load() {
  loading.value = true
  error.value = ''
  Object.keys(drafts).forEach((k) => delete drafts[k])
  Object.keys(snapshots).forEach((k) => delete snapshots[k])
  activeKey.value = page.value.blocks[0]?.key || 'hero'
  try {
    sections.value = await contentService.getAdminPageSections(props.pageKey)
    sections.value.forEach(resetDraft)
  } catch (e) {
    error.value = e.message || 'Không tải được nội dung trang'
  } finally {
    loading.value = false
  }
}

async function save(key) {
  const section = sections.value.find((s) => s.sectionKey === key)
  const draft = drafts[key]
  if (!section || !draft) return
  saving.value = true
  message.value = ''
  messageError.value = false
  try {
    const updated = await contentService.updatePageSection(props.pageKey, key, {
      enabled: draft.enabled,
      sortOrder: section.sortOrder,
      useDefaultDesktop: true,
      useDefaultMobile: true,
      extraJson: buildExtra(draft.extra),
      updatedAt: section.updatedAt,
    })
    const idx = sections.value.findIndex((s) => s.sectionKey === updated.sectionKey)
    if (idx >= 0) sections.value[idx] = updated
    resetDraft(updated)
    message.value = `Đã lưu "${activeBlock.value?.label || key}".`
  } catch (e) {
    messageError.value = true
    message.value = e.message || 'Lưu thất bại'
  } finally {
    saving.value = false
  }
}

function onBeforeUnload(event) {
  if (!anyDirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onBeforeRouteLeave(() => {
  if (!anyDirty.value) return true
  return window.confirm('Có khối chưa lưu. Rời trang sẽ mất các thay đổi này. Tiếp tục?')
})

// Same component serves both pages; switching via the sidebar only changes the prop.
watch(
  () => props.pageKey,
  (next, prev) => {
    if (prev && anyDirty.value && !window.confirm('Có khối chưa lưu. Chuyển trang sẽ mất các thay đổi này. Tiếp tục?')) return
    load()
  }
)

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  load()
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', onBeforeUnload))
</script>
