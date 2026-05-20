<template>
  <div ref="scrollContainerRef" class="h-full min-h-0 overflow-auto p-4 bg-gray-200">
    <div class="flex flex-col items-center gap-4">
      <canvas
        v-for="page in renderedPages"
        :key="page"
        :ref="(el) => setCanvasEl(page, el)"
        class="shadow-lg bg-white max-w-full"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount, nextTick } from 'vue'
import * as pdfjsLib from 'pdfjs-dist'
import pdfjsWorker from 'pdfjs-dist/build/pdf.worker.min.mjs?url'
import {
  MSG_PDF_INVALID_OR_EMPTY,
  MSG_PDF_MISA_JSON_INSTEAD,
  MSG_PDF_NOT_PDF_BYTES,
  MSG_PDF_VIEWER_FAILED,
  PDF_JSON_PREFIX,
  PDF_MAGIC_BYTE_D,
  PDF_MAGIC_BYTE_F,
  PDF_MAGIC_BYTE_P,
  PDF_MAGIC_BYTE_PERCENT,
  PDF_MIN_HEADER_LENGTH,
  PDF_PREVIEW_TEXT_LENGTH,
} from '../constants/meinvoice.constants.js'

pdfjsLib.GlobalWorkerOptions.workerSrc = pdfjsWorker

const props = defineProps({
  pdfData: {
    type: [ArrayBuffer, Uint8Array],
    default: null,
  },
})

const emit = defineEmits(['error'])

const scrollContainerRef = ref(null)
const renderedPages = ref([])
const canvasByPage = new Map()
let pdfDoc = null
let renderTasks = []
let loadingGeneration = 0

function setCanvasEl(pageNum, el) {
  if (el) {
    canvasByPage.set(pageNum, el)
  } else {
    canvasByPage.delete(pageNum)
  }
}

function toUint8Array(data) {
  if (!data) return null
  if (data instanceof Uint8Array) return data
  return new Uint8Array(data)
}

function isPdfBytes(bytes) {
  return (
    bytes?.length >= PDF_MIN_HEADER_LENGTH &&
    bytes[0] === PDF_MAGIC_BYTE_PERCENT &&
    bytes[1] === PDF_MAGIC_BYTE_P &&
    bytes[2] === PDF_MAGIC_BYTE_D &&
    bytes[3] === PDF_MAGIC_BYTE_F
  )
}

function assertPdfMagic(bytes) {
  if (!bytes?.length) {
    throw new Error(MSG_PDF_INVALID_OR_EMPTY)
  }
  if (!isPdfBytes(bytes)) {
    const preview = new TextDecoder().decode(bytes?.slice(0, PDF_PREVIEW_TEXT_LENGTH) ?? [])
    throw new Error(
      preview.startsWith(PDF_JSON_PREFIX) ? MSG_PDF_MISA_JSON_INSTEAD : MSG_PDF_NOT_PDF_BYTES
    )
  }
}

async function waitForCanvasElements(expected) {
  for (let i = 0; i < 30; i++) {
    if (canvasByPage.size >= expected) return
    await new Promise((resolve) => requestAnimationFrame(resolve))
    await nextTick()
  }
}

async function cancelRenders() {
  for (const task of renderTasks) {
    try {
      await task.cancel()
    } catch {
      /* ignore */
    }
  }
  renderTasks = []
}

async function destroyPdf() {
  await cancelRenders()
  if (pdfDoc) {
    try {
      await pdfDoc.destroy()
    } catch {
      /* ignore */
    }
    pdfDoc = null
  }
  renderedPages.value = []
  canvasByPage.clear()
}

async function renderAllPages() {
  if (!pdfDoc) return

  const containerWidth = scrollContainerRef.value?.clientWidth || 900
  const pageNums = Array.from({ length: pdfDoc.numPages }, (_, i) => i + 1)

  for (const pageNum of pageNums) {
    const canvas = canvasByPage.get(pageNum)
    if (!canvas) continue

    const page = await pdfDoc.getPage(pageNum)
    const context = canvas.getContext('2d')
    const unscaled = page.getViewport({ scale: 1 })
    const scale = Math.min(2, Math.max(0.5, (containerWidth - 32) / unscaled.width))
    const viewport = page.getViewport({ scale })

    canvas.width = Math.floor(viewport.width)
    canvas.height = Math.floor(viewport.height)

    const task = page.render({ canvasContext: context, viewport })
    renderTasks.push(task)
    await task.promise
  }
  renderTasks = []
}

async function loadPdf(data) {
  const gen = ++loadingGeneration
  await destroyPdf()

  const bytes = toUint8Array(data)
  if (!bytes) return

  try {
    assertPdfMagic(bytes)
    pdfDoc = await pdfjsLib.getDocument({ data: bytes }).promise
    if (gen !== loadingGeneration) return

    renderedPages.value = Array.from({ length: pdfDoc.numPages }, (_, i) => i + 1)
    await nextTick()
    await waitForCanvasElements(pdfDoc.numPages)
    await renderAllPages()
  } catch (e) {
    if (gen !== loadingGeneration) return
    emit('error', e?.message || MSG_PDF_VIEWER_FAILED)
  }
}

watch(
  () => props.pdfData,
  (data) => {
    if (data) {
      loadPdf(data)
    } else {
      destroyPdf()
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  loadingGeneration++
  destroyPdf()
})
</script>
