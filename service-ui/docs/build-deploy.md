# Build và deploy (service-ui)

## Build

| Lệnh | Mô tả |
|---|---|
| `npm run build:prod` | Build production (đọc `.env` + `.env.production`) |
| `npm run build:deploy` | Build production + sinh `.htaccess` và `web.config` vào `dist/` (`deploy.js`) |
| `npm run build:full` | Kiểm tra môi trường + build (`build-and-deploy.js`) |
| `npm run build:check` | Chỉ kiểm tra môi trường |
| `npm run build:preview` | Build rồi chạy preview |
| `npm run preview` | Xem bản build tại http://localhost:5173 |

Sau build, kiểm tra URL API đã đúng: tìm giá trị `VITE_API_BASE_URL` trong `dist/assets/*.js` hoặc xem tab Network.

## Deploy

Upload **toàn bộ nội dung** `dist/` lên web root. App dùng Vue Router history mode nên server phải trả `index.html` cho mọi route không phải file tĩnh.

### Apache (`.htaccess`, sinh bởi `build:deploy`)

```apache
Options -MultiViews
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteRule ^ index.html [QSA,L]
```

Cần bật `mod_rewrite`.

### IIS (`web.config`, sinh bởi `build:deploy`; bản mẫu ở `service-ui/web.config`)

Rule rewrite mọi request không phải file hoặc thư mục về `/`.

### Nginx

```nginx
server {
    listen 80;
    root /var/www/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;

    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

## Checklist

- [ ] `.env.production` trỏ đúng API production.
- [ ] `npm run build:deploy` thành công, `dist/` có `index.html`, `assets/`, `.htaccess`, `web.config`.
- [ ] Đã kiểm tra bằng `npm run preview`.
- [ ] Sau deploy: mở thẳng một route sâu (vd `/products/1`) không bị 404; Console không lỗi; API không bị CORS.

## Lỗi thường gặp

- **`Cannot GET /products/2`**: thiếu rewrite history mode (xem cấu hình server ở trên).
- **API vẫn gọi localhost**: build sai mode; dùng `npm run build:prod`, kiểm tra `.env.production`.
- **CORS**: thêm domain frontend vào cấu hình CORS của service-api.
- **Màn trắng**: mở DevTools, xem Console và Network (thường do assets không upload đủ).
