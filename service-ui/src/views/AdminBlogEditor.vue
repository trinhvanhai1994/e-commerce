<template>
  <AdminLayout>
    <div class="max-w-7xl mx-auto">
      <!-- Top bar -->
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div>
          <router-link to="/admin/posts" class="text-xs text-gray-500 hover:text-green-700">← Danh sách bài viết</router-link>
          <div class="flex flex-wrap items-center gap-2">
            <h1 class="text-2xl font-bold text-green-700">{{ isNew ? 'Tạo bài viết' : 'Sửa bài viết' }}</h1>
            <span
              class="text-xs px-2 py-0.5 rounded-full font-semibold"
              :class="savedStatus === 'PUBLISHED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-800'"
            >{{ isNew ? 'Chưa tạo' : savedStatus === 'PUBLISHED' ? 'Đã xuất bản' : 'Nháp' }}</span>
            <span v-if="dirty" class="text-xs px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 font-semibold">Chưa lưu</span>
          </div>
          <p class="text-sm text-gray-500">Bấm vào một khối ở bản xem trước để sửa đúng khối đó. Bản xem trước hiển thị cả thay đổi chưa lưu.</p>
        </div>
        <div v-if="!loading && !loadError" class="flex flex-wrap items-center gap-2">
          <a
            v-if="savedStatus === 'PUBLISHED' && saved?.slug"
            :href="`/blog/${saved.slug}`"
            target="_blank"
            rel="noopener"
            class="px-3 py-1.5 text-sm rounded-lg border bg-white font-semibold text-gray-700 hover:bg-gray-50"
          >Xem trên web ↗</a>
          <button type="button" class="px-4 py-2 border bg-white rounded-lg text-sm disabled:opacity-40" :disabled="saving || !dirty" @click="revert">
            Hoàn tác
          </button>
          <button
            v-if="savedStatus === 'PUBLISHED'"
            type="button"
            class="px-4 py-2 border border-yellow-400 bg-yellow-50 text-yellow-800 rounded-lg text-sm font-semibold disabled:opacity-40"
            :disabled="saving"
            @click="saveWithStatus('DRAFT')"
          >Chuyển về nháp</button>
          <button
            v-else
            type="button"
            class="px-4 py-2 border border-green-600 text-green-700 bg-white rounded-lg text-sm font-semibold disabled:opacity-40"
            :disabled="saving"
            @click="saveWithStatus('PUBLISHED')"
          >Lưu và xuất bản</button>
          <button
            type="button"
            class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm disabled:opacity-50"
            :disabled="saving || (!dirty && !isNew)"
            @click="save()"
          >{{ saving ? 'Đang lưu...' : isNew ? 'Tạo bài viết' : 'Lưu' }}</button>
        </div>
      </div>

      <div v-if="loading" class="text-center py-10 text-gray-500">Đang tải...</div>
      <div v-else-if="loadError" class="bg-red-50 text-red-700 p-4 rounded mb-4">{{ loadError }}</div>

      <template v-else>
        <div v-if="message" class="mb-4 p-3 rounded-lg text-sm flex flex-wrap items-center gap-3" :class="messageError ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'">
          <span>{{ message }}</span>
          <button v-if="conflict" type="button" class="px-3 py-1 rounded border border-red-300 bg-white text-xs font-semibold" @click="reloadAfterConflict">
            Tải lại bản mới nhất
          </button>
        </div>

        <div class="grid lg:grid-cols-12 gap-4 items-start">
          <!-- ============ LEFT: preview ============ -->
          <div class="lg:col-span-6 lg:sticky lg:top-20">
            <div class="flex flex-wrap items-center justify-between gap-2 mb-2">
              <h2 class="text-sm font-bold text-gray-700 uppercase tracking-wide">Xem trước</h2>
              <div class="inline-flex rounded-lg border bg-white p-0.5 text-xs">
                <button
                  v-for="d in devices"
                  :key="d.key"
                  type="button"
                  class="px-2.5 py-1 rounded-md font-semibold"
                  :class="device === d.key ? 'bg-green-600 text-white' : 'text-gray-600'"
                  @click="device = d.key"
                >{{ d.label }}</button>
              </div>
            </div>
            <div class="flex gap-1 mb-2 text-sm">
              <button
                v-for="t in previewTabs"
                :key="t.key"
                type="button"
                class="px-3 py-1.5 rounded-t-lg font-semibold border-b-2"
                :class="previewTab === t.key ? 'border-green-600 text-green-700 bg-white' : 'border-transparent text-gray-500 hover:text-gray-700'"
                @click="previewTab = t.key"
              >{{ t.label }}</button>
            </div>

            <div ref="previewScroller" class="bg-gray-200 rounded-2xl p-3 lg:max-h-[calc(100vh-11rem)] lg:overflow-y-auto">
              <div
                class="mx-auto bg-white rounded-xl shadow-inner overflow-hidden transition-all duration-300"
                :class="device === 'mobile' ? 'max-w-[375px]' : 'w-full'"
              >
                <!-- Detail page -->
                <div v-if="previewTab === 'detail'" class="px-4 py-6">
                  <img v-if="coverUrl" :src="coverUrl" :alt="draft.title" class="w-full h-auto object-contain rounded-lg mb-6" />
                  <div v-else class="w-full aspect-[16/10] rounded-lg mb-6 bg-gray-100 flex items-center justify-center text-xs text-gray-400">Chưa có ảnh cover</div>
                  <h1 class="text-2xl md:text-3xl font-bold text-gray-900 mb-3">
                    <template v-if="draft.title">{{ draft.title }}</template>
                    <span v-else class="text-gray-300">Tiêu đề bài viết</span>
                  </h1>
                  <div class="text-sm text-gray-500 mb-8">{{ previewDate }}</div>
                  <BlogArticleBody :blocks="previewBlocks" :active-index="activeIndex" selectable @select="selectBlock($event, 'preview')" />
                  <div v-if="!draft.contentJson.length" class="text-sm text-gray-400 text-center py-6 border border-dashed rounded">
                    Bài viết chưa có nội dung. Thêm khối ở cột bên phải.
                  </div>
                </div>

                <!-- Card on Blog list -->
                <div v-else-if="previewTab === 'blog'" class="p-4 bg-gray-50">
                  <p class="text-[11px] text-gray-500 mb-2">Thẻ này nằm trong lưới 3 cột của trang Blog (1 cột trên điện thoại).</p>
                  <div class="bg-white rounded-xl shadow p-0 flex flex-col overflow-hidden" :class="device === 'mobile' ? 'w-full' : 'max-w-[280px]'">
                    <img v-if="coverUrl" :src="coverUrl" :alt="draft.title" class="w-full h-auto object-contain rounded-t-xl" />
                    <div v-else class="w-full aspect-[16/10] bg-gray-100 flex items-center justify-center text-xs text-gray-400">Chưa có ảnh cover</div>
                    <div class="p-4 flex-1 flex flex-col">
                      <h2 class="font-bold text-base md:text-lg text-gray-800 mb-2 line-clamp-2">{{ draft.title || 'Tiêu đề bài viết' }}</h2>
                      <p class="text-gray-600 text-sm mb-3 line-clamp-2">{{ draft.excerpt || 'Mô tả ngắn sẽ hiện ở đây.' }}</p>
                      <span class="mt-auto text-green-500 text-sm font-semibold">Đọc tiếp</span>
                    </div>
                  </div>
                  <p v-if="draft.status !== 'PUBLISHED'" class="mt-3 text-xs text-amber-700">Bài đang là Nháp nên thẻ này chưa hiện trên trang Blog.</p>
                </div>

                <!-- Card on Home -->
                <div v-else class="p-4 bg-gray-50 space-y-4">
                  <div>
                    <div class="text-[11px] font-bold text-center text-green-700 mb-2">BÀI VIẾT NỔI BẬT · TRANG CHỦ</div>
                    <div class="grid gap-2" :class="device === 'mobile' ? 'grid-cols-1' : 'grid-cols-3'">
                      <div
                        v-for="cell in homeStrip"
                        :key="cell.no"
                        class="rounded-lg border p-1.5 text-[10px] bg-white"
                        :class="cell.isThis ? 'ring-2 ring-green-500 border-green-500' : ''"
                      >
                        <div class="font-bold mb-1" :class="cell.isThis ? 'text-green-700' : 'text-gray-500'">Vị trí {{ cell.no }}<template v-if="cell.isThis"> · Bài này</template></div>
                        <template v-if="cell.post">
                          <img v-if="cell.cover" :src="cell.cover" alt="" class="w-full aspect-[16/10] object-cover rounded" />
                          <div v-else class="w-full aspect-[16/10] rounded bg-gray-100"></div>
                          <div class="font-semibold mt-1 line-clamp-2">{{ cell.post.title || 'Tiêu đề bài viết' }}</div>
                          <div v-if="cell.note" class="text-amber-700">{{ cell.note }}</div>
                        </template>
                        <div v-else class="w-full aspect-[16/10] rounded border border-dashed flex items-center justify-center text-gray-400">Trống</div>
                      </div>
                    </div>
                    <p v-if="!onHomeNow" class="mt-2 text-xs text-gray-600 text-center">Không hiện trên Trang chủ.</p>
                  </div>

                  <div v-if="onHomeNow">
                    <p class="text-[11px] text-gray-500 mb-2">Thẻ của bài này trên Trang chủ:</p>
                    <div class="bg-white rounded-xl shadow p-3 md:p-4 flex flex-col items-center" :class="device === 'mobile' ? 'w-full' : 'max-w-[280px]'">
                      <img v-if="coverUrl" :src="coverUrl" :alt="draft.title" class="w-full h-32 md:h-40 object-cover rounded mb-3" />
                      <div v-else class="w-full h-32 md:h-40 rounded mb-3 bg-gray-100"></div>
                      <div class="font-bold text-green-700 text-sm md:text-base mb-1 text-center">{{ draft.title || 'Tiêu đề bài viết' }}</div>
                      <div class="text-gray-600 text-xs md:text-sm text-center">{{ draft.excerpt || 'Mô tả ngắn sẽ hiện ở đây.' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ============ RIGHT: editor ============ -->
          <div class="lg:col-span-6 space-y-4">
            <!-- Meta -->
            <section class="bg-white rounded-xl shadow p-4 md:p-6 space-y-4">
              <h2 class="text-lg font-bold text-gray-800">Thông tin bài viết</h2>
              <label class="block text-sm font-semibold">Tiêu đề
                <input v-model="draft.title" class="mt-1 w-full border rounded-lg px-3 py-2" placeholder="Tiêu đề hiển thị trên thẻ và trang chi tiết" />
              </label>

              <div>
                <div class="flex items-center justify-between gap-2">
                  <label for="post-slug" class="text-sm font-semibold">Slug <span class="font-normal text-gray-400">(đường dẫn /blog/...)</span></label>
                  <button
                    v-if="!slugLocked"
                    type="button"
                    class="text-xs text-green-700 font-semibold disabled:opacity-40"
                    :disabled="!draft.title.trim()"
                    @click="draft.slug = slugify(draft.title)"
                  >Tạo từ tiêu đề</button>
                </div>
                <div class="mt-1 flex items-center rounded-lg border overflow-hidden" :class="slugLocked ? 'bg-gray-100' : slugError ? 'border-red-400' : ''">
                  <span class="px-3 text-sm text-gray-400 select-none">/blog/</span>
                  <input
                    id="post-slug"
                    v-model="draft.slug"
                    class="flex-1 px-1 py-2 bg-transparent outline-none disabled:text-gray-500"
                    :disabled="slugLocked"
                    placeholder="vi-du-bai-viet"
                  />
                </div>
                <p v-if="slugLocked" class="text-xs text-gray-500 mt-1">Slug đã khoá vì bài đang xuất bản. Muốn đổi, chuyển bài về Nháp trước (link cũ sẽ không còn mở được).</p>
                <p v-else-if="slugError" class="text-xs text-red-600 mt-1">{{ slugError }}</p>
                <p v-else class="text-xs text-gray-500 mt-1">
                  Chỉ chữ thường không dấu, số và dấu gạch ngang, ví dụ <code>an-uong-lanh-manh</code>.
                  <template v-if="saved?.publishedAt"> Bài đã từng xuất bản, đổi slug sẽ làm link cũ không còn mở được.</template>
                </p>
              </div>

              <label class="block text-sm font-semibold">
                <span class="flex items-center justify-between">
                  <span>Mô tả ngắn <span class="font-normal text-gray-400">(hiện trên thẻ ở trang Blog và Trang chủ)</span></span>
                  <span class="text-xs font-normal" :class="draft.excerpt.length > EXCERPT_SOFT_MAX ? 'text-amber-700' : 'text-gray-400'">{{ draft.excerpt.length }} ký tự</span>
                </span>
                <textarea v-model="draft.excerpt" rows="3" class="mt-1 w-full border rounded-lg px-3 py-2" />
                <span v-if="draft.excerpt.length > EXCERPT_SOFT_MAX" class="block text-xs font-normal text-amber-700">Thẻ ở trang Blog chỉ hiện khoảng 2 dòng, phần dài hơn sẽ bị cắt.</span>
              </label>

              <div class="max-w-sm no-default-toggle">
                <CmsImageUploader
                  v-model="draft.coverImage"
                  :use-default="false"
                  slot-key="blog_cover"
                  entity-type="blog"
                  :entity-id="draft.slug || 'new'"
                  label="Ảnh cover"
                />
              </div>
            </section>

            <!-- Visibility -->
            <section class="bg-white rounded-xl shadow p-4 md:p-6 space-y-4">
              <h2 class="text-lg font-bold text-gray-800">Hiển thị</h2>
              <div class="grid sm:grid-cols-2 gap-2">
                <button
                  v-for="s in statusOptions"
                  :key="s.value"
                  type="button"
                  class="text-left border rounded-lg p-3 transition"
                  :class="draft.status === s.value ? 'border-green-600 ring-2 ring-green-200 bg-green-50/50' : 'hover:border-gray-400'"
                  @click="setStatus(s.value)"
                >
                  <div class="flex items-center gap-2 font-semibold text-sm">
                    <span class="w-3.5 h-3.5 rounded-full border-2 flex items-center justify-center" :class="draft.status === s.value ? 'border-green-600' : 'border-gray-300'">
                      <span v-if="draft.status === s.value" class="w-1.5 h-1.5 rounded-full bg-green-600"></span>
                    </span>
                    {{ s.label }}
                  </div>
                  <p class="text-xs text-gray-500 mt-1">{{ s.help }}</p>
                </button>
              </div>
              <p v-if="draft.status !== savedStatus && !isNew" class="text-xs text-amber-700">Trạng thái mới chỉ có hiệu lực sau khi bấm Lưu.</p>

              <div class="border-t pt-4 space-y-3">
                <label class="flex items-center gap-2 text-sm font-semibold select-none" :class="draft.status === 'PUBLISHED' ? 'cursor-pointer' : 'text-gray-400 cursor-not-allowed'">
                  <input
                    type="checkbox"
                    class="w-4 h-4"
                    :checked="draft.showOnHome && draft.status === 'PUBLISHED'"
                    :disabled="draft.status !== 'PUBLISHED'"
                    @change="toggleShowOnHome($event.target.checked)"
                  />
                  Hiện trong "Bài viết nổi bật" ở Trang chủ
                </label>
                <p v-if="draft.status !== 'PUBLISHED'" class="text-xs text-gray-500">Chỉ bài đã xuất bản mới hiện được trên Trang chủ. Chọn "Đã xuất bản" để bật tuỳ chọn này.</p>

                <div v-if="draft.status === 'PUBLISHED' && draft.showOnHome">
                  <p class="text-xs text-gray-500 mb-2">Chọn vị trí (Trang chủ hiện tối đa 3 bài, từ trái sang phải):</p>
                  <div class="grid grid-cols-3 gap-2">
                    <button
                      v-for="n in 3"
                      :key="n"
                      type="button"
                      class="border rounded-lg p-2 text-left text-xs disabled:cursor-not-allowed"
                      :class="draft.homeOrder === n ? 'border-green-600 ring-2 ring-green-200 bg-green-50/50' : takenBy(n) ? 'bg-gray-50 text-gray-400' : 'hover:border-gray-400'"
                      :disabled="!!takenBy(n)"
                      @click="draft.homeOrder = n"
                    >
                      <div class="font-bold text-sm" :class="draft.homeOrder === n ? 'text-green-700' : ''">Vị trí {{ n }}</div>
                      <div v-if="takenBy(n)" class="line-clamp-2">Đã có: {{ takenBy(n).title }}</div>
                      <div v-else-if="draft.homeOrder === n" class="text-green-700">Bài này</div>
                      <div v-else class="text-gray-500">Còn trống</div>
                    </button>
                  </div>
                  <p v-if="freeSlots.length === 0 && !draft.homeOrder" class="text-xs text-red-600 mt-2">
                    Cả 3 vị trí đã có bài. Bỏ "Hiện trên Trang chủ" ở một bài khác trong danh sách bài viết trước.
                  </p>
                  <p v-if="otherPostsError" class="text-xs text-amber-700 mt-2">{{ otherPostsError }}</p>
                </div>
              </div>
            </section>

            <!-- Blocks -->
            <section class="bg-white rounded-xl shadow p-4 md:p-6 space-y-3">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h2 class="text-lg font-bold text-gray-800">Nội dung</h2>
                  <p class="text-xs text-gray-500">{{ draft.contentJson.length }} khối, theo đúng thứ tự hiển thị trên trang chi tiết. Tiêu đề H2 tạo mục lục.</p>
                </div>
                <div class="flex gap-2 text-xs">
                  <button type="button" class="px-2 py-1 border rounded" @click="setAllOpen(true)">Mở hết</button>
                  <button type="button" class="px-2 py-1 border rounded" @click="setAllOpen(false)">Thu gọn hết</button>
                </div>
              </div>

              <InsertMenu :open="insertMenuAt === -1" label="+ Thêm khối ở đầu bài" @toggle="toggleInsertMenu(-1)" @pick="insertBlock(-1, $event)" />

              <div v-for="(block, idx) in draft.contentJson" :key="blockKeys[idx]" class="space-y-2">
                <div
                  :id="`block-card-${blockKeys[idx]}`"
                  class="border rounded-lg transition scroll-mt-4"
                  :class="activeIndex === idx ? 'border-green-500 ring-2 ring-green-200' : ''"
                >
                  <!-- Card header -->
                  <div class="flex items-center gap-2 px-3 py-2 cursor-pointer select-none" @click="onCardHeaderClick(idx)">
                    <span class="text-[11px] font-bold text-gray-400 w-5 text-right">{{ idx + 1 }}</span>
                    <span class="text-[11px] font-bold px-1.5 py-0.5 rounded" :class="typeMeta(block.type).badge">{{ typeMeta(block.type).label }}</span>
                    <span class="flex-1 min-w-0 truncate text-sm text-gray-600">{{ blockSummary(block) }}</span>
                    <div class="flex items-center gap-0.5 text-gray-500" @click.stop>
                      <button type="button" class="icon-btn" title="Lên trên" :disabled="idx === 0" @click="moveBlock(idx, -1)">▲</button>
                      <button type="button" class="icon-btn" title="Xuống dưới" :disabled="idx === draft.contentJson.length - 1" @click="moveBlock(idx, 1)">▼</button>
                      <button type="button" class="icon-btn" title="Nhân bản" @click="duplicateBlock(idx)">⧉</button>
                      <button type="button" class="icon-btn text-red-600" title="Xoá khối" @click="removeBlock(idx)">✕</button>
                    </div>
                    <span class="text-gray-400 text-xs w-3">{{ isOpen(idx) ? '▾' : '▸' }}</span>
                  </div>

                  <!-- Card body -->
                  <div v-if="isOpen(idx)" class="px-3 pb-3 pt-1 border-t space-y-2" @focusin="activeIndex = idx">
                    <textarea
                      v-if="block.type === 'p'"
                      v-model="block.text"
                      rows="4"
                      class="w-full border rounded px-3 py-2 text-sm"
                      placeholder="Nội dung đoạn văn"
                    />
                    <input
                      v-else-if="block.type === 'h2'"
                      v-model="block.text"
                      class="w-full border rounded px-3 py-2 text-base font-bold"
                      placeholder="Tiêu đề mục (hiện trong mục lục)"
                    />
                    <template v-else-if="block.type === 'ul'">
                      <textarea
                        :value="(block.items || []).join('\n')"
                        rows="4"
                        class="w-full border rounded px-3 py-2 text-sm"
                        placeholder="Mỗi dòng một mục"
                        @input="block.items = $event.target.value.split('\n')"
                      />
                      <p class="text-[11px] text-gray-400">Mỗi dòng là một mục. Dòng trống sẽ bị bỏ khi lưu.</p>
                    </template>
                    <div v-else-if="block.type === 'img'" class="grid sm:grid-cols-2 gap-3 items-start">
                      <div class="no-default-toggle">
                        <CmsImageUploader
                          v-model="block.src"
                          :use-default="false"
                          slot-key="blog_inline"
                          entity-type="blog"
                          :entity-id="draft.slug || 'new'"
                          label="Ảnh trong bài"
                        />
                      </div>
                      <label class="block text-xs font-semibold">Mô tả ảnh <span class="font-normal text-gray-400">(alt, cho SEO và trình đọc màn hình)</span>
                        <input v-model="block.alt" class="mt-1 w-full border rounded px-2 py-1 text-sm" />
                      </label>
                    </div>
                    <div v-else-if="block.type === 'cta'" class="grid sm:grid-cols-3 gap-2">
                      <label class="block text-xs font-semibold">Chữ in đậm phía trước
                        <input v-model="block.bold" class="mt-1 w-full border rounded px-2 py-1 text-sm" placeholder="Xem thêm:" />
                      </label>
                      <label class="block text-xs font-semibold">Chữ của link
                        <input v-model="block.linkText" class="mt-1 w-full border rounded px-2 py-1 text-sm" placeholder="Sản phẩm ABC" />
                      </label>
                      <label class="block text-xs font-semibold">Đường dẫn
                        <input v-model="block.to" class="mt-1 w-full border rounded px-2 py-1 text-sm" placeholder="/products/1" />
                      </label>
                    </div>
                    <div v-else-if="block.type === 'table'" class="space-y-2">
                      <div class="overflow-x-auto">
                        <table class="text-sm border-collapse">
                          <thead>
                            <tr>
                              <th v-for="(h, ci) in block.headers" :key="ci" class="p-0.5 align-top">
                                <input v-model="block.headers[ci]" class="w-32 border rounded px-2 py-1 text-xs font-semibold bg-gray-50" placeholder="Tiêu đề cột" />
                                <button type="button" class="block mx-auto text-[10px] text-red-600 mt-0.5" @click="removeTableCol(block, ci)">Xoá cột</button>
                              </th>
                              <th></th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(row, ri) in block.rows" :key="ri">
                              <td v-for="(cell, ci) in row" :key="ci" class="p-0.5">
                                <input v-model="row[ci]" class="w-32 border rounded px-2 py-1 text-xs" />
                              </td>
                              <td class="p-0.5"><button type="button" class="text-[10px] text-red-600" @click="block.rows.splice(ri, 1)">Xoá dòng</button></td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                      <div class="flex gap-2 text-xs">
                        <button type="button" class="px-2 py-1 border border-dashed rounded text-green-700" @click="addTableRow(block)">+ Dòng</button>
                        <button type="button" class="px-2 py-1 border border-dashed rounded text-green-700" @click="addTableCol(block)">+ Cột</button>
                      </div>
                    </div>
                    <pre v-else class="text-xs bg-gray-50 p-2 overflow-auto">{{ block }}</pre>
                  </div>
                </div>

                <InsertMenu :open="insertMenuAt === idx" label="+ Chèn khối sau đây" @toggle="toggleInsertMenu(idx)" @pick="insertBlock(idx, $event)" />
              </div>
            </section>

            <div v-if="problems.length" class="bg-amber-50 text-amber-800 text-sm p-3 rounded-lg">
              <div class="font-semibold mb-1">Cần sửa trước khi lưu:</div>
              <ul class="list-disc pl-5 space-y-0.5">
                <li v-for="(p, i) in problems" :key="i">{{ p }}</li>
              </ul>
            </div>

            <div class="flex flex-wrap items-center justify-end gap-2">
              <span v-if="saved?.updatedAt" class="text-xs text-gray-400 mr-auto">Lưu lần cuối {{ formatDateTime(saved.updatedAt) }}</span>
              <button type="button" class="px-4 py-2 border bg-white rounded-lg text-sm disabled:opacity-40" :disabled="saving || !dirty" @click="revert">Hoàn tác</button>
              <button
                type="button"
                class="px-4 py-2 bg-green-600 text-white rounded-lg font-semibold text-sm disabled:opacity-50"
                :disabled="saving || (!dirty && !isNew)"
                @click="save()"
              >{{ saving ? 'Đang lưu...' : isNew ? 'Tạo bài viết' : 'Lưu' }}</button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, onBeforeRouteUpdate, useRoute, useRouter } from 'vue-router'
import AdminLayout from './AdminLayout.vue'
import CmsImageUploader from '../components/cms/CmsImageUploader.vue'
import BlogArticleBody from '../components/blog/BlogArticleBody.vue'
import { contentService } from '../services/content.service.js'
import { resolveCmsImage } from '../content/homeDefaults.js'

const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/
const EXCERPT_SOFT_MAX = 160

const BLOCK_TYPES = {
  p: { label: 'Đoạn văn', badge: 'bg-gray-100 text-gray-700' },
  h2: { label: 'Tiêu đề H2', badge: 'bg-green-100 text-green-800' },
  ul: { label: 'Danh sách', badge: 'bg-blue-100 text-blue-800' },
  img: { label: 'Ảnh', badge: 'bg-purple-100 text-purple-800' },
  cta: { label: 'Nút CTA', badge: 'bg-amber-100 text-amber-800' },
  table: { label: 'Bảng', badge: 'bg-teal-100 text-teal-800' },
}
const INSERTABLE = ['p', 'h2', 'ul', 'img', 'cta']

const InsertMenu = defineComponent({
  props: { open: Boolean, label: String },
  emits: ['toggle', 'pick'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'flex flex-wrap items-center gap-1.5' }, [
        h(
          'button',
          {
            type: 'button',
            class: 'text-xs px-2 py-1 rounded border border-dashed text-green-700 hover:bg-green-50',
            onClick: () => emit('toggle'),
          },
          props.open ? 'Đóng' : props.label
        ),
        ...(props.open
          ? INSERTABLE.map((type) =>
              h(
                'button',
                {
                  type: 'button',
                  class: 'text-xs px-2 py-1 rounded border bg-white hover:border-green-500 hover:text-green-700',
                  onClick: () => emit('pick', type),
                },
                BLOCK_TYPES[type].label
              )
            )
          : []),
      ])
  },
})

const devices = [
  { key: 'desktop', label: 'Máy tính' },
  { key: 'mobile', label: 'Điện thoại' },
]
const previewTabs = [
  { key: 'detail', label: 'Trang chi tiết' },
  { key: 'blog', label: 'Thẻ ở trang Blog' },
  { key: 'home', label: 'Thẻ ở Trang chủ' },
]
const statusOptions = [
  { value: 'DRAFT', label: 'Nháp', help: 'Chỉ admin thấy. Không hiện ở trang Blog, Trang chủ, và link /blog/... chưa mở được.' },
  { value: 'PUBLISHED', label: 'Đã xuất bản', help: 'Hiện ở trang Blog và mở được tại /blog/<slug>. Có thể chọn hiện thêm trên Trang chủ.' },
]

const route = useRoute()
const router = useRouter()

const postId = ref(null)
const isNew = computed(() => postId.value == null)
const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const message = ref('')
const messageError = ref(false)
const conflict = ref(false)

const device = ref('desktop')
const previewTab = ref('detail')
const previewScroller = ref(null)

/** Last server copy of the post (status, slug, publishedAt, updatedAt). */
const saved = ref(null)
const draft = ref(emptyDraft())
const snapshot = ref(JSON.stringify(draft.value))
/** Stable keys parallel to draft.contentJson, so moving blocks keeps card state; never sent to the API. */
const blockKeys = ref([])
const openKeys = reactive(new Set())
const activeIndex = ref(-1)
const insertMenuAt = ref(null)

const otherPosts = ref([])
const otherPostsError = ref('')

let keySeq = 0
const newKey = () => `b${++keySeq}`
const clone = (v) => JSON.parse(JSON.stringify(v))

function emptyDraft() {
  return {
    slug: '',
    title: '',
    excerpt: '',
    coverImage: '',
    contentJson: [],
    status: 'DRAFT',
    showOnHome: false,
    homeOrder: null,
  }
}

function normalizeBlock(block) {
  const b = clone(block || {})
  if (b.type === 'ul' && !Array.isArray(b.items)) b.items = []
  if (b.type === 'img' && typeof b.src !== 'string') b.src = ''
  if (b.type === 'table') {
    if (!Array.isArray(b.headers)) b.headers = []
    if (!Array.isArray(b.rows)) b.rows = []
  }
  return b
}

function draftFromPost(post) {
  return {
    slug: post.slug || '',
    title: post.title || '',
    excerpt: post.excerpt || '',
    coverImage: post.coverImage || '',
    contentJson: (post.contentJson || []).map(normalizeBlock),
    status: post.status === 'PUBLISHED' ? 'PUBLISHED' : 'DRAFT',
    showOnHome: !!post.showOnHome,
    homeOrder: post.showOnHome ? post.homeOrder || null : null,
  }
}

function applySaved(post) {
  saved.value = post
  postId.value = post.id
  const d = draftFromPost(post)
  const keepKeys = blockKeys.value.length === d.contentJson.length
  draft.value = d
  snapshot.value = JSON.stringify(d)
  if (!keepKeys) {
    blockKeys.value = d.contentJson.map(newKey)
    openKeys.clear()
    activeIndex.value = -1
  }
}

const dirty = computed(() => JSON.stringify(draft.value) !== snapshot.value)
const savedStatus = computed(() => saved.value?.status || 'DRAFT')
const slugLocked = computed(() => !isNew.value && savedStatus.value === 'PUBLISHED')

const slugError = computed(() => {
  const s = draft.value.slug.trim()
  if (!s) return 'Slug không được để trống.'
  if (!SLUG_PATTERN.test(s)) return 'Slug chỉ gồm chữ thường không dấu, số, và dấu gạch ngang giữa các từ (không có khoảng trắng, không bắt đầu hoặc kết thúc bằng "-").'
  return ''
})

function slugify(text) {
  return (text || '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'd')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

// ---------- preview ----------
const coverUrl = computed(() => resolveCmsImage(draft.value.coverImage || ''))
const previewBlocks = computed(() =>
  draft.value.contentJson.map((b) => (b.type === 'img' && b.src ? { ...b, src: resolveCmsImage(b.src) } : b))
)
const previewDate = computed(() => {
  if (saved.value?.publishedAt) return formatDate(saved.value.publishedAt)
  return `${formatDate(new Date())} (ngày xuất bản sẽ được ghi khi bấm xuất bản)`
})

// ---------- home slots ----------
const othersOnHome = computed(() =>
  otherPosts.value.filter((p) => p.showOnHome && p.homeOrder >= 1 && p.homeOrder <= 3)
)
const takenBy = (n) => othersOnHome.value.find((p) => p.homeOrder === n) || null
const freeSlots = computed(() => [1, 2, 3].filter((n) => !takenBy(n)))
const onHomeNow = computed(() => draft.value.status === 'PUBLISHED' && draft.value.showOnHome && !!draft.value.homeOrder)

const homeStrip = computed(() =>
  [1, 2, 3].map((no) => {
    if (onHomeNow.value && draft.value.homeOrder === no) {
      return { no, isThis: true, post: draft.value, cover: coverUrl.value, note: '' }
    }
    const other = takenBy(no)
    if (!other) return { no, isThis: false, post: null }
    return {
      no,
      isThis: false,
      post: other,
      cover: resolveCmsImage(other.coverImage || ''),
      note: other.status === 'PUBLISHED' ? '' : 'Nháp, chưa hiện',
    }
  })
)

function setStatus(status) {
  draft.value.status = status
  if (status === 'DRAFT') {
    draft.value.showOnHome = false
    draft.value.homeOrder = null
  }
}

function toggleShowOnHome(checked) {
  draft.value.showOnHome = checked
  if (!checked) {
    draft.value.homeOrder = null
  } else if (!draft.value.homeOrder || takenBy(draft.value.homeOrder)) {
    draft.value.homeOrder = freeSlots.value[0] || null
  }
}

// ---------- blocks ----------
function typeMeta(type) {
  return BLOCK_TYPES[type] || { label: type || '?', badge: 'bg-red-100 text-red-700' }
}

function blockSummary(block) {
  switch (block.type) {
    case 'p':
    case 'h2':
      return block.text || '(trống)'
    case 'ul': {
      const items = (block.items || []).filter((i) => (i || '').trim())
      return items.length ? `${items.length} mục: ${items.join(', ')}` : '(trống)'
    }
    case 'img':
      return block.src ? block.alt || block.src : '(chưa có ảnh)'
    case 'cta':
      return [block.bold, block.linkText].filter(Boolean).join(' ') || '(trống)'
    case 'table':
      return `${(block.headers || []).length} cột, ${(block.rows || []).length} dòng`
    default:
      return 'Khối không chỉnh được ở đây, sẽ được giữ nguyên'
  }
}

function newBlock(type) {
  if (type === 'ul') return { type, items: [''] }
  if (type === 'img') return { type, src: '', alt: '' }
  if (type === 'cta') return { type, bold: '', linkText: '', to: '/' }
  return { type, text: '' }
}

const isOpen = (idx) => openKeys.has(blockKeys.value[idx])

function setAllOpen(open) {
  openKeys.clear()
  if (open) blockKeys.value.forEach((k) => openKeys.add(k))
}

function toggleInsertMenu(at) {
  insertMenuAt.value = insertMenuAt.value === at ? null : at
}

function insertBlock(afterIdx, type) {
  const at = afterIdx + 1
  const key = newKey()
  draft.value.contentJson.splice(at, 0, newBlock(type))
  blockKeys.value.splice(at, 0, key)
  openKeys.add(key)
  insertMenuAt.value = null
  selectBlock(at, 'card')
}

function moveBlock(idx, delta) {
  const target = idx + delta
  const list = draft.value.contentJson
  if (target < 0 || target >= list.length) return
  const [block] = list.splice(idx, 1)
  list.splice(target, 0, block)
  const [key] = blockKeys.value.splice(idx, 1)
  blockKeys.value.splice(target, 0, key)
  if (activeIndex.value === idx) activeIndex.value = target
  else if (activeIndex.value === target) activeIndex.value = idx
}

function duplicateBlock(idx) {
  const key = newKey()
  draft.value.contentJson.splice(idx + 1, 0, clone(draft.value.contentJson[idx]))
  blockKeys.value.splice(idx + 1, 0, key)
  openKeys.add(key)
  selectBlock(idx + 1, 'card')
}

function removeBlock(idx) {
  const block = draft.value.contentJson[idx]
  if (blockSummary(block) !== '(trống)' && !window.confirm(`Xoá khối ${idx + 1} (${typeMeta(block.type).label})?`)) return
  draft.value.contentJson.splice(idx, 1)
  const [key] = blockKeys.value.splice(idx, 1)
  openKeys.delete(key)
  if (activeIndex.value === idx) activeIndex.value = -1
  else if (activeIndex.value > idx) activeIndex.value -= 1
}

function onCardHeaderClick(idx) {
  const key = blockKeys.value[idx]
  if (openKeys.has(key) && activeIndex.value === idx) {
    openKeys.delete(key)
    return
  }
  openKeys.add(key)
  selectBlock(idx, 'card')
}

async function selectBlock(idx, source) {
  activeIndex.value = idx
  const key = blockKeys.value[idx]
  if (key) openKeys.add(key)
  await nextTick()
  if (source === 'preview') {
    document.getElementById(`block-card-${key}`)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    return
  }
  if (previewTab.value !== 'detail') return
  const scroller = previewScroller.value
  const target = scroller?.querySelector(`#section-${idx}`)
  if (!scroller || !target) return
  const offset = target.getBoundingClientRect().top - scroller.getBoundingClientRect().top
  scroller.scrollTo({ top: scroller.scrollTop + offset - 24, behavior: 'smooth' })
}

function addTableRow(block) {
  block.rows.push(Array.from({ length: Math.max(1, block.headers.length) }, () => ''))
}

function addTableCol(block) {
  block.headers.push('')
  block.rows.forEach((row) => row.push(''))
}

function removeTableCol(block, ci) {
  block.headers.splice(ci, 1)
  block.rows.forEach((row) => row.splice(ci, 1))
}

// ---------- validation & save ----------
const problems = computed(() => {
  const d = draft.value
  const list = []
  if (!d.title.trim()) list.push('Tiêu đề đang trống.')
  if (!slugLocked.value && slugError.value) list.push(slugError.value)
  if (!d.excerpt.trim()) list.push('Mô tả ngắn đang trống.')
  d.contentJson.forEach((b, idx) => {
    if (b.type === 'img' && !b.src) list.push(`Khối ${idx + 1} (Ảnh) chưa có ảnh.`)
    else if (b.type === 'img' && !b.src.startsWith('/images/')) list.push(`Khối ${idx + 1} (Ảnh) có đường dẫn không hợp lệ, hãy tải ảnh lại.`)
  })
  if (d.status === 'PUBLISHED' && d.showOnHome) {
    if (!d.homeOrder) list.push('Chọn vị trí trên Trang chủ (1 đến 3) hoặc bỏ tick "Hiện trên Trang chủ".')
    else if (takenBy(d.homeOrder)) list.push(`Vị trí ${d.homeOrder} trên Trang chủ đã có bài khác.`)
  }
  return list
})

function buildPayload() {
  const d = draft.value
  const onHome = d.status === 'PUBLISHED' && d.showOnHome
  return {
    slug: d.slug.trim(),
    title: d.title.trim(),
    excerpt: d.excerpt.trim(),
    coverImage: d.coverImage || null,
    contentJson: d.contentJson.map((b) =>
      b.type === 'ul' ? { ...b, items: (b.items || []).filter((i) => (i || '').trim()) } : b
    ),
    status: d.status,
    showOnHome: onHome,
    homeOrder: onHome ? d.homeOrder : null,
    updatedAt: saved.value?.updatedAt ?? null,
  }
}

/** The shared http client drops the server body on non-2xx, so map status codes to readable text. */
function errorText(e) {
  const raw = e?.message || ''
  conflict.value = /status:\s*409/.test(raw)
  if (conflict.value) return 'Bài viết vừa được người khác cập nhật. Tải lại bản mới nhất rồi sửa tiếp (thay đổi chưa lưu ở đây sẽ mất).'
  if (/status:\s*400/.test(raw)) return 'Máy chủ từ chối dữ liệu. Thường do slug đã tồn tại, slug đổi khi bài đang xuất bản, hoặc vị trí Trang chủ vừa bị bài khác chiếm.'
  if (/status:\s*40[13]/.test(raw)) return 'Phiên đăng nhập hết hạn hoặc không có quyền. Đăng nhập lại rồi thử lại.'
  if (/status:\s*404/.test(raw)) return 'Không tìm thấy bài viết (có thể đã bị xoá).'
  return raw || 'Lưu thất bại'
}

async function save() {
  message.value = ''
  messageError.value = false
  conflict.value = false
  if (problems.value.length) {
    messageError.value = true
    message.value = 'Chưa lưu được, xem danh sách cần sửa bên dưới.'
    return false
  }
  saving.value = true
  try {
    const payload = buildPayload()
    if (isNew.value) {
      const created = await contentService.createPost(payload)
      applySaved(created)
      router.replace(`/admin/posts/${created.id}`)
      message.value = created.status === 'PUBLISHED' ? 'Đã tạo và xuất bản bài viết.' : 'Đã tạo bài viết (nháp).'
    } else {
      const updated = await contentService.updatePost(postId.value, payload)
      applySaved(updated)
      message.value = updated.status === 'PUBLISHED' ? 'Đã lưu. Trang công khai đã cập nhật.' : 'Đã lưu bản nháp.'
    }
    loadOtherPosts()
    return true
  } catch (e) {
    messageError.value = true
    message.value = errorText(e)
    return false
  } finally {
    saving.value = false
  }
}

async function saveWithStatus(status) {
  const prev = { status: draft.value.status, showOnHome: draft.value.showOnHome, homeOrder: draft.value.homeOrder }
  if (status === 'PUBLISHED' && !window.confirm('Lưu và xuất bản? Bài sẽ hiện công khai ở trang Blog.')) return
  if (status === 'DRAFT' && !window.confirm('Chuyển bài về Nháp? Bài sẽ biến mất khỏi trang Blog và Trang chủ.')) return
  setStatus(status)
  const ok = await save()
  if (!ok) Object.assign(draft.value, prev)
}

function revert() {
  const d = JSON.parse(snapshot.value)
  const keepKeys = d.contentJson.length === blockKeys.value.length
  draft.value = d
  if (!keepKeys) {
    blockKeys.value = d.contentJson.map(newKey)
    openKeys.clear()
  }
  if (activeIndex.value >= d.contentJson.length) activeIndex.value = -1
  message.value = ''
}

// ---------- loading ----------
async function loadOtherPosts() {
  otherPostsError.value = ''
  try {
    const posts = await contentService.getAdminPosts(false)
    otherPosts.value = (posts || []).filter((p) => p.id !== postId.value)
  } catch (_) {
    otherPosts.value = []
    otherPostsError.value = 'Không tải được danh sách bài khác, chưa biết vị trí nào đã có bài.'
  }
}

async function load() {
  loading.value = true
  loadError.value = ''
  message.value = ''
  conflict.value = false
  blockKeys.value = []
  openKeys.clear()
  activeIndex.value = -1
  try {
    if (route.params.id === 'new') {
      postId.value = null
      saved.value = null
      draft.value = emptyDraft()
      snapshot.value = JSON.stringify(draft.value)
    } else {
      applySaved(await contentService.getAdminPost(route.params.id))
    }
  } catch (e) {
    loadError.value = e.message || 'Không tải được bài viết'
  } finally {
    loading.value = false
  }
  loadOtherPosts()
}

async function reloadAfterConflict() {
  await load()
}

function formatDate(value) {
  try {
    return new Date(value).toLocaleDateString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

function formatDateTime(value) {
  try {
    return new Date(value).toLocaleString('vi-VN')
  } catch (_) {
    return String(value)
  }
}

// ---------- leave guards ----------
const LEAVE_TEXT = 'Bài viết có thay đổi chưa lưu. Rời trang sẽ mất các thay đổi này. Tiếp tục?'

function onBeforeUnload(event) {
  if (!dirty.value) return
  event.preventDefault()
  event.returnValue = ''
}

onBeforeRouteLeave(() => !dirty.value || window.confirm(LEAVE_TEXT))
onBeforeRouteUpdate((to, from) => {
  if (to.params.id === from.params.id) return true
  // Our own replace after creating a post lands here with a clean draft.
  if (String(to.params.id) === String(postId.value)) return true
  return !dirty.value || window.confirm(LEAVE_TEXT)
})

watch(
  () => route.params.id,
  (id, oldId) => {
    if (oldId === undefined || id === undefined) return
    if (String(id) === String(postId.value)) return
    load()
  }
)

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  load()
})
onBeforeUnmount(() => window.removeEventListener('beforeunload', onBeforeUnload))
</script>

<style scoped>
.icon-btn {
  @apply w-6 h-6 rounded text-xs flex items-center justify-center hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent;
}
/* Blog images have no packaged default, so hide the uploader's "Dùng ảnh mặc định" toggle. */
.no-default-toggle :deep(.cms-uploader > label) {
  display: none;
}
</style>
