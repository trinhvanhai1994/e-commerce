# Stack patterns: cách repo này dùng Spring Boot + Vue
> Generated from 621a5a9 (2026-11-14) plus working tree (CMS homepage + blog chưa commit), by project-onboard.
> Đây là cache của codebase, không phải spec. Kiểm chứng lại với code trước khi dựa vào; refresh with: "refresh docs/ai".
> Stack: service-api = Spring Boot 3.2 / Java 17 / Spring Data JPA (Hibernate 6) / PostgreSQL / Flyway / jjwt; service-ui = Vue 3.4 / Pinia 2 / Vue Router 4 / fetch-based http client.
> Đường dẫn backend viết tắt: `api/` = `service-api/src/main/java/com/dragun/ecommerce/`, `ui/` = `service-ui/src/`.

## 1. Bộ ba layer để copy

| Layer | File mẫu (mới nhất, sạch nhất) | Ghi chú |
|---|---|---|
| Controller admin | `api/controller/admin/AdminHomepageController.java:16-41` | `@PreAuthorize("hasRole('ADMIN')")` ở class, `@Valid @RequestBody`, bọc `ApiResponse.success(...)` |
| Controller public | `api/controller/external/ContentController.java:16-44` | thêm `CacheControl.maxAge(30s).cachePublic()` |
| Service | `api/service/ContentService.java`, `api/service/BlogPostService.java` | `@Service @RequiredArgsConstructor`, read dùng `readOnly = true` |
| Repository | `api/repository/BlogPostRepository.java:10-35` | `JpaRepository<E, Long>`, derived query + `@Query` JPQL text block |
| Entity | `api/model/entity/HomepageSection.java`, `BlogPost.java` | Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor` |

Các file cũ hơn (`ProductService`, `OrderService`, `AdminService`) chứa phần lớn anti-pattern ở mục 10; đừng lấy làm mẫu.

## 2. DI và cấu hình

- Constructor injection qua Lombok `@RequiredArgsConstructor` + field `private final` ở mọi service/controller (vd `api/service/ContentService.java:23,30-31`, `api/config/SecurityConfig.java:27-32`). Không có `@Autowired` field injection trong `api/` (grep: 0 kết quả).
- Ngoại lệ: `PancakeApiClient` tự viết constructor để build `WebClient` (`api/integration/pancake/client/PancakeApiClient.java:26-34`).
- Config: `@Value` trực tiếp (`api/security/JwtTokenProvider.java:20-23`, `api/config/WebConfig.java:18`, `api/service/FileStorageService.java:21`) lẫn `@ConfigurationProperties`-style POJO (`api/integration/config/PancakeIntegrationConfig.java`). `api/config/JwtConfig.java` khai báo `secret/expiration` nhưng không class nào dùng (grep `JwtConfig`: 0 tham chiếu): dead code, `jwt.*` thực tế đọc bằng `@Value`.
- Base entity: KHÔNG có `@MappedSuperclass`. Mỗi entity tự khai báo `id` (`IDENTITY`, `Long`) và tự viết hook timestamp (mục 4).

## 3. Transaction boundary

- Boundary = method public của `@Service`, `@Transactional` của Spring (`org.springframework.transaction.annotation`). Controller không mở transaction.
- Read: CMS dùng `@Transactional(readOnly = true)` (`api/service/ContentService.java:33,51`, `api/service/BlogPostService.java:29,36,44,54`, `api/service/DashboardStatsService.java:26`). `ProductService` và `OrderService` read method KHÔNG có `@Transactional` (`api/service/ProductService.java:20-83`, `api/service/OrderService.java:90-106`) và chỉ chạy được lazy load nhờ Open Session In View (mặc định bật vì `spring.jpa.open-in-view` không được set trong `service-api/src/main/resources/application.yml:25-32`) (inferred).
- Write: `@Transactional` mặc định (REQUIRED). Không có `TransactionTemplate`, không có `@TransactionalEventListener`/post-commit hook: side effect (gọi Pancake) chạy ngay trong transaction (mục 10).
- `REQUIRES_NEW` chỉ xuất hiện 1 lần, trên method `private` (`api/service/VisitorTrackingService.java:96-97`), nên bị proxy bỏ qua (mục 10).
- Seed: `CmsSeedService implements ApplicationRunner`, `@Order(100)`, `@Transactional` trên `run`, insert-if-absent theo `existsBySectionKey`/slug, nuốt mọi exception chỉ log (`api/service/CmsSeedService.java:25-44,57`). `LocationDataInitService` cũng là initializer `@Transactional` (`api/service/LocationDataInitService.java:33`).

## 4. Entity lifecycle, save vs saveAndFlush, timestamp

- Timestamp do JPA callback set, không dùng `@CreationTimestamp`/`@UpdateTimestamp`/auditing:
  - `HomepageSection.touch()` gộp `@PrePersist @PreUpdate`, set `updatedAt` + default null-field (`api/model/entity/HomepageSection.java:72-92`).
  - `BlogPost.onCreate()`/`onUpdate()` (`api/model/entity/BlogPost.java:71-91`).
  - Entity cũ (`Product`, `Order`, `User`, `Visitor`, Pancake mapping) dùng `LocalDateTime.now()` không truncate (`api/model/entity/Product.java:105-123`).
- CMS dùng `OffsetDateTime` + `.truncatedTo(ChronoUnit.MICROS)` vì Postgres `timestamptz` chỉ giữ micro giây; nếu không truncate, giá trị trả về client (nano) khác giá trị lưu và lần save sau fail optimistic lock (`api/model/entity/HomepageSection.java:75-76`, `api/model/entity/BlogPost.java:73-74`).
- `@PreUpdate` chỉ chạy khi Hibernate flush. Với `save()` trên entity managed, flush xảy ra lúc commit, tức SAU khi response DTO đã được build, nên DTO mang `updatedAt` cũ. Vì vậy các method update CMS dùng `saveAndFlush` rồi mới map DTO: `api/service/ContentService.java:94-96`, `api/service/BlogPostService.java:128,145`. Quy tắc: method update nào trả `updatedAt` cho client làm version token phải `saveAndFlush`.
- `create` dùng `save()` là đủ vì `IDENTITY` buộc INSERT ngay, `@PrePersist` chạy trước đó (`api/service/BlogPostService.java:86`) (inferred từ hành vi Hibernate với IDENTITY).
- `softDelete` dùng `save()` và không trả `updatedAt` (`api/service/BlogPostService.java:148-158`): chấp nhận được.

## 5. Optimistic locking (CMS)

- Không có `@Version` ở bất kỳ entity nào (grep: 0). Thay vào đó so sánh `updatedAt` client gửi lên với DB, mismatch -> `409 CONFLICT`:
  - Homepage: bắt buộc, `null` cũng là conflict (`api/service/ContentService.java:66-69`).
  - Blog: bắt buộc khi PUT, thiếu là 400, lệch là 409 (`api/service/BlogPostService.java:94-103`).
- So sánh qua `toInstant()` để bỏ qua khác biệt offset.
- FE giữ token: `AdminHomepage.vue` thay section trong list bằng response (`ui/views/AdminHomepage.vue:289-293`); `AdminBlogEditor.vue` gán `form.updatedAt = updated.updatedAt` (`ui/views/AdminBlogEditor.vue:180-181`).
- Giới hạn: check-then-write trong cùng transaction, không có row lock hay `WHERE updated_at = ?`, nên hai request đồng thời vẫn có thể cùng pass (inferred). Muốn chặt: thêm `@Version` hoặc `@Lock(PESSIMISTIC_WRITE)` trên `findBySectionKey`.

## 6. Query patterns và N+1

- Derived query là mặc định; JPQL `@Query` text block khi cần điều kiện literal (`api/repository/BlogPostRepository.java:19-26`). Không có `@EntityGraph`, `JOIN FETCH`, `Specification`, hay pagination (`Pageable`) ở đâu cả (grep: 0).
- Soft delete thủ công: CMS dùng cột `deleted_at` + suffix `...AndDeletedAtIsNull` trong mọi query (`api/repository/BlogPostRepository.java:11-34`); Product dùng `deleted` boolean (`api/service/ProductService.java:23,65`). Không có `@Where`/`@SQLRestriction`. `listAdmin(includeDeleted=true)` gọi `findAll()` không sort (`api/service/BlogPostService.java:46-47`).
- Sort/limit trong Java thay vì SQL: `ProductService` sort theo priority và `.limit(4)` trên stream (`api/service/ProductService.java:23-45`); `ContentService` `.limit(3)` trên stream (`api/service/ContentService.java:41-42`).
- N+1 hiện có (inferred, không đo):
  - `Product.gallery` là `@ElementCollection(LAZY)` (`api/model/entity/Product.java:43-48`) và `mapToResponse` đọc gallery cho từng product (`api/service/ProductService.java:183`): 1 + N query trên mọi list sản phẩm.
  - `Order.items` `@OneToMany` lazy (`api/model/entity/Order.java:67-68`), `mapToResponse` duyệt items mỗi order (`api/service/OrderService.java:151`) trong `getAllOrders()`.
  - Cả hai chỉ chạy được nhờ OSIV. Cách sửa: `@EntityGraph(attributePaths = "gallery")` trên method repository.
- jsonb: `@JdbcTypeCode(SqlTypes.JSON)` + `columnDefinition = "jsonb"`, kiểu Java `Map<String, Object>` hoặc `List<Map<String, Object>>` (`api/model/entity/HomepageSection.java:56-58`). Không có typed DTO cho nội dung jsonb; validate thủ công bằng `instanceof` pattern trong service (`api/service/ContentService.java:163-184`, `api/service/BlogPostService.java:185-224`). Khi sửa, luôn copy sang `new HashMap<>(...)` trước khi set để Hibernate thấy dirty (`api/service/ContentService.java:85`).

## 7. Concurrency, scheduler, external HTTP

- Servlet stack, thread-per-request. Không có `@Async`/`@EnableAsync`/executor riêng (grep: 0).
- `@EnableScheduling` ở `api/EcommerceApplication.java:8`; 2 job cron trong `api/integration/pancake/scheduler/PancakeSyncScheduler.java:20,37`, guard bằng flag config (mặc định `enabled = false`, `api/integration/config/PancakeIntegrationConfig.java:33,44,50`), bọc try/catch log.
- WebClient (từ spring-webflux) dùng như client đồng bộ: method trả `Mono`, caller gọi `.block()` (`api/integration/pancake/service/PancakeProductSyncService.java:47,203,216`). `executeWithRetry` gắn `Retry.backoff` (chỉ retry `WebClientResponseException`, kể cả 4xx) và `.timeout(30s)` (`api/integration/pancake/client/PancakeApiClient.java:36-52`). Timeout đặt SAU retry nên là tổng thời gian, không phải per-attempt.
- Rủi ro: `.block()` nằm trong method `@Transactional` (`PancakeProductSyncService.java:37-47`) và trong request admin (`api/service/AdminService.java:65-87`): giữ DB connection suốt thời gian chờ HTTP (tối đa ~30s).

## 8. Security, JWT, CORS

- `SecurityFilterChain` stateless, CSRF tắt, `JwtAuthenticationFilter` đặt trước `UsernamePasswordAuthenticationFilter` (`api/config/SecurityConfig.java:35-56`). `@EnableMethodSecurity` bật nên `@PreAuthorize` ở controller có hiệu lực (`SecurityConfig.java:26`).
- Danh sách public ở 2 nơi phải giữ đồng bộ: `Constants.PUBLIC_ENDPOINTS` (`api/util/Constants.java:6-16`) và `shouldNotFilter` của filter (`api/security/JwtAuthenticationFilter.java:27-39`). Thêm endpoint public = sửa cả hai.
- Filter: đọc `Bearer`, load `UserDetails` từ DB mỗi request, lỗi token bị nuốt im lặng (`JwtAuthenticationFilter.java:57-75`). Token HMAC từ `jwt.secret`, hết hạn mặc định 24h (`api/security/JwtTokenProvider.java:20-27`). `JwtService` chỉ là wrapper ủy quyền 1-1 cho `JwtTokenProvider` (`api/service/JwtService.java:10-25`).
- Username cho audit lấy từ `Authentication` param, fallback `"admin"` (`api/controller/admin/AdminHomepageController.java:35`).
- CORS: danh sách origin hard-code trong code, không đọc từ config/profile, `allowCredentials(true)`, `allowedHeaders("*")` (`api/config/CorsConfig.java:18-30`). Thêm domain = sửa Java + build lại.
- Static `/images/**` phục vụ từ `file.upload-dir` qua `WebConfig` (`api/config/WebConfig.java:43-58`).

## 9. Exception -> HTTP

- `@RestControllerAdvice` duy nhất: `api/exception/GlobalExceptionHandler.java`. Envelope lỗi luôn `ApiResponse.error(message)`, `success=false`.
- Hai hệ exception song song:
  - Custom: `ResourceNotFoundException` -> 404, `BadRequestException` -> 400, `UnauthorizedException` -> 401 (`GlobalExceptionHandler.java:17-33`); dùng trong Product/Order/Admin.
  - `ResponseStatusException` (Spring) -> status của nó (`GlobalExceptionHandler.java:52-57`); toàn bộ code CMS dùng kiểu này, kể cả 409.
  - Code mới: theo neighbouring file. Không có error code máy đọc được, chỉ có message tiếng Việt.
- `MethodArgumentNotValidException` -> 400 với map field->message trong `data` (`GlobalExceptionHandler.java:35-50`).
- Catch-all `Exception` -> 500 và trả `ex.getMessage()` cho client (`GlobalExceptionHandler.java:59-63`).
- Controller upload tự try/catch và tự build response (`api/controller/admin/AdminFileUploadController.java:33-42`) thay vì để advice xử lý.

## 10. Anti-pattern đang có (đừng copy)

| # | Ở đâu | Vấn đề | Dạng đúng |
|---|---|---|---|
| 1 | `api/service/VisitorTrackingService.java:96-97` | `@Transactional(REQUIRES_NEW)` trên method `private`, gọi nội bộ: proxy bỏ qua, retry chạy trong transaction đã hỏng sau `DataIntegrityViolationException` (`:80-86`) | Tách sang bean khác, method public |
| 2 | `api/integration/pancake/service/PancakeProductSyncService.java:55` gọi `syncProductFromPancake` (`:118`) | Self-invocation: `@Transactional` của item bị bỏ qua, cả vòng lặp 1 transaction; lỗi DB 1 item có thể làm rollback-only toàn bộ (inferred) | Bean riêng / `TransactionTemplate` per item |
| 3 | `api/service/AdminService.java:65-93` | Gọi `syncProductToPancake` (`@Transactional`, rethrow ở `PancakeProductSyncService.java:245-247`) trong transaction ngoài rồi catch: transaction bị đánh dấu rollback-only, commit có thể ném `UnexpectedRollbackException` dù đã catch (inferred). HTTP chạy trong transaction | Sync sau commit (`@TransactionalEventListener(AFTER_COMMIT)`) hoặc ngoài `@Transactional` |
| 4 | `api/service/OrderService.java:52-71` | Trừ stock read-modify-write không lock: oversell khi đặt đồng thời | `UPDATE ... SET stock = stock - :q WHERE id = :id AND stock >= :q` |
| 5 | `api/service/OrderService.java:128-138` | `orderId` = count hôm nay + 1: trùng khi đồng thời | DB sequence / unique + retry |
| 6 | `api/service/ProductService.java:20-83`, `api/service/OrderService.java:90-106` | Read không `@Transactional(readOnly=true)`, dựa OSIV, N+1 (mục 6) | `readOnly = true` + `@EntityGraph` |
| 7 | `api/service/ProductService.java:24-45` | Sort/limit trong Java, comparator copy-paste 3 lần | `findBy...OrderByPriorityAsc`, `Top4` |
| 8 | `api/exception/GlobalExceptionHandler.java:59-63` | Lộ `ex.getMessage()`; `AccessDeniedException` từ `@PreAuthorize` có thể rơi vào đây thành 500 thay vì 403 (inferred) | Message chung + log; handler riêng cho `AccessDeniedException` |
| 9 | `api/service/FileStorageService.java:54-55`, `api/controller/admin/AdminFileUploadController.java:51-81,103-113` | Endpoint legacy `/upload`, `/delete`: `basePath.resolve(relativePath)` không `normalize()`/check `startsWith(base)`: path traversal (chỉ admin) | Dùng `upload-cms` (`api/service/CmsImageService.java:75,97` có check `..`) |
| 10 | `api/service/ContentService.java:186-201` vs `api/service/BlogPostService.java:252-264` | Validate path ảnh viết 2 lần, khác nhau (`sanitizeImage` cho blank đi qua) | Một helper chung |
| 11 | `api/service/BlogPostService.java:243-250` | Lọc XSS bằng regex | FE render text, không `v-html`; hoặc thư viện sanitizer |
| 12 | `api/security/JwtAuthenticationFilter.java:73-75` | Nuốt mọi exception không log | Log debug |
| 13 | `api/config/CorsConfig.java:18-26` | Origin hard-code | Đọc từ `application-*.yml` |
| 14 | `api/config/WebConfig.java:40` | `System.out.println` thay logger | `@Slf4j` |

## 11. Frontend (service-ui)

- Component: `<script setup>` + Composition API (`ui/views/Home.vue:281`, `ui/views/AdminHomepage.vue:155-158`). State cục bộ bằng `ref`; không dùng composable riêng (không có `ui/composables/`).
- Pinia chỉ cho cart và order, persist thủ công vào `localStorage` (`ui/stores/cart.js:5-35` options API, `ui/stores/order.js:5-27` setup API: 2 style lẫn lộn). CMS, product, auth KHÔNG có store; mỗi view tự fetch trong `onMounted`.
- HTTP: singleton `httpClient` chọn adapter theo `VITE_USE_SERVICE_API` (`ui/services/http/client.js:20-31,110`); adapter dùng `fetch`, gắn `Authorization: Bearer` từ `localStorage.authToken` (`ui/services/http/adapters/service-api.adapter.js:83,104`), tự unwrap `{success,data}` (`:184-186`) và ném `Error` có `.status`, `.data` (`:150-177`). Interceptor là mảng module-level, mặc định chỉ có logging (`ui/services/http/interceptors.js:9-21,101-124`). `apiConfig.timeout` khai báo (`ui/utils/api.js:9`) nhưng `fetch` không có `AbortController` nên không có timeout thật (inferred, grep 0).
- Service layer: object literal theo domain (`ui/services/content.service.js:13-82`), mỗi hàm `unwrap(await httpClient.x(...))`. Upload multipart bỏ qua `httpClient`, gọi `fetch` trực tiếp và tự đọc token (`content.service.js:59-81`, `ui/services/product-management.service.js:85-92`). `ui/utils/api.js` là lớp re-export backward-compat.
- Auth: token JWT trong `localStorage` (`authToken`, `adminUser`, `isAdmin`) (`ui/services/admin.service.js:59-61`). `isAuthenticated()` chỉ decode `exp` phía client, không verify chữ ký (`admin.service.js:72-110`): đúng vai trò UX guard, server mới là enforcement. Không tự logout khi nhận 401.
- Router: mỗi route admin lặp lại cùng một `beforeEnter` (8 lần, `ui/router/index.js:96-186`); `beforeEach` toàn cục chỉ xử lý login page và tracking visitor (`router/index.js:207-237`). Route mới: copy `beforeEnter` hoặc tốt hơn chuyển sang `meta.requiresAdmin` + check trong `beforeEach`.
- Fallback-to-defaults (CMS):
  - Home khởi tạo state bằng `defaultHomepage`, merge `{ ...defaults, ...api.sections }` theo key; lỗi mạng -> giữ default (`ui/views/Home.vue:309-313,380-394`). Section DB `enabled=false` được trả về để ẩn, key vắng mặt mới dùng default (`api/service/ContentService.java:35-36`, `ui/views/Home.vue:313`).
  - Ảnh 3 trạng thái `useDefault`/override/rỗng qua `resolveSlotImage` (`ui/content/homeDefaults.js:122-128`).
  - Blog: không có fallback. API lỗi thì `Blog.vue`/`ArticleDetail.vue` hiện lỗi + Thử lại, khối blog ở Home ẩn (bản seed cũ có thể hiện bài admin đã sửa hoặc gỡ). `page-sections.seed.json` vẫn là fallback cho story/philosophy, đồng bộ tự động từ BE bằng `ui/scripts/sync-cms-seed.mjs`.
- Kích thước component: nhiều view khổng lồ, logic + template trong 1 file: `ProductDetail.vue` 2373 dòng, `AdminOrders.vue` 1439, `Me.vue` 1358, `Home.vue` 1200, `Cart.vue` 968, `AdminProductDetail.vue` 962, `App.vue` 755 (wc -l). File CMS mới nhỏ hơn (`AdminHomepage.vue` 305, `AdminBlogEditor.vue` 193): giữ quy mô đó, tách component con (`ui/components/cms/CmsImageUploader.vue`).

## Open questions
- Không có test chạy được (surefire 0 test theo `00-overview.md`) nên chưa xác minh được các mục đánh dấu (inferred), đặc biệt #2, #3, #8.
- Chưa có reference tái sử dụng `stack-reference-java.md`/`javascript.md`: brief giới hạn chỉ ghi file này.
