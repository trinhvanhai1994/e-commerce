# service-ui (Thi Yên Store)

Frontend storefront + trang admin: Vue 3 (Composition API), Vite, Vue Router 4, Pinia, Tailwind CSS.

## Bắt đầu nhanh

```bash
npm install
npm run dev        # http://localhost:5173, gọi service-api theo VITE_API_BASE_URL
```

- Cài đặt, biến môi trường: [docs/setup.md](docs/setup.md)
- Build và deploy: [docs/build-deploy.md](docs/build-deploy.md)

## Cấu trúc `src/`

```
src/
├── main.js, App.vue      # entry, layout chung (header/footer, logo lấy từ CMS)
├── router/               # route public + /admin/* (guard đăng nhập admin)
├── views/                # trang public (Home, Products, Blog...) và Admin*.vue
├── components/           # component dùng lại (ProductCard, ImageSwiper, cms/CmsImageUploader)
├── services/             # gọi API: http/ (client + interceptors), *.service.js theo domain
├── stores/               # Pinia: cart, order
├── content/              # nội dung + ảnh mặc định của trang chủ (fallback khi CMS lỗi)
├── constants/            # imageSlots (tỷ lệ, kích thước ảnh theo slot CMS)
└── utils/                # api.js (facade gọi services), xử lý ảnh, domain, sản phẩm
```

## Quy ước

- Gọi API qua `services/*.service.js` (hoặc facade `utils/api.js`), không gọi `fetch` trực tiếp trong view.
- Nội dung trang chủ và blog do CMS quản lý (`/admin/homepage`, `/admin/blog`), API `/api/dragun/content/*`.
- Yêu cầu và spec nghiệp vụ: `../docs/req/`.
