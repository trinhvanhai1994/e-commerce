# 20 - API surface (service-api + service-ui client)

> Provenance: generated from commit `621a5a9` (2026-11-14) plus uncommitted working tree, by project-onboard.
> Cache, not spec: verify against code. Refresh with: `refresh docs/ai`.
> Prefix `BE/` = `service-api/src/main/java/com/dragun/ecommerce/`, `FE/` = `service-ui/src/`.
> Controller CMS (`AdminHomepageController`, `AdminBlogPostController`, `ContentController`, route `/upload-cms`) đang **untracked / uncommitted** tại thời điểm sinh tài liệu.

## 1. Quy ước chung

| Mục | Giá trị | Nguồn |
|---|---|---|
| Base URL | `http://<host>:5678`, context-path `/` | `service-api/src/main/resources/application.yml:51-53` |
| FE base URL | `VITE_API_BASE_URL`, fallback `http://localhost:5678` | `FE/utils/api.js:5` |
| Envelope thành công | `{ success: true, data, message? }` | `BE/model/dto/response/ApiResponse.java:12-30` |
| Envelope lỗi | `{ success: false, message }` (validation: `data` = map field->message) | `BE/model/dto/response/ApiResponse.java:32-37`, `BE/exception/GlobalExceptionHandler.java:35-50` |
| Auth | JWT Bearer, stateless, không có refresh/logout endpoint; hết hạn mặc định 86400000 ms | `BE/config/SecurityConfig.java:49-53`, `BE/security/JwtAuthenticationFilter.java:47-77`, `application.yml:57` |
| CSRF | tắt | `BE/config/SecurityConfig.java:37` |
| CORS | whitelist origin, methods GET/POST/PUT/DELETE/OPTIONS/PATCH, credentials=true | `BE/config/CorsConfig.java:18-29` |
| Upload limit | file 10MB, request 12MB | `application.yml:7-8` |
| Static ảnh | `/images/**` -> `file:${file.upload-dir}` | `BE/config/WebConfig.java:18,43-44` |
| FE unwrap | adapter trả `data.data \|\| data` nếu `success`, reject envelope nếu `success:false` | `FE/services/http/adapters/service-api.adapter.js:183-185` |
| FE gắn token | `Authorization: Bearer <localStorage.authToken>` | `FE/services/http/adapters/service-api.adapter.js:80-83` |

### Phân quyền (thứ tự matcher)

| Matcher | Quy tắc | Nguồn |
|---|---|---|
| `/api/public/**` | permitAll | `BE/config/SecurityConfig.java:41` |
| `Constants.PUBLIC_ENDPOINTS`: `/api/dragun/products/**`, `/api/dragun/content/**`, `/api/extend/orders`, `/api/extend/orders/**`, `/api/public/visitors/**`, `/provinces`, `/districts/**`, `/wards/**`, `/images/**` | permitAll | `BE/util/Constants.java:6-16`, `SecurityConfig.java:42` |
| `/api/dragun/admin/login` | permitAll | `SecurityConfig.java:43` |
| `/api/dragun/admin/**` | hasRole ADMIN | `Constants.java:18-20`, `SecurityConfig.java:45` |
| mọi request khác (gồm `/api/thiyen/**`) | authenticated | `SecurityConfig.java:47` |

JWT filter bị bỏ qua cho đúng các prefix public ở `BE/security/JwtAuthenticationFilter.java:27-39` (danh sách này phải giữ đồng bộ tay với `Constants.PUBLIC_ENDPOINTS`). Method security bật qua `@EnableMethodSecurity` (`SecurityConfig.java:26`).

## 2. Endpoint catalogue

Cột Auth: `PUBLIC` = permitAll; `ADMIN` = SecurityConfig matcher + `@PreAuthorize` trên class. Cột FE = file service-ui gọi tới (`-` = không có caller).

### 2.1 Auth

| Method | Path | Auth | Request -> Response | Lỗi chính | Side effect | FE caller |
|---|---|---|---|---|---|---|
| POST | `/api/dragun/admin/login` | PUBLIC | `LoginRequest{username,password}` (`@NotBlank`) -> `LoginResponse{token,user{id,name,email,role}}` | 400 validation; 401 "Sai tài khoản hoặc mật khẩu!" (`BE/service/AdminService.java:42`) | không | `FE/services/admin.service.js:15` |

Nguồn: `BE/controller/admin/AdminAuthController.java:13,19-23`, `BE/model/dto/request/LoginRequest.java:13-17`, `BE/model/dto/response/LoginResponse.java:13-24`.

### 2.2 Sản phẩm (storefront, PUBLIC)

| Method | Path | Response | Lỗi | FE caller |
|---|---|---|---|---|
| GET | `/api/dragun/products/list` | `List<ProductResponse>` ACTIVE | - | `FE/services/product.service.js:14` |
| GET | `/api/dragun/products/featured` | `List<ProductResponse>` top ACTIVE theo priority | - | `FE/services/product.service.js:23` |
| GET | `/api/dragun/products/{id}` | `ProductResponse` ACTIVE | 404 `ResourceNotFoundException` (`BE/service/ProductService.java:76`) | `FE/services/product.service.js:33` |
| GET | `/api/dragun/products/{id}/details` | giống `/{id}` (cùng service call) | 404 | `FE/services/product.service.js:42` |

Nguồn: `BE/controller/external/ProductController.java:13,19-45`. `/{id}` và `/{id}/details` trùng logic (`ProductController.java:36,43`).

### 2.2b Đánh giá sản phẩm (chưa commit)

| Method | Path | Quyền | Request -> Response | Lỗi | FE caller |
|---|---|---|---|---|---|
| GET | `/api/dragun/products/{id}/reviews` | PUBLIC | -> `ProductReviewSummaryResponse{averageRating, reviewCount, reviews[]}` chỉ `APPROVED`, mới nhất trước, cache 30s | 404 sản phẩm không tồn tại | `FE/services/review.service.js` `getProductReviews` |
| POST | `/api/dragun/products/{id}/reviews` | PUBLIC | multipart `authorName`, `rating`, `content`, `images[]` (≤ 4, slot `review_image`), `website` (honeypot) -> review `APPROVED` ngay (`approved_by = auto`) | 400 dữ liệu/ảnh sai; 404; 429 quá 5 lần/10 phút/IP | `review.service.js` `submitReview` (fetch trực tiếp) |
| GET | `/api/dragun/admin/reviews?productId=&status=&page=&size=` | ADMIN | -> `ProductReviewPageResponse{items, totalElements, page, size, totalPages, statusCounts}` | 400 status sai | `review.service.js` `getAdminReviews` |
| GET | `/api/dragun/admin/reviews/{id}` | ADMIN | -> `ProductReviewResponse` | 404 | `getAdminReview` |
| POST | `/api/dragun/admin/reviews` | ADMIN | `ProductReviewRequest` (mặc định `APPROVED`, `source=ADMIN`) | 400 `@Valid`/ảnh không phải `/images/...`; 404 sản phẩm | `createReview` |
| PUT | `/api/dragun/admin/reviews/{id}` | ADMIN | `ProductReviewRequest` + `updatedAt` bắt buộc | 400 thiếu `updatedAt`; 409 lệch | `updateReview` |
| PATCH | `/api/dragun/admin/reviews/{id}/status` | ADMIN | `{status}` `PENDING/APPROVED/HIDDEN`; lần đầu `APPROVED` ghi `approved_at/by` | 400; 404 | `updateReviewStatus` |
| DELETE | `/api/dragun/admin/reviews/{id}` | ADMIN | xoá hẳn (file ảnh giữ lại) | 404 | `deleteReview` |

Nguồn: `BE/controller/external/ProductReviewController.java`, `BE/controller/admin/AdminProductReviewController.java`, `BE/service/ProductReviewService.java`. Route công khai nằm dưới `/api/dragun/products/**` nên đã `permitAll` và được `JwtAuthenticationFilter` bỏ qua, không cần sửa hai danh sách public route.

### 2.3 Sản phẩm (admin)

| Method | Path | Request -> Response | Lỗi | Side effect | FE caller |
|---|---|---|---|---|---|
| GET | `/api/dragun/admin/products` | -> `List<ProductResponse>` (ACTIVE + INACTIVE) | 403 | - | `FE/services/admin.service.js:26`, `FE/services/product-management.service.js:17` |
| POST | `/api/dragun/admin/products` | `ProductRequest` (name `@NotBlank`, price/stock `@Positive`) -> `ProductResponse` | 400 validation | ghi DB; nếu `syncToPancake=true` đẩy lên Pancake, lỗi sync chỉ log (`BE/service/AdminService.java:80-91`) | `FE/services/product-management.service.js:42`, `FE/services/admin.service.js:37` |
| PUT | `/api/dragun/admin/products` | `ProductRequest` có `id` trong body (không có path id) -> `ProductResponse` | 400, 404 | như POST | `FE/services/product-management.service.js:52`, `FE/services/admin.service.js:37` |
| DELETE | `/api/dragun/admin/products/{id}` | -> `null` | 404 (inferred) | xóa sản phẩm (`AdminService.java:100-101`) | `FE/services/product-management.service.js:61`, `FE/services/admin.service.js:49` |
| PATCH | `/api/dragun/admin/products/{id}/toggle-status` | -> `ProductResponse` | 404 (inferred) | đảo ACTIVE/INACTIVE | `FE/services/product-management.service.js:70` |

Nguồn: `BE/controller/admin/AdminProductController.java:16-61`, `BE/model/dto/request/ProductRequest.java:17-52`. FE `getProductById` không có route riêng, nó tải toàn bộ list rồi `find` (`FE/services/product-management.service.js:26-29`).

### 2.4 Đơn hàng (`/api/extend/orders`, toàn bộ PUBLIC)

| Method | Path | Request -> Response | Lỗi | Side effect | FE caller |
|---|---|---|---|---|---|
| POST | `/api/extend/orders` | `CreateOrderRequest{customerInfo{name,phone,address,province,district,ward}, items[{productId,quantity}]}` -> `{orderId, message}` | 400 validation / "Insufficient stock"; 404 product | tạo order PENDING, COD, phí ship cứng 20000, **trừ stock** trong `@Transactional` (`BE/service/OrderService.java:31-87`) | `FE/services/order.service.js:15` (từ `FE/views/Checkout.vue:363`) |
| GET | `/api/extend/orders/{orderId}` | -> `OrderResponse` | 404 | - | `FE/services/order.service.js:24` (`FE/views/OrderSuccess.vue:42`) |
| GET | `/api/extend/orders` | -> `List<OrderResponse>` tất cả đơn, mới nhất trước | - | - | `FE/services/order.service.js:32` (`FE/views/AdminOrders.vue:535`, `FE/views/AdminUsers.vue:253`) |
| GET | `/api/extend/orders/customer/{phone}` | -> `List<OrderResponse>` | - | - | `FE/services/order.service.js:41` |
| PUT | `/api/extend/orders/{orderId}/status` | `UpdateOrderStatusRequest{status}` -> `OrderResponse` | 400 "Invalid order status"; 404 | đổi trạng thái, không hoàn stock khi hủy (inferred, không thấy logic ở `OrderService.java:108-121`) | `FE/services/order.service.js:57` (`FE/views/AdminOrders.vue:1132`) |

Nguồn: `BE/controller/external/OrderController.java:17-56`, `BE/model/dto/request/CreateOrderRequest.java:18-52`. `quantity` chỉ có `@NotNull`, không có `@Positive` (`CreateOrderRequest.java:51-52`).

### 2.5 Địa giới hành chính (PUBLIC, không có prefix `/api`)

| Method | Path | Response | FE caller |
|---|---|---|---|
| GET | `/provinces` | `List<Province>` (entity trả thẳng) | `FE/services/location.service.js:14` |
| GET | `/districts/{provinceCode}` | `List<District>` | `FE/services/location.service.js:27` |
| GET | `/wards/{districtCode}` | `List<Ward>` | `FE/services/location.service.js:40` |

Nguồn: `BE/controller/external/LocationController.java:14-33`.

### 2.6 CMS nội dung (untracked)

| Method | Path | Auth | Request -> Response | Lỗi chính | Side effect | FE caller |
|---|---|---|---|---|---|---|
| GET | `/api/dragun/content/homepage` | PUBLIC | -> `HomepageContentResponse`, `Cache-Control: max-age=30, public` | - | - | `FE/services/content.service.js:15` |
| GET | `/api/dragun/content/posts` | PUBLIC | -> `List<BlogPostResponse>` đã publish | - | - | `FE/services/content.service.js:19` |
| GET | `/api/dragun/content/posts/{slug}` | PUBLIC | -> `BlogPostResponse` | 404 (inferred) | - | `FE/services/content.service.js:23` |
| GET | `/api/dragun/admin/homepage/sections` | ADMIN | -> `List<HomepageSectionResponse>` | - | - | `FE/services/content.service.js:27` |
| PUT | `/api/dragun/admin/homepage/sections/{sectionKey}` | ADMIN | `HomepageSectionRequest` (`updatedAt` `@NotNull`, optimistic lock) -> `HomepageSectionResponse` | 400 section_key/ảnh/extra_json sai; 409 bị sửa bởi người khác (`BE/service/ContentService.java:61,68,168-198`) | ghi section, audit user | `FE/services/content.service.js:31` |
| GET | `/api/dragun/content/pages/{pageKey}` | PUBLIC | `pageKey` = `story` (/me) hoặc `philosophy` (/healthcare) -> `Map<sectionKey, HomepageSectionResponse>` (gồm cả khối tắt), cache 30s | 404 trang không tồn tại | - | `FE/composables/usePageContent.js` |
| GET | `/api/dragun/admin/pages/{pageKey}/sections` | ADMIN | -> `List<HomepageSectionResponse>` của trang | 404 trang | - | `FE/views/AdminPageContent.vue` |
| PUT | `/api/dragun/admin/pages/{pageKey}/sections/{sectionKey}` | ADMIN | `HomepageSectionRequest`, nội dung trong `extraJson` (khoá cho phép: `ContentService.PAGE_SECTIONS`); mọi object có `useDefaultImage` theo luật ảnh 3 trạng thái; list tối đa 40 mục; link `link`/`to` được kiểm | 400, 404, 409 như homepage | ghi section | `FE/views/AdminPageContent.vue` |
| GET | `/api/dragun/admin/posts?includeDeleted=` | ADMIN | -> `List<BlogPostResponse>` | - | - | `FE/services/content.service.js:36` |
| GET | `/api/dragun/admin/posts/{id}` | ADMIN | -> `BlogPostResponse` | 404 (inferred) | - | `FE/services/content.service.js:40` |
| POST | `/api/dragun/admin/posts` | ADMIN | `BlogPostRequest{slug,title,excerpt (@NotBlank), coverImage, contentJson[], status, showOnHome, homeOrder, updatedAt}` -> `BlogPostResponse` | 400 slug trùng/không hợp lệ, vượt `@Size` (slug 120, ảnh 1000), block sai, home_order; PUT bắt buộc `updatedAt` (400 thiếu, 409 lệch); 409 vi phạm ràng buộc DB (`BE/service/BlogPostService.java:65,165-261`) | tạo post | `FE/services/content.service.js:44` |
| PUT | `/api/dragun/admin/posts/{id}` | ADMIN | `BlogPostRequest` -> `BlogPostResponse` | 409 conflict (`BlogPostService.java:96`); 400 đổi slug sau publish (`:102`) | cập nhật post | `FE/services/content.service.js:48` |
| PATCH | `/api/dragun/admin/posts/{id}/status` | ADMIN | `BlogPostStatusRequest{status}` -> `BlogPostResponse` | 400 status khác DRAFT/PUBLISHED (`BlogPostService.java:238`) | publish/unpublish | `FE/services/content.service.js:52` |
| POST | `/api/dragun/admin/posts/{id}/soft-delete` | ADMIN | -> `null` | 404 (inferred) | set `deletedAt` (inferred từ `BlogPostService.java:149`) | `FE/services/content.service.js:56` |

Nguồn: `BE/controller/external/ContentController.java:17-43`, `BE/controller/admin/AdminHomepageController.java:17-40`, `BE/controller/admin/AdminBlogPostController.java:18-77`. Lỗi CMS dùng `ResponseStatusException`, map ở `GlobalExceptionHandler.java:52-57`.

### 2.7 File upload (ADMIN)

| Method | Path | Request -> Response | Lỗi | Side effect | FE caller |
|---|---|---|---|---|---|
| POST | `/api/dragun/admin/files/upload-cms` | multipart `file`, `slot`, `entityType?`, `entityId?` -> path (String) do server sinh | 400 `IllegalArgumentException` (định dạng/kích thước/slot; `CmsImageService.validate` rồi `store`, tách để đánh giá kiểm tra cả lô ảnh trước khi lưu); 500 IO | ghi file vào upload-dir | `FE/services/content.service.js:59-78` (fetch trực tiếp) |
| POST | `/api/dragun/admin/files/upload` | multipart `file`, `path` (legacy, client chọn path) -> path | 400 rỗng/không phải `image/*`/path rỗng; 500 IO | ghi file | `FE/services/product-management.service.js:79-99` (fetch trực tiếp) |
| DELETE | `/api/dragun/admin/files/delete?path=` | -> `null` | 500 | xóa file; file không tồn tại vẫn 200 | - |

Nguồn: `BE/controller/admin/AdminFileUploadController.java:15-125`.

### 2.8 Dashboard, visitor, Pancake

| Method | Path | Auth | Request -> Response | Side effect | FE caller |
|---|---|---|---|---|---|
| GET | `/api/dragun/admin/dashboard/stats` | ADMIN | -> `DashboardStatsResponse` | - | `FE/services/admin.service.js:126` |
| POST | `/api/public/visitors/track?sessionId=` | PUBLIC | form param -> sessionId (String) | ghi lượt truy cập, set cookie `visitor_session_id` 1 năm, `HttpOnly=false`, `Secure=false` | `FE/services/visitor-tracking.service.js:94` |
| POST | `/api/thiyen/admin/integration/pancake/sync/products?direction=` | authenticated + `hasRole('ROLE_ADMIN')` | `FROM_PANCAKE\|TO_PANCAKE\|BIDIRECTIONAL` -> `{syncedFromPancake,syncedToPancake,total}` | sync 2 chiều với Pancake POS | - |
| POST | `/api/thiyen/admin/integration/pancake/sync/orders?direction=` | như trên | như trên | sync đơn | - |
| GET | `/api/thiyen/admin/integration/pancake/sync/logs?entityType=&page=&size=` | như trên | -> `Page<PancakeSyncLog>` (entity trả thẳng) | - | - |
| GET | `/api/thiyen/admin/integration/pancake/config` | như trên | -> map config (shopId che `***`, `hasApiKey`) | - | - |
| POST | `/api/thiyen/admin/integration/pancake/test-connection` | như trên | -> `{connected,message}` | gọi Pancake API (blocking `.block()`) | - |

Nguồn: `BE/controller/admin/AdminDashboardController.java:14-22`, `BE/controller/publicapi/VisitorTrackingController.java:17-57`, `BE/controller/admin/AdminIntegrationController.java:22-167`. Pancake controller nằm dưới prefix `/api/thiyen`, không phải `/api/dragun`, nên không khớp `ADMIN_ENDPOINTS`; chỉ được bảo vệ bởi `anyRequest().authenticated()` + `@PreAuthorize` (`AdminIntegrationController.java:24`). Mọi lỗi trong controller này trả HTTP 200 với `success:false` (`:58-62, 91-95, 161-165`).

## 3. Entry point không phải HTTP

| Entry point | Trigger | Hành vi | Nguồn |
|---|---|---|---|
| `PancakeSyncScheduler.syncProducts` | cron `pancake.sync.schedule.products.cron`, mặc định `0 */30 * * * *` | nếu `sync.enabled` và schedule products enabled: `syncFromPancake` rồi `syncToPancake`; lỗi chỉ log | `BE/integration/pancake/scheduler/PancakeSyncScheduler.java:20-35` |
| `PancakeSyncScheduler.syncOrders` | cron mặc định `0 */15 * * * *` | tương tự cho đơn hàng | `PancakeSyncScheduler.java:37-52` |
| Scheduling bật | `@EnableScheduling` | - | `BE/EcommerceApplication.java:8` |
| `CmsSeedService` | `ApplicationRunner`, `@Order(100)` | seed `cms-seed/homepage-sections.json` và post nếu key/slug chưa tồn tại (idempotent); lỗi chỉ log | `BE/service/CmsSeedService.java:26-47,57,103` |
| `LocationDataInitService.initLocationData` | **`@PostConstruct`** (không phải ApplicationRunner) | nạp province/district/ward từ JSON khi bảng rỗng | `BE/service/LocationDataInitService.java:32-56` |

`@Transactional` trên phương thức `@PostConstruct` không có hiệu lực vì proxy chưa áp dụng lúc khởi tạo (inferred, `LocationDataInitService.java:32-33`).

## 4. Hình dạng lỗi thực tế

| Tình huống | HTTP | Body | Nguồn |
|---|---|---|---|
| `ResourceNotFoundException` | 404 | envelope | `GlobalExceptionHandler.java:17-21` |
| `BadRequestException` | 400 | envelope | `:23-27` |
| `UnauthorizedException` (login sai) | 401 | envelope | `:29-33` |
| `@Valid` fail | 400 | envelope, `data` = map lỗi field | `:35-50` |
| `ResponseStatusException` (CMS) | theo status (400/409) | envelope | `:52-57` |
| Mọi `Exception` khác | 500 | `"An error occurred: " + ex.getMessage()` (lộ message nội bộ) | `:59-63` |
| Thiếu/sai token ở tầng filter | 403 không body (inferred: không cấu hình `authenticationEntryPoint`, default `Http403ForbiddenEntryPoint`) | không phải envelope | `SecurityConfig.java:36-53` |
| `@PreAuthorize` từ chối | 500 thay vì 403 (inferred: `AccessDeniedException` bị handler `Exception.class` bắt trước) | envelope | `GlobalExceptionHandler.java:59-63` |
| Pancake integration lỗi | 200 | `success:false` | `AdminIntegrationController.java:58-62` |
| File upload lỗi | 400/500 | envelope, tự build trong controller | `AdminFileUploadController.java:36-41,87-95` |

## 5. Cảnh báo cho người sửa API

- **Endpoint không auth nhưng thay đổi dữ liệu:** `PUT /api/extend/orders/{orderId}/status` (ai cũng đổi được trạng thái đơn; FE admin dùng chính route public này, `FE/views/AdminOrders.vue:1132`); `POST /api/extend/orders` (trừ stock, chấp nhận `quantity` âm vì thiếu `@Positive`, `CreateOrderRequest.java:51-52`, inferred: số âm sẽ cộng stock và giảm tổng tiền theo `OrderService.java:59-70`); `POST /api/public/visitors/track` (ghi DB, chấp nhận nhận dạng session do client đưa vào).
- **Lộ dữ liệu không auth:** `GET /api/extend/orders` trả toàn bộ đơn (tên, SĐT, địa chỉ) và `GET /api/extend/orders/customer/{phone}` dò theo SĐT; matcher `/api/extend/orders/**` permitAll (`Constants.java:9-10`).
- Danh sách public bị khai báo 2 nơi (`Constants.java:6-16` và `JwtAuthenticationFilter.java:30-38`); thêm route public phải sửa cả hai.
- Pancake controller dùng prefix `/api/thiyen` lệch với phần còn lại (`/api/dragun`) và không có FE caller.
- Entity JPA trả thẳng ra API: `Province/District/Ward` (`LocationController.java:21-33`), `PancakeSyncLog` trong `Page` (`AdminIntegrationController.java:100`).
- FE adapter trả nguyên envelope khi `data` null (`service-api.adapter.js:185`), nên các route trả `null` (delete, soft-delete) nhận về `{success,data,message}` thay vì `null`.
- FE<->BE: mọi lời gọi FE đều có route BE khớp. Route BE không có FE caller: `DELETE /api/dragun/admin/files/delete`, toàn bộ `/api/thiyen/admin/integration/pancake/**`.
