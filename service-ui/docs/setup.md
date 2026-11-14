# Cài đặt và chạy local (service-ui)

## Yêu cầu

- Node.js LTS (>= 18) và npm.
- `service-api` chạy local nếu muốn dữ liệu thật (xem `../../service-api/README.md`).

## Chạy

```bash
npm install
npm run dev          # http://localhost:5173 (cổng khai báo trong vite.config.js)
```

Lệnh khác: `npm run dev:prod` (chạy dev với biến môi trường production), `npm run lint`, `npm run format`.

## Biến môi trường

Vite đọc file theo mode: `.env` (mọi mode), `.env.production` (`--mode production`), `.env.local` / `.env.*.local` (ghi đè trên máy, không commit).

| Biến | Dùng ở đâu |
|---|---|
| `VITE_API_BASE_URL` | Base URL của service-api (http client, ảnh, upload CMS) |
| `VITE_SERVICE_API_URL`, `VITE_USE_SERVICE_API` | Cấu hình http client (`src/services/http/`) |
| `VITE_ENABLE_MOCK` | Bật dữ liệu mock |
| `VITE_APP_STORE_NAME`, `VITE_APP_STORE_DESCRIPTION`, `VITE_APP_CONTACT_EMAIL`, `VITE_APP_CONTACT_PHONE` | Thông tin cửa hàng hiển thị (tuỳ chọn) |

Mọi biến `VITE_*` được đóng gói vào bundle phía trình duyệt: không đặt bí mật vào đây.

## Lỗi thường gặp

- `npm` không nhận: cài lại Node.js, chọn "Add to PATH".
- Cổng 5173 bận: đổi `server.port` trong `vite.config.js`.
- Cài dependencies lỗi: xoá `node_modules` và `package-lock.json` rồi `npm install` lại.
