<template>
  <div class="cms-uploader border border-dashed rounded-lg p-3 bg-gray-50">
    <div class="flex items-start justify-between gap-2 mb-2">
      <div>
        <div class="font-semibold text-sm text-gray-800">{{ label || slotMeta.label }}</div>
        <div class="text-xs text-gray-500 mt-0.5">{{ hint }}</div>
      </div>
      <span
        v-if="statusBadge"
        class="text-xs px-2 py-0.5 rounded-full font-semibold"
        :class="statusBadgeClass"
      >{{ statusBadge }}</span>
    </div>

    <label v-if="hasDefaultToggle" class="flex items-center gap-2 text-sm mb-2 cursor-pointer select-none">
      <input
        type="checkbox"
        :checked="useDefault"
        :disabled="loading || disabled"
        @change="onUseDefaultChange"
      />
      <span>Dùng ảnh mặc định</span>
    </label>

    <div
      class="relative w-full bg-white border rounded-lg overflow-hidden mb-2 flex items-center justify-center"
      :style="previewBoxStyle"
    >
      <template v-if="usingDefault">
        <img
          v-if="defaultSrc"
          :src="defaultSrc"
          :alt="`${label || slotMeta.label} (mặc định)`"
          class="max-w-full max-h-full object-contain"
        />
        <div
          v-else
          class="text-xs text-amber-700 bg-amber-50 w-full h-full flex items-center justify-center p-4 text-center font-semibold"
        >
          Đang dùng ảnh mặc định
        </div>
        <span
          v-if="defaultSrc"
          class="absolute top-1 left-1 text-[10px] px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 font-semibold"
        >Ảnh mặc định</span>
      </template>
      <img
        v-else-if="displayUrl"
        :src="displayUrl"
        :alt="label || slotMeta.label"
        class="max-w-full max-h-full object-contain"
      />
      <div v-else class="text-xs text-gray-400 p-4 text-center">Chưa có ảnh (màn hình sẽ trống)</div>
      <div v-if="loading" class="absolute inset-0 bg-white/70 flex items-center justify-center text-xs">
        Đang xử lý...
      </div>
    </div>

    <p v-if="error" class="text-xs text-red-600 mb-2">{{ error }}</p>

    <div v-if="!usingDefault" class="flex gap-2">
      <input ref="fileInput" type="file" class="hidden" :accept="accept" @change="onFileChange" />
      <button
        type="button"
        class="flex-1 px-3 py-1.5 text-xs rounded font-semibold bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
        :disabled="loading || disabled"
        @click="fileInput?.click()"
      >
        {{ modelValue ? 'Thay đổi ảnh' : 'Chọn ảnh' }}
      </button>
      <button
        v-if="modelValue && clearable"
        type="button"
        class="px-3 py-1.5 text-xs rounded font-semibold bg-red-500 text-white hover:bg-red-600"
        :disabled="loading || disabled"
        @click="clear"
      >
        Xóa
      </button>
    </div>
    <ImageCropDialog
      v-if="cropping"
      :loaded="cropping.loaded"
      :file="cropping.file"
      @confirm="onCropConfirm"
      @cancel="onCropCancel"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getImageSlot } from '../../constants/imageSlots.js'
import { formatSlotHint, loadImageForSlot, renderImageForSlot } from '../../utils/imageSlotPipeline.js'
import ImageCropDialog from './ImageCropDialog.vue'
import { getImageUrlFromApi } from '../../utils/imageUtils.js'
import { contentService } from '../../services/content.service.js'

const props = defineProps({
  modelValue: { type: String, default: '' },
  /**
   * Bind with v-model:use-default when the slot has a packaged default image (CMS pages).
   * Left unbound (product images): no "Dùng ảnh mặc định" toggle, the uploader always allows upload.
   * The default must stay `undefined` so an absent prop is told apart from `false`.
   */
  useDefault: { type: Boolean, default: undefined },
  slotKey: { type: String, required: true },
  label: { type: String, default: '' },
  entityType: { type: String, default: 'cms' },
  entityId: { type: String, default: 'common' },
  disabled: { type: Boolean, default: false },
  clearable: { type: Boolean, default: true },
  /** Resolved URL of the packaged default image, previewed while "Dùng ảnh mặc định" is on. */
  defaultSrc: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'update:useDefault', 'uploaded', 'error'])

const fileInput = ref(null)
const loading = ref(false)
const error = ref('')
const localPreview = ref('')
// Ảnh lệch tỷ lệ slot đang chờ admin chọn vùng hiển thị: { file, loaded }
const cropping = ref(null)

const hasDefaultToggle = computed(() => props.useDefault !== undefined)
const usingDefault = computed(() => hasDefaultToggle.value && props.useDefault === true)

const slotMeta = computed(() => getImageSlot(props.slotKey))
const hint = computed(() => formatSlotHint(props.slotKey))
const accept = computed(() => slotMeta.value.mime.join(','))

const displayUrl = computed(() => {
  if (usingDefault.value) return ''
  if (localPreview.value) return localPreview.value
  if (!props.modelValue) return ''
  if (props.modelValue.startsWith('data:') || props.modelValue.startsWith('blob:') || props.modelValue.startsWith('http')) {
    return props.modelValue
  }
  return getImageUrlFromApi(props.modelValue)
})

const previewBoxStyle = computed(() => {
  const ratio = slotMeta.value.aspectRatio
  return {
    aspectRatio: String(ratio),
    maxHeight: '220px',
  }
})

const statusBadge = computed(() => {
  if (error.value) return 'Sai mẫu'
  if (usingDefault.value) return 'Mặc định'
  if (props.modelValue) return `Đạt chuẩn ${slotMeta.value.outputWidth}×${slotMeta.value.outputHeight}`
  return 'Trống'
})

const statusBadgeClass = computed(() => {
  if (error.value) return 'bg-red-100 text-red-700'
  if (usingDefault.value) return 'bg-amber-100 text-amber-800'
  if (props.modelValue) return 'bg-green-100 text-green-700'
  return 'bg-gray-100 text-gray-600'
})

watch(
  () => props.modelValue,
  () => {
    localPreview.value = ''
    error.value = ''
  }
)

function onUseDefaultChange(event) {
  const checked = !!event.target.checked
  emit('update:useDefault', checked)
  if (checked) {
    localPreview.value = ''
    error.value = ''
    emit('update:modelValue', '')
  }
}

async function onFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return

  loading.value = true
  error.value = ''
  let loaded
  try {
    loaded = await loadImageForSlot(file, props.slotKey)
  } catch (e) {
    fail(e)
    loading.value = false
    return
  }
  if (loaded.needsCrop) {
    // Không tự cắt giữa: admin chọn vùng hoặc "Vừa toàn bộ ảnh", để không mất nội dung ở mép
    cropping.value = { file, loaded }
    return
  }
  await upload(file, loaded, null)
}

async function onCropConfirm(placement) {
  const { file, loaded } = cropping.value
  cropping.value = null
  await upload(file, loaded, placement)
}

function onCropCancel() {
  cropping.value = null
  loading.value = false
}

async function upload(file, loaded, placement) {
  loading.value = true
  try {
    const processed = await renderImageForSlot(loaded, file, placement)
    localPreview.value = processed.previewUrl
    const path = await contentService.uploadCmsImage(processed.file, props.slotKey, props.entityType, props.entityId)
    emit('update:useDefault', false)
    emit('update:modelValue', path)
    emit('uploaded', path)
  } catch (e) {
    fail(e)
    localPreview.value = ''
  } finally {
    loading.value = false
  }
}

function fail(e) {
  error.value = e.message || 'Upload thất bại'
  emit('error', error.value)
}

function clear() {
  localPreview.value = ''
  error.value = ''
  emit('update:useDefault', false)
  emit('update:modelValue', '')
}
</script>
