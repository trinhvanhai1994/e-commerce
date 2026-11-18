// Task-006: 9 mục ở trang chi tiết sản phẩm. Mỗi mục là văn bản nhiều dòng trong một trường của sản phẩm:
// dòng "# " là tiêu đề con, dòng "> " là chú thích, còn lại mỗi dòng một mục. Mục không còn dòng nào thì ẩn.

/** style: plain = danh sách không dấu •, bullet = danh sách có dấu •, paragraph = mỗi dòng một đoạn. */
export const PRODUCT_SECTIONS = [
  { key: 'specifications', title: 'THÔNG TIN SẢN PHẨM', style: 'plain', hint: 'Ví dụ: Quy cách: Lon thiếc 600g' },
  { key: 'ingredients', title: 'THÀNH PHẦN', style: 'paragraph', hint: 'Đoạn văn liệt kê thành phần' },
  { key: 'benefits', title: 'CÔNG DỤNG', style: 'bullet', hint: 'Combo nhiều sản phẩm: dùng # để chia nhóm' },
  { key: 'targetUsers', title: 'ĐỐI TƯỢNG PHÙ HỢP', style: 'bullet', hint: 'Dòng > là chú thích chung, ví dụ độ tuổi' },
  { key: 'highlights', title: 'ƯU ĐIỂM NỔI BẬT', style: 'bullet', hint: '' },
  { key: 'technology', title: 'CÔNG NGHỆ VƯỢT TRỘI TỐI ƯU DƯỠNG CHẤT', style: 'bullet', hint: '' },
  { key: 'storage', title: 'HƯỚNG DẪN BẢO QUẢN', style: 'paragraph', hint: '' },
  { key: 'shelfLife', title: 'HẠN SỬ DỤNG', style: 'paragraph', hint: 'Ví dụ: 10 tháng từ ngày sản xuất' },
]

export const PRODUCT_SECTION_KEYS = ['videoUrl', ...PRODUCT_SECTIONS.map((s) => s.key)]

/** Văn bản nhiều dòng thành [{ type: 'heading' | 'note' | 'item', text }]; bỏ dòng trống. */
export function parseSectionLines(text) {
  if (!text) return []
  return String(text)
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      if (line.startsWith('# ')) return { type: 'heading', text: line.slice(2).trim() }
      if (line.startsWith('> ')) return { type: 'note', text: line.slice(2).trim() }
      return { type: 'item', text: line }
    })
    .filter((line) => line.text)
}

/** Các mục có nội dung của một sản phẩm, đúng thứ tự trên trang. */
export function productDetailSections(product) {
  if (!product) return []
  return PRODUCT_SECTIONS.map((section) => ({ ...section, lines: parseSectionLines(product[section.key]) }))
    .filter((section) => section.lines.length > 0)
}

const YOUTUBE_PATTERNS = [
  /^https:\/\/(?:www\.|m\.)?youtube\.com\/watch\?(?:.*&)?v=([A-Za-z0-9_-]{6,20})/,
  /^https:\/\/(?:www\.|m\.)?youtube\.com\/(?:shorts|embed|live)\/([A-Za-z0-9_-]{6,20})/,
  /^https:\/\/youtu\.be\/([A-Za-z0-9_-]{6,20})/,
]

/** Link video thành { kind: 'youtube' | 'file', src } hoặc null khi trống/không hợp lệ. */
export function productVideo(url) {
  const value = (url || '').trim()
  if (!value) return null
  for (const pattern of YOUTUBE_PATTERNS) {
    const match = value.match(pattern)
    if (match) return { kind: 'youtube', src: `https://www.youtube-nocookie.com/embed/${match[1]}` }
  }
  if (value.startsWith('https://') || value.startsWith('/video/')) return { kind: 'file', src: value }
  return null
}

/** Cùng luật với BE (AdminService.normalizeVideoUrl), để báo lỗi ngay trên form. */
export function videoUrlError(url) {
  const value = (url || '').trim()
  if (!value) return ''
  if (value.length > 1000) return 'Link video tối đa 1000 ký tự'
  if (!(value.startsWith('https://') || value.startsWith('/video/')) || /\s/.test(value)) {
    return 'Link video phải bắt đầu bằng https:// hoặc /video/'
  }
  return ''
}
