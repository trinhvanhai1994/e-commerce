# 🚀 Hướng Dẫn Build cho Production với API https://dragun.cloud

## 📋 Tóm tắt nhanh

Để build `service-ui` gọi API production tại `https://dragun.cloud`:

```bash
cd service-ui
npm run build:prod
```

## 🔧 Các bước chi tiết

### 1. Kiểm tra file cấu hình môi trường

File `env.production` đã được tạo với cấu hình:
```env
VITE_API_BASE_URL=https://dragun.cloud
VITE_SERVICE_API_URL=https://dragun.cloud
```

### 2. Build cho Production

#### Cách 1: Build đơn giản (Khuyến nghị)
```bash
cd service-ui
npm run build:prod
```

#### Cách 2: Build và tạo file deploy tự động
```bash
cd service-ui
npm run build:deploy
```
Lệnh này sẽ:
- ✅ Build với production mode
- ✅ Tự động tạo file `.htaccess` (Apache)
- ✅ Tự động tạo file `web.config` (IIS)
- ✅ Sẵn sàng để deploy

#### Cách 3: Build với kiểm tra đầy đủ
```bash
cd service-ui
npm run build:full
```

### 3. Kiểm tra build

Sau khi build, kiểm tra thư mục `dist/`:
```bash
ls -la dist/
```

Bạn sẽ thấy:
```
dist/
├── index.html
├── assets/
│   ├── index-*.css
│   ├── index-*.js
│   └── vendor-*.js
├── images/
├── video/
├── .htaccess (nếu dùng build:deploy)
└── web.config (nếu dùng build:deploy)
```

### 4. Preview build local (Tùy chọn)

Để test build trước khi deploy:
```bash
npm run preview
```

Mở trình duyệt tại `http://localhost:4173` và kiểm tra:
- ✅ Trang chủ load được
- ✅ API calls đang gọi đến `https://dragun.cloud`
- ✅ Không có lỗi trong Console (F12)

### 5. Deploy lên Server

Upload **toàn bộ nội dung** trong thư mục `dist/` lên web root của server:
- `/var/www/html/` (Linux)
- `/public_html/` (cPanel)
- Hoặc thư mục web root của bạn

## 🔍 Kiểm tra API URL sau khi build

### Cách 1: Kiểm tra trong code đã build
```bash
# Tìm kiếm API URL trong file JS đã build
grep -r "dragun.cloud" dist/assets/
```

### Cách 2: Kiểm tra trong browser
1. Mở website đã deploy
2. Mở Developer Tools (F12)
3. Vào tab **Network**
4. Reload trang
5. Kiểm tra các request API có domain `https://dragun.cloud`

### Cách 3: Kiểm tra trong Console
Mở Console (F12) và chạy:
```javascript
console.log(import.meta.env.VITE_API_BASE_URL)
// Hoặc kiểm tra trong Network tab xem API calls
```

## ⚙️ Cấu hình môi trường

### File env.production
File này được sử dụng khi build với mode `production`:
```env
VITE_API_BASE_URL=https://dragun.cloud
VITE_SERVICE_API_URL=https://dragun.cloud
VITE_APP_TITLE=Thi Yên Store
VITE_APP_DESCRIPTION=Your trusted source for quality products
```

### Thay đổi API URL
Nếu cần thay đổi API URL, sửa file `env.production`:
```env
VITE_API_BASE_URL=https://your-new-api-domain.com
```

Sau đó rebuild:
```bash
npm run build:prod
```

## 🐛 Xử lý lỗi

### Lỗi: API vẫn gọi localhost
**Nguyên nhân:** File `env.production` không được load đúng

**Giải pháp:**
1. Kiểm tra file `env.production` có trong thư mục `service-ui/`
2. Đảm bảo build với mode production: `npm run build:prod`
3. Xóa cache và rebuild:
   ```bash
   rm -rf dist node_modules/.vite
   npm run build:prod
   ```

### Lỗi: CORS khi gọi API
**Nguyên nhân:** Server API chưa cấu hình CORS cho domain frontend

**Giải pháp:**
- Cấu hình CORS trên server API để cho phép domain frontend
- Kiểm tra backend có cho phép origin của frontend không

### Lỗi: Build chậm hoặc bị lỗi
**Giải pháp:**
```bash
# Xóa cache và rebuild
rm -rf dist node_modules/.vite
npm install
npm run build:prod
```

## 📝 Checklist Build Production

Trước khi deploy, đảm bảo:

- [ ] File `env.production` có `VITE_API_BASE_URL=https://dragun.cloud`
- [ ] Đã chạy `npm run build:prod` thành công
- [ ] Đã kiểm tra thư mục `dist/` có đầy đủ file
- [ ] Đã test với `npm run preview` (tùy chọn)
- [ ] Đã kiểm tra API calls trong Network tab gọi đến `https://dragun.cloud`
- [ ] Đã upload nội dung `dist/` lên server
- [ ] Đã test website trên server production

## 🎯 Tóm tắt các lệnh

```bash
# Build production
npm run build:prod

# Build và tạo file deploy
npm run build:deploy

# Build với kiểm tra đầy đủ
npm run build:full

# Preview build
npm run preview

# Xóa và rebuild
rm -rf dist && npm run build:prod
```

## 📞 Lưu ý quan trọng

1. **Luôn build với mode production** khi deploy lên server thật
2. **Kiểm tra API URL** sau khi build để đảm bảo đúng domain
3. **Test trên preview** trước khi deploy lên server
4. **Backup** code và file cấu hình trước khi thay đổi

---

**Chúc bạn build và deploy thành công! 🚀**

