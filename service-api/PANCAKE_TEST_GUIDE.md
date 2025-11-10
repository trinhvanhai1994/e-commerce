# Hướng dẫn chạy Test tạo sản phẩm lên Pancake POS

## Mô tả

Test này sẽ lấy 5 sản phẩm đang tồn tại trong database và tạo chúng lên Pancake POS sử dụng cấu hình thật từ `application.yml`.

## Yêu cầu

1. **Database đã có sản phẩm**: Đảm bảo database có ít nhất 5 sản phẩm
2. **Cấu hình Pancake đã đúng**: Kiểm tra trong `application.yml`:
   - `pancake.api.shop-id`: Shop ID của bạn
   - `pancake.api.api-key`: API Key hợp lệ
   - `pancake.api.warehouse-id`: Warehouse ID
   - `pancake.api.base-url`: URL API Pancake

## Cách chạy test

### Chạy test integration (tạo 5 sản phẩm lên Pancake)

```bash
cd service-api
mvn test -Dtest="PancakeProductSyncIntegrationTest#testSync5ProductsToPancake" -Dspring.profiles.active=dev
```

### Chạy test liệt kê sản phẩm cần sync

```bash
mvn test -Dtest="PancakeProductSyncIntegrationTest#testListProductsNeedingSync" -Dspring.profiles.active=dev
```

### Chạy tất cả tests Pancake

```bash
mvn test -Dtest="*Pancake*" -Dspring.profiles.active=dev
```

## Lưu ý

⚠️ **QUAN TRỌNG**: 
- Test này sẽ **gọi API Pancake POS thật** và tạo sản phẩm thật trên hệ thống Pancake
- Đảm bảo API Key và Shop ID đã được cấu hình đúng
- Test sẽ lấy 5 sản phẩm đầu tiên từ database (ưu tiên các sản phẩm chưa có `pancake_product_id`)
- Sau khi sync thành công, sản phẩm sẽ có `pancake_product_id` được cập nhật trong database

## Kết quả mong đợi

Test sẽ hiển thị:
- Danh sách 5 sản phẩm đang được sync
- Kết quả từng sản phẩm (thành công/thất bại)
- Pancake Product ID của từng sản phẩm đã tạo
- Tổng kết số lượng thành công/thất bại

## Troubleshooting

### Lỗi kết nối API
- Kiểm tra API Key có đúng không
- Kiểm tra Shop ID có đúng không
- Kiểm tra kết nối mạng đến `https://api.pancake.vn`

### Không có sản phẩm nào trong database
- Chạy migrations để insert dữ liệu mẫu
- Hoặc tạo sản phẩm thủ công qua admin panel

### Sản phẩm đã có pancake_product_id
- Test sẽ bỏ qua các sản phẩm đã có `pancake_product_id`
- Nếu muốn sync lại, xóa `pancake_product_id` trong database

