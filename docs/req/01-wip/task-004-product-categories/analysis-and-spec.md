---
type: task-wip
title: "Quản lý danh mục sản phẩm: admin thêm, sửa, xoá"
status: todo
updated: 2026-11-18
sources:
  - ../../00-request/request-004-product-categories.md
---

# Quản lý danh mục sản phẩm: admin thêm, sửa, xoá

> Tài liệu làm việc (01-wip). Bản này là phân tích và kế hoạch, chờ owner chốt các câu ở mục 4
> trước khi code. Điều agent tự đề xuất gắn `[PROPOSED]`.

## 1. Hiện trạng trước thay đổi

- `products.category` là chuỗi `VARCHAR(255)` (V1, nới ở V9), không có bảng danh mục. Giá trị đang dùng:
  `ME_DEN`, `HONG_DAU`, `COMBO` (seed V4; DB local: 2, 2, 1 sản phẩm).
- Danh sách 3 danh mục viết cứng ở nhiều chỗ FE:
  - `AdminProductDetail.vue` (select + map thư mục ảnh `black`/`pink`/`mix`);
  - `AdminProducts.vue` (select trong modal + `getCategoryName`);
  - `Products.vue` (nút lọc + `getCategoryName`);
  - `ProductCard.vue` (map cũ `me-den`/`hong-dau`, không khớp mã hiện tại) `[INFERRED]` lỗi có sẵn;
  - `Home.vue`, `AdminOrders.vue`: dữ liệu mẫu dự phòng dùng mã cũ `me-den`/`combo`.
- Đồng bộ Pancake POS (`ProductMapper`) ghi thẳng `category` từ Pancake, nên có thể xuất hiện mã ngoài danh sách.

## 2. Quyết định đã chốt

- Admin cần một màn để thêm, sửa, xoá danh mục; form sản phẩm chọn từ danh sách đó (nguồn: request-004).
- Các điểm còn lại chờ owner (mục 4).

## 3. Thiết kế đề xuất `[PROPOSED]`

### 3.1 Dữ liệu (`V23__Create_product_categories.sql`)

- Bảng `product_categories`: `id`, `code` (unique, viết HOA, không đổi sau khi tạo), `name`, `sort_order`,
  `visible` (hiện ở trang Sản phẩm), `created_at`, `updated_at`.
- Seed 3 dòng `ME_DEN` Mè đen, `HONG_DAU` Hồng đậu, `COMBO` Combo.
- `products.category` giữ nguyên là chuỗi mã (không thêm khoá ngoại), để không đổi API sản phẩm và không vỡ đồng bộ Pancake.

### 3.2 API

| Method | Path | Quyền | Ghi chú |
|---|---|---|---|
| GET | `/api/dragun/categories` | public | danh mục `visible`, theo `sort_order` |
| GET | `/api/dragun/admin/categories` | ADMIN | tất cả, kèm số sản phẩm đang dùng |
| POST | `/api/dragun/admin/categories` | ADMIN | `{name, code?, sortOrder, visible}`; 400 thiếu tên, 409 trùng mã |
| PUT | `/api/dragun/admin/categories/{id}` | ADMIN | sửa tên, thứ tự, hiển thị; khoá lạc quan `updatedAt` (409) |
| DELETE | `/api/dragun/admin/categories/{id}` | ADMIN | 404; xử lý sản phẩm đang dùng theo câu hỏi mục 4 |

### 3.3 Giao diện

- Màn mới `/admin/categories` (`AdminCategories.vue`, mục menu "Danh mục" trong `AdminSidebar.vue`):
  bảng danh mục, form thêm/sửa, nút xoá có hộp xác nhận.
- Form sản phẩm (`AdminProductDetail.vue`, `AdminProducts.vue`) lấy danh sách từ API admin.
- Trang `/products`, `ProductCard`: nút lọc và tên danh mục lấy từ API public.

### 3.4 Test

- `ProductCategoryServiceTest`: tạo, trùng mã (409), thiếu tên (400), sửa lệch `updatedAt` (409),
  không tồn tại (404), xoá khi còn sản phẩm, API public chỉ trả danh mục `visible`.
- Phân quyền HTTP: khách không gọi được API admin (401/403).

## 4. Câu hỏi mở (chờ owner)

1. Xoá danh mục đang có sản phẩm: chặn (đề xuất) / chuyển sản phẩm sang danh mục khác / vẫn xoá để sản phẩm thành "Chưa phân loại".
2. Mã danh mục: tự sinh từ tên, không đổi được (đề xuất) / admin tự nhập.
3. Danh mục mới tự hiện nút lọc ở trang Sản phẩm, có công tắc ẩn/hiện và thứ tự (đề xuất) / chỉ dùng nội bộ admin.
