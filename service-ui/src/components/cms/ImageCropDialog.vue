<template>
  <Teleport to="body">
    <div class="fixed inset-0 z-[1000] bg-black/60 flex items-center justify-center p-4" @keydown.esc="cancel" tabindex="-1" ref="root">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl p-4 md:p-5">
        <div class="flex items-start justify-between gap-3 mb-3">
          <div>
            <h3 class="font-bold text-gray-800">Chọn vùng hiển thị</h3>
            <p class="text-xs text-gray-500 mt-0.5">
              Ảnh {{ loaded.width }}×{{ loaded.height }} khác tỷ lệ khung {{ slot.label }} ({{ slot.outputWidth }}×{{ slot.outputHeight }}).
              Kéo ảnh để chọn vị trí, cuộn chuột hoặc kéo thanh trượt để phóng to. Khung dưới đây là đúng phần sẽ hiển thị.
            </p>
          </div>
          <button type="button" class="text-gray-400 hover:text-gray-700 text-xl leading-none" aria-label="Đóng" @click="cancel">×</button>
        </div>

        <div class="flex justify-center">
          <div
            class="relative overflow-hidden rounded-lg border-2 border-green-600 select-none touch-none cursor-move"
            :class="transparentBg ? 'crop-checker' : 'bg-white'"
            :style="{ width: frameW + 'px', height: frameH + 'px' }"
            @pointerdown="onPointerDown"
            @pointermove="onPointerMove"
            @pointerup="onPointerUp"
            @pointercancel="onPointerUp"
            @wheel.prevent="onWheel"
          >
            <img
              :src="objectUrl"
              alt=""
              draggable="false"
              class="absolute top-0 left-0 max-w-none pointer-events-none"
              :style="{ width: loaded.width * scale + 'px', height: loaded.height * scale + 'px', transform: `translate(${ox}px, ${oy}px)` }"
            />
          </div>
        </div>

        <div class="mt-4 flex items-center gap-3">
          <span class="text-xs text-gray-500 w-14">Thu nhỏ</span>
          <input
            type="range"
            class="flex-1 accent-green-600"
            :min="minScale"
            :max="maxScale"
            :step="(maxScale - minScale) / 200 || 0.001"
            :value="scale"
            @input="zoomTo(Number($event.target.value))"
          />
          <span class="text-xs text-gray-500 w-14 text-right">Phóng to</span>
        </div>

        <div class="mt-3 flex flex-wrap gap-2">
          <button type="button" class="px-3 py-1.5 text-xs rounded border font-semibold hover:bg-gray-50" @click="applyFit">
            Vừa toàn bộ ảnh (không cắt)
          </button>
          <button type="button" class="px-3 py-1.5 text-xs rounded border font-semibold hover:bg-gray-50" @click="applyCover">
            Lấp đầy khung
          </button>
        </div>

        <p v-if="hasGaps" class="mt-2 text-xs text-gray-600">
          Phần khung không có ảnh sẽ là {{ transparentBg ? 'nền trong suốt' : 'nền trắng' }}.
        </p>
        <p v-if="blurry" class="mt-2 text-xs text-amber-700">
          Ảnh đang bị phóng to {{ upscale.toFixed(1) }} lần so với gốc nên có thể bị mờ. Nên thu nhỏ hoặc dùng ảnh lớn hơn.
        </p>

        <div class="mt-4 flex justify-end gap-2 border-t pt-3">
          <button type="button" class="px-4 py-2 text-sm rounded border hover:bg-gray-50" @click="cancel">Huỷ</button>
          <button type="button" class="px-4 py-2 text-sm rounded bg-green-600 text-white font-semibold hover:bg-green-700" @click="confirm">
            Dùng vùng này
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { BLUR_UPSCALE_LIMIT, outputMimeForSlot } from '../../utils/imageSlotPipeline.js'

const props = defineProps({
  // Kết quả loadImageForSlot: { img, slot, width, height }
  loaded: { type: Object, required: true },
  file: { type: File, required: true },
})
const emit = defineEmits(['confirm', 'cancel'])

const root = ref(null)
const slot = props.loaded.slot
const objectUrl = URL.createObjectURL(props.file)
const transparentBg = outputMimeForSlot(props.file, slot) === 'image/png'

// Khung hiển thị đúng tỷ lệ slot, vừa màn hình
const viewportW = typeof window !== 'undefined' ? window.innerWidth : 1024
const viewportH = typeof window !== 'undefined' ? window.innerHeight : 768
const frameW = Math.round(Math.min(560, viewportW - 72, viewportH * 0.55 * slot.aspectRatio))
const frameH = Math.round(frameW / slot.aspectRatio)

const { width: iw, height: ih } = props.loaded
const fitScale = Math.min(frameW / iw, frameH / ih)
const coverScale = Math.max(frameW / iw, frameH / ih)
const minScale = fitScale
const maxScale = coverScale * 4

const scale = ref(coverScale)
const ox = ref((frameW - iw * coverScale) / 2)
const oy = ref((frameH - ih * coverScale) / 2)

// Pixel output trên mỗi pixel gốc ở mức zoom hiện tại
const outputPerFrame = slot.outputWidth / frameW
const upscale = computed(() => scale.value * outputPerFrame)
const blurry = computed(() => upscale.value > BLUR_UPSCALE_LIMIT)
const hasGaps = computed(() => iw * scale.value < frameW - 0.5 || ih * scale.value < frameH - 0.5)

function clampAxis(pos, size, frame) {
  // Ảnh lớn hơn khung: không để lộ khoảng trống; nhỏ hơn khung: không để ảnh lọt ra ngoài
  return size >= frame ? Math.min(0, Math.max(frame - size, pos)) : Math.max(0, Math.min(frame - size, pos))
}

function setPosition(x, y) {
  ox.value = clampAxis(x, iw * scale.value, frameW)
  oy.value = clampAxis(y, ih * scale.value, frameH)
}

/** Zoom giữ nguyên điểm (cx, cy) của khung, mặc định tâm khung. */
function zoomTo(next, cx = frameW / 2, cy = frameH / 2) {
  const s = Math.min(maxScale, Math.max(minScale, next))
  const ratio = s / scale.value
  const x = cx - (cx - ox.value) * ratio
  const y = cy - (cy - oy.value) * ratio
  scale.value = s
  setPosition(x, y)
}

function applyFit() {
  scale.value = fitScale
  setPosition((frameW - iw * fitScale) / 2, (frameH - ih * fitScale) / 2)
}

function applyCover() {
  scale.value = coverScale
  setPosition((frameW - iw * coverScale) / 2, (frameH - ih * coverScale) / 2)
}

let drag = null
function onPointerDown(e) {
  drag = { id: e.pointerId, x: e.clientX, y: e.clientY, ox: ox.value, oy: oy.value }
  e.currentTarget.setPointerCapture(e.pointerId)
}
function onPointerMove(e) {
  if (!drag || drag.id !== e.pointerId) return
  setPosition(drag.ox + e.clientX - drag.x, drag.oy + e.clientY - drag.y)
}
function onPointerUp(e) {
  if (drag && drag.id === e.pointerId) drag = null
}
function onWheel(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  zoomTo(scale.value * (e.deltaY < 0 ? 1.08 : 1 / 1.08), e.clientX - rect.left, e.clientY - rect.top)
}

function confirm() {
  const k = outputPerFrame
  emit('confirm', { dx: ox.value * k, dy: oy.value * k, dw: iw * scale.value * k, dh: ih * scale.value * k })
}
function cancel() {
  emit('cancel')
}

onMounted(() => root.value?.focus())
onBeforeUnmount(() => URL.revokeObjectURL(objectUrl))

defineExpose({ applyFit, applyCover, zoomTo })
</script>

<style scoped>
.crop-checker {
  background-color: #fff;
  background-image:
    linear-gradient(45deg, #e5e7eb 25%, transparent 25%),
    linear-gradient(-45deg, #e5e7eb 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #e5e7eb 75%),
    linear-gradient(-45deg, transparent 75%, #e5e7eb 75%);
  background-size: 16px 16px;
  background-position: 0 0, 0 8px, 8px -8px, -8px 0;
}
</style>
