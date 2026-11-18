# Nghiệp vụ (business rules)
> Generated from 621a5a9 (2026-11-14) by project-onboard, including uncommitted working-tree changes (CMS homepage + blog).
> This is a cache of the codebase, not a specification. Verify anything load-bearing
> against the code before relying on it, and refresh with: "refresh docs/ai".
> Đường dẫn Java viết tắt: `api/` = `service-api/src/main/java/com/dragun/ecommerce/`, `ui/` = `service-ui/src/`.

## Domain glossary
| Thuật ngữ | Nghĩa trong code | Nơi định nghĩa |
|---|---|---|
| Product | Sản phẩm bột/thực phẩm Thi Yên; `status` = `ACTIVE`/`INACTIVE` (String), `deleted` = soft delete, `priority` nhỏ = hiện trước (mặc định 999) | `api/service/ProductService.java:86-99` |
| Featured products | 4 sản phẩm ACTIVE đầu tiên theo `priority` cho trang chủ | `api/service/ProductService.java:33-46` |
| Order | Đơn COD; `orderId` dạng `yyyyMMdd` + 5 số; `orderType` = `THI_YEN` | `api/service/OrderService.java:45-47,128-139` |
| OrderStatus | `PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED` (JSON dùng giá trị viết hoa) | `api/model/enums/OrderStatus.java` |
| Customer / "Users" admin | Không có tài khoản khách; trang AdminUsers dựng danh sách khách từ orders | `ui/views/AdminUsers.vue:247` |
| Admin | User DB có `role = ROLE_ADMIN` (seed `admin`) | `service-api/src/main/resources/db/migration/V6__Fix_admin_password.sql:10-11` |
| Homepage section | Khối CMS trang chủ, key cố định: `hero, story, quality, meal_gallery, floating_chat, header_logo` | `api/service/ContentService.java:26-28` |
| Blog post | Bài viết `DRAFT`/`PUBLISHED`, nội dung là mảng block JSON, có thể ghim lên Home (tối đa 3) | `api/service/BlogPostService.java` |
| ImageSlot | Quy cách ảnh CMS/product (tỷ lệ, kích thước tối thiểu, MIME, 10MB) | `api/cms/ImageSlot.java:11-21` |
| Pancake | POS bên ngoài; đồng bộ product/order hai chiều theo lịch | `api/integration/pancake/` |

## Entities and invariants
| Entity | Invariant | Enforced at | Tested? |
|---|---|---|---|
| Product | Sản phẩm mới mặc định `deleted=false`, `status=ACTIVE`, `priority=999` | `api/service/ProductService.java:88-96`, lặp lại ở `api/service/AdminService.java:130-142` | không |
| Product | Xóa = soft delete (`deleted=true`), không xóa row | `api/service/ProductService.java:155-161` | không |
| Product | Storefront chỉ thấy `ACTIVE` và chưa xóa; trả 404 nếu không thỏa | `api/service/ProductService.java:71-79` | không |
| Product | Gallery được ghi đè toàn bộ khi update (clear + flush rồi set lại, giữ thứ tự) | `api/service/ProductService.java:117-128` | không |
| Order | `orderId` unique ở DB | `V1__Initial_schema.sql:45` | không |
| Order | Giá item chụp tại thời điểm đặt (`price`, `productName` lưu vào OrderItem) | `api/service/OrderService.java:62-67` | không |
| Order | Tiền dùng `DECIMAL(19,2)` / `BigDecimal` (không dùng float) | `V1__Initial_schema.sql:55-57` | n/a |
| HomepageSection | `section_key` unique; chỉ cập nhật được key nằm trong whitelist | `V16__Create_cms_tables.sql:5`, `api/service/ContentService.java:60-62` | không |
| BlogPost | `slug` unique toàn bảng (kể cả bản đã soft delete); `status IN (DRAFT, PUBLISHED)`; `home_order` 1..3 và unique giữa các bài đang hiện Home | `V16__Create_cms_tables.sql:21,35-41` | không |

## Business rules

### Đặt hàng (storefront, `POST /api/extend/orders`)
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Bắt buộc | `customerInfo.name/phone/address` not blank, `items` not empty, mỗi item có `productId`, `quantity` not null. Province/district/ward **không bắt buộc** ở BE | `api/model/dto/request/CreateOrderRequest.java:18-52` |
| Kiểm tồn kho | Nếu `stock < quantity` thì 400 "Insufficient stock" | `api/service/OrderService.java:55-57` |
| Trừ tồn kho | Trừ ngay khi tạo đơn, trong cùng transaction | `api/service/OrderService.java:69-71` |
| Phí ship BE | Luôn cố định 20.000đ, `total = subTotal + 20000` | `api/service/OrderService.java:78-83` |
| Thanh toán | Luôn `COD`, trạng thái đầu `PENDING` | `api/service/OrderService.java:45-46` |
| Sinh mã đơn | `yyyyMMdd` + (số đơn trong ngày + 1) dạng 5 số | `api/service/OrderService.java:128-139` |
| Giảm giá | BE không nhận mã giảm giá; request không có trường discount | `CreateOrderRequest.java` |

### Giỏ hàng / checkout (chỉ ở FE, không được BE áp dụng)
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Phí ship FE | 20.000đ nếu giỏ có product `id` 1 hoặc 3 **và** subtotal <= 299.000đ, ngược lại 0 | `ui/views/Checkout.vue:407-414`, `ui/views/Cart.vue:342-348`, `ui/App.vue:186-193` |
| Text phí ship | "20.000đ cho đơn 1 sản phẩm, miễn phí khi mua 2 sản phẩm" (rule thứ ba, khác cả hai bên trên) | `ui/views/ProductDetail.vue:337-338` |
| Mã giảm giá (mock) | `TINEE10` 10%, `WELCOME20` 20%, `FREESHIP` miễn ship; chỉ hiển thị trong Cart, không gửi lên BE | `ui/views/Cart.vue:330-339,404-415` |
| Validate form | Bắt buộc name, address, province, district, ward, phone (không kiểm định dạng SĐT) | `ui/views/Checkout.vue:330-339` |
| Giá giỏ hàng | Lưu localStorage, đồng bộ lại giá từ API products | `ui/stores/cart.js:10-36,73-80` |

### Quản lý đơn (admin)
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Đổi trạng thái | Chấp nhận mọi giá trị hợp lệ của enum (không phân biệt hoa/thường), **không có state machine** (có thể DELIVERED -> PENDING) | `api/service/OrderService.java:108-122`, `api/model/enums/OrderStatus.java:32-50` |
| Hủy đơn | `CANCELLED` không hoàn tồn kho | `api/service/OrderService.java:119` (inferred: không có code hoàn kho) |
| Endpoint | Admin UI dùng chung `/api/extend/orders` (list, đổi status) với storefront, không qua `/api/dragun/admin/**` | `ui/services/order.service.js:32,57`, `api/controller/external/OrderController.java:37-56` |

### Sản phẩm (admin)
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Lưu | Một endpoint create/update: có `id` thì update, không thì create | `api/service/AdminService.java:65-77` |
| Đồng bộ Pancake khi lưu | Nếu `syncToPancake=true` thì đẩy lên Pancake; lỗi chỉ log, sản phẩm vẫn lưu | `api/service/AdminService.java:79-94` |
| Toggle status | ACTIVE <-> INACTIVE; mọi giá trị khác thành ACTIVE | `api/service/ProductService.java:163-177` |

### CMS trang chủ
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Optimistic lock | PUT section bắt buộc gửi `updatedAt` khớp DB, nếu không 409 | `api/service/ContentService.java:66-69` |
| Cấu trúc extra_json | `quality.items` đúng 2 phần tử; `meal_gallery.tiles` đúng 4 phần tử; các key khác tự do | `api/service/ContentService.java:163-184` |
| Ảnh 3 trạng thái | `useDefault*` null/true -> xóa path, FE dùng ảnh đóng gói; false -> dùng path đã upload | `api/service/ContentService.java:74-84,100-148`, `ui/content/homeDefaults.js:122-128` |
| Path ảnh | Chỉ chấp nhận `/images/...`; cấm `..`, `\0`, `http(s)://`, `data:` | `api/service/ContentService.java:186-201` |
| Section tắt | Trả về `enabled=false` để FE ẩn; key không có trong DB -> FE dùng mặc định | `api/service/ContentService.java:35-40`, `ui/views/Home.vue:380-393` |
| Fallback | API lỗi -> FE hiển thị toàn bộ nội dung mặc định đóng gói | `ui/views/Home.vue:389-392` |
| Seed | Khi khởi động, chèn section/bài từ `cms-seed/*.json` nếu chưa tồn tại (theo key/slug), không ghi đè | `api/service/CmsSeedService.java:35-117` |

### Blog
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Slug | Regex `^[a-z0-9]+(?:-[a-z0-9]+)*$`, unique trong các bài chưa xóa | `api/service/BlogPostService.java:24,63-66,226-230` |
| Slug bất biến | Không đổi slug khi bài đang `PUBLISHED` (FE cũng disable input) | `api/service/BlogPostService.java:99-103`, `ui/views/AdminBlogEditor.vue:9` |
| publishedAt | Gán lần đầu chuyển sang PUBLISHED, không reset khi về DRAFT | `api/service/BlogPostService.java:83-85,122-124,136-138` |
| Ghim Home | `homeOrder` 1..3, tối đa 3 bài, không trùng thứ tự | `api/service/BlogPostService.java:160-183` |
| Về DRAFT | Qua PATCH status: tự bỏ ghim Home. Qua PUT update: **không** tự bỏ ghim | `api/service/BlogPostService.java:139-142` vs `:113-126` |
| Hiển thị Home | Chỉ bài PUBLISHED, chưa xóa, showOnHome, sắp theo homeOrder, tối đa 3 | `api/repository/BlogPostRepository.java:19-26`, `api/service/ContentService.java:41-44` |
| Soft delete | Set `deletedAt`, bỏ ghim, status về DRAFT | `api/service/BlogPostService.java:148-158` |
| Optimistic lock | PUT blog bắt buộc gửi `updatedAt` (thiếu: 400, lệch: 409), giống section; tạo mới thì bỏ qua | `api/service/BlogPostService.java:94-103` |
| Nội dung | Block type thuộc `p, h2, ul, img, cta, table`; `ul` cần `items[]`, `img` cần `src` hợp lệ; text lọc `<script>` và thuộc tính `on*` bằng regex | `api/service/BlogPostService.java:25,185-250` |

### Upload ảnh CMS (`ImageSlot`)
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Kiểm file | Không rỗng, <= 10MB, MIME theo slot (server chỉ nhận JPEG/PNG; `quality_icon`/`value_icon` chỉ PNG), nội dung phải khớp MIME khai báo, tối đa 16 triệu điểm ảnh (đọc từ header trước khi decode), đuôi file lấy theo định dạng thật. Admin UI nhận cả WebP làm nguồn nhưng luôn mã hoá lại sang PNG/JPEG | `api/service/CmsImageService.java`, `ui/utils/imageSlotPipeline.js` |
| Kích thước | >= min của slot; tỷ lệ trong dung sai, hoặc sát output size (±2px) | `api/service/CmsImageService.java:38-53`, `api/cms/ImageSlot.java:57-64` |
| Path do server sinh | `products/{id}/main_*` hoặc `.../gallery/*`; blog -> `cms/blogs/...`; còn lại `cms/{slot}/...` | `api/service/CmsImageService.java:80-93` |
| FE cắt ảnh trước | FE crop/resize theo spec slot riêng rồi mới upload | `ui/constants/imageSlots.js:4,139`, `ui/utils/imageSlotPipeline.js` |

### Đồng bộ Pancake POS
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Lịch | Product mỗi 30 phút, order mỗi 15 phút (cron cấu hình được), có cờ bật/tắt | `api/integration/pancake/scheduler/PancakeSyncScheduler.java:20-53` |
| Hướng | `FROM_PANCAKE` / `TO_PANCAKE` / `BIDIRECTIONAL` theo config | `api/integration/pancake/service/PancakeOrderSyncService.java:42-48,81-87` |
| Từ Pancake | Ghi đè price/stock local bằng variation đầu tiên (null -> 0) | `api/integration/pancake/mapper/ProductMapper.java:41-55,149-150` |
| Trạng thái đơn | Map 1-1 theo tên; giá trị lạ -> PENDING | `api/integration/pancake/mapper/OrderMapper.java:24-37` |
| Lên Pancake | Đẩy các bản ghi chưa sync hoặc `updatedAt` trong 24h gần nhất | `api/repository/OrderRepository.java:28-29`, `PancakeProductSyncService.java:87-88` |
| Lỗi | Lỗi từng bản ghi ghi vào `PancakeSyncLog`, không dừng batch | `PancakeOrderSyncService.java:58-66` |

### Dashboard
| Rule | Chi tiết | Nguồn |
|---|---|---|
| Doanh thu | Tổng `total` của **mọi** đơn (kể cả CANCELLED, gồm phí ship) | `api/service/DashboardStatsService.java:32-34` |
| Người mua | Số SĐT distinct trong orders | `api/service/DashboardStatsService.java:40-44` |
| Visitor | Unique theo `sessionId` (bảng visitors) | `api/service/VisitorTrackingService.java:32-46` |

## State machines
| Entity | Trạng thái | Chuyển đổi cho phép | Nguồn |
|---|---|---|---|
| Order | PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED | Bất kỳ -> bất kỳ (không có guard) | `api/service/OrderService.java:108-122` |
| Product | ACTIVE, INACTIVE (+ cờ deleted) | Toggle tự do; delete là một chiều qua API | `api/service/ProductService.java:155-177` |
| BlogPost | DRAFT, PUBLISHED (+ deletedAt) | DRAFT <-> PUBLISHED tự do; soft delete -> DRAFT + deletedAt | `api/service/BlogPostService.java:131-158` |

## Authorization (nghiệp vụ)
| Hành động | Ai được phép thực tế | Nguồn |
|---|---|---|
| Xem/sửa CMS, product admin, upload, dashboard | `ROLE_ADMIN` (`/api/dragun/admin/**`) | `api/config/SecurityConfig.java:45`, `api/util/Constants.java:18-20` |
| Tạo đơn, xem đơn theo mã, **xem toàn bộ đơn, xem đơn theo SĐT, đổi trạng thái đơn** | Bất kỳ ai, không cần đăng nhập (`/api/extend/orders/**` permitAll) | `api/util/Constants.java:9-10`, `api/config/SecurityConfig.java:42` |
| Pancake integration (`/api/thiyen/admin/...`) | Mọi user đã xác thực (rơi vào `anyRequest().authenticated()`, không kiểm role) | `api/controller/admin/AdminIntegrationController.java:22`, `api/config/SecurityConfig.java:47` |

## Known gaps
- Không có test nào cho order, product admin, CMS, blog; 7 test class chỉ phủ Location và Pancake product (`service-api/src/test/java/...`).
- `quantity` không có `@Positive`: quantity âm qua kiểm tồn kho, **cộng** stock và cho subtotal âm (`CreateOrderRequest.java:51-52`, `OrderService.java:55-71`) (inferred từ code, chưa chạy thử).
- Phí ship có 3 phiên bản: BE flat 20.000đ, FE theo product id 1/3 và ngưỡng 299.000đ, text ProductDetail theo số sản phẩm. Tổng FE hiển thị có thể khác `total` lưu DB; mã giảm giá FE không được BE ghi nhận.
- Sinh `orderId` bằng count trong ngày: hai đơn đồng thời có thể trùng mã -> lỗi unique (inferred). Đơn import từ Pancake cũng được đếm.
- Tồn kho không khóa row (không pessimistic lock/version) -> oversell khi đặt đồng thời (inferred).
- Hủy đơn không hoàn kho; doanh thu dashboard tính cả đơn hủy.
- Slug blog unique toàn bảng ở DB nhưng code chỉ kiểm bài chưa xóa: tái dùng slug của bài đã xóa (hoặc seed lại bài seed đã xóa) sẽ vi phạm constraint thay vì 400 (inferred) (`V16__Create_cms_tables.sql:21` vs `BlogPostService.java:64`, `CmsSeedService.java:103`).
- Có thể ghim bài DRAFT lên Home qua create/update (`applyHomeRules` không kiểm status); bài sẽ chiếm slot nhưng không hiển thị.
- Province/district/ward không được BE validate (tùy chọn, không đối chiếu bảng location), FE thì bắt buộc.
- DB default `orders.status = 'pending'` (chữ thường) lệch enum viết hoa (`V1__Initial_schema.sql:53`); code luôn set nên chưa gây lỗi.
- Hằng `ORDER_STATUS_*` chữ thường deprecated vẫn còn trong `api/util/Constants.java:22-33`.
