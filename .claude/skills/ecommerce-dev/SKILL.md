---
name: ecommerce-dev
description: Triển khai chức năng mới trong repo e-commerce Thi Yên (service-api Spring Boot + service-ui Vue) theo đúng flow và quy tắc riêng của dự án, từ yêu cầu, kế hoạch, migration, API, trang, tới test, gate xanh và kiểm tra trên trình duyệt. Dùng khi được yêu cầu làm chức năng, màn hình, API, bảng dữ liệu, mục CMS hay trang admin mới, hoặc mở rộng chức năng đang có ("thêm chức năng...", "làm màn hình...", "tạo API...", "làm trang admin quản lý...", "implement feature..."). Không dùng cho sửa bug đơn lẻ (dùng skill debug), câu hỏi thuần, review diff, hay viết tài liệu requirement.
---

# ecommerce-dev: triển khai chức năng mới trong repo e-commerce

Skill này là flow làm chức năng mới của riêng repo này. Nó không lặp lại tài liệu: mỗi bước trỏ tới nơi giữ thông tin gốc. Khi tài liệu và code lệch nhau, code đúng, và sửa tài liệu trong cùng thay đổi.

## 1. Đọc trước khi viết

1. `CLAUDE.md` (gốc repo): bản đồ, flow, lệnh, bẫy hay gặp.
2. Yêu cầu: task trong `docs/req/01-wip/task-<nnn>-<slug>/`, quy tắc tài liệu ở `docs/req/CLAUDE.md`.
3. `docs/ai/00-overview.md` (mục Gotchas, "Where new code goes"), `docs/ai/40-conventions.md` mục 0 (file mẫu để copy), `docs/ai/50-stack-patterns.md` (transaction, lock, security).
4. File mẫu tương ứng trong code. Copy pattern của nó, không tự nghĩ pattern mới.

### Ghi yêu cầu (chức năng mới luôn có hồ sơ)

- Lời owner, nguyên văn: `docs/req/00-request/request-<nnn>-<slug>.md`, số tiếp theo (đang có tới `request-002`).
- Phân tích và quyết định: `docs/req/01-wip/task-<nnn>-<slug>/analysis-and-spec.md`, cùng số với request. Copy cấu trúc của `task-002-product-reviews/analysis-and-spec.md`: hiện trạng, quyết định đã chốt (có nguồn), thiết kế dữ liệu, API, giao diện, rủi ro và câu hỏi mở.
- Điều mình tự đề xuất (giới hạn, số liệu) gắn `[PROPOSED]`; điều suy ra gắn `[INFERRED]` (quy tắc ở `docs/req/CLAUDE.md` mục B1).
- Mở rộng chức năng đã có: ghi thêm mục "Thay đổi <ngày>" vào request và task cũ, không tạo số mới.

## 2. Làm rõ và lên kế hoạch trước khi code

- Hỏi owner những điểm làm sai thì phải làm lại: ai tạo hoặc sửa dữ liệu (khách, admin), dữ liệu có hiện công khai không, có cần duyệt không, giới hạn (số ảnh, độ dài), dữ liệu cũ xử lý thế nào. Mỗi câu kèm một phương án mặc định đề xuất.
- Viết kế hoạch ngắn và chờ owner đồng ý khi chức năng có bảng mới, API mới, hoặc đụng cả BE và FE:
  - bảng và cột mới (migration số mấy);
  - danh sách endpoint (method, path, quyền, request, response, mã lỗi);
  - màn hình mới hoặc sửa (route, ai dùng, mục menu admin);
  - file mẫu sẽ copy cho từng lớp;
  - test sẽ viết.
- Liệt kê chỗ đang gọi thứ sắp đổi (Grep ở `service-ui/src/services/` và `views/`). Đổi API mà FE đang dùng: giữ tương thích hoặc sửa cả hai trong cùng thay đổi.
- Chức năng có sẵn thì dùng lại, không viết song song. Ví dụ admin sửa, ẩn, xoá đánh giá đã có.

## 3. Backend (service-api), làm trước

Thứ tự: migration → entity → repository → service → controller → test.

Quy tắc riêng, kiểm từng mục khi đụng tới:

- **Migration**: `src/main/resources/db/migration/V<số tiếp theo>__*.sql`. Không sửa migration đã chạy. Mọi cột mới phải có migration: dev dùng `ddl-auto: update` nên che lỗi, nhưng test profile `it` và prod dùng `validate` sẽ fail.
- **Path**: public `/api/dragun/...`, admin `/api/dragun/admin/...` với `@PreAuthorize("hasRole('ADMIN')")` ở class. Response bọc `ApiResponse`. Message cho người dùng viết tiếng Việt.
- **Route public mới ngoài prefix đã mở**: thêm vào cả `util/Constants.PUBLIC_ENDPOINTS` và `security/JwtAuthenticationFilter.shouldNotFilter`.
- **Không copy** kiểu `/api/extend/orders/**` (`permitAll` cả thao tác ghi).
- **Lỗi nghiệp vụ**: ném `ResponseStatusException` với mã đúng (400, 404, 409, 429), như `service/ProductReviewService.java`.
- **Sửa có khoá lạc quan**: so `updatedAt` client gửi, 409 khi lệch, `saveAndFlush` để `@PreUpdate` chạy trước khi build response. Timestamp truncate về `MICROS` (mẫu `ContentService.updateSection`).
- **Ảnh upload**: slot mới phải thêm ở cả `cms/ImageSlot.java` và `service-ui/src/constants/imageSlots.js`.
- **CMS seed** (`resources/cms-seed/*.json`): phải bằng đúng nội dung site đang hiển thị. Sửa bản BE, bản FE được `npm run dev/build` tự chép sang.
- **Hành động cần khách đăng nhập**: chặn ở `SecurityConfig` bằng `hasRole("CUSTOMER")` (matcher đặt trước `PUBLIC_ENDPOINTS`), đọc khách bằng `@AuthenticationPrincipal CustomerPrincipal`. Lỗi 401/403 được ghi JSON thẳng trong `SecurityConfig.exceptionHandling`; đừng để rơi xuống `/error` (mất token, 403 thành 401).
- **`@Modifying` JPQL**: dùng `flushAutomatically = true, clearAutomatically = true`, nếu không entity đã nạp trong cùng transaction giữ giá trị cũ.
- **API public có cache** (`CacheControl` 30s): FE cần thấy dữ liệu mới ngay sau khi ghi thì gọi lại với query `_=<timestamp>` (mẫu `reviewService.getProductReviews(id, { fresh: true })`).

## 4. Test backend

- Test cần Spring context: `@SpringBootTest` + `@ActiveProfiles("it")` + `@Import(TestcontainersConfiguration.class)`. Copy `src/test/java/com/dragun/ecommerce/service/ProductReviewServiceTest.java`.
- **Không dùng profile `test`** trong test: đó là môi trường TEST đã deploy, đọc thông tin DB từ biến môi trường.
- Unit test thuần: `@ExtendWith(MockitoExtension.class)`, copy `integration/pancake/service/PancakeProductSyncServiceTest.java`.
- Thêm `@Transactional` ở class để mỗi test tự rollback. Bean giữ state trong bộ nhớ (ví dụ `ReviewSubmissionLimiter`) dùng chung giữa các test, nên mỗi test dùng key riêng (`UUID`).
- Test gọi hệ ngoài thật (Pancake POS): gắn `@Tag("external")`. Surefire bỏ qua tag này khi chạy mặc định.
- Chức năng mới có service mới thì có class test mới cho nó. Phủ tối thiểu: luồng đúng, dữ liệu sai (400), không tồn tại (404), xung đột (409) nếu có lock, quyền nếu là admin, và tác động phụ (dữ liệu public đổi đúng).
- Phân quyền HTTP: copy `controller/CustomerSecurityFlowTest.java` (MockMvc qua filter thật, tạo khách bằng `TestCustomers`). MockMvc không chạy `/error` dispatch, nên kiểm mã 401/403 trên server thật nữa.
- Bảng mới: `EcommerceApplicationTests` chạy Flyway và `ddl-auto: validate` trên Postgres thật, nên entity lệch migration sẽ đỏ ở đây. Chạy nó sớm, ngay sau khi viết entity.
- **Chứng minh test bắt được lỗi**: tạm làm hỏng đúng dòng vừa sửa, chạy test thấy đỏ, khôi phục, chạy lại thấy xanh. Kiểm `git diff` để chắc đã khôi phục.

Lệnh, chạy trong `service-api/` với JDK 17 (máy có thể mặc định Java 8, xem `scripts/setup/`):

```bash
mvn test -Dtest=ProductReviewServiceTest   # một class khi đang lặp
mvn test                                   # gate: toàn bộ, cần Docker đang chạy
```

## 5. Frontend (service-ui), làm sau

Thứ tự: `services/*.service.js` → view/component → route → mục menu.

- Trang admin mới cần đủ ba chỗ: view `views/Admin<Tên>.vue`, route `/admin/<tên>` có `beforeEnter` kiểm đăng nhập trong `router/index.js`, và mục menu trong `views/AdminSidebar.vue`. Thiếu mục menu thì admin không tìm thấy trang.
- Trang public mới: route trong `router/index.js`, link từ header hoặc trang liên quan, xử lý ba trạng thái đang tải, lỗi kèm nút Thử lại, không có dữ liệu (mẫu `views/ArticleDetail.vue`).
- Cần khách đăng nhập: `authStore.requireLogin(() => hànhĐộng(), 'lý do')` mở popup rồi chạy tiếp; gọi API của khách qua `authStore.authorized((token) => service.x(token))` (tự refresh khi 401). Không lưu token khách vào `localStorage['authToken']` (đó là token admin).
- Gọi API qua `services/*.service.js` (`httpClient` + `unwrap`), không gọi `fetch` trong view. Upload multipart là ngoại lệ, dùng `fetch` trong service (mẫu `reviewService.submitReview`).
- Trang admin: bọc `AdminLayout`, route có `beforeEnter` kiểm đăng nhập (mẫu `views/AdminHomepage.vue`, `router/index.js`).
- Trang public đọc CMS: merge dữ liệu API lên default trong `src/content/homeDefaults.js`. Section `enabled:false` thì ẩn, API public không lọc bỏ.
- **Ảnh upload**: dùng `CmsImageUploader` (tự mở hộp chọn vùng khi ảnh lệch tỷ lệ slot). Chỉ bind `v-model:use-default` khi slot có ảnh mặc định đóng gói. Khung hiển thị phải cùng tỷ lệ slot (`aspect-square`, `aspect-[16/10]`) với `object-cover`, hoặc `w-full h-auto`; không dùng `h-*` cố định với `object-cover`.
- **Ảnh lỗi**: dùng `applyImageFallback(event, path)` trong `utils/imageUtils.js`. Không tự gán `event.target.src` trong `@error`: ảnh dự phòng cũng lỗi sẽ gây request lặp vô hạn (`onerror = null` không chặn được `@error` của Vue).
- Biến trong template phải đúng tên biến của `v-for` (lỗi cũ: `:key` dùng `img` trong khi biến là `image`).
- Đổi text hiển thị cho khách thì đổi cả message BE tương ứng nếu FE dùng message từ API.

## 6. Chạy app và kiểm tra trên trình duyệt

- Dev server: preview `service-api` (cổng 5678) và `service-ui` (cổng 5173) trong `.claude/launch.json`. Không chạy dev server bằng shell.
- API không lên, log báo `Connection to localhost:5433 refused`: Postgres dev đang tắt. Tìm container bằng `docker ps -a` (image postgres, cổng 5433) và `docker start` nó.
- Đăng nhập admin local: tài khoản seed trong `db/migration/V6__Fix_admin_password.sql`.
- Trang sản phẩm là `/products/:id` (có `s`).
- **Browser pane bị ẩn**: trang chạy nền, hiệu ứng chuyển route không xong, trang mới không hiện. Đó không phải lỗi code. Kiểm `tabs_context`, nhờ người dùng mở pane, hoặc kiểm bằng API.
- Lỗi lạ ngay sau khi sửa nhiều file liên tiếp (ví dụ `X is not defined`): thường là bản hot-reload dở dang. Tải lại hẳn trang rồi kiểm lại.
- **Kiểm request lặp** sau thay đổi UI: chạy trong trang, kết quả phải gần 0 khi trang đứng yên.

```js
performance.clearResourceTimings(); await new Promise(r => setTimeout(r, 10000));
performance.getEntriesByType('resource').length
```

- Ảnh mặc định `/images/...` vỡ ở local là lỗi có sẵn (file nằm ở `service-ui/public/images`, URL trỏ host API). Không cần sửa trong task khác.
- Bằng chứng: thao tác thật (gửi form, bấm nút), rồi đối chiếu dữ liệu qua API public. Dọn hoặc ẩn dữ liệu test, không xoá cứng khi chưa hỏi.

## 7. Gate trước khi báo xong

```bash
cd service-api && mvn test        # ghi số test pass/fail
cd service-ui && npm run build    # ESLint chưa có config, chưa có test FE
```

Chỉ báo xong khi cả hai xanh trong lượt này, kèm số liệu. Đỏ thì sửa hoặc báo nguyên văn lỗi.

## 8. Cập nhật tài liệu cùng thay đổi

- `docs/ai/00-overview.md` (Gotchas), `20-api.md` (route, hành vi), `30-data-model.md` (bảng, migration), `40-conventions.md` (file mẫu, lệnh): sửa mục nào thay đổi làm sai.
- `docs/req/`: cập nhật task `01-wip` theo cái đã làm thật (mục thiết kế đã triển khai), ghi rủi ro và câu hỏi mở (gắn `[INFERRED]` khi là suy luận).
- Chức năng mới: thêm route vào `20-api.md`, bảng vào `30-data-model.md` (kèm số migration), dòng mới vào bảng "Where new code goes" của `00-overview.md` nếu nó thành file mẫu tốt hơn.
- Văn bản tiếng Việt, không dùng em-dash.
- Phát hiện bẫy mới đáng nhớ: thêm vào `CLAUDE.md` mục 6 hoặc vào skill này.

## 9. Commit

- **Tài khoản git**: repo này chỉ được commit và push bằng email `trinhhai28041994@gmail.com` (tài khoản GitHub `trinhvanhai1994`). Máy local đăng nhập nhiều tài khoản, global config là người khác. Trước khi hỏi commit và trước khi push, chạy `bash .claude/skills/ecommerce-dev/scripts/check-git-account.sh`; chỉ commit/push khi in `GIT_ACCOUNT=ok`. Sai thì báo owner, đề xuất đặt `git config --local`, không tự sửa global config, không tự viết lại commit cũ.
- Hook `PreToolUse` trong `.claude/settings.json` (file local, bị ignore) gọi cùng script với `--hook`, chặn `git commit`/`git push`/`git-open-pr` khi tài khoản sai. Máy mới cần tạo lại hook đó.
- Hỏi trước khi commit. Không stage file `.env*` (ví dụ `service-ui/.env` đang có thay đổi local), `dist/`, `target/`, `uploads/`.
- Một thay đổi gồm cả BE và FE thì commit chung, hoặc commit BE trước ở dạng FE cũ vẫn chạy.
