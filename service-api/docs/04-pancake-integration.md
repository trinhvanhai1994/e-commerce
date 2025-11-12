# Hướng dẫn Tích hợp Pancake POS

## Tổng quan

Project tích hợp với Pancake POS để đồng bộ sản phẩm và quản lý kho hàng.

## Thông tin Cần thiết

Để cấu hình tích hợp Pancake POS, bạn cần 3 thông tin sau:

1. **Shop ID** - Đã có: `1021175491`
2. **API Key** - Cần lấy từ Pancake POS
3. **Warehouse ID** - Cần lấy từ Pancake POS

## 1. Lấy API Key

### Bước 1: Đăng nhập Pancake POS

1. Truy cập: https://pos.pancake.vn/
2. Đăng nhập bằng tài khoản của bạn

### Bước 2: Vào phần Cấu hình

1. Trong menu bên trái, chọn **Cấu hình** (Settings)
2. Trong phần **Nâng cao** (Advanced), tìm và chọn **Webhook-API**
   - Hoặc tìm mục **API** hoặc **Tích hợp** (Integration)

### Bước 3: Lấy API Key

1. Tại đây bạn sẽ thấy danh sách các API Key
2. Nếu chưa có, nhấn **Thêm mới** (Create New) để tạo API Key mới
3. **Sao chép** API Key và đảm bảo API Key ở trạng thái **Bật** (Enabled)

## 2. Lấy Warehouse ID (ID Kho hàng)

### Bước 1: Đăng nhập Pancake POS

1. Truy cập: https://pos.pancake.vn/
2. Đăng nhập bằng tài khoản của bạn

### Bước 2: Vào phần Cấu hình

1. Trong menu bên trái, chọn **Cấu hình** (Settings)
2. Trong phần **Cửa hàng** (Store), chọn **Kho hàng** (Warehouse)
   - Hoặc tìm mục **Quản lý kho** (Inventory Management)

### Bước 3: Lấy Warehouse ID

1. Tại đây bạn sẽ thấy danh sách các kho hàng
2. Chọn kho hàng mặc định hoặc kho hàng bạn muốn sử dụng
3. **Sao chép** ID của kho hàng (thường hiển thị trong URL hoặc trong thông tin chi tiết kho)

## 3. Cấu hình trong Application

### Cách 1: Cấu hình trong application.yml

Cập nhật file `src/main/resources/application.yml`:

```yaml
pancake:
  api:
    base-url: ${PANCAKE_API_BASE_URL:https://api.pancake.vn}
    shop-id: ${PANCAKE_SHOP_ID:1021175491}
    api-key: ${PANCAKE_API_KEY:YOUR_API_KEY_HERE}
    warehouse-id: ${PANCAKE_WAREHOUSE_ID:YOUR_WAREHOUSE_ID_HERE}
    timeout: ${PANCAKE_API_TIMEOUT:30000}
```

### Cách 2: Sử dụng Environment Variables (Khuyến nghị cho Production)

**Windows PowerShell:**
```powershell
$env:PANCAKE_API_KEY="your-api-key-here"
$env:PANCAKE_WAREHOUSE_ID="your-warehouse-id-here"
```

**Linux/Mac:**
```bash
export PANCAKE_API_KEY="your-api-key-here"
export PANCAKE_WAREHOUSE_ID="your-warehouse-id-here"
```

## 4. Kiểm tra Kết nối

### Cách 1: Chạy Application và Test qua API

1. **Chạy ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```

2. **Đăng nhập Admin:**
   ```bash
   POST http://localhost:5678/api/thiyen/admin/login
   {
     "username": "admin",
     "password": "thiyen1"
   }
   ```

3. **Test connection:**
   ```bash
   POST http://localhost:5678/api/thiyen/admin/integration/pancake/test-connection
   Authorization: Bearer <your-jwt-token>
   ```

### Cách 2: Sử dụng Admin UI

Sử dụng giao diện admin để test connection trực tiếp.

## 5. Chạy Test Tích hợp

### Test Sync 5 Sản phẩm lên Pancake

```bash
cd service-api
mvn test -Dtest="PancakeProductSyncIntegrationTest#testSync5ProductsToPancake" -Dspring.profiles.active=dev
```

### Test Liệt kê Sản phẩm cần Sync

```bash
mvn test -Dtest="PancakeProductSyncIntegrationTest#testListProductsNeedingSync" -Dspring.profiles.active=dev
```

### Chạy tất cả Tests Pancake

```bash
mvn test -Dtest="*Pancake*" -Dspring.profiles.active=dev
```

## Lưu ý Quan trọng

⚠️ **QUAN TRỌNG**: 
- Test này sẽ **gọi API Pancake POS thật** và tạo sản phẩm thật trên hệ thống Pancake
- Đảm bảo API Key và Shop ID đã được cấu hình đúng
- Test sẽ lấy 5 sản phẩm đầu tiên từ database (ưu tiên các sản phẩm chưa có `pancake_product_id`)
- Sau khi sync thành công, sản phẩm sẽ có `pancake_product_id` được cập nhật trong database

## Kết quả Mong đợi

Test sẽ hiển thị:
- Danh sách 5 sản phẩm đang được sync
- Kết quả từng sản phẩm (thành công/thất bại)
- Pancake Product ID của từng sản phẩm đã tạo
- Tổng kết số lượng thành công/thất bại

## Bảo mật

- **KHÔNG** commit API Key vào Git
- Sử dụng biến môi trường cho production
- Giữ bí mật API Key, không chia sẻ công khai
- Nếu API Key bị lộ, hãy tạo API Key mới ngay lập tức

## Troubleshooting

### Không tìm thấy mục Webhook-API
- Kiểm tra quyền truy cập của tài khoản
- Liên hệ support Pancake POS nếu cần

### Không tìm thấy Warehouse ID
- Một số cửa hàng có thể không có kho hàng riêng
- Thử sử dụng ID kho mặc định hoặc để trống (nếu API cho phép)

### Lỗi kết nối API
- Kiểm tra API Key đã được bật chưa
- Kiểm tra Shop ID có đúng không
- Kiểm tra base-url có đúng không (https://api.pancake.vn)
- Kiểm tra kết nối mạng đến `https://api.pancake.vn`

### Không có sản phẩm nào trong database
- Chạy migrations để insert dữ liệu mẫu
- Hoặc tạo sản phẩm thủ công qua admin panel

### Sản phẩm đã có pancake_product_id
- Test sẽ bỏ qua các sản phẩm đã có `pancake_product_id`
- Nếu muốn sync lại, xóa `pancake_product_id` trong database

