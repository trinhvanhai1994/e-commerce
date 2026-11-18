# e-commerce: Coding Conventions

> Sinh từ commit `621a5a9` (2026-11-14) cộng working tree (nhiều file CMS/blog còn untracked), bởi project-onboard.
> Đây là cache, không phải spec: kiểm lại với code trước khi dựa vào điều gì quan trọng. Làm mới bằng: "refresh docs/ai".
> Công cụ enforce: không có (không linter/formatter cho Java; Prettier chỉ chạy tay; ESLint không có config; không CI).
> Quy tắc tài liệu `docs/req/` xem `docs/req/CLAUDE.md`; flow triển khai xem `CLAUDE.md` mục 3. File này không lặp lại.

Ký hiệu cột "Enforce": **tool** = công cụ làm build fail; **pattern** = code đồng nhất, chưa có tool; **mixed** = hai kiểu cùng tồn tại (xem mục 15); **legacy** = không copy.

## 0. Reference implementations (đọc trước)

Gốc BE: `service-api/src/main/java/com/dragun/ecommerce/` (viết tắt `BE/`). Gốc FE: `service-ui/src/` (viết tắt `FE/`).

| Cần viết | Copy file này | Vì nó cho thấy |
|---|---|---|
| Endpoint public (đọc) | `BE/controller/external/ContentController.java` | `/api/dragun/...`, `ApiResponse.success`, `CacheControl` 30s (`:26-28`), controller mỏng |
| Endpoint admin (CRUD) | `BE/controller/admin/AdminBlogPostController.java` | `@PreAuthorize("hasRole('ADMIN')")` ở class (`:20`), `@Valid @RequestBody`, lấy username từ `Authentication` (`:42`), message tiếng Việt |
| Service có optimistic locking | `BE/service/ContentService.java` (`updateSection`, `:58-96`) | so `updatedAt` client gửi với DB, 409 khi lệch, `saveAndFlush` để `@PreUpdate` chạy trước khi build response (`:94-96`) |
| Entity + migration Flyway | `BE/model/entity/BlogPost.java` + `service-api/src/main/resources/db/migration/V16__Create_cms_tables.sql` | `OffsetDateTime` truncate micro (`BlogPost.java:71-75`), `jsonb` qua `@JdbcTypeCode` (`:40-42`), CHECK constraint, partial index |
| Repository | `BE/repository/BlogPostRepository.java` | `JpaRepository<E, Long>`, derived query có `DeletedAtIsNull` cho soft-delete (`:10-15`) |
| Request DTO | `BE/model/dto/request/BlogPostRequest.java` | Lombok + `jakarta.validation` (`@NotBlank`) |
| FE service module | `FE/services/content.service.js` | `httpClient` + `unwrap()` envelope (`:3-11`), named export + default export (`:13`, `:84`) |
| Trang admin | `FE/views/AdminHomepage.vue` | `<template>` trước `<script setup>` (`:151`), `AdminLayout`, state `loading/saving/error/message`, gửi lại `updatedAt` (`:289`) |
| Trang public | `FE/views/ArticleDetail.vue` | `<script setup>` (`:128`), gọi `contentService`, API lỗi thì hiện thông báo + nút Thử lại (`loadError`), 404 thì "Không tìm thấy"; không fallback dữ liệu seed |
| Route admin mới | `FE/router/index.js:158-168` | lazy `import()`, `beforeEnter` kiểm `adminService.isAuthenticated()` |

## 1. Scope & stack

| Service | Stack |
|---|---|
| service-api | Java 17 (`service-api/pom.xml:22`), Spring Boot 3.2, Spring Data JPA, Spring Security + JWT, Flyway, PostgreSQL, Lombok, Maven |
| service-ui | Vue 3 + Vite 5 + Pinia + vue-router 4 + Tailwind 3, JavaScript thuần (không TypeScript), npm (`service-ui/package.json`) |

## 2. Toolchain & commands

| Việc | Lệnh | Trạng thái |
|---|---|---|
| Compile BE | `mvn -q compile` | chạy được (cần JDK 17, xem `CLAUDE.md` mục 4) |
| Test BE | `mvn test` | 37 test, cần Docker. Test cần Spring context: `@SpringBootTest` + `@ActiveProfiles("it")` + `@Import(TestcontainersConfiguration.class)` (copy `service/ProductReviewServiceTest.java`); unit test thuần: `@ExtendWith(MockitoExtension.class)` (copy `integration/pancake/service/PancakeProductSyncServiceTest.java`). Test gọi hệ ngoài thật gắn `@Tag("external")` |
| Format BE | không có | không spotless/checkstyle trong `pom.xml` |
| Build FE | `npm run build` | là kiểm tra duy nhất có tác dụng |
| Format FE | `npm run format` (`prettier --write src/`) | chạy tay, không có `.prettierrc` nên dùng mặc định Prettier; chưa được áp đều (xem mục 15) |
| Lint FE | `npm run lint` | không dùng được: không có file cấu hình ESLint |
| CI | không có | không có `.github/` hay pipeline trong repo |

## 3. Project layout

| Thư mục | Trách nhiệm | Enforce |
|---|---|---|
| `BE/controller/admin/` | endpoint `/api/dragun/admin/**`, tên `Admin<Thing>Controller` | pattern |
| `BE/controller/external/` | endpoint public cho FE (`ContentController`, `ProductController`, `OrderController`, `LocationController`) | pattern |
| `BE/controller/publicapi/` | endpoint `/api/public/**` (chỉ `VisitorTrackingController`) | pattern. Không có package `controller/public` |
| `BE/service/` | business logic, `@Transactional`, mapping entity sang response | pattern |
| `BE/repository/` | Spring Data interface | pattern |
| `BE/model/{entity,dto/request,dto/response,enums,converter}` | JPA entity, DTO vào/ra, enum, `AttributeConverter` | pattern |
| `BE/cms/` | `ImageSlot` enum, phải đồng bộ với `FE/constants/imageSlots.js` (`CLAUDE.md` mục 5) | convention only |
| `BE/integration/pancake/` | tích hợp Pancake, tự có `client/dto/mapper/model/repository/service/scheduler` | pattern |
| `service-api/src/main/resources/cms-seed/` | JSON seed cho CMS, nạp bởi `CmsSeedService` | pattern |
| `FE/services/*.service.js` | toàn bộ gọi API; `FE/services/http/` là client + adapter + interceptor | mixed (xem `utils/api.js`, mục 15) |
| `FE/views/` | trang (public và admin cùng thư mục, admin có tiền tố `Admin`) | pattern |
| `FE/components/`, `FE/components/cms/` | component dùng lại | pattern |
| `FE/content/` | nội dung mặc định/fallback (`homeDefaults.js`, `pageDefaults.js` + `page-sections.seed.json` sinh bởi `scripts/sync-cms-seed.mjs`) | pattern |
| `FE/constants/`, `FE/stores/`, `FE/utils/` | hằng số, Pinia store, helper | pattern |

## 4. Naming

| Thứ | Quy ước | Ví dụ | Enforce |
|---|---|---|---|
| Controller BE | `<Thing>Controller`, admin thêm tiền tố `Admin` | `AdminBlogPostController.java:21` | pattern |
| Service / Repository | `<Thing>Service`, `<Entity>Repository` | `BlogPostService.java:22`, `BlogPostRepository.java:10` | pattern |
| DTO | `<Thing>Request` / `<Thing>Response` | `BlogPostRequest`, `BlogPostResponse` | pattern |
| Mapper trong service | mixed: `mapToResponse` (`ProductService.java:179`) vs `toDetailResponse`/`toSummaryResponse` static (`BlogPostService.java:266-281`) | | mixed |
| Bảng / cột DB | `snake_case`, bảng số nhiều | `blog_posts`, `show_on_home` (`V16__Create_cms_tables.sql:19-27`) | pattern |
| Constraint / index | `chk_<bảng>_<cột>`, `uq_...`, `idx_<bảng>_<cột>` | `V16__Create_cms_tables.sql:35-47` | pattern |
| Migration | `V<n>__<Mo_ta_snake>.sql`, số liên tiếp | `V18__Cms_image_use_default_flags.sql` | tool (Flyway từ chối version trùng) |
| Hằng số Java | `UPPER_SNAKE_CASE` `private static final` | `BlogPostService.java:24-25` | pattern |
| File FE service | `kebab-case.service.js`, object `camelCaseService` | `product-management.service.js` / `productManagementService` | pattern |
| File Vue | `PascalCase.vue` | `AdminBlogEditor.vue` | pattern |
| Pinia store | `use<Thing>Store`, id ngắn | `stores/cart.js:5` | pattern |
| Env FE | `VITE_*` | `FE/services/http/config.js:2` | tool (Vite chỉ expose `VITE_*`) |
| Env BE | `UPPER_SNAKE`, có default `${X:default}` khi không bí mật | `application.yml:71-93` | pattern |
| Branch | `feature/<slug>`, `dev/<slug>` | `feature/dark-mode`, `dev/misa-integration` | convention only (chỉ 2 mẫu) |

## 5. Layering & dependencies

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Controller chỉ gọi service, không gọi repository | `AdminBlogPostController.java:23`, `ContentController.java:21-22` | pattern |
| Ngoại lệ: `LocationController` trả thẳng entity (`List<Province>`), không có DTO | `controller/external/LocationController.java:21` | legacy, không copy |
| `@Transactional` đặt ở service, `readOnly = true` cho đọc | `BlogPostService.java:29,61` | pattern |
| Inject qua constructor bằng `@RequiredArgsConstructor` + `private final`; không có `@Autowired` | `BlogPostService.java:21,27` (grep `@Autowired`: 0 kết quả) | pattern |
| Entity sang response map trong service, không dùng MapStruct | `BlogPostService.java:266` | pattern |
| FE: view gọi `services/*.service.js`, không `fetch` trực tiếp | ngoại lệ upload multipart dùng `fetch` (`content.service.js:66-74`, `product-management.service.js:86`) | mixed |

## 6. Language conventions

**Java**
- Lombok cho entity/DTO: `@Data @Builder @NoArgsConstructor @AllArgsConstructor` (`BlogPost.java:18-21`, `ApiResponse.java:8-11`). pattern.
- Timestamp do entity tự set trong `@PrePersist`/`@PreUpdate` (`BlogPost.java:71-90`), không dùng `@CreationTimestamp`. pattern.
- Tiền tệ: `BigDecimal` (`Order.java:56-62`, `Product.java:29`). pattern.
- Trạng thái: mixed giữa enum + converter (`Order.java:49-50`) và `String` + CHECK DB (`BlogPost.java:45`, `Product.java:99`). Xem mục 15.
- Kiểu thời gian: mixed, code cũ `LocalDateTime` (`Order.java:71`), code CMS mới `OffsetDateTime` (`BlogPost.java:54-63`). Code mới dùng `OffsetDateTime` (inferred từ code mới nhất).

**Vue / JS**
- `<script setup>` cho component mới; `<template>` đứng trước `<script setup>` (`AdminHomepage.vue:1,151`, `AdminBlogList.vue:1,47`). pattern. Options API còn ở `Me.vue:609`, `HealthCare.vue:199`, `MapView.vue:15`, `components/ImageSwiper.vue:24`: legacy.
- Import: mixed giữa alias `@/` (`stores/cart.js:2`) và đường dẫn tương đối (`AdminHomepage.vue:155`). Alias khai báo ở `service-ui/vite.config.js:13-14`, `service-ui/jsconfig.json`.
- Quote/semicolon: phần lớn nháy đơn, không chấm phẩy; `ArticleDetail.vue:129-132` dùng nháy kép, chứng tỏ Prettier không được chạy đều.
- Pinia: mixed giữa options store (`stores/cart.js:5`) và setup store (`stores/order.js:5`).

## 7. Error handling

| Tầng | Quy tắc | Bằng chứng | Enforce |
|---|---|---|---|
| BE translate | `GlobalExceptionHandler` (`@RestControllerAdvice`) bọc mọi lỗi thành `ApiResponse.error(message)` | `exception/GlobalExceptionHandler.java:14-63` | pattern |
| BE throw (code mới, CMS) | `ResponseStatusException(HttpStatus.X, "thông điệp tiếng Việt")` | `BlogPostService.java:40,65,96`, `ContentService.java:61-68` | mixed |
| BE throw (code cũ) | custom `ResourceNotFoundException` / `BadRequestException` / `UnauthorizedException` | `ProductService` (7 chỗ), `OrderService` (5), `AdminService` (2) | mixed |
| BE lỗi validation `@Valid` | 400, `data` = map field sang message, `message` = các message nối bằng `; ` (UI admin chỉ hiện `message`) | `GlobalExceptionHandler.java:35-50` | pattern |
| BE lỗi khác | 500, `"An error occurred: " + ex.getMessage()`, không log | `GlobalExceptionHandler.java:59-63` | pattern (rủi ro lộ chi tiết nội bộ) |
| BE `IllegalArgumentException` | không có handler riêng nên thành 500, trừ khi controller tự `try/catch` | `AdminFileUploadController.java:33-42`, `CmsImageService` (8 chỗ `new IllegalArgumentException`/`RuntimeException`) | mixed |
| Optimistic lock | 409 `CONFLICT` với "...đã được cập nhật bởi người khác. Hãy tải lại." | `ContentService.java:66-68`, `BlogPostService.java:94-97` | pattern |
| FE | adapter ném `Error` có `.status`, `.data` khi HTTP lỗi | `FE/services/http/adapters/service-api.adapter.js:163-177` | pattern |
| FE envelope `success:false` | adapter `Promise.reject(data)` (object thường, không phải `Error`); `content.service.js` tự `unwrap` và ném `Error` | `service-api.adapter.js:184-185`, `content.service.js:3-11` | mixed |
| FE view | `try/catch`, gán `error.value = e.message || '<fallback tiếng Việt>'` | `AdminHomepage.vue:233-234` | pattern |

## 8. Data & migrations

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Mọi thay đổi schema qua migration mới `V<n+1>`, không sửa migration đã chạy | `CLAUDE.md` mục 3 | tool ở prod (checksum Flyway) |
| Prod `ddl-auto: validate`: entity phải khớp migration | `application-prod.yml:11`, `application.yml:27` | tool (app không khởi động nếu lệch) |
| Dev/test `ddl-auto: update` + `validate-on-migrate: false`: Hibernate có thể tự tạo cột mà migration không có | `application-dev.yml:11-14`, `application-test.yml:11-14` | rủi ro, xem mục 15 |
| DDL idempotent `CREATE TABLE/INDEX IF NOT EXISTS` | `V16__Create_cms_tables.sql:3,19,39` | pattern (code mới) |
| PK `BIGSERIAL` / `GenerationType.IDENTITY` | `V16__Create_cms_tables.sql:20`, `BlogPost.java:24-26` | pattern |
| Soft-delete bằng `deleted_at`; query lọc `DeletedAtIsNull` | `BlogPostRepository.java:11-15` | pattern (blog); product dùng cờ khác (`V10__Add_deleted_and_status_to_products.sql`) |
| Audit `created_by`/`updated_by` = username từ JWT | `AdminBlogPostController.java:42`, `BlogPostService.java:79-80` | pattern (CMS) |
| Seed dữ liệu CMS bằng JSON + marker migration, không INSERT trong migration | `cms-seed/*.json`, `V17__Cms_seed_marker.sql`, `CmsSeedService` | pattern (code mới); code cũ seed bằng SQL (`V4__Insert_initial_products.sql`) |

## 9. API conventions

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Public: `/api/dragun/<resource>`; admin: `/api/dragun/admin/<resource>` | `ContentController.java:17`, `AdminBlogPostController.java:18` | pattern, nhưng có ngoại lệ (dưới) |
| Ngoại lệ path: `/api/extend/orders`, `/api/public/visitors`, `/api/thiyen/admin/integration/pancake`, `/provinces` không prefix | `OrderController.java:17`, `VisitorTrackingController.java:17`, `AdminIntegrationController.java:22`, `LocationController.java:20` | legacy, không copy |
| Path public mới phải thêm vào `Constants.PUBLIC_ENDPOINTS`, không thì bị chặn bởi `anyRequest().authenticated()` | `util/Constants.java:6-16`, `config/SecurityConfig.java:41-47` | tool (runtime 401/403) |
| Admin: `@PreAuthorize("hasRole('ADMIN')")` ở class, cộng URL rule `ADMIN_ENDPOINTS` | `AdminBlogPostController.java:20`, `SecurityConfig.java:45` | pattern; `AdminIntegrationController.java:24` viết `hasRole('ROLE_ADMIN')` |
| Response luôn `ResponseEntity<ApiResponse<T>>`, envelope `{success, data, message}` | `model/dto/response/ApiResponse.java:12-37` | pattern |
| Message thành công tiếng Việt qua `ApiResponse.success(data, msg)` | `AdminBlogPostController.java:43` | pattern (code mới); code cũ tiếng Anh (`VisitorTrackingController.java:56`) |
| Soft-delete dùng `POST /{id}/soft-delete`, đổi trạng thái `PATCH /{id}/status` | `AdminBlogPostController.java:56,69` | pattern (CMS) |
| Không có pagination; list trả toàn bộ | `BlogPostService.java:45-52`, `OrderController.java:37-41` | pattern (inferred: chưa cần) |
| JSON field camelCase (Jackson mặc định), FE dùng y nguyên (`post.contentJson`) | `ArticleDetail.vue:146` | pattern |

## 10. Logging & observability

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Logger qua Lombok `@Slf4j` (14 class), không dùng `LoggerFactory` | `AdminFileUploadController.java:17` | pattern |
| Placeholder `{}`, truyền exception làm tham số cuối để có stack trace | `AdminFileUploadController.java:39` | pattern |
| Level: `com.dragun.ecommerce` DEBUG mặc định, INFO ở prod | `application.yml:62`, `application-prod.yml:20` | config |
| Không log token/password: không tìm thấy vi phạm; FE debug chỉ log `hasToken`/`tokenLength` khi dev | `service-api.adapter.js:121-130` | convention only |
| Sai lệch: `System.out.println` | `config/WebConfig.java:40` | legacy |
| `console.log` còn 21 chỗ trong `FE/` | grep | convention only |

## 11. Configuration & secrets

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| BE đọc config từ `application*.yml`, giá trị nhạy cảm qua env không default (`${JWT_SECRET}`, `${PANCAKE_API_KEY}`) | `application.yml:56,79` | pattern |
| Profile qua `SPRING_PROFILES_ACTIVE`, mặc định `dev` | `application.yml:16` | config |
| Nhóm config dùng `@ConfigurationProperties` | `config/JwtConfig.java:7`, `integration/config/PancakeIntegrationConfig.java:8` | mixed với `@Value` (`JwtTokenProvider.java:20-23`, `FileStorageService.java:21`, `WebConfig.java:18`); `jwt.*` được đọc cả hai cách |
| FE đọc `import.meta.env.VITE_*`; base URL tập trung ở `services/http/config.js:2` | nhưng lặp lại ở `utils/api.js:5`, `utils/imageUtils.js:3`, `visitor-tracking.service.js:3`, `AdminProductDetail.vue:352` | mixed |
| Không đọc hay in giá trị `.env*` | `CLAUDE.md` mục 5 | convention (có hook chặn đọc) |

## 12. Testing

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Hiện trạng: không có test nào chạy (BE 7 file bị comment, FE không có test) | mục 2 | n/a |
| Mẫu còn lại trong file comment: JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`, `@DisplayName`, `@Mock`/`@InjectMocks`, dữ liệu qua `builder()` trong `@BeforeEach` | `LocationServiceTest.java:27-50` | baseline, chưa có precedent sống |
| Vị trí: `service-api/src/test/java/` mirror package main, tên `<Class>Test`, integration test hậu tố `IntegrationTest` | `PancakeProductSyncIntegrationTest.java` | pattern |
| Tên method cũ: `test<Method>_<Case>` | `ProductMapperTest.java:75,95` | pattern (trong code comment) |
| Phụ thuộc test có sẵn: `spring-boot-starter-test`, `spring-security-test`, H2 | `pom.xml:107-127` | config |
| Gợi ý (suggestion, chưa có precedent): service có business rule (`BlogPostService.applyHomeRules`, optimistic lock của `ContentService`) nên có unit test Mockito theo mẫu trên | | convention only |

## 13. Security baseline

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| JWT stateless, CSRF tắt | `SecurityConfig.java:37,49-51` | config |
| Validate request bằng `@Valid` + annotation trên DTO; rule nghiệp vụ check thủ công trong service | `BlogPostRequest.java:12-16`, `BlogPostService.java:160-183` | pattern |
| Sanitize text/ảnh do admin nhập trước khi lưu | `BlogPostService.java:72-74` (`sanitizeText`, `sanitizeImage`) | pattern (CMS) |
| Path ảnh do server sinh theo `ImageSlot`, client không chọn tên file | `AdminFileUploadController.java:27-35`, `cms/ImageSlot.java` | pattern |
| FE guard admin bằng `beforeEnter` lặp ở từng route, chỉ là UX; quyền thật do BE kiểm | `router/index.js:93-188` | pattern |

## 14. Git workflow

| Quy tắc | Bằng chứng | Enforce |
|---|---|---|
| Base branch `main`; nhánh tính năng `feature/*`, `dev/*` | `git branch -a` | convention only |
| Commit message: tiếng Anh, viết thường, ngắn 2-4 từ, không prefix Conventional Commits, không scope | `git log -10`: `fix config prod release`, `update docker release`, `add video`, `refactor home` | convention only |
| Một commit có thể chạm cả BE và FE (monorepo) | `d347112 crors FE` (inferred từ message) | convention only |
| Không commit `node_modules/`, `dist/`, `target/`, `uploads/`, `.env.local` | `CLAUDE.md` mục 5; `service-ui/.gitignore` (untracked), `service-api/.gitignore` | tool một phần (.gitignore) |
| Chỉ commit khi người dùng yêu cầu; không có quy trình PR/review trong repo | không có `.github/` | convention only |

## 15. Open decisions (cần người quyết)

1. **Kiểu exception BE**: `ResponseStatusException` (CMS mới) hay custom `ResourceNotFoundException`/`BadRequestException` (code cũ)? Cả hai đều ra cùng envelope qua `GlobalExceptionHandler.java:17-57`. Code mới đang dùng `ResponseStatusException`.
2. **`IllegalArgumentException` thành 500**: thêm handler 400 trong `GlobalExceptionHandler`, hay giữ `try/catch` trong controller như `AdminFileUploadController.java:33-42`?
3. **Handler generic lộ `ex.getMessage()` và không log** (`GlobalExceptionHandler.java:59-63`): nên log + trả message chung?
4. ~~Optimistic lock không đồng nhất~~: đã thống nhất, cả section và blog đều bắt buộc `updatedAt` khi cập nhật.
5. **Trạng thái**: enum + `AttributeConverter` (`Order`) hay `String` + CHECK DB (`BlogPost`, `Product`)? Còn hằng `@Deprecated` ở `Constants.java:22-33`.
6. **Thời gian**: `LocalDateTime` (code cũ) hay `OffsetDateTime` (CMS)? Có nên migrate dần?
7. **Prefix API**: `/api/extend/orders`, `/api/thiyen/...`, `/api/public/...`, `/provinces` lệch chuẩn `/api/dragun/...`. Đổi cần sửa FE cùng lúc (`CLAUDE.md` mục 3.5). Ngoài ra `GET /api/extend/orders` (toàn bộ đơn) và `PUT /api/extend/orders/{id}/status` đang public do `Constants.java:9-10`: cần xác nhận có chủ ý.
8. **`hasRole('ADMIN')` vs `hasRole('ROLE_ADMIN')`** (`AdminIntegrationController.java:24`): thống nhất về `'ADMIN'`.
9. **Config**: `@ConfigurationProperties` hay `@Value`? `jwt.*` đang đọc cả hai nơi (`JwtConfig.java:7`, `JwtTokenProvider.java:20`).
10. **Dev/test `ddl-auto: update`**: che giấu migration thiếu cột cho tới khi lên prod (`validate`). Đổi dev sang `validate`?
11. **FE lớp gọi API**: `utils/api.js` (lớp "backward compatibility", vẫn được 11 file dùng) vs import thẳng `services/*.service.js`; và `unwrap()` trong `content.service.js` vs dựa vào adapter (`admin.service.js:26-27` tự xử lý `data.data`). Code mới dùng service trực tiếp.
12. **FE base URL lặp ở 5 nơi** (mục 11) và upload multipart bỏ qua `httpClient` (`content.service.js:66`, `product-management.service.js:86`).
13. **FE style**: alias `@/` vs đường dẫn tương đối; Pinia options vs setup store; nháy đơn vs kép. Thêm `.prettierrc` + `.eslintrc` để chốt.
14. ~~Test bị comment toàn bộ~~: đã bật lại (2026-11-18), test context chạy trên Testcontainers, xem dòng "Test BE".

### Đề xuất nâng enforce (cần hỏi trước khi thêm tool)
- FE: thêm `.eslintrc.cjs` (`plugin:vue/vue3-recommended`) + `.prettierrc` để `npm run lint` chạy được; chặn `console.log`.
- BE: thêm `spotless` (google-java-format) hoặc `checkstyle` vào `pom.xml`.
- CI tối thiểu: `mvn -q compile`, `npm run build`, grep cấm `/api/thiyen`, `System.out.println`.
- Đổi `application-dev.yml` sang `ddl-auto: validate` để lệch entity/migration lộ sớm.
