# PLAN THIẾT KẾ: TẤT CẢ DỮ LIỆU LẤY TỪ API (service-api)

## 🎯 MỤC TIÊU
Đảm bảo **100% dữ liệu hiển thị trên giao diện** (ảnh, text, thông tin sản phẩm) đều được lấy từ **API calls đến service-api**, không còn dữ liệu tĩnh từ frontend.

## 📋 PHÂN TÍCH VẤN ĐỀ HIỆN TẠI

### ❌ Vấn đề 1: Ảnh tĩnh trong templates
- Nhiều component dùng path tĩnh: `src="/images/..."`
- Ảnh được load trực tiếp từ `service-ui/public/images/`
- Không qua API server

**Ví dụ:**
```vue
<img src="/images/banner.png" />
<img src="/images/products/me-den.jpg" />
```

### ❌ Vấn đề 2: Mock data trong code
- Fallback data hardcode trong component
- Không gọi API khi có lỗi
- Dữ liệu không đồng bộ với database

**Ví dụ:**
```javascript
const fallbackProducts = [
  { id: 1, name: '...', image: '/images/...' }
]
```

### ❌ Vấn đề 3: Utility functions chưa đầy đủ
- `getProductImage()` có fallback nhưng vẫn dùng path tĩnh
- Chưa có wrapper cho tất cả loại ảnh (banner, icon, blog, etc.)

## ✅ GIẢI PHÁP THIẾT KẾ

### 1. **Image Utility Layer (Hoàn thiện)**

#### 1.1. `imageUtils.js` - Core utility
```javascript
// Đã có: getImageUrlFromApi(path)
// ✅ Hoạt động tốt, chỉ cần đảm bảo tất cả ảnh đều qua function này
```

#### 1.2. Tạo wrapper cho các loại ảnh
```javascript
// imageUtils.js
export function getStaticImageUrl(imagePath) {
  // Wrapper cho ảnh static (banner, icon, logo)
  // Tất cả đều load từ API server
  return getImageUrlFromApi(imagePath)
}
```

### 2. **Component Data Flow**

```
┌─────────────────┐
│   Component     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  API Service    │ ──► Call API endpoint
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  service-api    │ ──► Return data + image paths
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  imageUtils     │ ──► Convert path → API URL
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Component      │ ──► Display image from API
└─────────────────┘
```

### 3. **Các Component Cần Sửa**

#### 3.1. **Home.vue**
- ❌ `src="/images/banner.png"` → ✅ `getImageUrlFromApi('/images/banner.png')`
- ❌ `src="/images/baner-second.png"` → ✅ `getImageUrlFromApi('/images/baner-second.png')`
- ❌ `src="/images/iso.png"` → ✅ `getImageUrlFromApi('/images/iso.png')`
- ❌ `src="/images/gmp.png"` → ✅ `getImageUrlFromApi('/images/gmp.png')`
- ❌ `src="/images/four-home.png"` → ✅ `getImageUrlFromApi('/images/four-home.png')`
- ❌ `src="/images/struct/1.png"` → ✅ `getImageUrlFromApi('/images/struct/1.png')`
- ❌ Blog images hardcode → ✅ Lấy từ API hoặc dùng `getImageUrlFromApi()`

#### 3.2. **ProductDetail.vue**
- ❌ `poster="/images/products/details/black/1.png"` → ✅ `getImageUrlFromApi(...)`
- ❌ `src="/images/icons/Step1.png"` → ✅ `getImageUrlFromApi(...)`
- ❌ `src="/images/logo/logo.png"` → ✅ `getImageUrlFromApi(...)`
- ❌ Review images hardcode → ✅ Lấy từ API hoặc dùng `getImageUrlFromApi()`
- ❌ Fallback `'/images/products/details/black/1.png'` → ✅ `getImageUrlFromApi(...)`

#### 3.3. **Blog.vue & ArticleDetail.vue**
- ❌ Blog images hardcode → ✅ Lấy từ API hoặc dùng `getImageUrlFromApi()`
- ❌ Article images hardcode → ✅ Lấy từ API hoặc dùng `getImageUrlFromApi()`

#### 3.4. **Me.vue & HealthCare.vue**
- ❌ `src="/images/me/banner.jpg"` → ✅ `getImageUrlFromApi(...)`
- ❌ `src="/images/logo/logo.png"` → ✅ `getImageUrlFromApi(...)`
- ❌ `src="/images/icons/..."` → ✅ `getImageUrlFromApi(...)`
- ❌ Product images hardcode → ✅ Lấy từ API

#### 3.5. **AdminHeader.vue & AdminLogin.vue**
- ❌ `src="/images/logo/logo.png"` → ✅ `getImageUrlFromApi(...)`

#### 3.6. **AdminOrders.vue**
- ❌ Default products hardcode → ✅ Gọi API để lấy product data
- ❌ Fallback images → ✅ `getImageUrlFromApi(...)`

### 4. **API Integration**

#### 4.1. Product Data
```javascript
// ✅ Đã có: productAPI.getProducts()
// ✅ Đã có: productAPI.getProductById(id)
// ✅ Đã có: productAPI.getFeaturedProducts()
// Cần đảm bảo: Tất cả component đều dùng API, không dùng mock data
```

#### 4.2. Image Paths từ API
```javascript
// API trả về:
{
  id: 1,
  name: "...",
  mainImage: "/images/products/me-den.jpg",  // Path từ API
  gallery: ["/images/products/details/black/1.png", ...]
}

// Frontend xử lý:
const imageUrl = getImageUrlFromApi(product.mainImage)
// → "http://localhost:5678/images/products/me-den.jpg"
```

### 5. **Fallback Strategy**

#### 5.1. Khi API fail
```javascript
// ❌ KHÔNG dùng mock data hardcode
// ✅ Retry API call
// ✅ Hiển thị error message
// ✅ Fallback image từ API server (không phải frontend)
```

#### 5.2. Khi image path không có
```javascript
// ✅ Dùng placeholder từ API server
// ✅ Hoặc ảnh mặc định từ API server
// ❌ KHÔNG dùng ảnh từ frontend public folder
```

## 🔧 IMPLEMENTATION STEPS

### Step 1: Update imageUtils.js
- [x] Đã có `getImageUrlFromApi()` - hoạt động tốt
- [ ] Thêm helper cho static images nếu cần

### Step 2: Fix Home.vue
- [ ] Replace tất cả `src="/images/..."` → `:src="getImageUrlFromApi('/images/...')"`
- [ ] Remove mock data, chỉ dùng API

### Step 3: Fix ProductDetail.vue
- [ ] Replace tất cả static image paths
- [ ] Đảm bảo gallery images từ API

### Step 4: Fix Blog components
- [ ] Replace blog images với `getImageUrlFromApi()`
- [ ] Hoặc tạo API endpoint cho blog images

### Step 5: Fix Admin components
- [ ] Replace logo paths
- [ ] Remove mock data trong AdminOrders

### Step 6: Fix Me.vue & HealthCare.vue
- [ ] Replace tất cả static paths

### Step 7: Fix ProductCard component
- [ ] Đảm bảo dùng `getProductImage(product)` với product object

### Step 8: Testing
- [ ] Test tất cả pages
- [ ] Verify ảnh load từ API server
- [ ] Check Network tab: tất cả ảnh request đến `localhost:5678`

## 📝 RULES

1. **KHÔNG** dùng `src="/images/..."` trực tiếp trong template
2. **LUÔN** dùng `getImageUrlFromApi(path)` hoặc `getProductImage(product)`
3. **KHÔNG** hardcode mock data trong component
4. **LUÔN** gọi API để lấy data
5. **KHÔNG** dùng fallback từ frontend public folder
6. **LUÔN** dùng fallback từ API server

## ✅ VERIFICATION CHECKLIST

- [ ] Tất cả ảnh trong Home.vue load từ API
- [ ] Tất cả ảnh trong ProductDetail.vue load từ API
- [ ] Tất cả ảnh trong Products.vue load từ API
- [ ] Tất cả ảnh trong Blog.vue load từ API
- [ ] Tất cả ảnh trong Admin components load từ API
- [ ] Network tab: Không có request đến `/images/...` từ frontend
- [ ] Network tab: Tất cả ảnh request đến `localhost:5678/images/...`
- [ ] Không có mock data hardcode trong components
- [ ] Tất cả product data từ API calls

