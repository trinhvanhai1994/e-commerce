# Kết luận tích hợp MISA MeInvoice (API tạo hóa đơn chưa phát hành)

Tài liệu gốc: `docs/Tài liệu API Tạp hóa đơn Misa.md` (MISA MeInvoice – tích hợp từ ERP).

Trong `service-api` đã triển khai:

- Cấu hình `meinvoice.*` trong `application.yml` (URL sandbox/prod, tài khoản, mẫu số/ký hiệu mặc định).
- Client HTTP: token (`POST /webapp/token`), danh sách mẫu (`/webapp/templates`), tạo HĐ draft (`/webapp/insert`), tra cứu theo RefID (`/webapp/getlist`).
- Lưu vết gọi API: bảng `meinvoice_submissions` (Flyway `V16__Create_meinvoice_submissions.sql`) để idempotency theo đơn (một đơn chỉ một lần thành công).
- API admin (JWT admin): `POST /api/dragun/admin/integration/meinvoice/test-connection`, `GET .../templates`, `POST .../orders/{orderId}/draft-invoice`, `POST .../lookup-by-ref-ids`.

Đã **gỡ** tích hợp cũ **MISA eShop Open Platform** (`eshopapp.misa.vn`, HMAC `app_id`) — script `create-misa-token.ps1` và mục README tương ứng.

### Khớp với Postman chính thức (`docs/API AIO WEB APP_MISA.postman_collection.json`)

- **Base URL**: `https://{test|}api.meinvoice.vn/api/integration` + đường dẫn `/webapp/...` (tương đương Postman `urlWeb` = `.../integration/webapp` + `/token`).
- **GetToken**: response `data` thường là **chuỗi JSON** — Postman dùng `JSON.parse(res.data)`; code parse cả **text** và **object**.
- **GetTemplate**: body **PascalCase** `TaxCode`, `UserName`, `Password` (khác request token).
- **Header**: dùng `taxcode` (chữ thường) + `Authorization: Bearer …` như các request có auth trong collection.
- **Insert / Preview**: Postman **không** gắn query `invoiceWithCode` — code **bỏ** query trên hai endpoint này; `getlist`, `paging`, `delete`, `templates` vẫn gắn theo tham số.
- **Payload insert**: bổ sung các trường hay gặp trong mẫu Postman: `InvNo` (`<Chưa cấp số>`), `CurrencyID`, `VATRate` master, `InvDate`/`CreatedDate`/`ModifiedDate` theo offset **+07:00**, `PaymentMethod` kiểu `TM/CK` / `Tiền mặt` / `CK`.
- **Xem PDF popup (admin)**: `POST .../meinvoice/orders/{orderId}/invoice-preview?refId=&by=` — MISA `POST /webapp/preview` → `pdfBase64` → UI decode → PDF.js (`MeinvoicePreviewPdfParser`).
- **Tải PDF về máy (admin)**: `GET .../meinvoice/invoices/pdf?refId=` — MISA `GET /webapp/viewrefid` → stream PDF (`MeinvoiceViewRefIdParser`).

---

## Những nội dung còn thiếu / cần bổ sung để liên kết thành công và chính xác

### 1. Thông tin từ phía MISA / kế toán (bắt buộc trước khi gọi thật)

- **Tài khoản MeInvoice** (sandbox hoặc production): `taxcode` (MST), `username`, `password` — map vào biến môi trường `MEINVOICE_*`.
- **Tờ khai / trạng thái CQT**: tài liệu yêu cầu tờ khai ở trạng thái **CQT chấp nhận** và mẫu hóa đơn **Đang sử dụng**. Nếu chưa có, API sẽ trả mã lỗi dạng `DeclarationNotExist`, `InvalidDeclaration`, v.v.
- **Ký hiệu và mẫu hóa đơn**: lấy `IPTemplateID` → cấu hình `MEINVOICE_TEMPLATE_ID`, và `InvSeries` → `MEINVOICE_INV_SERIES` (sau khi gọi `GET .../templates` hoặc nhập tay theo kế toán).
- **Tham số `invoiceWithCode`**: chọn đúng loại **có mã / không mã** theo tờ khai và mẫu (`MEINVOICE_INVOICE_WITH_CODE`).

### 2. Header `Taxcode` so với tài liệu

Tài liệu có chỗ ghi `Taxcode: {token}` (dễ hiểu nhầm với Bearer). Code hiện gửi:

- `Authorization: Bearer {access_token}`
- `taxcode: {MST}` (header **chữ thường** như Postman; giá trị `meinvoice.credentials.taxcode`)

Nếu môi trường thực tế của MISA yêu cầu giá trị khác (ví dụ trùng với token), cần **đối chiếu Postman chính thức** của MISA và chỉnh `MeinvoiceApiClient` cho khớp.

### 3. Nghiệp vụ số tiền và thuế (cần BA chốt)

- **`meinvoice.defaults.assume-prices-exclude-vat`**: `true` = `OrderItem.price` / phí ship là đơn giá **chưa VAT**; `false` = coi là **đã gồm VAT** và hệ thống quy đổi ra tiền trước thuế: `net = gross * 100 / (100 + R)` với `R` là phần trăm VAT cấu hình.
- **Thuế suất theo từng mặt hàng**: hiện dùng một mức mặc định cho mọi dòng; Pancake/ERP có thể cần map theo nhóm hàng hoặc từ master sản phẩm.
- **Phí vận chuyển**: đang thêm một dòng hàng “Phí vận chuyển” + VAT; cần kế toán xác nhận có xuất riêng hay gộp vào giá hàng.
- **Đối chiếu tổng tiền** với `Order.total`: chưa có bước validate cứng; nếu lệch với quy tắc làm tròn của MeInvoice có thể bị từ chối — nên bổ sung kiểm tra hoặc điều chỉnh làm tròn theo doc.

### 4. Phạm vi API so với “phát hành thuế”

Theo tài liệu, **sau khi tạo HĐ qua API**, người dùng thường làm **các bước còn lại trên Webapp MeInvoice** (ký, phát hành, tra cứu CQT). Module hiện tại tập trung **tạo hóa đơn chưa phát hành** (`/webapp/insert`) và **tra cứu trạng thái** (`/webapp/getlist`). Nếu cần tự động hoá toàn bộ phát hành/ ký số không qua web, phải có **tài liệu/API bổ sung** từ MISA (ngoài phạm vi “tích hợp nhanh” trong file `.md`).

### 5. Refresh token

Bảng mã lỗi có `TokenExpiredCode` — gọi hàm **RefreshToken** (endpoint có thể không nằm trong đoạn đã trích của file markdown). Hiện code **lấy token mới bằng `/webapp/token`** khi cache hết hạn. Nếu MISA bắt buộc dùng API refresh riêng, cần bổ sung endpoint và parse `expires_in` nếu có.

### 6. Các API chưa bọc / đã gỡ

- `POST /webapp/preview` — **đang dùng** (xem PDF popup admin).
- `DELETE /webapp/delete` — **đang dùng** (xóa HĐ nháp trên MISA; giữ `meinvoice_ref_id`, set `orders.meinvoice_draft_deleted=true`, `meinvoice_submissions.success=false`).
- `GET /webapp/viewrefid` — **đang dùng** (tải PDF về máy; popup vẫn dùng preview base64).
- `POST /webapp/paging` và `/webapp/paging/calculating` — chưa bọc.

### 7. Bảo mật và vận hành

- Không commit `MEINVOICE_PASSWORD` vào Git; chỉ dùng secret manager / biến môi trường server.
- Log response MeInvoice có thể chứa dữ liệu nhạy cảm — hạn chế log full ở production.

---

## Tham chiếu mã nguồn

| Thành phần | Vị trí |
|------------|--------|
| Cấu hình | `MeinvoiceIntegrationConfig`, `application.yml` |
| HTTP client + header | `MeinvoiceApiClient` |
| Parse PDF base64 (preview) | `MeinvoicePreviewPdfParser` |
| Parse PDF binary/JSON (viewrefid) | `MeinvoiceViewRefIdParser` |
| Token cache | `MeinvoiceAuthService` |
| Build payload + insert | `MeinvoiceInvoiceService` |
| API admin | `AdminMeinvoiceController` |
| Lưu RefID / idempotency | `MeinvoiceSubmission`, `MeinvoiceSubmissionRepository`, migration V16 |

Sau khi bổ sung các mục mục (1)–(3), bật `MEINVOICE_ENABLED=true` và gọi `test-connection`, rồi `draft-invoice` với một `orderId` thử trên sandbox.
