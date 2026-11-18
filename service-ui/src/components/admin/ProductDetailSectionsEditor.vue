<template>
  <div class="space-y-4">
    <p class="text-xs text-gray-600 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
      Mỗi dòng là một mục trên trang. Dòng bắt đầu bằng <code># </code> là tiêu đề con (ví dụ tên từng sản phẩm trong combo),
      dòng bắt đầu bằng <code>&gt; </code> là dòng chú thích. Để trống thì mục đó không hiện trên trang.
    </p>

    <div>
      <label class="block font-semibold mb-1 text-gray-700">Video giới thiệu sản phẩm</label>
      <input
        :value="modelValue.videoUrl"
        @input="update('videoUrl', $event.target.value)"
        class="w-full border rounded-lg px-3 py-2"
        :class="videoError ? 'border-red-500' : ''"
        placeholder="https://www.youtube.com/watch?v=... hoặc /video/intro.mp4"
      />
      <p v-if="videoError" class="text-xs text-red-600 mt-1">{{ videoError }}</p>
      <p v-else class="text-xs text-gray-500 mt-1">
        Dán link YouTube hoặc đường dẫn file mp4. Để trống thì ẩn khối video.
        <span v-if="video" class="text-green-700">Nhận dạng: {{ video.kind === 'youtube' ? 'video YouTube' : 'file video' }}.</span>
      </p>
    </div>

    <div v-for="section in sections" :key="section.key">
      <label class="block font-semibold mb-1 text-gray-700">{{ section.title }}</label>
      <textarea
        :value="modelValue[section.key]"
        @input="update(section.key, $event.target.value)"
        class="w-full border rounded-lg px-3 py-2 text-sm"
        :rows="rowsFor(modelValue[section.key])"
      ></textarea>
      <p v-if="section.hint" class="text-xs text-gray-500 mt-1">{{ section.hint }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PRODUCT_SECTIONS, productVideo, videoUrlError } from '../../utils/productSections.js'

const props = defineProps({
  // Object phẳng có videoUrl và 8 trường văn bản (PRODUCT_SECTION_KEYS)
  modelValue: { type: Object, required: true },
})
const emit = defineEmits(['update:modelValue'])

const sections = PRODUCT_SECTIONS
const videoError = computed(() => videoUrlError(props.modelValue.videoUrl))
const video = computed(() => productVideo(props.modelValue.videoUrl))

function update(key, value) {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
}

function rowsFor(text) {
  const lines = String(text || '').split('\n').length
  return Math.min(Math.max(lines + 1, 2), 12)
}
</script>
