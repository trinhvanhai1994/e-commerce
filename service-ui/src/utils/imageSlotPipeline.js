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

function isAspectOk(width, height, slot) {
  const actual = width / height
  const diff = Math.abs(actual - slot.aspectRatio)
  return diff <= slot.aspectTolerance || diff <= slot.aspectTolerance * slot.aspectRatio
}

/**
 * Center-crop to slot aspect, then resize to output WxH. Returns a PNG or JPEG File (see mime choice below).
 */
export async function processImageForSlot(file, slotKey) {
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
  if (img.width < slot.minWidth || (slot.minHeight > 0 && img.height < slot.minHeight)) {
    throw new Error(
      `Ảnh quá nhỏ. Tối thiểu ${slot.minWidth}x${slot.minHeight || '?'}, hiện tại ${img.width}x${img.height}`
    )
  }

  // Reject extreme aspect before crop if user image is wildly off and would upscale crop too much
  const targetAspect = slot.aspectRatio
  let cropW = img.width
  let cropH = img.height
  if (img.width / img.height > targetAspect) {
    cropW = Math.round(img.height * targetAspect)
  } else {
    cropH = Math.round(img.width / targetAspect)
  }
  const sx = Math.floor((img.width - cropW) / 2)
  const sy = Math.floor((img.height - cropH) / 2)

  if (cropW < slot.minWidth * 0.9) {
    throw new Error(
      `Sau khi cắt theo tỷ lệ ${slot.aspectRatio.toFixed(2)}, ảnh còn quá nhỏ (${cropW}x${cropH}). Hãy dùng ảnh đúng mẫu hơn.`
    )
  }

  const canvas = document.createElement('canvas')
  canvas.width = slot.outputWidth
  canvas.height = slot.outputHeight
  const ctx = canvas.getContext('2d')
  ctx.drawImage(img, sx, sy, cropW, cropH, 0, 0, slot.outputWidth, slot.outputHeight)

  // Keep PNG for PNG sources; use JPEG otherwise, except for slots that do not accept JPEG (icons: png/webp),
  // where a WebP source must be re-encoded to PNG or the server rejects it.
  const pngOnly = !slot.mime.includes('image/jpeg')
  const mime =
    (file.type === 'image/png' || pngOnly) && slot.mime.includes('image/png') ? 'image/png' : 'image/jpeg'
  const blob = await new Promise((resolve, reject) => {
    canvas.toBlob(
      (b) => (b ? resolve(b) : reject(new Error('Không xuất được ảnh'))),
      mime,
      0.85
    )
  })

  if (!isAspectOk(slot.outputWidth, slot.outputHeight, slot)) {
    throw new Error('Lỗi nội bộ: output không đúng tỷ lệ slot')
  }

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

export function formatSlotHint(slotKey) {
  const slot = getImageSlot(slotKey)
  return `${slot.label}: ${slot.outputWidth}×${slot.outputHeight} (tỷ lệ ~${slot.aspectRatio.toFixed(2)}), tối đa ${(slot.maxBytes / 1024).toFixed(0)}KB`
}
