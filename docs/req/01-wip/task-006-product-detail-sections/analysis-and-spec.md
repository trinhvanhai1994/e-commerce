---
type: task-wip
title: "Admin sửa 9 mục thông tin ở trang chi tiết sản phẩm"
status: doing
updated: 2026-11-18
sources:
  - ../../00-request/request-006-product-detail-sections.md
---

# Admin sửa 9 mục thông tin ở trang chi tiết sản phẩm

> Tài liệu làm việc (01-wip). Điều agent tự đề xuất gắn `[PROPOSED]`, điều suy ra gắn `[INFERRED]`.

## 1. Hiện trạng trước thay đổi

- 9 mục ở `/products/:id` viết cứng trong `service-ui/src/views/ProductDetail.vue`, rẽ nhánh theo id sản phẩm (1-2 mè đen, 3-4 hồng đậu, 52 combo).
- Video: chọn cứng `/video/hongdau.mp4` cho id 3, 4 và `/video/intro.mp4` cho mọi sản phẩm khác (file trong `service-ui/public/video`).
- Bảng `products` đã có cột `benefits`, `ingredients`, `specifications`, `technology`, `storage` và form admin có ô tương ứng, nhưng trang không đọc các cột này; nội dung trong DB khác nội dung đang hiển thị.
- Combo đã đổi id 52 thành 5 ở migration V5, nhưng `ProductDetail.vue` vẫn kiểm id 52, nên trang id 5 chỉ còn tiêu đề trống ở CÔNG DỤNG, ĐỐI TƯỢNG PHÙ HỢP, CÔNG NGHỆ.
- Form admin có ô "Đối tượng sử dụng", "Cách sử dụng", "Nhà sản xuất" không được gửi lên BE; "Hướng dẫn chi tiết" dùng chung biến với "Mô tả chi tiết".

## 2. Quyết định đã chốt (owner, 2026-11-18)

- Video: admin dán link. Link YouTube nhúng iframe; đường dẫn `/video/...` hoặc link mp4 dùng thẻ video. Để trống thì ẩn.
- Ghi đè dữ liệu cũ bằng đúng nội dung đang hiển thị, nên deploy xong trang không đổi.
- Nhập dạng văn bản nhiều dòng, mỗi dòng một mục; dòng `# ` là tiêu đề con.
- Sản phẩm id 5 nhận nội dung đang viết cho id 52.

## 3. Thiết kế đã triển khai

### Dữ liệu (migration V25)

| Mục | Cột | Hiển thị |
|---|---|---|
| Video giới thiệu | `video_url VARCHAR(1000)` mới | YouTube: iframe `youtube-nocookie`; còn lại: thẻ video |
| THÔNG TIN SẢN PHẨM | `specifications` (có sẵn) | danh sách không dấu chấm |
| THÀNH PHẦN | `ingredients` (có sẵn) | đoạn văn |
| CÔNG DỤNG | `benefits` (có sẵn) | danh sách có dấu • |
| ĐỐI TƯỢNG PHÙ HỢP | `target_users TEXT` mới | danh sách có dấu • |
| ƯU ĐIỂM NỔI BẬT | `highlights TEXT` mới | danh sách có dấu • |
| CÔNG NGHỆ VƯỢT TRỘI TỐI ƯU DƯỠNG CHẤT | `technology` (có sẵn) | danh sách có dấu • |
| HƯỚNG DẪN BẢO QUẢN | `storage` (có sẵn) | đoạn văn |
| HẠN SỬ DỤNG | `shelf_life TEXT` mới | đoạn văn |

- Cú pháp mỗi dòng: `# ` tiêu đề con, `> ` dòng chú thích (không dấu •), còn lại là một mục. Dòng trống bị bỏ. Mục không còn dòng nào thì ẩn.
- V25 cập nhật 9 cột cho id 1-5 theo đúng chữ trong `ProductDetail.vue` trước thay đổi. `[INFERRED]` id trên prod giống dev (1-5), suy từ V5 đổi 52 thành 5. Sản phẩm không có id 1-5 không bị đổi.

### API

- Không thêm endpoint. `ProductRequest`/`ProductResponse` thêm `videoUrl`, `targetUsers`, `highlights`, `shelfLife`; admin lưu qua `POST/PUT /api/dragun/admin/products`, trang đọc `GET /api/dragun/products/{id}`.
- `videoUrl` rỗng thành `null`; khác rỗng phải bắt đầu bằng `https://` hoặc `/video/`, tối đa 1000 ký tự, sai trả 400.

### Giao diện

- Component dùng chung `components/admin/ProductDetailSectionsEditor.vue` trong `AdminProductDetail.vue` (sửa) và modal thêm ở `AdminProducts.vue`.
- `ProductDetail.vue` đọc 9 mục qua `utils/productSections.js` (phân tích cú pháp dòng, nhận diện YouTube).

### Sửa kèm trong cùng thay đổi

- **Trùng khoá khi thêm sản phẩm**: V4 chèn id 1-5 cố định mà không đẩy `products_id_seq` (DB dev: sequence ở 1, id lớn nhất 5), nên 5 lần đầu admin thêm sản phẩm bị lỗi trùng khoá. V25 `setval` sequence lên `MAX(id)`, chỉ tăng, không kéo lùi.
- **Ảnh sản phẩm không upload được** (owner báo 2026-11-18, nguyên văn: "bỏ dùng ảnh mặc định nhưng không upload đc ảnh khác nên save bị lỗi"): `CmsImageUploader` mặc định `useDefault = true`; trang sửa sản phẩm không bind `v-model:use-default` nên bỏ tick vẫn hiện "Đang dùng ảnh mặc định", không có nút chọn ảnh, kể cả khi sản phẩm đã có ảnh. Kiểm toàn bộ 19 chỗ dùng: chỉ ảnh đại diện (`product_main`) và ảnh con gallery (`product_gallery`) ở `AdminProductDetail.vue` bị; trang chủ, trang Câu chuyện, Triết lý (bind `v-model:use-default`) và blog (`:use-default="false"`) không bị. Sửa: ô "Dùng ảnh mặc định" chỉ hiện khi trang cha bind `use-default`; không bind thì luôn cho upload.

- **Ảnh upload bị cắt hoặc hiển thị lệch khung** (owner yêu cầu review 2026-11-18: "đảm bảo ảnh được upload luôn đc resize để hiển thị chuẩn vị trí khung hình ... không ... bị cắt góc nội dung"). Owner chọn "Cho admin chọn vùng cắt".
  - Upload: trước đây mọi ảnh bị cắt giữa theo tỷ lệ slot mà admin không biết. Nay ảnh lệch tỷ lệ mở hộp chọn vùng (`ImageCropDialog.vue`), có "Vừa toàn bộ ảnh" (không cắt, thêm nền). Ảnh đánh giá của khách: giữ toàn bộ ảnh, thêm nền. JPEG nền trắng (WebP trong suốt trước đây thành nền đen); thu nhỏ bằng `imageSmoothingQuality: high`; cảnh báo khi ảnh bị phóng to quá 1,25 lần.
  - Hiển thị: `aspect-square`/`aspect-video` không sinh CSS vì plugin `@tailwindcss/aspect-ratio` thay theme; thêm lại ở `tailwind.config.js`. Sửa khung cắt ảnh: thẻ sản phẩm `Products.vue`, sản phẩm bán chạy và bài viết ở `Home.vue`, carousel chính và thumbnail `ProductDetail.vue` (desktop trước đây là khung ngang 256-320px cao, mobile rộng cố định 400px tràn màn hình), xem trước ảnh đánh giá, xem trước cover ở `AdminBlogEditor.vue`, `ProductCard.vue` (chưa dùng).

## 4. Rủi ro và câu hỏi mở

- `[INFERRED]` Sản phẩm mới không có link video sẽ không có khối video (trước đây mọi sản phẩm hiện `intro.mp4`).
- `[PROPOSED]` Không thêm khoá lạc quan cho sản phẩm: API sản phẩm hiện chưa có, hai admin sửa cùng lúc thì người lưu sau thắng.
- Trang id 5: tiêu đề "1. BỘT NGŨ HẮC MÈ ĐEN" nay nằm dưới tiêu đề CÔNG DỤNG thay vì phía trên.
