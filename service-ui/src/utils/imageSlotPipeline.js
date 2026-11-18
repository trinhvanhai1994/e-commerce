import { getImageSlot } from '../constants/imageSlots.js'

function loadImageFromFile(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('Không đọc được ảnh'))
    }
    img.src = url
  })
}

/** Ảnh đã đúng tỷ lệ slot (trong dung sai), không cần chọn vùng cắt. */
export function isAspectOk(width, height, slot) {
  const actual = width / height
  const diff = Math.abs(actual - slot.aspectRatio)
  return diff <= slot.aspectTolerance || diff <= slot.aspectTolerance * slot.aspectRatio
}

/** PNG giữ nền trong suốt; còn lại xuất JPEG (nền trắng). Slot không nhận JPEG (icon) luôn xuất PNG. */
export function outputMimeForSlot(file, slot) {
  const pngOnly = !slot.mime.includes('image/jpeg')
  return (file.type === 'image/png' || pngOnly) && slot.mime.includes('image/png') ? 'image/png' : 'image/jpeg'
}

/**
 * Bước 1: kiểm định dạng, dung lượng, kích thước tối thiểu. Trả về ảnh đã đọc để chọn vùng cắt hoặc xuất.
 */
export async function loadImageForSlot(file, slotKey) {
  const slot = getImageSlot(slotKey)
  if (!file || !file.type) {
    throw new Error('File không hợp lệ')
  }
  if (!slot.mime.includes(file.type)) {
    throw new Error(`Định dạng không hợp lệ cho ${slot.label}. Cho phép: ${slot.mime.join(', ')}`)
  }
  if (file.size > slot.maxBytes) {
    throw new Error(`File vượt quá ${(slot.maxBytes / 1024).toFixed(0)}KB cho ${slot.label}`)
  }
  const img = await loadImageFromFile(file)
  const iw = img.naturalWidth || img.width
  const ih = img.naturalHeight || img.height
  // Ảnh lệch tỷ lệ được xét theo cạnh tương ứng của khung, nên ảnh dọc vào banner ngang không bị loại oan
  const coverScale = Math.max(slot.outputWidth / iw, slot.outputHeight / ih)
  const fitScale = Math.min(slot.outputWidth / iw, slot.outputHeight / ih)
  if (iw < slot.minWidth && ih < (slot.minHeight || slot.minWidth / slot.aspectRatio)) {
    throw new Error(
      `Ảnh quá nhỏ. Tối thiểu ${slot.minWidth}x${slot.minHeight || Math.round(slot.minWidth / slot.aspectRatio)}, hiện tại ${iw}x${ih}`
    )
  }
  return { img, slot, width: iw, height: ih, coverScale, fitScale, needsCrop: !isAspectOk(iw, ih, slot) }
}

/** Vị trí ảnh trên khung xuất (toạ độ pixel output): lấp đầy khung, căn giữa. */
export function coverPlacement(width, height, slot) {
  const scale = Math.max(slot.outputWidth / width, slot.outputHeight / height)
  return centeredPlacement(width, height, slot, scale)
}

/** Vị trí ảnh trên khung xuất: thấy toàn bộ ảnh, phần thừa là nền. */
export function fitPlacement(width, height, slot) {
  const scale = Math.min(slot.outputWidth / width, slot.outputHeight / height)
  return centeredPlacement(width, height, slot, scale)
}

function centeredPlacement(width, height, slot, scale) {
  const dw = width * scale
  const dh = height * scale
  return { dx: (slot.outputWidth - dw) / 2, dy: (slot.outputHeight - dh) / 2, dw, dh }
}

/** Ảnh bị phóng to quá mức này (pixel output trên pixel gốc) sẽ mờ thấy rõ. */
export const BLUR_UPSCALE_LIMIT = 1.25

/**
 * Bước 2: vẽ ảnh lên khung đúng kích thước slot theo placement { dx, dy, dw, dh } (toạ độ output),
 * đúng như admin thấy ở hộp chọn vùng cắt. Phần khung không có ảnh: nền trắng (JPEG) hoặc trong suốt (PNG).
 */
export async function renderImageForSlot(loaded, file, placement) {
  const { img, slot, width, height } = loaded
  const place = placement || coverPlacement(width, height, slot)
  const mime = outputMimeForSlot(file, slot)

  const canvas = document.createElement('canvas')
  canvas.width = slot.outputWidth
  canvas.height = slot.outputHeight
  const ctx = canvas.getContext('2d')
  if (mime === 'image/jpeg') {
    // JPEG không có kênh alpha: không tô nền thì vùng trong suốt / vùng thừa thành màu đen
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }
  ctx.imageSmoothingEnabled = true
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(img, 0, 0, width, height, place.dx, place.dy, place.dw, place.dh)

  const blob = await new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Không xuất được ảnh'))), mime, 0.9)
  })

  const ext = mime === 'image/png' ? 'png' : 'jpg'
  const processed = new File([blob], `${slot.key}.${ext}`, { type: mime })
  return {
    file: processed,
    previewUrl: URL.createObjectURL(blob),
    width: slot.outputWidth,
    height: slot.outputHeight,
    slot,
  }
}

/**
 * Một bước, không hỏi người dùng (ảnh đánh giá của khách).
 * mode 'cover': lấp đầy khung, cắt giữa; 'fit': giữ toàn bộ ảnh, thêm nền.
 * Ảnh đúng tỷ lệ thì hai mode cho cùng kết quả.
 */
export async function processImageForSlot(file, slotKey, mode = 'cover') {
  const loaded = await loadImageForSlot(file, slotKey)
  const place = mode === 'fit'
    ? fitPlacement(loaded.width, loaded.height, loaded.slot)
    : coverPlacement(loaded.width, loaded.height, loaded.slot)
  return renderImageForSlot(loaded, file, place)
}

export function formatSlotHint(slotKey) {
  const slot = getImageSlot(slotKey)
  return `${slot.label}: ${slot.outputWidth}×${slot.outputHeight} (tỷ lệ ~${slot.aspectRatio.toFixed(2)}), tối đa ${(slot.maxBytes / 1024).toFixed(0)}KB`
}
