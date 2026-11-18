---
type: task-wip
title: "Đánh giá sản phẩm: khách gửi, đăng ngay, admin hậu kiểm"
status: doing
updated: 2026-11-18
sources:
  - ../../00-request/request-002-product-reviews.md
---

# Đánh giá sản phẩm: khách gửi, đăng ngay, admin hậu kiểm

> Tài liệu làm việc (01-wip). Mục 2 là các quyết định owner đã chốt (có nguồn). Các con số giới hạn ở mục 3
> là agent đề xuất `[PROPOSED]`, đã code theo, owner có thể đổi. Khi chốt, tách sang `03-business`,
> `04-system/srs.md`, `05-design/{apis,screens,data-model}` qua đúng skill sở hữu.

## 1. Hiện trạng trước thay đổi

- `ProductDetail.vue` hiện 5 đánh giá viết cứng, giống nhau cho mọi sản phẩm.
- "Viết đánh giá" chỉ lưu vào `localStorage` của trình duyệt đó (ảnh dạng base64), người khác không thấy;
  khách tự xoá được đánh giá của mình trên máy mình.
- Dòng tóm tắt đầu trang viết cứng "5 trên tổng 5 sao (dựa trên `product.reviewCount || 32` đánh giá)".
- BE không có bảng hay API đánh giá; cột `products.review_count` có nhưng không ai cập nhật.

## 2. Quyết định đã chốt (nguồn: request-002)

| # | Nội dung |
|---|---|
| R1 | Khách gửi đánh giá, lưu DB trạng thái `APPROVED` và hiện công khai ngay (`approved_by = auto`); chỉ `APPROVED` hiện công khai. Đổi ngày 2026-11-18, trước đó lưu `PENDING` chờ duyệt |
| R2 | Admin hậu kiểm: sửa, ẩn (hiện lại), xoá, tự thêm đánh giá cho từng sản phẩm. `PENDING` vẫn còn trong hệ để admin tạm gỡ một đánh giá |
| R3 | 5 đánh giá cũ được seed vào mọi sản phẩm hiện có (`source = SEED`, `APPROVED`) |
| R4 | Mỗi đánh giá tối đa 4 ảnh |
| R5 | Điểm trung bình và số lượng tính từ đánh giá `APPROVED` |

## 3. Thiết kế đã triển khai

### 3.1 Dữ liệu (`V21__Create_product_reviews.sql`)

`product_reviews`: `product_id` (FK `products`, `ON DELETE CASCADE`), `author_name` (100), `rating` 1..5,
`content` TEXT, `images` JSONB mảng tối đa 4 phần tử, `status` `PENDING|APPROVED|HIDDEN`,
`source` `CUSTOMER|ADMIN|SEED`, `approved_at/by`, `created_at`, `updated_at/by`.
Index `(product_id, status, created_at DESC)` và `(status, created_at DESC)`.
Seed chỉ chèn cho sản phẩm chưa có dòng `SEED`; sản phẩm tạo sau migration bắt đầu không có đánh giá.

### 3.2 API

| Method | Path | Quyền | Ghi chú |
|---|---|---|---|
| GET | `/api/dragun/products/{id}/reviews` | public | Đánh giá `APPROVED` mới nhất trước + `averageRating` (1 chữ số thập phân), `reviewCount`; 404 nếu sản phẩm không tồn tại; cache 30s |
| POST | `/api/dragun/products/{id}/reviews` | public, multipart | `authorName`, `rating`, `content`, `images[]` (≤ 4), `website` (honeypot). Lưu `APPROVED`, `approved_at = now`, `approved_by = auto` |
| GET | `/api/dragun/admin/reviews?productId=&status=&page=&size=` | ADMIN | Trang kết quả + `statusCounts` theo bộ lọc sản phẩm |
| GET/PUT/DELETE | `/api/dragun/admin/reviews/{id}` | ADMIN | PUT bắt buộc `updatedAt` (thiếu 400, lệch 409); DELETE xoá hẳn |
| POST | `/api/dragun/admin/reviews` | ADMIN | Mặc định `APPROVED`, `source = ADMIN` |
| PATCH | `/api/dragun/admin/reviews/{id}/status` | ADMIN | Chuyển `APPROVED` lần đầu thì ghi `approved_at/by` |

### 3.3 Chống lạm dụng form công khai `[PROPOSED]`

- Nội dung 5 đến 2000 ký tự, tên ≤ 100 ký tự, số sao 1..5.
- Tối đa 5 lần gửi / 10 phút / IP (bộ đếm trong bộ nhớ, `ReviewSubmissionLimiter`). IP lấy từ `X-Real-IP`,
  rồi phần tử cuối của `X-Forwarded-For`, rồi `remoteAddr` (giả định có đúng một reverse proxy phía trước).
  Chạy nhiều instance API thì mỗi instance đếm riêng.
- Honeypot `website`: có giá trị thì trả thành công nhưng không lưu.
- Ảnh: slot `review_image` (vuông, xuất 800×800, tối thiểu 300×300). Trình duyệt cắt vuông và nén trước khi
  gửi; server kiểm tra lại định dạng thật, kích thước, tỷ lệ và kiểm tra cả 4 ảnh trước khi lưu ảnh nào.

### 3.4 Giao diện

- `ProductDetail.vue`: đọc từ API, trạng thái đang tải / lỗi + Thử lại / chưa có đánh giá; form gửi kèm
  ảnh, báo "Đánh giá đã được đăng" rồi tải lại danh sách (bỏ qua cache 30s bằng query `_`); bỏ nút Xoá phía khách.
- `AdminProductReviews.vue` (`/admin/reviews`): tab Chờ duyệt / Đã duyệt / Đã ẩn / Tất cả có số đếm, mặc định
  tab Tất cả, lọc theo sản phẩm (lưu trên URL), duyệt/ẩn/hiện lại/sửa/xoá, dialog thêm/sửa có upload ảnh. Mục sidebar
  "Đánh giá sản phẩm"; nút "Đánh giá của sản phẩm" trên trang sửa sản phẩm.

## 4. Rủi ro và việc còn mở

- `[INFERRED]` Đăng ngay nghĩa là spam hoặc nội dung xấu hiện công khai cho tới khi admin ẩn. Lớp chặn còn lại
  chỉ là giới hạn 5 lần / 10 phút / IP và honeypot (mục 3.3).
- `[INFERRED]` Ảnh khách gửi được lưu ngay khi gửi, URL ngẫu nhiên, vẫn truy cập được nếu biết URL sau khi
  đánh giá bị ẩn. Xoá đánh giá không xoá file ảnh.
- Đánh giá `PENDING` gửi trước 2026-11-18 không tự chuyển `APPROVED`; admin duyệt tay ở tab Chờ duyệt.
- `[INFERRED]` Ảnh của 5 đánh giá seed dùng đường dẫn `/images/review/comment*/...` y như code cũ, phục vụ
  từ thư mục upload của API; môi trường nào thiếu các file này thì ảnh hỏng như trước.
- Q-001: có cần thông báo (email/Zalo) cho admin khi có đánh giá mới không? Chưa làm. Quan trọng hơn sau
  khi bỏ bước duyệt, vì admin chỉ biết có đánh giá mới khi tự mở `/admin/reviews`.
- Q-002: có cần khách xác thực đã mua hàng (số điện thoại đơn hàng) mới được đánh giá không? Chưa làm.
- Cột `products.review_count` không còn dùng cho hiển thị; chưa xoá để tránh đổi API sản phẩm.
