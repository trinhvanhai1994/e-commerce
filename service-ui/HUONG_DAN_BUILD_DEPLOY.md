# Hướng Dẫn Build và Deploy lên Server

## 📋 Mục Lục
1. [Chuẩn bị](#chuẩn-bị)
2. [Build Project](#build-project)
3. [Kiểm tra Build](#kiểm-tra-build)
4. [Deploy lên Server](#deploy-lên-server)
5. [Cấu hình Server](#cấu-hình-server)
6. [Xử lý lỗi](#xử-lý-lỗi)

---

## 🔧 Chuẩn bị

### 1. Kiểm tra môi trường
Đảm bảo bạn đã cài đặt:
- **Node.js** (phiên bản >= 16.x)
- **npm** (phiên bản >= 7.x)

Kiểm tra bằng lệnh:
```bash
node --version
npm --version
```

### 2. Cài đặt dependencies
Nếu chưa cài đặt hoặc muốn cài lại:
```bash
npm install
```

### 3. Kiểm tra file cấu hình môi trường
Dự án sử dụng các file môi trường:
- `env.local` - cho môi trường local
- `env.production` - cho môi trường production

Đảm bảo file `env.production` có cấu hình đúng:
```env
VITE_API_BASE_URL=https://www.phodem.click
VITE_APP_TITLE=Thi Yên Store
VITE_APP_DESCRIPTION=Your trusted source for quality products
```

---

## 🏗️ Build Project

### Cách 1: Build cho Production (Khuyến nghị)
```bash
npm run build:prod
```

Lệnh này sẽ:
- Build project với mode `production`
- Tối ưu hóa code (minify, tree-shaking)
- Tạo thư mục `dist/` chứa các file đã build

### Cách 2: Build với deploy script tự động
```bash
npm run build:deploy
```

Lệnh này sẽ:
- Build project
- Tự động tạo file `.htaccess` (cho Apache)
- Tự động tạo file `web.config` (cho IIS)
- Sẵn sàng để deploy

### Cách 3: Build thông thường
```bash
npm run build
```

### Các lệnh build khác:
```bash
# Build cho local environment
npm run build:local

# Build cho production (tương đương build:prod)
npm run build:prod
```

---

## ✅ Kiểm tra Build

### 1. Kiểm tra thư mục dist
Sau khi build, kiểm tra thư mục `dist/` có chứa:
```
dist/
├── index.html          # File HTML chính
├── assets/            # CSS, JS đã được bundle
│   ├── index-*.css
│   ├── index-*.js
│   └── vendor-*.js
├── images/            # Hình ảnh từ public/
├── video/             # Video từ public/
├── .htaccess          # (nếu dùng build:deploy)
└── web.config         # (nếu dùng build:deploy)
```

### 2. Preview build local
Chạy lệnh để xem preview:
```bash
npm run preview
```

Hoặc preview với production mode:
```bash
npm run preview:prod
```

Mở trình duyệt tại `http://localhost:4173` để kiểm tra.

### 3. Kiểm tra kích thước file
Đảm bảo các file không quá lớn:
- `index-*.js` thường < 500KB
- `vendor-*.js` thường < 200KB
- CSS files thường < 100KB

---

## 🚀 Deploy lên Server

### Phương pháp 1: Upload thủ công (FTP/SFTP)

#### Bước 1: Chuẩn bị file
```bash
# Build project
npm run build:deploy

# Hoặc build thông thường rồi tạo file cấu hình thủ công
npm run build:prod
```

#### Bước 2: Upload thư mục dist
1. Mở FTP client (FileZilla, WinSCP, v.v.)
2. Kết nối đến server
3. Upload **toàn bộ nội dung** trong thư mục `dist/` lên thư mục web root của server
   - Thường là: `/public_html/` hoặc `/www/` hoặc `/htdocs/`
   - **Lưu ý:** Upload nội dung bên trong `dist/`, không upload thư mục `dist/`!

#### Bước 3: Kiểm tra quyền file
Đảm bảo các file có quyền đọc:
```bash
chmod 644 index.html
chmod 644 assets/*
chmod 755 images/
```

### Phương pháp 2: Deploy bằng Git (nếu server hỗ trợ)

#### Bước 1: Build trên server
SSH vào server và chạy:
```bash
cd /path/to/project
git pull origin main
npm install
npm run build:prod
```

#### Bước 2: Copy file dist
```bash
# Copy nội dung dist/ vào web root
cp -r dist/* /var/www/html/
# Hoặc
rsync -av dist/ /var/www/html/
```

### Phương pháp 3: Sử dụng CI/CD (GitHub Actions, GitLab CI, v.v.)

Tạo file `.github/workflows/deploy.yml`:
```yaml
name: Build and Deploy

on:
  push:
    branches: [ main, prod/admin ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm install
      
      - name: Build
        run: npm run build:prod
      
      - name: Deploy to server
        uses: SamKirkland/FTP-Deploy-Action@4.3.0
        with:
          server: ${{ secrets.FTP_SERVER }}
          username: ${{ secrets.FTP_USERNAME }}
          password: ${{ secrets.FTP_PASSWORD }}
          local-dir: ./dist/
```

---

## ⚙️ Cấu hình Server

### Apache Server (.htaccess)

File `.htaccess` sẽ được tạo tự động khi chạy `npm run build:deploy`.

Nếu cần tạo thủ công, tạo file `.htaccess` trong thư mục `dist/`:
```apache
Options -MultiViews
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteRule ^ index.html [QSA,L]
```

**Lưu ý:** Đảm bảo Apache đã bật module `mod_rewrite`:
```bash
sudo a2enmod rewrite
sudo systemctl restart apache2
```

### IIS Server (web.config)

File `web.config` sẽ được tạo tự động khi chạy `npm run build:deploy`.

Nếu cần tạo thủ công, tạo file `web.config` trong thư mục `dist/`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <system.webServer>
    <rewrite>
      <rules>
        <rule name="Handle History Mode and hash fallback" stopProcessing="true">
          <match url="(.*)" />
          <conditions logicalGrouping="MatchAll">
            <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
            <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
          </conditions>
          <action type="Rewrite" url="/" />
        </rule>
      </rules>
    </rewrite>
  </system.webServer>
</configuration>
```

### Nginx Server

Tạo file cấu hình Nginx:
```nginx
server {
    listen 80;
    server_name thiyen.vn www.thiyen.vn;
    root /var/www/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/javascript application/json;

    # Cache static assets
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Handle Vue Router (History Mode)
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

---

## 🔍 Kiểm tra sau khi Deploy

### 1. Kiểm tra trang chủ
Truy cập: `https://thiyen.vn` hoặc domain của bạn

### 2. Kiểm tra Vue Router
Truy cập trực tiếp một route: `https://thiyen.vn/products/2`
- ✅ Nếu load được: Cấu hình đúng
- ❌ Nếu 404: Kiểm tra lại `.htaccess` hoặc `web.config`

### 3. Kiểm tra API
Mở Developer Tools (F12) → Network tab
- Kiểm tra API calls có trả về đúng không
- Kiểm tra không có lỗi CORS

### 4. Kiểm tra Console
Mở Developer Tools (F12) → Console tab
- Không có lỗi JavaScript
- Không có lỗi 404 cho assets

### 5. Kiểm tra Performance
- Sử dụng Lighthouse (Chrome DevTools)
- Kiểm tra tốc độ load trang
- Kiểm tra SEO score

---

## 🐛 Xử lý Lỗi

### Lỗi: "Cannot GET /products/2"
**Nguyên nhân:** Server chưa được cấu hình để handle Vue Router History Mode

**Giải pháp:**
- Kiểm tra file `.htaccess` (Apache) hoặc `web.config` (IIS) đã có trong thư mục deploy chưa
- Đảm bảo `mod_rewrite` đã được bật (Apache)
- Kiểm tra cấu hình Nginx nếu dùng Nginx

### Lỗi: "404 Not Found" cho assets (CSS, JS)
**Nguyên nhân:** Đường dẫn assets không đúng

**Giải pháp:**
- Kiểm tra file `index.html` có đường dẫn đúng đến assets không
- Đảm bảo thư mục `assets/` đã được upload
- Kiểm tra base path trong `vite.config.js`

### Lỗi: "API calls failed"
**Nguyên nhân:** Cấu hình API URL không đúng

**Giải pháp:**
- Kiểm tra file `env.production` có đúng API URL không
- Rebuild lại project: `npm run build:prod`
- Kiểm tra CORS settings trên server API

### Lỗi: "White screen" hoặc "Blank page"
**Nguyên nhân:** 
- JavaScript error
- Assets không load được
- Console có lỗi

**Giải pháp:**
1. Mở Developer Tools (F12)
2. Kiểm tra Console tab xem có lỗi gì
3. Kiểm tra Network tab xem assets có load không
4. Kiểm tra file `index.html` có đúng không

### Lỗi: Build quá chậm hoặc bị lỗi
**Giải pháp:**
```bash
# Xóa cache và rebuild
rm -rf node_modules dist
npm install
npm run build:prod
```

---

## 📝 Checklist Deploy

Trước khi deploy, đảm bảo:

- [ ] Đã chạy `npm install` thành công
- [ ] Đã kiểm tra file `env.production` có đúng cấu hình
- [ ] Đã chạy `npm run build:prod` thành công
- [ ] Đã kiểm tra thư mục `dist/` có đầy đủ file
- [ ] Đã chạy `npm run preview` để test local
- [ ] Đã tạo file `.htaccess` hoặc `web.config` (nếu dùng `build:deploy` thì tự động)
- [ ] Đã upload toàn bộ nội dung `dist/` lên server
- [ ] Đã kiểm tra quyền file trên server
- [ ] Đã test trang chủ hoạt động
- [ ] Đã test Vue Router (truy cập trực tiếp route)
- [ ] Đã kiểm tra API calls hoạt động
- [ ] Đã kiểm tra không có lỗi trong Console

---

## 🎯 Tóm tắt các lệnh quan trọng

```bash
# Build cho production
npm run build:prod

# Build và tạo file cấu hình deploy
npm run build:deploy

# Preview build local
npm run preview

# Xóa và rebuild
rm -rf dist && npm run build:prod
```

---

## 📞 Cần hỗ trợ?

Nếu gặp vấn đề:
1. Kiểm tra lại các bước trong checklist
2. Xem log trong Console (F12)
3. Kiểm tra Network tab xem request nào bị lỗi
4. Kiểm tra cấu hình server (Apache/Nginx/IIS)

---

**Chúc bạn deploy thành công! 🚀**

