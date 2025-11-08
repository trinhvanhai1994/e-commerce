# Hướng Dẫn Cài Đặt và Chạy Project

## Bước 1: Cài đặt Node.js

Nếu máy bạn chưa có Node.js, bạn cần cài đặt:

1. **Tải Node.js:**
   - Truy cập: https://nodejs.org/
   - Tải phiên bản **LTS** (Long Term Support) - khuyến nghị
   - Hoặc tải phiên bản mới nhất

2. **Cài đặt Node.js:**
   - Chạy file installer vừa tải về
   - Làm theo hướng dẫn (Next, Next, Install...)
   - ✅ Đảm bảo tích chọn "Add to PATH" trong quá trình cài đặt

3. **Kiểm tra cài đặt:**
   Mở PowerShell hoặc Command Prompt và chạy:
   ```bash
   node --version
   npm --version
   ```
   
   Nếu hiển thị số phiên bản (ví dụ: `v18.17.0` và `9.6.7`) thì đã cài đặt thành công!

---

## Bước 2: Cài đặt Dependencies (Thư viện) cho Project

1. **Mở Terminal/PowerShell tại thư mục project:**
   - Mở File Explorer, điều hướng đến thư mục `D:\project-common\huyen-store`
   - Click chuột phải vào thư mục → Chọn "Open in Terminal" hoặc "Open PowerShell window here"
   - Hoặc mở PowerShell và chạy:
     ```bash
     cd D:\project-common\huyen-store
     ```

2. **Cài đặt các thư viện cần thiết:**
   ```bash
   npm install
   ```
   
   Lệnh này sẽ tải và cài đặt tất cả các thư viện cần thiết (Vue, Vite, Tailwind CSS, v.v.)
   - ⏱️ Quá trình này có thể mất 2-5 phút tùy tốc độ mạng
   - ✅ Khi thấy "added XXX packages" là đã thành công

---

## Bước 3: Chạy Development Server

Sau khi cài đặt xong, chạy lệnh sau để khởi động ứng dụng:

```bash
npm run dev
```

**Kết quả:**
- Server sẽ chạy tại: `http://localhost:3000`
- Mở trình duyệt và truy cập: `http://localhost:3000`
- Bạn sẽ thấy ứng dụng web của bạn!

**Lưu ý:**
- Giữ cửa sổ Terminal/PowerShell mở trong khi chạy ứng dụng
- Để dừng server, nhấn `Ctrl + C` trong Terminal

---

## Các Lệnh Khác Có Thể Dùng

### Chạy Development Server với Production Mode:
```bash
npm run dev:prod
```

### Build project để deploy:
```bash
npm run build
```

### Xem preview của build:
```bash
npm run preview
```

### Format code:
```bash
npm run format
```

### Lint code:
```bash
npm run lint
```

---

## Cấu Hình Môi Trường (Tùy chọn)

Nếu bạn muốn thay đổi API URL, tạo file `.env.local` trong thư mục gốc:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Thi Yên Store
VITE_APP_DESCRIPTION=Your trusted source for quality products
```

**Mặc định:** Nếu không có file `.env`, ứng dụng sẽ sử dụng API URL: `https://debase.vn`

---

## Xử Lý Lỗi Thường Gặp

### Lỗi: "npm is not recognized"
- **Nguyên nhân:** Node.js chưa được cài đặt hoặc chưa thêm vào PATH
- **Giải pháp:** Cài lại Node.js và đảm bảo tích chọn "Add to PATH"

### Lỗi: "EACCES" hoặc "Permission denied"
- **Nguyên nhân:** Không đủ quyền
- **Giải pháp:** Chạy PowerShell/CMD với quyền Administrator

### Lỗi: "Port 3000 is already in use"
- **Nguyên nhân:** Cổng 3000 đã được sử dụng
- **Giải pháp:** 
  - Đóng ứng dụng đang dùng cổng 3000
  - Hoặc thay đổi cổng trong `vite.config.js`

### Lỗi khi cài đặt dependencies
- **Giải pháp:** Xóa thư mục `node_modules` và file `package-lock.json`, sau đó chạy lại:
  ```bash
  rm -r node_modules
  rm package-lock.json
  npm install
  ```
  (Trên Windows PowerShell, dùng `Remove-Item -Recurse node_modules`)

---

## Tóm Tắt Các Bước

1. ✅ Cài đặt Node.js (nếu chưa có)
2. ✅ Mở Terminal tại thư mục project
3. ✅ Chạy `npm install`
4. ✅ Chạy `npm run dev`
5. ✅ Mở trình duyệt tại `http://localhost:3000`

---

## Cần Hỗ Trợ?

Nếu gặp vấn đề, hãy kiểm tra:
- Node.js version: `node --version` (nên >= 16.x)
- npm version: `npm --version` (nên >= 7.x)
- Đảm bảo đang ở đúng thư mục project


