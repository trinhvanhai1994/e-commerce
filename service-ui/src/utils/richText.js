/**
 * Minimal rich text for CMS paragraphs: HTML is escaped first, then **text** becomes <strong>text</strong>.
 * Safe to bind with v-html because no admin-provided markup survives escaping.
 */
const ESCAPES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }

export function escapeHtml(text) {
  return String(text ?? '').replace(/[&<>"']/g, (ch) => ESCAPES[ch])
}

export function renderRichText(text) {
  return escapeHtml(text).replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
}
