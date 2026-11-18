# Deploy task-006: 9 mục trang chi tiết sản phẩm (2026-11-18)

## SQL cần chạy trên prod

Không cần chạy tay. Flyway tự chạy `V25__Add_product_detail_sections.sql` khi service-api mới khởi động:

- thêm 4 cột `video_url`, `target_users`, `highlights`, `shelf_life` vào `products` (`ADD COLUMN IF NOT EXISTS`);
- đẩy `products_id_seq` lên id lớn nhất (chỉ tăng);
- ghi đè 9 mục của sản phẩm id 1-5 bằng nội dung trang đang hiển thị.

Prod dùng `ddl-auto: validate`, nên API mới chỉ lên được sau khi V25 chạy thành công.

## Kiểm tra trước khi deploy

Seed dựa trên id sản phẩm 1-5 giống DB dev (`[INFERRED]` từ V5 đổi 52 thành 5). Chạy trên prod, kết quả phải là 5 dòng đúng tên:

```sql
SELECT id, name FROM products WHERE id BETWEEN 1 AND 5 ORDER BY id;
-- 1 BỘT NGŨ HẮC MÈ ĐEN, 2 COMBO 2 LON ... MÈ ĐEN, 3 BỘT NGŨ SẮC HỒNG ĐẬU, 4 COMBO 2 LON ... HỒNG ĐẬU, 5 COMBO 2 (1 ...)
```

Id lệch thì dừng, báo lại để sửa seed trước khi deploy.

## Thứ tự deploy

1. service-api trước. Giao diện cũ vẫn chạy: trang cũ không đọc cột mới; form admin cũ vẫn lưu được nhưng gửi thiếu 4 trường mới nên lưu sản phẩm bằng giao diện cũ sẽ xoá 4 mục đó. Tránh sửa sản phẩm trong lúc giữa hai lần deploy.
2. service-ui ngay sau.

## Kiểm tra sau deploy

```sql
SELECT version, success FROM flyway_schema_history WHERE version = '25';
SELECT id, video_url, split_part(benefits, E'\n', 1) AS cong_dung_1, shelf_life FROM products WHERE id <= 5 ORDER BY id;
SELECT last_value FROM products_id_seq;  -- >= MAX(id)
```

Mở `/products/1`, `/products/3`, `/products/5`: nội dung như trước, id 5 nay có đủ CÔNG DỤNG, ĐỐI TƯỢNG PHÙ HỢP.
