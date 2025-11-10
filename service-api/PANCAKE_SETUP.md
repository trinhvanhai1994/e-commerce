# Hướng dẫn lấy thông tin Pancake POS

## Các thông tin cần thiết

Để cấu hình tích hợp Pancake POS, bạn cần 3 thông tin sau:

1. **Shop ID** - Đã có: `1021175491`
2. **API Key** - Cần lấy
3. **Warehouse ID** - Cần lấy

---

## Cách lấy API Key

1. **Đăng nhập vào Pancake POS**
   - Truy cập: https://pos.pancake.vn/
   - Đăng nhập bằng tài khoản của bạn

2. **Vào phần Cấu hình**
   - Trong menu bên trái, chọn **Cấu hình** (Settings)

3. **Truy cập Webhook-API**
   - Trong phần **Nâng cao** (Advanced), tìm và chọn **Webhook-API**
   - Hoặc tìm mục **API** hoặc **Tích hợp** (Integration)

4. **Lấy API Key**
   - Tại đây bạn sẽ thấy danh sách các API Key
   - Nếu chưa có, nhấn **Thêm mới** (Create New) để tạo API Key mới
   - **Sao chép** API Key và đảm bảo API Key ở trạng thái **Bật** (Enabled)

---

## Cách lấy Warehouse ID (ID Kho hàng)

1. **Đăng nhập vào Pancake POS**
   - Truy cập: https://pos.pancake.vn/
   - Đăng nhập bằng tài khoản của bạn

2. **Vào phần Cấu hình**
   - Trong menu bên trái, chọn **Cấu hình** (Settings)

3. **Truy cập Kho hàng**
   - Trong phần **Cửa hàng** (Store), chọn **Kho hàng** (Warehouse)
   - Hoặc tìm mục **Quản lý kho** (Inventory Management)

4. **Lấy Warehouse ID**
   - Tại đây bạn sẽ thấy danh sách các kho hàng
   - Chọn kho hàng mặc định hoặc kho hàng bạn muốn sử dụng
   - **Sao chép** ID của kho hàng (thường hiển thị trong URL hoặc trong thông tin chi tiết kho)

---

## Cấu hình trong application.yml

Sau khi có đầy đủ thông tin, cập nhật file `application.yml`:

```yaml
pancake:
  api:
    base-url: ${PANCAKE_API_BASE_URL:https://api.pancake.vn}
    shop-id: ${PANCAKE_SHOP_ID:1021175491}
    api-key: ${PANCAKE_API_KEY:YOUR_API_KEY_HERE}
    warehouse-id: ${PANCAKE_WAREHOUSE_ID:YOUR_WAREHOUSE_ID_HERE}
    timeout: ${PANCAKE_API_TIMEOUT:30000}
```

**Hoặc sử dụng biến môi trường** (khuyến nghị cho production):

```bash
export PANCAKE_API_KEY="your-api-key-here"
export PANCAKE_WAREHOUSE_ID="your-warehouse-id-here"
```

---

## Kiểm tra kết nối

Sau khi cấu hình, bạn có thể test kết nối bằng cách:

1. **Chạy ứng dụng**
   ```bash
   mvn spring-boot:run
   ```

2. **Gọi API test connection** (cần đăng nhập admin trước)
   ```bash
   POST http://localhost:5678/api/thiyen/admin/integration/pancake/test-connection
   ```

3. **Hoặc sử dụng Admin UI** để test connection

---

## Lưu ý bảo mật

- **KHÔNG** commit API Key vào Git
- Sử dụng biến môi trường cho production
- Giữ bí mật API Key, không chia sẻ công khai
- Nếu API Key bị lộ, hãy tạo API Key mới ngay lập tức

---

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

