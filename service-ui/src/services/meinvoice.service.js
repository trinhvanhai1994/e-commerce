import { httpClient } from './http/client.js'
import {
  LOCAL_STORAGE_AUTH_TOKEN,
  MEINVOICE_API_BASE,
  MEINVOICE_LOOKUP_BY_ORDER,
  MEINVOICE_LOOKUP_BY_PANCAKE,
  MIME_TYPE_APPLICATION_PDF,
  MSG_PDF_DOWNLOAD_FAILED,
  MSG_PDF_EMPTY,
  MSG_PDF_NOT_PDF_BYTES,
  PDF_DOWNLOAD_FILENAME_FORMAT,
  PDF_MAGIC_BYTE_D,
  PDF_MAGIC_BYTE_F,
  PDF_MAGIC_BYTE_P,
  PDF_MAGIC_BYTE_PERCENT,
  PDF_MIN_HEADER_LENGTH,
  QUERY_PARAM_ORDER_ID,
  QUERY_PARAM_REF_ID,
  RESPONSE_FIELD_PDF_BASE64,
} from '../constants/meinvoice.constants.js'

function buildLookupQuery(by) {
  const param = by === MEINVOICE_LOOKUP_BY_PANCAKE ? MEINVOICE_LOOKUP_BY_PANCAKE : MEINVOICE_LOOKUP_BY_ORDER
  return `?by=${param}`
}

function base64ToArrayBuffer(base64) {
  const cleaned = String(base64).replace(/\s/g, '')
  const binary = atob(cleaned)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i)
  }
  return bytes.buffer
}

/**
 * Create MeInvoice draft (POST /webapp/insert) for an order.
 * @param {string} orderKey - orderId or pancake order id when by=pancake
 * @param {typeof MEINVOICE_LOOKUP_BY_ORDER | typeof MEINVOICE_LOOKUP_BY_PANCAKE} by
 */
export async function createDraftInvoice(orderKey, by = MEINVOICE_LOOKUP_BY_ORDER) {
  return httpClient.post(
    `${MEINVOICE_API_BASE}/orders/${encodeURIComponent(orderKey)}/draft-invoice${buildLookupQuery(by)}`,
    {}
  )
}

/**
 * Preview PDF via MeInvoice POST /webapp/preview (base64) — for PDF.js popup.
 * @param {string} orderKey
 * @param {string} refId
 * @param {typeof MEINVOICE_LOOKUP_BY_ORDER | typeof MEINVOICE_LOOKUP_BY_PANCAKE} by
 * @returns {Promise<ArrayBuffer>}
 */
export async function previewInvoicePdfArrayBuffer(orderKey, refId, by = MEINVOICE_LOOKUP_BY_ORDER) {
  const params = new URLSearchParams()
  params.set(QUERY_PARAM_REF_ID, refId)
  params.set('by', by === MEINVOICE_LOOKUP_BY_PANCAKE ? MEINVOICE_LOOKUP_BY_PANCAKE : MEINVOICE_LOOKUP_BY_ORDER)
  const data = await httpClient.post(
    `${MEINVOICE_API_BASE}/orders/${encodeURIComponent(orderKey)}/invoice-preview?${params.toString()}`,
    {}
  )
  const pdfBase64 = data?.[RESPONSE_FIELD_PDF_BASE64]
  if (!pdfBase64) {
    throw new Error(MSG_PDF_EMPTY)
  }
  return base64ToArrayBuffer(pdfBase64)
}

/**
 * Delete unpublished draft on MeInvoice (DELETE /webapp/delete via backend proxy).
 * @param {string} refId
 * @param {string} [orderId] - orders.order_id to clear meinvoice_invoiced locally
 */
function isPdfArrayBuffer(buffer) {
  if (!buffer || buffer.byteLength < PDF_MIN_HEADER_LENGTH) {
    return false
  }
  const bytes = new Uint8Array(buffer)
  return (
    bytes[0] === PDF_MAGIC_BYTE_PERCENT
    && bytes[1] === PDF_MAGIC_BYTE_P
    && bytes[2] === PDF_MAGIC_BYTE_D
    && bytes[3] === PDF_MAGIC_BYTE_F
  )
}

function triggerBrowserPdfDownload(blob, filename) {
  const objectUrl = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = filename
  anchor.rel = 'noopener'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}

/**
 * Download PDF via backend proxy → MeInvoice GET /webapp/viewrefid.
 * @param {string} refId
 */
export async function downloadInvoicePdf(refId) {
  const adapter = httpClient.getAdapter()
  if (!adapter?.buildUrl || !adapter?.buildQueryString) {
    throw new Error(MSG_PDF_DOWNLOAD_FAILED)
  }
  const url = `${adapter.buildUrl(`${MEINVOICE_API_BASE}/invoices/pdf`)}${adapter.buildQueryString({
    [QUERY_PARAM_REF_ID]: refId,
  })}`
  const token = localStorage.getItem(LOCAL_STORAGE_AUTH_TOKEN)
  const headers = { Accept: MIME_TYPE_APPLICATION_PDF }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(url, { method: 'GET', headers })
  const contentType = response.headers.get('content-type') || ''
  if (!response.ok) {
    if (contentType.includes('json')) {
      const body = await response.json()
      throw new Error(body?.message || MSG_PDF_DOWNLOAD_FAILED)
    }
    throw new Error(MSG_PDF_DOWNLOAD_FAILED)
  }
  if (contentType.includes('json')) {
    const body = await response.json()
    throw new Error(body?.message || MSG_PDF_DOWNLOAD_FAILED)
  }
  const buffer = await response.arrayBuffer()
  if (!isPdfArrayBuffer(buffer)) {
    throw new Error(MSG_PDF_NOT_PDF_BYTES)
  }
  const safeRef = String(refId).replace(/[^a-zA-Z0-9-]/g, '_')
  const filename = PDF_DOWNLOAD_FILENAME_FORMAT.replace('%s', safeRef)
  triggerBrowserPdfDownload(new Blob([buffer], { type: MIME_TYPE_APPLICATION_PDF }), filename)
}

export async function deleteDraftInvoice(refId, orderId) {
  const params = new URLSearchParams()
  params.set(QUERY_PARAM_REF_ID, refId)
  if (orderId) {
    params.set(QUERY_PARAM_ORDER_ID, orderId)
  }
  return httpClient.delete(`${MEINVOICE_API_BASE}/invoices?${params.toString()}`)
}

export default {
  createDraftInvoice,
  deleteDraftInvoice,
  downloadInvoicePdf,
  previewInvoicePdfArrayBuffer,
}
