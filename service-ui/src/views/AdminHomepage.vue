<template>
  <AdminLayout>
    <div class="max-w-7xl mx-auto">
      <!-- Top bar -->
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div>
          <h1 class="text-2xl font-bold text-green-700">Quản lý Trang chủ</h1>
          <p class="text-sm text-gray-500">Bấm vào một khối trên bản đồ để sửa đúng vị trí đó trên trang chủ. Mỗi khối lưu riêng.</p>
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
          <a href="/" target="_blank" rel="noopener" class="px-3 py-1.5 text-sm rounded-lg border bg-white font-semibold text-gray-700 hover:bg-gray-50">
            Mở trang chủ ↗
          </a>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10 text-gray-500">Đang tải...</div>
      <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded mb-4">{{ error }}</div>

      <div v-else class="grid lg:grid-cols-12 gap-4 items-start">
        <!-- ============ LEFT: page map ============ -->
        <div class="lg:col-span-5 lg:sticky lg:top-20">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-sm font-bold text-gray-700 uppercase tracking-wide">Bản đồ trang chủ</h2>
            <div class="flex items-center gap-3 text-[11px] text-gray-500">
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-amber-500"></span>Chưa lưu</span>
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-gray-400"></span>Đang ẩn</span>
              <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-blue-400"></span>Quản lý ở trang khác</span>
            </div>
          </div>

          <div class="bg-gray-200 rounded-2xl p-3 lg:max-h-[calc(100vh-11rem)] lg:overflow-y-auto">
            <div
              class="relative mx-auto bg-white rounded-xl shadow-inner overflow-hidden transition-all duration-300"
              :class="device === 'mobile' ? 'max-w-[260px]' : 'w-full'"
            >
              <button
                v-for="block in mapBlocks"
                :key="block.key"
                type="button"
                class="map-block group relative block w-full text-left transition"
                :class="[
                  activeKey === block.key ? 'ring-4 ring-green-500 ring-inset z-10' : 'hover:ring-2 hover:ring-green-300 hover:ring-inset',
                  isHidden(block.key) ? 'opacity-40' : '',
                ]"
                @click="select(block.key)"
              >
                <!-- number + label badge -->
                <span
                  class="absolute top-1 left-1 z-20 flex items-center gap-1 text-[10px] font-bold px-1.5 py-0.5 rounded"
                  :class="activeKey === block.key ? 'bg-green-600 text-white' : block.linked ? 'bg-blue-100 text-blue-800' : 'bg-white/90 text-gray-700 shadow'"
                >
                  <span>{{ block.no }}</span><span>{{ block.label }}</span>
                  <span v-if="isDirty(block.key)" class="w-1.5 h-1.5 rounded-full bg-amber-500"></span>
                  <span v-if="isHidden(block.key)" class="ml-0.5 font-semibold">· ẩn</span>
                </span>

                <!-- Header -->
                <div v-if="block.key === 'header_logo'" class="flex items-center gap-2 px-3 pt-6 pb-2 border-b bg-white">
                  <img v-if="preview.logo" :src="preview.logo" alt="" class="h-7 w-7 object-contain" />
                  <div class="flex-1 flex gap-1.5 justify-end">
                    <span v-for="i in (device === 'mobile' ? 2 : 5)" :key="i" class="h-1.5 w-8 rounded bg-gray-200"></span>
                  </div>
                </div>

                <!-- Hero -->
                <div v-else-if="block.key === 'hero'" class="bg-gray-100">
                  <img
                    v-if="device === 'mobile' ? preview.heroMobile : preview.heroDesktop"
                    :src="device === 'mobile' ? preview.heroMobile : preview.heroDesktop"
                    alt=""
                    class="w-full object-cover"
                    :class="device === 'mobile' ? 'aspect-[4/5]' : 'aspect-[21/9]'"
                  />
                  <div v-else class="aspect-[21/9] flex items-center justify-center text-xs text-gray-400">Chưa có ảnh banner</div>
                </div>

                <!-- Story -->
                <div v-else-if="block.key === 'story'" class="grid gap-2 p-3 pt-6" :class="device === 'mobile' ? 'grid-cols-1' : 'grid-cols-5'">
                  <div :class="device === 'mobile' ? '' : 'col-span-3'">
                    <div class="text-[10px] font-bold text-green-700 leading-tight whitespace-pre-line line-clamp-2">{{ draftOf('story')?.title }}</div>
                    <div class="text-[9px] text-gray-500 leading-snug line-clamp-3 mt-1 whitespace-pre-line">{{ draftOf('story')?.body }}</div>
                    <span v-if="draftOf('story')?.ctaText" class="inline-block mt-1.5 text-[9px] bg-green-500 text-white rounded-full px-2 py-0.5">{{ draftOf('story')?.ctaText }}</span>
                  </div>
                  <div :class="device === 'mobile' ? '' : 'col-span-2'">
                    <img v-if="preview.story" :src="preview.story" alt="" class="w-full aspect-[4/5] object-cover rounded" />
                  </div>
                </div>

                <!-- Products (linked) -->
                <div v-else-if="block.key === '__products'" class="p-3 pt-6 bg-blue-50/40">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">SẢN PHẨM BÁN CHẠY</div>
                  <div class="grid gap-1.5" :class="device === 'mobile' ? 'grid-cols-2' : 'grid-cols-4'">
                    <div v-for="i in 4" :key="i" class="aspect-square rounded bg-white border border-dashed border-blue-200"></div>
                  </div>
                </div>

                <!-- Quality -->
                <div v-else-if="block.key === 'quality'" class="p-3 pt-6">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">{{ draftOf('quality')?.title }}</div>
                  <div v-for="(item, idx) in preview.qualityItems" :key="idx" class="flex items-center gap-2 mb-1">
                    <img v-if="item.icon" :src="item.icon" alt="" class="w-6 h-6 object-contain" />
                    <div class="flex-1 min-w-0">
                      <div class="text-[9px] font-bold truncate">{{ item.title }}</div>
                      <div class="text-[8px] text-gray-500 truncate">{{ item.body }}</div>
                    </div>
                  </div>
                </div>

                <!-- Meal gallery -->
                <div v-else-if="block.key === 'meal_gallery'" class="p-3 pt-6">
                  <img v-if="preview.mealBanner" :src="preview.mealBanner" alt="" class="w-full rounded mb-1.5 aspect-video object-cover" />
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">{{ draftOf('meal_gallery')?.extra?.title }}</div>
                  <div class="grid grid-cols-4 gap-1">
                    <div v-for="(src, idx) in preview.mealTiles" :key="idx" class="aspect-square rounded bg-gray-100 overflow-hidden">
                      <img v-if="src" :src="src" alt="" class="w-full h-full object-cover" />
                    </div>
                  </div>
                </div>

                <!-- Featured posts (linked) -->
                <div v-else-if="block.key === '__posts'" class="p-3 pt-6 bg-blue-50/40">
                  <div class="text-[10px] font-bold text-center text-green-700 mb-1.5">BÀI VIẾT NỔI BẬT</div>
                  <div class="grid gap-1.5" :class="device === 'mobile' ? 'grid-cols-1' : 'grid-cols-3'">
                    <div v-for="post in featuredPosts" :key="post.id" class="bg-white rounded border p-1">
                      <img v-if="post.coverImage" :src="cmsImg(post.coverImage)" alt="" class="w-full aspect-[16/10] object-cover rounded" />
                      <div class="text-[8px] font-semibold mt-0.5 line-clamp-2">{{ post.title }}</div>
                    </div>
                    <div v-if="!featuredPosts.length" class="text-[9px] text-gray-500 text-center py-2 col-span-3">Chưa có bài nào hiện trên Home (khối sẽ ẩn)</div>
                  </div>
                </div>

                <!-- Footer -->
                <div v-else-if="block.key === 'footer'" class="p-3 pt-6 bg-yellow-50 border-t border-yellow-200">
                  <div class="grid gap-2 text-[8px] text-gray-600" :class="device === 'mobile' ? 'grid-cols-1' : 'grid-cols-3'">
                    <div class="bg-white/70 rounded p-1.5 text-center">
                      <img v-if="preview.logo" :src="preview.logo" alt="" class="h-5 mx-auto mb-0.5 object-contain" />
                      <div class="whitespace-pre-line line-clamp-2">{{ footerDraft.address }}</div>
                      <div class="font-bold text-green-700">{{ footerDraft.phone }}</div>
                    </div>
                    <div class="bg-white/70 rounded p-1.5 text-center">
                      <div class="font-bold text-green-700 mb-0.5">{{ footerDraft.policyTitle }}</div>
                      <div v-for="(l, i) in footerDraft.policyLinks" :key="i" class="truncate">{{ l.label }}</div>
                    </div>
                    <div class="bg-white/70 rounded p-1.5 text-center">
                      <div class="font-bold text-green-700 mb-0.5">{{ footerDraft.contactTitle }}</div>
                      <div class="bg-green-100 rounded px-1">HOTLINE: {{ footerDraft.hotline }}</div>
                    </div>
                  </div>
                  <div class="flex justify-center gap-1 mt-1.5">
                    <span v-for="(s, i) in footerDraft.socials" :key="i" class="text-[8px] bg-white border rounded px-1.5">{{ s.label }}</span>
                  </div>
                  <div class="text-center text-[8px] text-gray-500 mt-1">{{ footerDraft.copyright }}</div>
                </div>

                <!-- Floating chat: rendered as a pinned strip -->
                <div v-else-if="block.key === 'floating_chat'" class="flex items-center justify-end gap-1.5 px-3 pt-6 pb-2 bg-gray-50 border-t border-dashed">
                  <span class="text-[9px] text-gray-500 mr-auto">Nổi cố định bên phải màn hình</span>
                  <span class="w-5 h-5 rounded-full bg-blue-500"></span>
                  <span class="w-5 h-5 rounded-full bg-green-500 text-[6px] text-white font-bold flex items-center justify-center">ZALO</span>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- ============ RIGHT: editor ============ -->
        <div class="lg:col-span-7">
          <div v-if="activeBlock" class="bg-white rounded-xl shadow p-4 md:p-6 space-y-4">
            <!-- Editor header -->
            <div class="flex flex-wrap items-start justify-between gap-3 border-b pb-3">
              <div>
                <div class="flex items-center gap-2">
                  <span class="w-7 h-7 rounded-full bg-green-600 text-white text-sm font-bold flex items-center justify-center">{{ activeBlock.no }}</span>
                  <h2 class="text-lg font-bold text-gray-800">{{ activeBlock.label }}</h2>
                  <span v-if="isDirty(activeKey)" class="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 font-semibold">Chưa lưu</span>
                </div>
                <p class="text-xs text-gray-500 mt-1">{{ positionText(activeBlock) }}</p>
                <p v-if="activeBlock.hint" class="text-xs text-gray-500">{{ activeBlock.hint }}</p>
              </div>
              <label v-if="activeDraft && activeBlock.toggle !== false" class="flex items-center gap-2 text-sm font-semibold cursor-pointer select-none">
                <input v-model="activeDraft.enabled" type="checkbox" class="w-4 h-4" />
                Hiển thị trên trang
              </label>
            </div>

            <!-- Linked blocks (managed elsewhere) -->
            <div v-if="activeBlock.linked" class="space-y-3">
              <p class="text-sm text-gray-700">{{ activeBlock.linkedText }}</p>
              <router-link :to="activeBlock.linkTo" class="inline-block px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold text-sm">
                {{ activeBlock.linkLabel }} →
              </router-link>
            </div>

            <div v-else-if="!activeDraft" class="bg-amber-50 text-amber-800 text-sm p-3 rounded">
              Khối này chưa có trong cơ sở dữ liệu. Khởi động lại service-api để seed tạo khối với nội dung đang hiển thị, sau đó tải lại trang.
            </div>

            <template v-else>
              <!-- header_logo -->
              <div v-if="activeKey === 'header_logo'" class="max-w-xs">
                <CmsImageUploader
                  v-model="activeDraft.imageDesktop"
                  v-model:use-default="activeDraft.useDefaultDesktop"
                  :default-src="defaultImg('header_logo', 'imageDesktop')"
                  slot-key="site_logo"
                  entity-type="cms"
                  entity-id="header-logo"
                  label="Logo website"
                />
              </div>

              <!-- hero -->
              <div v-if="activeKey === 'hero'" class="grid md:grid-cols-2 gap-4">
                <CmsImageUploader
                  v-model="activeDraft.imageDesktop"
                  v-model:use-default="activeDraft.useDefaultDesktop"
                  :default-src="defaultImg('hero', 'imageDesktop')"
                  slot-key="hero_desktop"
                  entity-type="cms"
                  entity-id="hero"
                  label="Banner máy tính"
                />
                <CmsImageUploader
                  v-model="activeDraft.imageMobile"
                  v-model:use-default="activeDraft.useDefaultMobile"
                  :default-src="defaultImg('hero', 'imageMobile')"
                  slot-key="hero_mobile"
                  entity-type="cms"
                  entity-id="hero"
                  label="Banner điện thoại"
                />
              </div>

              <!-- story -->
              <template v-if="activeKey === 'story'">
                <label class="block text-sm font-semibold">Tiêu đề <span class="font-normal text-gray-400">(xuống dòng bằng Enter)</span>
                  <textarea v-model="activeDraft.title" rows="2" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <label class="block text-sm font-semibold">Nội dung
                  <textarea v-model="activeDraft.body" rows="7" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <div class="grid md:grid-cols-2 gap-3">
                  <label class="block text-sm font-semibold">Chữ trên nút
                    <input v-model="activeDraft.ctaText" class="mt-1 w-full border rounded-lg px-3 py-2" />
                  </label>
                  <label class="block text-sm font-semibold">Link của nút <span class="font-normal text-gray-400">(vd /me)</span>
                    <input v-model="activeDraft.ctaLink" class="mt-1 w-full border rounded-lg px-3 py-2" />
                  </label>
                </div>
                <div class="max-w-sm">
                  <CmsImageUploader
                    v-model="activeDraft.imageDesktop"
                    v-model:use-default="activeDraft.useDefaultDesktop"
                    :default-src="defaultImg('story', 'imageDesktop')"
                    slot-key="story_side"
                    entity-type="cms"
                    entity-id="story"
                    label="Ảnh bên phải"
                  />
                </div>
              </template>

              <!-- quality -->
              <template v-if="activeKey === 'quality'">
                <label class="block text-sm font-semibold">Tiêu đề khối
                  <input v-model="activeDraft.title" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <div v-for="(item, idx) in activeDraft.extra.items" :key="idx" class="border rounded-lg p-3 grid md:grid-cols-3 gap-3">
                  <CmsImageUploader
                    v-model="item.icon"
                    v-model:use-default="item.useDefaultIcon"
                    :default-src="cmsImg(DEFAULT_IMAGE_PATHS.quality.icons[idx])"
                    slot-key="quality_icon"
                    :entity-id="`quality-${idx}`"
                    :label="`Biểu tượng ${idx + 1}`"
                  />
                  <div class="md:col-span-2 space-y-2">
                    <input v-model="item.title" class="w-full border rounded px-3 py-2 text-sm font-semibold" placeholder="Tiêu đề" />
                    <textarea v-model="item.body" rows="4" class="w-full border rounded px-3 py-2 text-sm" placeholder="Mô tả" />
                  </div>
                </div>
              </template>

              <!-- meal_gallery -->
              <template v-if="activeKey === 'meal_gallery'">
                <label class="block text-sm font-semibold">Tiêu đề
                  <input v-model="activeDraft.extra.title" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <CmsImageUploader
                  v-model="activeDraft.extra.banner"
                  v-model:use-default="activeDraft.extra.useDefaultBanner"
                  :default-src="cmsImg(DEFAULT_IMAGE_PATHS.meal_gallery.banner)"
                  slot-key="meal_banner"
                  entity-id="meal-banner"
                  label="Ảnh banner trên tiêu đề"
                />
                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <div v-for="(tile, idx) in activeDraft.extra.tiles" :key="idx" class="space-y-1">
                    <CmsImageUploader
                      v-model="tile.src"
                      v-model:use-default="tile.useDefault"
                      :default-src="cmsImg(DEFAULT_IMAGE_PATHS.meal_gallery.tiles[idx])"
                      slot-key="meal_tile"
                      :label="`Ô ${idx + 1}`"
                      :entity-id="`meal-${idx}`"
                    />
                    <input v-model="tile.alt" class="w-full border rounded px-2 py-1 text-xs" placeholder="Mô tả ảnh" />
                  </div>
                </div>
              </template>

              <!-- floating_chat -->
              <template v-if="activeKey === 'floating_chat'">
                <label class="block text-sm font-semibold">Link Messenger <span class="font-normal text-gray-400">(https://m.me/...)</span>
                  <input v-model="activeDraft.extra.messengerUrl" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
                <label class="block text-sm font-semibold">Link Zalo <span class="font-normal text-gray-400">(https://zalo.me/...)</span>
                  <input v-model="activeDraft.extra.zaloUrl" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
              </template>

              <!-- footer -->
              <template v-if="activeKey === 'footer'">
                <div class="grid md:grid-cols-3 gap-3">
                  <fieldset class="border rounded-lg p-3 space-y-2">
                    <legend class="text-xs font-bold text-gray-600 px-1">Cột 1 · Thông tin</legend>
                    <label class="block text-xs font-semibold">Địa chỉ
                      <textarea v-model="activeDraft.extra.address" rows="3" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Số điện thoại
                      <input v-model="activeDraft.extra.phone" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Email
                      <input v-model="activeDraft.extra.email" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <p class="text-[11px] text-gray-400">Logo lấy từ khối 1 · Header.</p>
                  </fieldset>

                  <fieldset class="border rounded-lg p-3 space-y-2">
                    <legend class="text-xs font-bold text-gray-600 px-1">Cột 2 · Chính sách</legend>
                    <label class="block text-xs font-semibold">Tiêu đề cột
                      <input v-model="activeDraft.extra.policyTitle" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <div v-for="(link, idx) in activeDraft.extra.policyLinks" :key="idx" class="border rounded p-2 space-y-1 bg-gray-50">
                      <input v-model="link.label" class="w-full border rounded px-2 py-1 text-sm" placeholder="Tên hiển thị" />
                      <input v-model="link.to" class="w-full border rounded px-2 py-1 text-xs" placeholder="/terms" />
                      <div class="flex gap-2 text-[11px]">
                        <button type="button" class="text-gray-600 disabled:opacity-30" :disabled="idx === 0" @click="move(activeDraft.extra.policyLinks, idx, -1)">▲</button>
                        <button type="button" class="text-gray-600 disabled:opacity-30" :disabled="idx === activeDraft.extra.policyLinks.length - 1" @click="move(activeDraft.extra.policyLinks, idx, 1)">▼</button>
                        <button type="button" class="ml-auto text-red-600" @click="activeDraft.extra.policyLinks.splice(idx, 1)">Xoá</button>
                      </div>
                    </div>
                    <button
                      v-if="activeDraft.extra.policyLinks.length < 8"
                      type="button"
                      class="w-full text-xs border border-dashed rounded py-1 text-green-700"
                      @click="activeDraft.extra.policyLinks.push({ label: '', to: '/' })"
                    >+ Thêm link</button>
                  </fieldset>

                  <fieldset class="border rounded-lg p-3 space-y-2">
                    <legend class="text-xs font-bold text-gray-600 px-1">Cột 3 · Liên hệ</legend>
                    <label class="block text-xs font-semibold">Tiêu đề cột
                      <input v-model="activeDraft.extra.contactTitle" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Hotline
                      <input v-model="activeDraft.extra.hotline" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Giờ làm việc
                      <input v-model="activeDraft.extra.hotlineHours" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Chữ link FAQ
                      <input v-model="activeDraft.extra.faqLabel" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                    <label class="block text-xs font-semibold">Link FAQ
                      <input v-model="activeDraft.extra.faqLink" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                    </label>
                  </fieldset>
                </div>

                <fieldset class="border rounded-lg p-3 space-y-2">
                  <legend class="text-xs font-bold text-gray-600 px-1">Hàng dưới · Mạng xã hội</legend>
                  <label class="block text-xs font-semibold max-w-sm">Dòng giới thiệu
                    <input v-model="activeDraft.extra.socialTitle" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                  </label>
                  <div v-for="(s, idx) in activeDraft.extra.socials" :key="idx" class="grid grid-cols-12 gap-2 items-center">
                    <select v-model="s.type" class="col-span-3 border rounded px-2 py-1 text-sm">
                      <option v-for="t in socialTypes" :key="t" :value="t">{{ t }}</option>
                    </select>
                    <input v-model="s.label" class="col-span-3 border rounded px-2 py-1 text-sm" placeholder="Nhãn" />
                    <input v-model="s.url" class="col-span-5 border rounded px-2 py-1 text-sm" placeholder="https://..." />
                    <button type="button" class="col-span-1 text-red-600 text-xs" @click="activeDraft.extra.socials.splice(idx, 1)">Xoá</button>
                  </div>
                  <button
                    v-if="activeDraft.extra.socials.length < 6"
                    type="button"
                    class="text-xs border border-dashed rounded px-3 py-1 text-green-700"
                    @click="activeDraft.extra.socials.push({ type: 'facebook', label: 'FACEBOOK', url: '#' })"
                  >+ Thêm mạng xã hội</button>
                </fieldset>

                <label class="block text-sm font-semibold">Dòng bản quyền
                  <input v-model="activeDraft.extra.copyright" class="mt-1 w-full border rounded-lg px-3 py-2" />
                </label>
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
              <p class="text-xs text-gray-400 text-right">Sau khi lưu, trang chủ cập nhật trong khoảng 30 giây.</p>
            </template>

            <p v-if="message" class="text-sm" :class="messageError ? 'text-red-600' : 'text-green-600'">{{ message }}</p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import CmsImageUploader from '../components/cms/CmsImageUploader.vue'
import { contentService } from '../services/content.service.js'
import { defaultHomepage, resolveCmsImage, resolveSlotImage, DEFAULT_IMAGE_PATHS } from '../content/homeDefaults.js'

/**
 * Blocks in the exact order they render on the public site (App.vue header → Home.vue → App.vue footer).
 * `linked` blocks are not CMS sections; they are managed on another admin page.
 */
const BLOCKS = [
  { key: 'header_logo', label: 'Header · Logo', hint: 'Logo cũng hiển thị ở footer.', toggle: false },
  { key: 'hero', label: 'Banner chính' },
  { key: 'story', label: 'Câu chuyện' },
  {
    key: '__products', label: 'Sản phẩm bán chạy', linked: true, toggle: false,
    linkedText: 'Khối này tự lấy 4 sản phẩm nổi bật (theo độ ưu tiên) từ danh sách sản phẩm.',
    linkTo: '/admin/products', linkLabel: 'Quản lý sản phẩm',
  },
  { key: 'quality', label: 'Tiêu chuẩn chất lượng' },
  { key: 'meal_gallery', label: 'Gallery bữa ăn' },
  {
    key: '__posts', label: 'Bài viết nổi bật', linked: true, toggle: false,
    linkedText: 'Khối này hiển thị tối đa 3 bài đã xuất bản có tick "Hiện trên Home", theo thứ tự 1 đến 3. Không có bài nào thì khối tự ẩn.',
    linkTo: '/admin/posts', linkLabel: 'Quản lý bài viết',
  },
  { key: 'footer', label: 'Footer', hint: 'Hiển thị ở cuối mọi trang.' },
  { key: 'floating_chat', label: 'Nút chat nổi', hint: 'Nút tròn cố định bên phải màn hình trang chủ.' },
]
const mapBlocks = BLOCKS.map((b, i) => ({ ...b, no: i + 1 }))

const devices = [
  { key: 'desktop', label: 'Máy tính' },
  { key: 'mobile', label: 'Điện thoại' },
]
const socialTypes = ['facebook', 'tiktok', 'youtube', 'instagram', 'zalo']

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
const featuredPosts = ref([])

const clone = (v) => JSON.parse(JSON.stringify(v))
const cmsImg = (path) => resolveCmsImage(path || '')
const sectionOf = (key) => sections.value.find((s) => s.sectionKey === key)
const draftOf = (key) => drafts[key]

const activeBlock = computed(() => mapBlocks.find((b) => b.key === activeKey.value))
const activeSection = computed(() => sectionOf(activeKey.value))
const activeDraft = computed(() => drafts[activeKey.value])

function normalize(section) {
  const key = section.sectionKey
  const extra = clone(section.extraJson || {})
  const d = {
    enabled: section.enabled !== false,
    title: section.title || '',
    body: section.body || '',
    ctaText: section.ctaText || '',
    ctaLink: section.ctaLink || '',
    imageDesktop: section.imageDesktop || '',
    imageMobile: section.imageMobile || '',
    useDefaultDesktop: section.useDefaultDesktop !== false,
    useDefaultMobile: section.useDefaultMobile !== false,
    extra,
  }
  if (key === 'quality') {
    const items = (extra.items || []).map((i) => ({
      title: i.title || '', body: i.body || '', icon: i.icon || '', useDefaultIcon: i.useDefaultIcon !== false,
    }))
    while (items.length < 2) items.push({ title: '', body: '', icon: '', useDefaultIcon: true })
    d.extra = { items }
  } else if (key === 'meal_gallery') {
    const tiles = (extra.tiles || []).map((t, idx) => ({
      src: t.src || '', alt: t.alt || `Thêm mô tả ${idx + 1}`, useDefault: t.useDefault !== false,
    }))
    while (tiles.length < 4) tiles.push({ src: '', alt: `Thêm mô tả ${tiles.length + 1}`, useDefault: true })
    d.extra = {
      title: extra.title || section.title || '',
      banner: extra.banner || '',
      useDefaultBanner: extra.useDefaultBanner !== false,
      tiles,
    }
  } else if (key === 'floating_chat') {
    d.extra = { messengerUrl: extra.messengerUrl || '', zaloUrl: extra.zaloUrl || '' }
  } else if (key === 'footer') {
    const defaults = clone(defaultHomepage.sections.footer.extraJson)
    d.extra = { ...defaults, ...extra }
    d.extra.policyLinks = clone(d.extra.policyLinks || [])
    d.extra.socials = clone(d.extra.socials || [])
  }
  return d
}

function buildExtra(key, d) {
  if (key === 'quality') {
    return {
      items: d.extra.items.map((i) => ({
        title: i.title,
        body: i.body,
        useDefaultIcon: i.useDefaultIcon !== false,
        icon: i.useDefaultIcon !== false ? null : i.icon || null,
      })),
    }
  }
  if (key === 'meal_gallery') {
    return {
      title: d.extra.title,
      useDefaultBanner: d.extra.useDefaultBanner !== false,
      banner: d.extra.useDefaultBanner !== false ? null : d.extra.banner || null,
      tiles: d.extra.tiles.map((t, idx) => ({
        alt: t.alt || `Thêm mô tả ${idx + 1}`,
        useDefault: t.useDefault !== false,
        src: t.useDefault !== false ? null : t.src || null,
      })),
    }
  }
  if (key === 'footer') {
    return {
      ...d.extra,
      policyLinks: d.extra.policyLinks.filter((l) => (l.label || '').trim()),
      socials: d.extra.socials.filter((s) => (s.label || '').trim()),
    }
  }
  return d.extra
}

function buildPayload(key, d, section) {
  return {
    title: key === 'meal_gallery' ? d.extra.title : d.title,
    body: d.body,
    ctaText: d.ctaText,
    ctaLink: d.ctaLink,
    useDefaultDesktop: d.useDefaultDesktop,
    useDefaultMobile: d.useDefaultMobile,
    imageDesktop: d.useDefaultDesktop ? null : d.imageDesktop || null,
    imageMobile: d.useDefaultMobile ? null : d.imageMobile || null,
    enabled: d.enabled,
    sortOrder: section.sortOrder,
    extraJson: buildExtra(key, d),
    updatedAt: section.updatedAt,
  }
}

function resetDraft(section) {
  const d = normalize(section)
  drafts[section.sectionKey] = d
  snapshots[section.sectionKey] = JSON.stringify(d)
}

const isDirty = (key) => !!drafts[key] && JSON.stringify(drafts[key]) !== snapshots[key]
const anyDirty = computed(() => Object.keys(drafts).some(isDirty))
const isHidden = (key) => drafts[key] && drafts[key].enabled === false && mapBlocks.find((b) => b.key === key)?.toggle !== false

function revert(key) {
  if (snapshots[key]) drafts[key] = JSON.parse(snapshots[key])
}

function select(key) {
  activeKey.value = key
  message.value = ''
}

function move(list, idx, delta) {
  const target = idx + delta
  if (target < 0 || target >= list.length) return
  const [item] = list.splice(idx, 1)
  list.splice(target, 0, item)
}

function positionText(block) {
  const prev = mapBlocks[block.no - 2]
  const next = mapBlocks[block.no]
  if (block.key === 'floating_chat') return 'Vị trí: nổi bên phải màn hình, không chiếm chỗ trong trang.'
  if (!prev) return `Vị trí ${block.no}: trên cùng của trang.`
  if (!next || next.key === 'floating_chat') return `Vị trí ${block.no}: cuối trang, sau "${prev.label}".`
  return `Vị trí ${block.no}: nằm giữa "${prev.label}" và "${next.label}".`
}

function defaultImg(key, field) {
  return cmsImg(DEFAULT_IMAGE_PATHS[key]?.[field])
}

function slotImg(useDefault, path, defaultPath) {
  return resolveSlotImage({ useDefault, path, defaultPath })
}

const footerDraft = computed(() => drafts.footer?.extra || defaultHomepage.sections.footer.extraJson)

const preview = computed(() => {
  const logo = drafts.header_logo
  const hero = drafts.hero
  const story = drafts.story
  const quality = drafts.quality
  const meal = drafts.meal_gallery
  return {
    logo: slotImg(logo?.useDefaultDesktop, logo?.imageDesktop, DEFAULT_IMAGE_PATHS.header_logo.imageDesktop),
    heroDesktop: slotImg(hero?.useDefaultDesktop, hero?.imageDesktop, DEFAULT_IMAGE_PATHS.hero.imageDesktop),
    heroMobile: slotImg(hero?.useDefaultMobile, hero?.imageMobile, DEFAULT_IMAGE_PATHS.hero.imageMobile),
    story: slotImg(story?.useDefaultDesktop, story?.imageDesktop, DEFAULT_IMAGE_PATHS.story.imageDesktop),
    qualityItems: (quality?.extra?.items || []).map((item, idx) => ({
      ...item,
      icon: slotImg(item.useDefaultIcon, item.icon, DEFAULT_IMAGE_PATHS.quality.icons[idx]),
    })),
    mealBanner: slotImg(meal?.extra?.useDefaultBanner, meal?.extra?.banner, DEFAULT_IMAGE_PATHS.meal_gallery.banner),
    mealTiles: (meal?.extra?.tiles || []).map((t, idx) =>
      slotImg(t.useDefault, t.src, DEFAULT_IMAGE_PATHS.meal_gallery.tiles[idx])
    ),
  }
})

function formatDate(value) {
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

async function loadFeaturedPosts() {
  try {
    const posts = await contentService.getAdminPosts(false)
    featuredPosts.value = (posts || [])
      .filter((p) => p.status === 'PUBLISHED' && p.showOnHome)
      .sort((a, b) => (a.homeOrder || 99) - (b.homeOrder || 99))
      .slice(0, 3)
  } catch (_) {
    featuredPosts.value = []
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    sections.value = await contentService.getAdminSections()
    sections.value.forEach(resetDraft)
  } catch (e) {
    error.value = e.message || 'Không tải được nội dung trang chủ'
  } finally {
    loading.value = false
  }
  loadFeaturedPosts()
}

async function save(key) {
  const section = sectionOf(key)
  const d = drafts[key]
  if (!section || !d) return
  saving.value = true
  message.value = ''
  messageError.value = false
  try {
    const updated = await contentService.updateSection(key, buildPayload(key, d, section))
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

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  load()
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', onBeforeUnload))
</script>
