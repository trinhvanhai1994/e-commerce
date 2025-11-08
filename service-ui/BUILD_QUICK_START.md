# 🚀 Build & Deploy - Quick Start

## Cách nhanh nhất để build và deploy

### 1️⃣ Build cho Production
```bash
npm run build:deploy
```
Lệnh này sẽ:
- ✅ Build project với production mode
- ✅ Tạo file `.htaccess` (Apache)
- ✅ Tạo file `web.config` (IIS)
- ✅ Sẵn sàng để deploy

### 2️⃣ Kiểm tra Build
```bash
npm run preview
```
Mở trình duyệt tại `http://localhost:4173` để kiểm tra.

### 3️⃣ Upload lên Server
Upload **toàn bộ nội dung** trong thư mục `dist/` lên web root của server.

---

## 📋 Các lệnh Build khác

| Lệnh | Mô tả |
|------|-------|
| `npm run build:prod` | Build production (không tạo file deploy) |
| `npm run build:deploy` | Build + tạo file cấu hình deploy |
| `npm run build:full` | Build với kiểm tra đầy đủ |
| `npm run build:preview` | Build + chạy preview tự động |
| `npm run build:check` | Chỉ kiểm tra, không build |
| `npm run preview` | Xem preview của build |

---

## ⚡ Build với Helper Script

### Build đầy đủ với kiểm tra
```bash
npm run build:full
```

### Build và preview tự động
```bash
npm run build:preview
```

### Chỉ kiểm tra môi trường
```bash
npm run build:check
```

---

## 📁 Cấu trúc sau khi Build

```
dist/
├── index.html          # File HTML chính
├── assets/            # CSS, JS đã bundle
│   ├── index-*.css
│   ├── index-*.js
│   └── vendor-*.js
├── images/            # Hình ảnh
├── video/             # Video
├── .htaccess          # Cấu hình Apache
└── web.config          # Cấu hình IIS
```

---

## ✅ Checklist trước khi Deploy

- [ ] Đã chạy `npm run build:deploy`
- [ ] Đã test với `npm run preview`
- [ ] Đã kiểm tra thư mục `dist/` có đầy đủ file
- [ ] Đã upload nội dung `dist/` lên server
- [ ] Đã test website trên server

---

## 🐛 Xử lý lỗi nhanh

### Build bị lỗi?
```bash
# Xóa và rebuild
npm run build:clean
# Hoặc
rm -rf dist node_modules
npm install
npm run build:prod
```

### Route không hoạt động (404)?
- Kiểm tra file `.htaccess` hoặc `web.config` đã có trong `dist/`
- Đảm bảo server đã cấu hình đúng (xem `HUONG_DAN_BUILD_DEPLOY.md`)

### Assets không load?
- Kiểm tra thư mục `assets/` đã được upload
- Kiểm tra đường dẫn trong `index.html`

---

## 📖 Xem hướng dẫn chi tiết

Xem file `HUONG_DAN_BUILD_DEPLOY.md` để biết:
- Cấu hình server chi tiết
- Các phương pháp deploy
- Xử lý lỗi đầy đủ
- Cấu hình Nginx/Apache/IIS

---

**💡 Tip:** Luôn chạy `npm run build:deploy` trước khi deploy để đảm bảo có đầy đủ file cấu hình!

