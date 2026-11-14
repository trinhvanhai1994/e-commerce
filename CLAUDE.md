# Hướng dẫn cho AI agent (gốc repo)

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

## 4. Lệnh kiểm tra

| Service | Lệnh |
|---|---|
| service-api | `mvn -q compile` (cần JDK 17; máy có thể mặc định Java 8, xem `service-api/scripts/setup/`). `mvn test` hiện chạy 0 test vì các test class đang bị comment, không coi là gate |
| service-ui | `npm run build` (ESLint chưa có file cấu hình nên `npm run lint` chưa dùng được; chưa có test) |

## 5. Quy ước chung

- API path: public `/api/dragun/...`, admin `/api/dragun/admin/...` (`@PreAuthorize("hasRole('ADMIN')")`), response bọc `ApiResponse`.
- Ảnh upload qua `/api/dragun/admin/files/upload-cms` theo slot (`cms/ImageSlot.java` ↔ `service-ui/src/constants/imageSlots.js`, phải giữ đồng bộ).
- Không commit `node_modules/`, `dist/`, `target/`, `uploads/`, file `.env.local`. Không đọc hoặc in giá trị trong file `.env*`.
- Văn bản tài liệu: tiếng Việt, không dùng em-dash (theo `docs/req/CLAUDE.md` mục D).
