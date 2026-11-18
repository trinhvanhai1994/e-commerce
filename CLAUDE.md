# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Hướng dẫn cho AI agent (gốc repo)

Đọc file này trước khi làm bất kỳ việc gì trong repo. Mục tiêu: mọi agent đi cùng một flow, đọc đúng nguồn, sửa đúng chỗ.

## 1. Bản đồ repo

```
e-commerce/
├── docs/
│   ├── req/          # Tài liệu yêu cầu 6 tầng (00-request → 05-design). Quy tắc: docs/req/CLAUDE.md
│   └── ai/           # Context pack về code (overview, API, data model, conventions) cho agent
├── service-api/      # Spring Boot 3.2 / Java 17 / PostgreSQL / Flyway. Tài liệu vận hành: service-api/docs/
└── service-ui/       # Vue 3 / Vite / Tailwind / Pinia. Tài liệu vận hành: service-ui/docs/
```

Mỗi thư mục có đúng một nơi giữ một loại thông tin. Không tạo file hướng dẫn mới ở gốc service (README + `docs/` là đủ).

## 2. Thứ tự đọc

1. `docs/req/CLAUDE.md`: cấu trúc tài liệu, quy ước ID, provenance.
2. Task đang làm trong `docs/req/01-wip/task-<nnn>-<slug>/` hoặc deliverable `03`–`05` có `status: reviewed`.
3. `docs/ai/` (nếu có): bản đồ code, API, data model, conventions. Đây là cache, kiểm lại với code khi quan trọng.
4. Code của service liên quan.

## 3. Flow triển khai một yêu cầu

1. **Yêu cầu**: nguồn thô vào `docs/req/00-request/`; phân tích trong `docs/req/01-wip/task-<nnn>-<slug>/`.
2. **Chốt spec**: tách sang `03-business` / `04-system` / `05-design` qua skill sở hữu tương ứng. Dev chỉ code từ tài liệu `reviewed`.
3. **Backend trước**: migration Flyway (`service-api/src/main/resources/db/migration/V<n>__*.sql`, số tiếp theo, không sửa migration đã chạy) → entity → repository → service → controller → test.
4. **Frontend sau**: `services/*.service.js` → view/component → route → kiểm tra trên trình duyệt.
5. **Đổi API đang được FE dùng**: giữ tương thích với FE hiện tại, hoặc sửa cả hai trong cùng thay đổi.
6. **Cập nhật tài liệu**: sửa `docs/ai/` hoặc `docs/req/` khi thay đổi làm chúng sai.

Làm chức năng mới: theo skill dự án `.claude/skills/ecommerce-dev/SKILL.md` (gọi bằng `/ecommerce-dev`), gồm hồ sơ yêu cầu, kế hoạch chờ owner duyệt, quy tắc riêng BE/FE, cách viết test, chạy và kiểm tra trên trình duyệt.

## 4. Lệnh chạy và kiểm tra

| Service | Chạy local | Kiểm tra |
|---|---|---|
| service-api (port 5678) | Bật JDK 17 trong shell: `. .\scripts\setup\setup-java-current-session.ps1 -Version 17` (PowerShell) hoặc `source ./scripts/setup/setup-java.sh 17`; rồi `scripts/run/run-dev.*` (profile `dev`, DB `ecommerce_dev`). Build/run theo môi trường: `scripts/{build,run}/*-{dev,test,prod}.*` | `mvn test` là gate (cần Docker đang chạy: test `@SpringBootTest` dùng PostgreSQL thật qua Testcontainers, profile `it`, Flyway + `ddl-auto: validate`). Một test: `mvn test -Dtest=ProductReviewServiceTest` (hoặc `-Dtest=Class#method`). Test gắn `@Tag("external")` gọi Pancake POS thật, bị loại khỏi lần chạy mặc định |
| service-ui (port 5173) | `npm run dev` (gọi API theo `VITE_API_BASE_URL`) | `npm run build`. ESLint chưa có file cấu hình nên `npm run lint` chưa dùng được; chưa có test; `npm run format` = prettier |

`predev`/`prebuild` của service-ui chạy `scripts/sync-cms-seed.mjs`, chép seed CMS từ `service-api/src/main/resources/cms-seed/` sang `service-ui/src/content/page-sections.seed.json`. Sửa file seed phía BE, không sửa bản FE.

## 5. Quy ước chung

- API path: public `/api/dragun/...`, admin `/api/dragun/admin/...` (`@PreAuthorize("hasRole('ADMIN')")`), response bọc `ApiResponse`.
- Ảnh upload qua `/api/dragun/admin/files/upload-cms` theo slot (`cms/ImageSlot.java` ↔ `service-ui/src/constants/imageSlots.js`, phải giữ đồng bộ).
- Commit và push repo này chỉ bằng email `trinhhai28041994@gmail.com` (GitHub `trinhvanhai1994`). Kiểm bằng `bash .claude/skills/ecommerce-dev/scripts/check-git-account.sh` trước khi hỏi commit và trước khi push (chi tiết: skill `ecommerce-dev` mục 9).
- Không commit `node_modules/`, `dist/`, `target/`, `uploads/`, file `.env.local`. Không đọc hoặc in giá trị trong file `.env*`.
- Văn bản tài liệu: tiếng Việt, không dùng em-dash (theo `docs/req/CLAUDE.md` mục D).

## 6. Bẫy hay gặp (chi tiết: `docs/ai/00-overview.md` mục Gotchas)

- Danh sách route public nằm ở hai nơi: `util/Constants.PUBLIC_ENDPOINTS` và `JwtAuthenticationFilter.shouldNotFilter`. Đổi cả hai.
- `/api/extend/orders/**` đang `permitAll` (kể cả đổi trạng thái); admin UI dùng chung route này. Không nhân bản pattern này cho route mới.
- Profile dev/test dùng `ddl-auto: update`, prod dùng `validate`: cột thiếu trong migration chỉ lộ ra ở prod. Mọi thay đổi schema phải có migration.
- CMS seed (`cms-seed/*.json`) phải bằng đúng nội dung site đang hiển thị: `CmsSeedService` chỉ chèn key còn thiếu. Storefront merge section từ API lên `service-ui/src/content/homeDefaults.js`; API public không được lọc bỏ section `enabled:false`.
