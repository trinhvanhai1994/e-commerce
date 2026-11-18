<template>
  <article class="prose prose-pink max-w-none">
    <div
      v-for="(block, idx) in blocks"
      :key="idx"
      :id="`section-${idx}`"
      class="scroll-mt-24"
      :class="selectable ? selectableClass(idx) : null"
      @click.capture="onBlockClick($event, idx)"
    >
      <div v-if="selectable && isEmpty(block)" class="not-prose my-2 rounded border border-dashed border-gray-300 p-3 text-sm italic text-gray-400">
        {{ emptyLabel(block) }}
      </div>
      <p v-else-if="block.type === 'p'" :class="block.class">{{ block.text }}</p>
      <h2 v-else-if="block.type === 'h2'" class="scroll-mt-24">{{ block.text }}</h2>
      <ul v-else-if="block.type === 'ul'" class="list-disc pl-5">
        <li v-for="(li, i) in block.items" :key="i">{{ li }}</li>
      </ul>
      <img v-else-if="block.type === 'img'" :src="block.src" :alt="block.alt || ''" class="w-full h-auto object-contain rounded-lg my-6" />
      <p v-else-if="block.type === 'cta'">
        <strong>{{ block.bold }}</strong>
        <router-link :to="block.to || '/'" class="text-green-600 hover:underline font-semibold">{{ block.linkText }}</router-link>
      </p>
      <div v-else-if="block.type === 'table'" class="overflow-x-auto my-6">
        <table class="w-full border border-gray-300 text-sm">
          <thead>
            <tr>
              <th v-for="(h, hi) in block.headers" :key="hi" class="border border-gray-300 bg-gray-50 p-2 text-left font-semibold">{{ h }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, ri) in block.rows" :key="ri">
              <td v-for="(cell, ci) in row" :key="ci" class="border border-gray-300 p-2 align-top">{{ cell }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </article>
</template>

<script setup>
/**
 * Renders blog `contentJson` blocks. Used by the public detail page and by the admin editor preview.
 * Callers pass blocks whose img src is already resolved to a displayable URL.
 */
const props = defineProps({
  blocks: { type: Array, default: () => [] },
  activeIndex: { type: Number, default: -1 },
  selectable: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const EMPTY_LABELS = {
  p: 'Đoạn văn trống',
  h2: 'Tiêu đề H2 trống',
  ul: 'Danh sách trống',
  img: 'Chưa chọn ảnh',
  cta: 'Nút CTA chưa có chữ',
  table: 'Bảng trống',
}

function selectableClass(idx) {
  return [
    'cursor-pointer rounded-md transition',
    props.activeIndex === idx ? 'ring-2 ring-green-500 ring-offset-4' : 'hover:ring-1 hover:ring-green-300 hover:ring-offset-4',
  ]
}

function isEmpty(block) {
  if (!block) return true
  switch (block.type) {
    case 'p':
    case 'h2':
      return !(block.text || '').trim()
    case 'ul':
      return !(block.items || []).some((i) => (i || '').trim())
    case 'img':
      return !block.src
    case 'cta':
      return !(block.bold || '').trim() && !(block.linkText || '').trim()
    case 'table':
      return !(block.headers || []).length && !(block.rows || []).length
    default:
      return false
  }
}

function emptyLabel(block) {
  return EMPTY_LABELS[block?.type] || 'Khối trống'
}

function onBlockClick(event, idx) {
  if (!props.selectable) return
  // In the editor preview, links must not navigate away from the unsaved editor.
  event.preventDefault()
  emit('select', idx)
}
</script>
