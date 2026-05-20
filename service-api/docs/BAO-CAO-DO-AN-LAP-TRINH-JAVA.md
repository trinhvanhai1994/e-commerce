---
title: "BÁO CÁO ĐỒ ÁN MÔN HỌC — LẬP TRÌNH JAVA"
subtitle: "Xây dựng hệ thống thương mại điện tử Thi Yên — Backend Spring Boot tích hợp Pancake POS và MISA MeInvoice"
author:
  - "Sinh viên: Trịnh Văn Hải"
  - "Lớp: K75.4"
  - "Giảng viên hướng dẫn: …"
  - "Trường: Đại học Công nghiệp Giao thông Vận tải"
date: "Năm học 2025 – 2026"
lang: vi-VN
fontsize: 13pt
mainfont: "Times New Roman"
sansfont: "Arial"
monofont: "Courier New"
linestretch: 1.5
geometry: "margin=2.5cm"
toc: true
toc-depth: 3
numbersections: true
---

<!-- ================================================================== -->
<!-- HƯỚNG DẪN CHUYỂN SANG DOCX (in ấn, nộp GV)                        -->
<!-- ================================================================== -->
<!-- Cài Pandoc: https://pandoc.org/installing.html                     -->
<!-- Trong thư mục docs, chạy (PowerShell):                             -->
<!--                                                                    -->
<!--   pandoc BAO-CAO-DO-AN-LAP-TRINH-JAVA.md `                         -->
<!--     -o BAO-CAO-DO-AN-LAP-TRINH-JAVA.docx `                         -->
<!--     --from markdown+yaml_metadata_block `                          -->
<!--     --toc --toc-depth=3 `                                          -->
<!--     -V mainfont="Times New Roman" `                                -->
<!--     -V fontsize=13pt `                                             -->
<!--     -V linestretch=1.5 `                                           -->
<!--     -V geometry:margin=2.5cm `                                    -->
<!--     --resource-path=.:..                                           -->
<!--                                                                    -->
<!-- Tùy chọn: dùng mẫu Word của trường                                 -->
<!--   pandoc ... -o output.docx --reference-doc=mau-truong.docx      -->
<!--                                                                    -->
<!-- Sau khi mở Word: kiểm tra ngắt trang (bìa → tóm tắt → mục lục →  -->
<!-- danh mục viết tắt → chương 1…); chèn ảnh minh họa Chương 6.       -->
<!-- ================================================================== -->

\newpage

# TRANG BÌA

**ĐẠI HỌC CÔNG NGHIỆP GIAO THÔNG VẬN TẢI**

**BÁO CÁO ĐỒ ÁN MÔN HỌC**

**LẬP TRÌNH JAVA**

---

**Đề tài:**

**XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ THI YÊN — BACKEND JAVA (SPRING BOOT) TÍCH HỢP PANCAKE POS VÀ MISA MEINVOICE**

---

| | |
|:---|:---|
| **Sinh viên thực hiện** | Trịnh Văn Hải |
| **Lớp** | K75.4 |
| **Môn học** | Lập trình Java |
| **Giảng viên hướng dẫn** | … |
| **Năm học** | 2025 – 2026 |

---

*Hà Nội, tháng … năm 2026*

\newpage

# TÓM TẮT ĐỒ ÁN

Đồ án xây dựng **hệ thống thương mại điện tử Thi Yên** gồm backend **Java 17 — Spring Boot 3.2** (`service-api`) và giao diện web **Vue 3** (`service-ui`). Backend cung cấp REST API cho quản lý sản phẩm, đơn hàng, địa giới hành, xác thực admin bằng **JWT**, đồng bộ dữ liệu với **Pancake POS** và tích hợp **MISA MeInvoice** để tạo hóa đơn điện tử nháp. Cơ sở dữ liệu **PostgreSQL** được quản lý phiên bản bằng **Flyway** (20 migration). Kết quả đạt được là một nền tảng có thể vận hành thử nghiệm trên môi trường dev, đáp ứng yêu cầu thực hành môn **Lập trình Java** và mô phỏng quy trình bán hàng — đồng bộ POS — xuất hóa đơn của doanh nghiệp vừa và nhỏ.

**Từ khóa:** Java, Spring Boot, REST API, JWT, PostgreSQL, Flyway, Thương mại điện tử, Pancake POS, MISA MeInvoice.

\newpage

# MỤC LỤC

*Bản in: mục lục tự động do Word/Pandoc tạo khi chuyển sang DOCX (`--toc`). Nếu chỉ xem file Markdown, dùng mục lục của trình xem hoặc bật `--toc` khi convert.*

\newpage

# DANH MỤC CÁC KÝ HIỆU, CHỮ VIẾT TẮT

| Ký hiệu / Viết tắt | Ý nghĩa |
|:-------------------|:--------|
| API | Application Programming Interface |
| TMĐT | Thương mại điện tử |
| JWT | JSON Web Token |
| JPA | Java Persistence API |
| ORM | Object-Relational Mapping |
| POS | Point of Sale (bán hàng tại quầy / OMS) |
| HĐĐT | Hóa đơn điện tử |
| CQT | Cơ quan thuế |
| DTO | Data Transfer Object |
| CRUD | Create, Read, Update, Delete |
| FE | Frontend (giao diện người dùng) |
| BE | Backend (máy chủ ứng dụng) |

\newpage

# CHƯƠNG 1. GIỚI THIỆU

## 1.1. Lý do chọn đề tài

Thương mại điện tử phát triển mạnh tại Việt Nam. Doanh nghiệp bán lẻ thường vận hành song song **website bán hàng**, **hệ thống quản lý đơn (POS/OMS)** và **phần mềm hóa đơn điện tử**. Khi các hệ thống không được kết nối, dữ liệu bị nhập trùng, dễ sai lệch tồn kho và chậm xuất hóa đơn cho khách hàng.

Đề tài **Xây dựng hệ thống TMĐT Thi Yên** hướng tới giải quyết bài toán tập trung dữ liệu tại **backend Java (Spring Boot)**: quản lý sản phẩm và đơn hàng, bảo mật khu vực quản trị, đồng bộ đơn từ **Pancake POS**, ánh xạ sang **MISA MeInvoice** để tạo hóa đơn nháp qua API. Đây là bài toán sát thực tế và phù hợp để vận dụng kiến thức **Lập trình Java** (OOP, framework, làm việc với CSDL, HTTP, bảo mật).

## 1.2. Mục tiêu đồ án

1. Xây dựng **REST API** bằng **Spring Boot** theo kiến trúc phân lớp (Controller — Service — Repository).
2. Thiết kế và triển khai **cơ sở dữ liệu PostgreSQL**, sử dụng **JPA/Hibernate** và **Flyway** migration.
3. Triển khai **xác thực và phân quyền** admin bằng **Spring Security** và **JWT**.
4. Tích hợp **Pancake POS** (đồng bộ sản phẩm, đơn hàng) và **MISA MeInvoice** (tạo hóa đơn chưa phát hành).
5. Xây dựng **giao diện web** (Vue.js) gọi API backend phục vụ khách hàng và quản trị viên.

## 1.3. Phạm vi đồ án

**Trong phạm vi:**

- Module `service-api`: toàn bộ logic Java, API, tích hợp, migration DB.
- Module `service-ui`: giao diện SPA (Vue 3, Vite, Tailwind CSS).
- Môi trường phát triển (dev): PostgreSQL local, cấu hình `application-dev.yml`.

**Ngoài phạm vi hoặc hạn chế:**

- Tự động ký số và phát hành hóa đơn lên CQT hoàn toàn qua API (theo tài liệu MISA, nhiều bước vẫn thực hiện trên web MeInvoice).
- Cổng thanh toán trực tuyến (VNPay, MoMo, …).
- Ứng dụng di động native (iOS/Android).

## 1.4. Đối tượng sử dụng

| Vai trò | Mô tả nhu cầu |
|:--------|:---------------|
| Khách hàng | Xem sản phẩm, đặt hàng, tra cứu đơn |
| Quản trị viên | Đăng nhập, quản lý sản phẩm/đơn hàng, xem thống kê |
| Vận hành / kế toán | Đồng bộ Pancake, kiểm tra mapping, xuất hóa đơn MeInvoice (API admin) |

## 1.5. Cấu trúc mã nguồn dự án

```text
e-commerce/
├── service-api/          # Backend Java Spring Boot (trọng tâm môn Java)
│   ├── src/main/java/    # Mã nguồn Java
│   ├── src/main/resources/
│   │   ├── application*.yml
│   │   └── db/migration/ # Flyway V1–V20
│   ├── docs/             # Tài liệu kỹ thuật
│   └── pom.xml
└── service-ui/           # Frontend Vue 3
    ├── src/views/        # Màn hình
    └── package.json
```

\newpage

# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ SỬ DỤNG

## 2.1. Công nghệ Backend (Java)

| STT | Công nghệ | Phiên bản / vai trò |
|:---:|:----------|:---------------------|
| 1 | Java | 17 (LTS) — ngôn ngữ lập trình chính |
| 2 | Spring Boot | 3.2.0 — framework ứng dụng |
| 3 | Spring Web | REST Controller, JSON |
| 4 | Spring Data JPA | ORM, Repository pattern |
| 5 | Spring Security | Phân quyền, JWT |
| 6 | Spring WebFlux (WebClient) | Gọi HTTP tới API bên thứ ba |
| 7 | PostgreSQL | Hệ quản trị CSDL |
| 8 | Flyway | Quản lý phiên bản schema |
| 9 | JJWT | Tạo và xác thực token |
| 10 | Lombok | Giảm mã lặp entity/DTO |
| 11 | Maven | Build và quản lý thư viện |

## 2.2. Công nghệ Frontend (bổ trợ)

| STT | Công nghệ | Vai trò |
|:---:|:----------|:--------|
| 1 | Vue.js 3 | Framework giao diện SPA |
| 2 | Vite 5 | Dev server, đóng gói production |
| 3 | Vue Router | Điều hướng trang |
| 4 | Tailwind CSS | Thiết kế giao diện responsive |
| 5 | Pinia | Quản lý trạng thái (nếu sử dụng) |

Frontend kết nối API qua biến môi trường `VITE_API_BASE_URL` (mặc định dev: `http://localhost:5678`).

## 2.3. Kiến trúc hệ thống

Hệ thống theo mô hình **Client — Server**. Backend áp dụng kiến trúc **ba lớp (Layered Architecture)**:

1. **Lớp trình bày (Presentation):** package `controller` — tiếp nhận HTTP, trả JSON.
2. **Lớp nghiệp vụ (Business):** package `service`, `integration.*.service` — xử lý logic, đồng bộ, map hóa đơn.
3. **Lớp truy cập dữ liệu (Persistence):** package `repository`, entity JPA — thao tác PostgreSQL.

Tích hợp bên thứ ba được tách package `integration.pancake` và `integration.meinvoice` (client, DTO, mapper, scheduler).

**Hình 2.1 — Sơ đồ kiến trúc tổng quan** *(chèn sơ đồ khi in: Client Vue → Spring Boot → PostgreSQL; nhánh Pancake, MeInvoice)*

## 2.4. Chuẩn định dạng phản hồi API

```json
{
  "success": true,
  "data": { },
  "message": "Mô tả kết quả"
}
```

## 2.5. Công nghệ bảo mật

- Mật khẩu người dùng admin mã hóa **BCrypt** trước khi lưu DB.
- Phiên admin sử dụng **JWT** trong header: `Authorization: Bearer <token>`.
- `SecurityConfig` và `JwtAuthenticationFilter` kiểm soát truy cập endpoint `/api/*/admin/**`.

\newpage

# CHƯƠNG 3. PHÂN TÍCH YÊU CẦU HỆ THỐNG

## 3.1. Yêu cầu chức năng

### 3.1.1. Module khách hàng (Public API)

| Mã | Chức năng | Mô tả ngắn |
|:---|:----------|:------------|
| F01 | Xem sản phẩm | Danh sách, chi tiết, tìm kiếm |
| F02 | Đặt hàng | Tạo đơn, nhập thông tin giao hàng |
| F03 | Tra cứu đơn | Theo mã đơn hoặc số điện thoại |
| F04 | Địa giới hành | Tỉnh / quận / phường phục vụ form checkout |
| F05 | Thống kê truy cập | Ghi nhận visitor (session) |

### 3.1.2. Module quản trị (Admin API)

| Mã | Chức năng | Mô tả ngắn |
|:---|:----------|:------------|
| A01 | Đăng nhập | Cấp JWT |
| A02 | Quản lý sản phẩm | CRUD, upload ảnh, soft delete |
| A03 | Quản lý đơn hàng | Xem, cập nhật trạng thái |
| A04 | Dashboard | Thống kê đơn, doanh thu (tổng quan) |
| A05 | Tích hợp Pancake | Sync catalog, sản phẩm, đơn; xem log |
| A06 | Tích hợp MeInvoice | Test kết nối, mẫu HĐ, preview, tạo HĐ nháp |

### 3.1.3. Module tích hợp Pancake POS

| Mã | Chức năng | Mô tả |
|:---|:----------|:------|
| P01 | Fetch catalog | Lấy danh mục SP/biến thể từ POS |
| P02 | Sync sản phẩm | Hai chiều FROM/TO Pancake (cấu hình) |
| P03 | Sync đơn hàng | Import đơn Pancake → bảng `orders` |
| P04 | Chống import trùng | Cờ `pancake_imported`, tham số `force` |
| P05 | Scheduler | Cron đồng bộ định kỳ (bật/tắt trong config) |
| P06 | Nhật ký | Bảng `pancake_sync_log` |

### 3.1.4. Module tích hợp MISA MeInvoice

| Mã | Chức năng | Mô tả |
|:---|:----------|:------|
| M01 | Xác thực token | `POST /webapp/token` (cache trong service) |
| M02 | Danh sách mẫu HĐ | `GET/POST /webapp/templates` |
| M03 | Preview mapping | Kiểm tra đơn → payload MeInvoice (không gọi MISA) |
| M04 | Preview trên MISA | `POST /webapp/preview` |
| M05 | Tạo HĐ nháp | `POST /webapp/insert` |
| M06 | Idempotency | `meinvoice_submissions`, cờ `meinvoice_invoiced` |

## 3.2. Yêu cầu phi chức năng

| Loại | Yêu cầu |
|:-----|:---------|
| Hiệu năng | API phản hồi trong thời gian chấp nhận được; timeout WebClient cấu hình |
| Bảo mật | Không lưu plaintext mật khẩu; không commit secret vào Git |
| Khả dụng | Hồ sơ cấu hình dev / test / prod |
| Mở rộng | Package `integration` tách biệt; hằng số tập trung |
| Bảo trì | Tài liệu `docs/`, migration có version |

## 3.3. Use case tiêu biểu

**UC-01 — Khách đặt hàng:** Chọn sản phẩm → giỏ hàng → checkout → hệ thống lưu `orders` và `order_items` → hiển thị mã đơn thành công.

**UC-02 — Đồng bộ đơn Pancake:** Admin gọi API sync → hệ thống map khách hàng, dòng hàng, gán `pancake_order_id`, `order_type = PANCAKE`.

**UC-03 — Xuất hóa đơn MeInvoice:** Kiểm tra `validation.ready` → gọi `draft-invoice` → MISA trả kết quả → lưu RefID và đánh dấu đơn đã invoiced.

\newpage

# CHƯƠNG 4. THIẾT KẾ HỆ THỐNG

## 4.1. Thiết kế cơ sở dữ liệu

### 4.1.1. Các bảng nghiệp vụ chính

| Bảng | Mục đích |
|:-----|:---------|
| `users` | Tài khoản quản trị |
| `products`, `product_images` | Sản phẩm và hình ảnh |
| `orders`, `order_items` | Đơn hàng và chi tiết |
| `provinces`, `districts`, `wards` | Địa giới hành Việt Nam |
| `visitors` | Thống kê lượt truy cập |
| `pancake_product_mapping`, `pancake_order_mapping` | Ánh xạ ID local ↔ Pancake |
| `pancake_sync_log` | Log đồng bộ |
| `pancake_catalog_entry` | Cache catalog POS |
| `meinvoice_submissions` | Lịch sử gọi API hóa đơn |

### 4.1.2. Flyway migration

- **V1:** Schema ban đầu (users, products, orders, địa giới hành).
- **V8:** Bảng tích hợp Pancake.
- **V16–V20:** MeInvoice submissions, catalog, cờ `meinvoice_invoiced`, `pancake_imported`.

### 4.1.3. Entity JPA chính

`User`, `Product`, `Order`, `OrderItem`, `Province`, `District`, `Ward`, `Visitor`.

**Hình 4.1 — Sơ đồ ER** *(chèn khi in: quan hệ Order — OrderItem — Product)*

## 4.2. Thiết kế package Backend

```text
com.dragun.ecommerce
├── config              # Security, CORS, Web
├── controller
│   ├── admin           # Auth, Product, Dashboard, Integration, Meinvoice
│   ├── external        # Order, Product (public)
│   └── publicapi       # Visitor
├── service             # Nghiệp vụ lõi
├── repository          # Spring Data JPA
├── model               # entity, dto
├── security            # JWT
├── exception           # GlobalExceptionHandler
└── integration
    ├── pancake         # client, mapper, scheduler, service
    └── meinvoice       # client, auth, invoice service
```

## 4.3. Thiết kế API (trích yếu)

### API công khai

| Method | Endpoint | Mô tả |
|:-------|:---------|:------|
| GET | `/api/dragun/products` | Danh sách / chi tiết sản phẩm |
| POST | `/api/extend/orders` | Tạo đơn hàng |
| GET | `/provinces`, `/districts/{code}`, `/wards/{code}` | Địa giới hành |

### API quản trị (yêu cầu JWT)

| Method | Endpoint | Mô tả |
|:-------|:---------|:------|
| POST | `/api/thiyen/admin/login` | Đăng nhập |
| POST | `/api/thiyen/admin/integration/pancake/sync/orders` | Đồng bộ đơn Pancake |
| POST | `/api/thiyen/admin/integration/meinvoice/test-connection` | Kiểm tra MISA |
| GET | `/api/thiyen/admin/integration/meinvoice/templates` | Danh sách mẫu HĐ |
| POST | `/api/thiyen/admin/integration/pancake-meinvoice/orders/{id}/draft-invoice` | Tạo HĐ nháp |

## 4.4. Thiết kế tích hợp Pancake POS

- **Base URL:** `https://pos.pages.fm/api/v1`
- **Thành phần:** `PancakeApiClient`, `PancakeOrderResponseParser`, `OrderMapper`, `PancakeOrderSyncService`
- **Xử lý dòng không map sản phẩm:** sản phẩm placeholder `__PANCAKE_ORDER_LINE_UNMAPPED__` (Flyway V17)

## 4.5. Thiết kế tích hợp MISA MeInvoice

- **Sandbox API:** `https://testapi.meinvoice.vn/api/integration`
- **Luồng:** `MeinvoiceAuthService` (token) → `MeinvoiceApiClient` → `MeinvoiceInvoiceService` (build payload, validation, insert)
- **Cấu hình:** `MEINVOICE_TAXCODE`, `MEINVOICE_USERNAME`, `MEINVOICE_PASSWORD`, `MEINVOICE_TEMPLATE_ID`, `MEINVOICE_INV_SERIES`, `MEINVOICE_INVOICE_WITH_CODE`

**Hình 4.2 — Sequence diagram: Sync đơn → Tạo hóa đơn** *(chèn khi in)*

## 4.6. Thiết kế giao diện (service-ui)

| Nhóm | Route / View |
|:-----|:-------------|
| Khách hàng | `/`, `/products`, `/cart`, `/checkout`, `/contact`, … |
| Admin | `/admin/login`, `/admin/orders`, `/admin/products`, `/admin` (dashboard) |

Router bảo vệ route `/admin/*` bằng kiểm tra JWT phía client (`adminService.isAuthenticated()`).

\newpage

# CHƯƠNG 5. CÀI ĐẶT, TRIỂN KHAI VÀ KIỂM THỬ

## 5.1. Môi trường phát triển

| Thành phần | Phiên bản khuyến nghị |
|:-----------|:----------------------|
| JDK | 17 |
| Apache Maven | 3.8+ |
| PostgreSQL | 14+ |
| Node.js | 18+ (cho frontend) |

## 5.2. Các bước cài đặt

### 5.2.1. Cơ sở dữ liệu

1. Tạo database (ví dụ `ecommerce` hoặc `ecommerce_dev`).
2. Cấu hình `spring.datasource` trong `application-dev.yml` hoặc biến môi trường `SPRING_DATASOURCE_*`.

### 5.2.2. Backend (service-api)

```powershell
cd service-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Ứng dụng chạy tại: `http://localhost:5678`. Flyway tự áp dụng migration khi khởi động.

### 5.2.3. Frontend (service-ui)

```powershell
cd service-ui
npm install
npm run dev
```

Giao diện dev: `http://localhost:5173`.

### 5.2.4. Cấu hình tích hợp (tùy chọn khi demo)

- **Pancake:** `PANCAKE_API_KEY`, `PANCAKE_SHOP_ID`, `PANCAKE_WAREHOUSE_ID`
- **MeInvoice:** `MEINVOICE_ENABLED=true`, MST, user, pass, template ID, inv series

## 5.3. Kịch bản kiểm thử

| STT | Nội dung kiểm thử | Kết quả mong đợi |
|:---:|:-------------------|:-----------------|
| 1 | Đăng nhập admin | Nhận JWT, truy cập được trang admin |
| 2 | Lấy danh sách sản phẩm | `success: true`, có dữ liệu |
| 3 | Tạo đơn hàng mới | Lưu DB, trả `order_id` |
| 4 | Test kết nối Pancake | `connected: true` (khi đã cấu hình key) |
| 5 | Sync đơn FROM_PANCAKE | `syncedFromPancake` hoặc `skipped` hợp lệ |
| 6 | Test kết nối MeInvoice | `tokenOk: true` (sandbox + tờ khai OK) |
| 7 | GET mapping đơn | `validation.ready: true` |
| 8 | POST draft-invoice | `recordedSuccess: true`, có `refId` |

**Lớp test tự động trong project:** `EcommerceApplicationTests`, `PancakeApiClientTest`, `PancakeProductSyncIntegrationTest`.

## 5.4. Khó khăn và hướng xử lý

| Khó khăn | Hướng xử lý |
|:---------|:-------------|
| JDK không đúng phiên bản 17 | Cài JDK 17, thiết lập `JAVA_HOME` |
| MeInvoice lỗi tờ khai / mẫu | Hoàn thành thiết lập trên web sandbox testapp3 |
| Đơn thiếu thông tin khách | Enrich từ API chi tiết đơn Pancake khi sync |
| Trùng xuất hóa đơn | Cờ `meinvoice_invoiced` và bảng `meinvoice_submissions` |

\newpage

# CHƯƠNG 6. KẾT QUẢ ĐẠT ĐƯỢC

## 6.1. Sản phẩm phần mềm

- Backend **Java Spring Boot** với khoảng **100 class** trong `service-api`, cung cấp REST API đầy đủ cho TMĐT và tích hợp ngoài.
- Frontend **Vue 3** phục vụ bán hàng và quản trị.
- **20 file migration Flyway** quản lý schema có kiểm soát.
- Bộ **tài liệu kỹ thuật** trong thư mục `service-api/docs/`.

## 6.2. Kiến thức Java đã vận dụng

- Lập trình hướng đối tượng: Entity, DTO, Service, Repository.
- Annotation và cơ chế **Dependency Injection** của Spring.
- **JPA** mapping quan hệ, truy vấn repository.
- **Spring Security**, JWT, mã hóa BCrypt.
- **WebClient**, xử lý JSON (Jackson), exception tập trung.
- Cấu hình đa môi trường (`application-*.yml`).

## 6.3. Minh chứng (chèn khi in báo cáo)

*Gợi ý chèn 5–8 hình ảnh:*

1. Giao diện trang chủ / danh sách sản phẩm.
2. Trang checkout / đặt hàng thành công.
3. Màn hình đăng nhập và quản lý đơn admin.
4. Postman: `test-connection` MeInvoice thành công.
5. Postman: `draft-invoice` thành công.
6. Sơ đồ ER hoặc kiến trúc hệ thống.

## 6.4. Hạn chế

- Một số endpoint admin dùng song song prefix `/api/dragun` và `/api/thiyen`.
- Chưa có màn hình admin riêng cho thao tác Sync Pancake / MeInvoice (chủ yếu qua API).
- MeInvoice: chưa bọc đủ API delete, paging, xem PDF trên REST admin.
- Thuế suất hóa đơn đang dùng một mức VAT mặc định cho mọi dòng hàng.

\newpage

# KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## Kết luận

Đồ án đã xây dựng thành công **hệ thống thương mại điện tử Thi Yên** với **backend Lập trình Java (Spring Boot)** đáp ứng các nghiệp vụ cốt lõi: quản lý sản phẩm — đơn hàng, bảo mật admin, đồng bộ **Pancake POS** và tích hợp **MISA MeInvoice** tạo hóa đơn điện tử nháp. Kiến trúc phân lớp rõ ràng, sử dụng PostgreSQL và Flyway, phù hợp yêu cầu học phần **Lập trình Java** và thực tiễn triển khai TMĐT tại doanh nghiệp.

Sinh viên **Trịnh Văn Hải** đã vận dụng kiến thức Java, framework Spring, làm việc với cơ sở dữ liệu quan hệ và REST API; đồng thời tiếp cận bài toán **tích hợp hệ thống phân tán** (third-party APIs).

## Hướng phát triển

1. Bổ sung giao diện admin cho đồng bộ Pancake và xuất hóa đơn MeInvoice.
2. Hoàn thiện API MeInvoice: preview PDF, xóa HĐ nháp, tra cứu trạng thái.
3. Tích hợp cổng thanh toán và thông báo đơn hàng (SMS/Zalo).
4. Báo cáo doanh thu, xuất Excel.
5. Triển khai Docker Compose (API + DB + UI).
6. Mở rộng bộ test tự động cho các service tích hợp.

\newpage

# TÀI LIỆU THAM KHẢO

1. Tài liệu dự án: `service-api/docs/01-setup.md`, `02-database.md`, `03-build-deploy.md`, `04-pancake-integration.md`, `05-meinvoice-integration-conclusion.md`.
2. MISA MeInvoice — *Tài liệu API Tạp hóa đơn Misa.md* (trong `service-api/docs/`).
3. Postman collection — `API AIO WEB APP_MISA.postman_collection.json`.
4. Spring Boot Documentation — https://spring.io/projects/spring-boot (truy cập 2026).
5. Spring Data JPA — https://spring.io/projects/spring-data-jpa (truy cập 2026).
6. Vue.js 3 — https://vuejs.org/ (truy cập 2026).
7. Pancake POS API — https://api-docs.pancake.biz/ (truy cập 2026).
8. MISA MeInvoice Sandbox — https://testapp3.meinvoice.vn (truy cập 2026).

\newpage

# PHỤ LỤC

## Phụ lục A — Một số lớp Java tiêu biểu

| Lớp | Package | Vai trò |
|:----|:--------|:--------|
| `EcommerceApplication` | root | Khởi động Spring Boot |
| `SecurityConfig` | config | Cấu hình bảo mật |
| `OrderService` | service | Nghiệp vụ đơn hàng |
| `PancakeOrderSyncService` | integration.pancake | Đồng bộ đơn Pancake |
| `MeinvoiceInvoiceService` | integration.meinvoice | Tạo payload và gọi MISA |
| `GlobalExceptionHandler` | exception | Xử lý lỗi API thống nhất |

## Phụ lục B — Tài khoản mặc định môi trường dev

| Thông tin | Giá trị (dev) |
|:----------|:---------------|
| Username admin | `admin` |
| Password admin | `thiyen1` |

*Lưu ý: Đổi mật khẩu trước khi triển khai production.*

---

**Sinh viên thực hiện**

*Trịnh Văn Hải*

**Chữ ký**

…………………………

**Ngày nộp:** … / … / 2026
