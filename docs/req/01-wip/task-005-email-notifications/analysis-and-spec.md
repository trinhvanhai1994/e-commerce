---
type: task-wip
title: "Email thông báo đơn hàng: báo admin và xác nhận cho khách"
status: doing
updated: 2026-11-18
sources:
  - ../../00-request/request-005-email-notifications.md
---

# Email thông báo đơn hàng: báo admin và xác nhận cho khách

> Tài liệu làm việc (01-wip). Owner duyệt kế hoạch với các mặc định ở mục 4 ngày 2026-11-18 ("Duyệt, bắt đầu code").
> Phần đã triển khai thật ở mục 5. Điều agent tự đề xuất gắn `[PROPOSED]`, điều suy ra gắn `[INFERRED]`.

## 1. Hiện trạng trước thay đổi

- service-api chưa có kênh gửi mail nào: `pom.xml` không có thư viện mail, Thymeleaf; chưa bật `@EnableAsync`
  (chỉ có `@EnableScheduling` ở `EcommerceApplication`).
- Chưa dùng Spring event ở đâu. Đồng bộ đơn sang Pancake chạy theo lịch (`PancakeSyncScheduler`, 15 phút),
  không gắn vào lúc tạo đơn.
- Tạo đơn: `POST /api/extend/orders` → `OrderController.createOrder` → `OrderService.createOrder(request, customerId)`
  (`@Transactional`). Đơn luôn là COD, phí ship cố định 20.000đ.
- Bảng `orders` không có cột email. `CreateOrderRequest.CustomerInfo` có `name`, `phone`, `address`,
  `province`, `district`, `ward`.
- `Checkout.vue` có ô họ tên, SĐT, địa chỉ; khi khách đăng nhập thì điền sẵn từ profile. `customers.email`
  là trường tuỳ chọn (task-003).

## 2. Quyết định đã chốt

- Owner duyệt kế hoạch mục 3 và các mặc định mục 4 (nguồn: chat 2026-11-18). Câu 2, 3 (địa chỉ gửi, email admin)
  vẫn chờ giá trị thật, đưa vào biến môi trường khi bật gửi.

- Giai đoạn 1 làm hai mail: báo admin có đơn mới, và xác nhận đơn cho khách (nguồn: request-005).
- Quên mật khẩu qua email không làm: task-003 đã chốt quên mật khẩu đi qua hotline, admin đặt lại (R6).

## 3. Thiết kế đề xuất `[PROPOSED]`

Phân tích code mẫu (giữ lại gì, bỏ gì) đã trình owner trong chat. Điểm chính: giữ cách gửi sau commit,
bất đồng bộ, nạp lại đơn từ DB, template HTML kèm bản text, có cờ bật tắt. Không chép: from email viết cứng,
retry POST (có thể gửi trùng), tắt keep-alive, bản text dựng từ HTML thô, log địa chỉ email ở mức INFO,
thư viện của Spring Boot 2.

### 3.1 Dữ liệu (`V24__Add_order_customer_email.sql`)

- `orders.customer_email VARCHAR(255) NULL`. Đơn cũ để NULL.
- Số migration: V23 đang thuộc task-004 (chưa commit). Lúc code kiểm lại số tiếp theo.

### 3.2 API

- Không thêm endpoint. `POST /api/extend/orders` nhận thêm `customerInfo.email` tuỳ chọn:
  `@Email`, tối đa 255 ký tự, chuỗi rỗng coi như không có. Sai định dạng trả 400.
- FE cũ không gửi `email` vẫn chạy như trước (tương thích ngược).
- `OrderResponse` thêm `customerEmail` để admin thấy trong chi tiết đơn.

### 3.3 Gửi mail (BE)

- Package `integration/mail/` (theo kiểu `integration/pancake/`):
  - `MailProperties` (`app.mail.*`): `enabled`, `from-email`, `from-name`, `admin-recipients` (danh sách),
    `store-url`, `brevo.api-key` lấy từ biến môi trường `BREVO_API_KEY`, `brevo.base-url`, timeout.
  - `MailSender` (interface) với hai bản: `BrevoMailSender` (Spring 6 `RestClient`, không retry,
    ghi log `messageId`, che địa chỉ email trong log) và `LoggingMailSender` (khi `enabled=false` hoặc
    thiếu key, chỉ ghi log; dev và test dùng bản này nên không gọi mạng).
  - `MailTemplateRenderer`: Thymeleaf core (`org.thymeleaf:thymeleaf`, không dùng starter để khỏi đăng ký
    view resolver web), template ở `resources/mail/`, cache bật.
- Luồng: `OrderService.createOrder` phát `OrderPlacedEvent(orderDbId)` → `OrderMailListener`
  (`@TransactionalEventListener(AFTER_COMMIT)` + `@Async("mailExecutor")`) nạp lại đơn kèm item →
  gửi mail admin, rồi gửi mail khách nếu đơn có email.
- Đơn rollback thì không có mail. Mail lỗi chỉ ghi log WARN, không ảnh hưởng đơn, không gửi lại.
- Mail admin: tiêu đề `[Đơn mới] <orderId> - <tên khách>`, nội dung tóm tắt khách, SĐT, địa chỉ,
  sản phẩm, tổng tiền, link `<admin-url>/admin/orders`.
- Mail khách: tiêu đề `Thi Yên xác nhận đơn hàng <orderId>`, nội dung lời cảm ơn, mã đơn, ngày đặt,
  danh sách sản phẩm, tạm tính, phí ship, tổng, phương thức COD, địa chỉ nhận, hotline.
- Nội dung đưa vào template bằng `th:text` (escape). Bản text dựng riêng, không lấy từ HTML.

### 3.4 Giao diện

- `Checkout.vue`: thêm ô "Email (không bắt buộc)" dưới SĐT, `type="email"`, điền sẵn từ `profile.email`
  khi khách đã đăng nhập. Kiểm định dạng phía FE; chú thích nhỏ "Nhận xác nhận đơn qua email".
- Admin chi tiết đơn: hiện email nếu có.

### 3.5 Test

- `BrevoMailSenderTest`: `MockRestServiceServer` kiểm payload, header `api-key`, xử lý 4xx/5xx trả false.
- `MailTemplateRendererTest`: render đủ trường, escape ký tự HTML, tiền định dạng VND.
- `OrderMailListenerTest` (Mockito): có email gửi 2 mail; không email chỉ gửi admin; `enabled=false` không gửi.
- `OrderServiceTest` hoặc test tạo đơn qua MockMvc: lưu `customer_email`; email sai trả 400;
  thiếu email vẫn tạo đơn được.
- `EcommerceApplicationTests` chạy Flyway + `validate` cho cột mới.

## 4. Câu hỏi mở (chờ owner)

| # | Câu hỏi | Mặc định đề xuất | Ảnh hưởng nếu sai |
|---|---|---|---|
| 1 | Nhà cung cấp mail | Brevo REST API như code mẫu (free 300 mail/ngày) | Đổi sang SMTP thì viết lại `BrevoMailSender` |
| 2 | Địa chỉ gửi (from) và tên hiển thị | `[TBD]` email trên domain shop đã xác minh SPF/DKIM trong Brevo, tên "Thi Yên" | Chưa xác minh domain thì mail vào spam hoặc bị Brevo từ chối |
| 3 | Email admin nhận báo đơn mới | `[TBD]` lấy từ cấu hình `app.mail.admin-recipients`, cho phép nhiều địa chỉ | Chỉ cần sửa cấu hình |
| 4 | Ô email ở checkout bắt buộc hay tuỳ chọn | Tuỳ chọn (khách đăng nhập bằng SĐT, nhiều khách không dùng email) | Bắt buộc sẽ giảm tỉ lệ đặt hàng |
| 5 | Có gửi mail khi đổi trạng thái đơn (đang giao, đã huỷ) không | Không, để giai đoạn sau | Thêm listener mới, không phải làm lại |
| 6 | Mail lỗi có cần gửi lại không | Không ở giai đoạn 1, chỉ ghi log; giai đoạn 2 thêm bảng outbox nếu cần | Mất một số mail khi Brevo lỗi |
| 7 | Có lưu email khách vào profile khi khách đăng nhập nhập email mới ở checkout không | Không, chỉ lưu theo đơn | Profile và đơn có thể lệch email |

## 5. Đã triển khai (2026-11-18)

- Migration `V24__Add_order_customer_email.sql`: `orders.customer_email VARCHAR(255) NULL`.
- `CreateOrderRequest.CustomerInfo.email`: tuỳ chọn, setter tự trim, chuỗi trắng thành null, `@Email` + tối đa 255.
  `OrderResponse.customerInfo.email` trả về cho admin.
- `OrderService.createOrder` lưu email và phát `service/event/OrderPlacedEvent(orderDbId)`.
- Package `integration/mail/`: `MailProperties` (`app.mail.*`), `MailConfig` (`@EnableAsync`, chọn `BrevoMailSender`
  hoặc `LoggingMailSender`), `MailTemplateRenderer` (Thymeleaf core, `resources/mail/*.html`), `OrderMailService`,
  `OrderMailListener` (`@Async` + `@TransactionalEventListener(AFTER_COMMIT)`).
- Template `mail/order-admin.html`, `mail/order-customer.html`. Địa chỉ trong mail ghép tên phường, quận, tỉnh
  từ bảng địa chính (đơn chỉ lưu mã).
- Biến môi trường: `MAIL_ENABLED`, `BREVO_API_KEY`, `MAIL_FROM_EMAIL`, `MAIL_FROM_NAME`, `MAIL_ADMIN_RECIPIENTS`,
  `MAIL_STORE_URL`, `MAIL_SUPPORT_PHONE` (đã thêm vào `service-api/.env.example`, mô tả ở `service-api/docs/03-build-deploy.md`).
- FE: `Checkout.vue` thêm ô "Email (không bắt buộc)", điền sẵn từ profile, kiểm định dạng; `AdminOrders.vue` hiện email
  trong chi tiết đơn.
- Test: `BrevoMailSenderTest` (4), `OrderMailServiceTest` (6), `OrderPlacedMailFlowTest` (5, gồm đơn rollback không gửi mail).

Rủi ro còn lại:
- Mail lỗi không gửi lại (câu 6). `[INFERRED]` Brevo gói free giới hạn số mail mỗi ngày; vượt thì mail bị từ chối, chỉ thấy trong log.
- Chưa gửi thử với Brevo thật: cần owner cấp API key và sender đã xác minh.
