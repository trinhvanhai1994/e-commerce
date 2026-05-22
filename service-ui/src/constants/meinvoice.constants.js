/** MeInvoice admin integration — paths, lookup modes, order sources. */

export const MEINVOICE_API_BASE = '/api/thiyen/admin/integration/meinvoice'

export const MEINVOICE_LOOKUP_BY_ORDER = 'order'
export const MEINVOICE_LOOKUP_BY_PANCAKE = 'pancake'

export const ORDER_TYPE_PANCAKE = 'PANCAKE'
export const ORDER_TYPE_THI_YEN = 'THI_YEN'

export const ORDER_SOURCE_LABELS = {
  [ORDER_TYPE_PANCAKE]: 'Pancake',
  [ORDER_TYPE_THI_YEN]: 'Thi Yên',
}

export const MIME_TYPE_APPLICATION_PDF = 'application/pdf'
export const MIME_TYPE_JSON = 'json'

export const HTTP_HEADER_AUTHORIZATION = 'Authorization'
export const HTTP_HEADER_ACCEPT = 'Accept'
export const HTTP_HEADER_BEARER_FORMAT = 'Bearer %s'

export const LOCAL_STORAGE_AUTH_TOKEN = 'authToken'

export const PDF_MAGIC_BYTE_PERCENT = 0x25
export const PDF_MAGIC_BYTE_P = 0x50
export const PDF_MAGIC_BYTE_D = 0x44
export const PDF_MAGIC_BYTE_F = 0x46
export const PDF_MIN_HEADER_LENGTH = 4
export const PDF_JSON_PREFIX = '{'
export const PDF_PREVIEW_TEXT_LENGTH = 120

export const MISA_REF_DISPLAY_MAX_LENGTH = 20
export const MISA_REF_DISPLAY_HEAD_LENGTH = 8
export const MISA_REF_DISPLAY_TAIL_LENGTH = 8

export const MSG_PDF_EMPTY = 'PDF trống'
export const MSG_PDF_NOT_PDF_RESPONSE = 'Phản hồi không phải file PDF'
export const MSG_PDF_LOAD_FAILED =
  'Không thể tải PDF hóa đơn. Kiểm tra MeInvoice và đăng nhập admin.'
export const MSG_PDF_DOWNLOAD_FAILED =
  'Không thể tải xuống PDF hóa đơn. Kiểm tra MeInvoice và đăng nhập admin.'
export const MSG_PDF_UNAUTHORIZED =
  'Phiên đăng nhập admin đã hết hạn. Vui lòng đăng nhập lại rồi xem PDF.'
export const PDF_DOWNLOAD_FILENAME_FORMAT = 'meinvoice-%s.pdf'
export const MSG_PDF_VIEWER_FAILED = 'Không hiển thị được PDF'
export const MSG_PDF_INVALID_OR_EMPTY = 'File PDF trống hoặc không hợp lệ'
export const MSG_PDF_NOT_PDF_BYTES = 'Dữ liệu tải về không phải file PDF (%PDF).'
export const MSG_PDF_MISA_JSON_INSTEAD =
  'MISA trả về JSON thay vì PDF — kiểm tra RefID hoặc trạng thái hóa đơn.'
export const MSG_ORDER_NO_MISA_REF = 'Đơn chưa có RefID hóa đơn MISA.'
export const MSG_DRAFT_SUCCESS = 'Đã tạo hóa đơn nháp trên MeInvoice.'
export const MSG_DRAFT_SUCCESS_WITH_REF_FORMAT = 'Đã tạo hóa đơn nháp trên MeInvoice.\nRef: %s'

export const QUERY_PARAM_REF_ID = 'refId'
export const QUERY_PARAM_ORDER_ID = 'orderId'

export const MSG_DELETE_MODAL_TITLE = 'Xóa hóa đơn nháp MeInvoice'
export const MSG_DELETE_MODAL_BODY =
  'Hóa đơn chưa phát hành sẽ bị xóa trên MeInvoice. Thao tác không thể hoàn tác.'
export const MSG_DELETE_CONFIRM_FORMAT =
  'Xóa hóa đơn nháp trên MeInvoice (chưa phát hành)?\nRef: %s'
export const MSG_DELETE_SUCCESS = 'Đã xóa hóa đơn nháp trên MeInvoice.'
export const MSG_DELETE_FAILED = 'Không thể xóa hóa đơn nháp. Kiểm tra MeInvoice và quyền admin.'

export const LABEL_DRAFT_DELETED_ON_MISA = 'Đã xóa nháp trên MeInvoice'
export const TITLE_DRAFT_DELETED_ON_MISA =
  'Hóa đơn nháp đã xóa trên MeInvoice. Chỉ xem hoặc tải PDF lưu trữ (RefID giữ nguyên).'
export const MSG_DRAFT_ALREADY_DELETED = 'Hóa đơn nháp này đã được xóa trên MeInvoice.'

export const RESPONSE_FIELD_PDF_BASE64 = 'pdfBase64'
export const RESPONSE_FIELD_RECORDED_SUCCESS = 'recordedSuccess'
export const RESPONSE_FIELD_MEINVOICE_REF_ID = 'meinvoiceRefId'
export const RESPONSE_FIELD_REF_ID = 'refId'
export const RESPONSE_FIELD_MISA_INVOICE_REF = 'misaInvoiceRef'
export const RESPONSE_FIELD_MEINVOICE_INVOICED = 'meinvoiceInvoiced'
export const RESPONSE_FIELD_MEINVOICE_DRAFT_DELETED = 'meinvoiceDraftDeleted'
export const RESPONSE_FIELD_MEINVOICE_PUBLISHED = 'meinvoicePublished'
export const RESPONSE_FIELD_MEINVOICE_PUBLISHED_AT = 'meinvoicePublishedAt'
export const RESPONSE_FIELD_MEINVOICE_TRANSACTION_ID = 'meinvoiceTransactionId'
export const RESPONSE_FIELD_MEINVOICE_INV_NO = 'meinvoiceInvNo'
/** Keys in publish-invoice API response body (before reload). */
export const RESPONSE_FIELD_PUBLISH_TRANSACTION_ID = 'transactionId'
export const RESPONSE_FIELD_PUBLISH_INV_NO = 'invNo'

export const LABEL_INVOICE_PUBLISHED = 'Đã phát hành'
export const TITLE_INVOICE_PUBLISHED = 'Hóa đơn đã phát hành trên MeInvoice (CQT).'
export const MSG_PUBLISH_SUCCESS = 'Đã phát hành hóa đơn trên MeInvoice.'
export const MSG_PUBLISH_FAILED = 'Không thể phát hành hóa đơn. Kiểm tra MeInvoice, HSM và quyền admin.'
export const MSG_PUBLISH_MODAL_TITLE = 'Phát hành hóa đơn MeInvoice'
export const MSG_PUBLISH_MODAL_BODY =
  'Hóa đơn sẽ được phát hành trên MeInvoice (không gửi email cho khách hàng). Thao tác không thể hoàn tác.'
export const MSG_PUBLISH_ALREADY_PUBLISHED = 'Hóa đơn này đã được phát hành trên MeInvoice.'
export const QUERY_PARAM_TRANSACTION_ID = 'transactionId'
