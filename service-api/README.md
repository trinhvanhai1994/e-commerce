# Thi Yen E-commerce API

Spring Boot REST API cho nền tảng thương mại điện tử Thi Yen.

## 📋 Mục lục

- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Quick Start](#quick-start)
- [Tài liệu chi tiết](#tài-liệu-chi-tiết)
- [Cấu trúc Project](#cấu-trúc-project)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Troubleshooting](#troubleshooting)

## Công nghệ sử dụng

- **Java 17** (LTS)
- **Spring Boot 3.2.0** (ổn định, tương thích JDK 17)
- **PostgreSQL** (database)
- **Maven** (build tool)
- **Spring Security** với JWT authentication
- **Spring Data JPA** (ORM)
- **Flyway** (database migration)
- **OAuth2 Client** (chuẩn bị cho tích hợp bên thứ 3)

## Quick Start

### 1. Cài đặt Môi trường

Xem hướng dẫn chi tiết: [docs/01-setup.md](./docs/01-setup.md)

**Tóm tắt:**
```bash
# Setup JDK 17 (Windows PowerShell)
. .\scripts\setup\setup-java-current-session.ps1 -Version 17

# Setup JDK 17 (Linux/Mac/Git Bash)
source ./scripts/setup/setup-java.sh 17
```

### 2. Setup Database

Xem hướng dẫn chi tiết: [docs/02-database.md](./docs/02-database.md)

**Tóm tắt:**
```sql
-- Tạo database
CREATE DATABASE ecommerce_dev;
CREATE DATABASE ecommerce_test;
CREATE DATABASE ecommerce_prod;
```

### 3. Build và Run

Xem hướng dẫn chi tiết: [docs/03-build-deploy.md](./docs/03-build-deploy.md)

**Windows (PowerShell):**
```powershell
# Build
.\scripts\build\build-dev.ps1

# Run
.\scripts\run\run-dev.ps1
```

**Linux/Mac (Bash):**
```bash
# Build
./scripts/build/build-dev.sh

# Run
./scripts/run/run-dev.sh
```

Application sẽ chạy tại: `http://localhost:5678`

## Tài liệu chi tiết

Tất cả tài liệu chi tiết được tổ chức trong thư mục `docs/`:

- **[01-setup.md](./docs/01-setup.md)** - Hướng dẫn cài đặt môi trường (JDK, Maven, PostgreSQL)
- **[02-database.md](./docs/02-database.md)** - Hướng dẫn setup database và migration
- **[03-build-deploy.md](./docs/03-build-deploy.md)** - Hướng dẫn build và deploy cho các môi trường
- **[04-pancake-integration.md](./docs/04-pancake-integration.md)** - Hướng dẫn tích hợp Pancake POS

## Cấu trúc Project

```
service-api/
├── docs/                    # Tài liệu chi tiết
│   ├── 01-setup.md
│   ├── 02-database.md
│   ├── 03-build-deploy.md
│   └── 04-pancake-integration.md
├── scripts/                 # Scripts tiện ích
│   ├── setup/               # Scripts setup môi trường
│   ├── build/               # Scripts build
│   └── run/                 # Scripts run
├── src/
│   ├── main/
│   │   ├── java/com/dragun/ecommerce/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── model/           # Entities và DTOs
│   │   │   ├── security/        # Security components
│   │   │   ├── exception/       # Exception handling
│   │   │   ├── integration/     # Third-party integrations
│   │   │   └── util/            # Utilities
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/    # Flyway migrations
│   └── test/                     # Test classes
└── pom.xml
```

## API Endpoints

### Public APIs (Không cần authentication)

#### Products
- `GET /api/thiyen/products/list` - Lấy danh sách sản phẩm
- `GET /api/thiyen/products/{id}` - Lấy thông tin sản phẩm
- `GET /api/thiyen/products/{id}/details` - Lấy chi tiết sản phẩm

#### Orders
- `POST /api/extend/orders` - Tạo đơn hàng mới
- `GET /api/extend/orders/{id}` - Lấy đơn hàng theo ID
- `GET /api/extend/orders/customer/{phone}` - Lấy đơn hàng theo số điện thoại
- `PUT /api/extend/orders/{id}/status` - Cập nhật trạng thái đơn hàng

#### Location
- `GET /provinces` - Lấy danh sách tỉnh/thành
- `GET /districts/{provinceCode}` - Lấy danh sách quận/huyện
- `GET /wards/{districtCode}` - Lấy danh sách phường/xã

### Admin APIs (Cần authentication)

#### Authentication
- `POST /api/thiyen/admin/login` - Đăng nhập admin
  ```json
  {
    "username": "admin",
    "password": "thiyen1"
  }
  ```

#### Products Management
- `GET /api/thiyen/admin/products` - Lấy danh sách sản phẩm (admin)
- `POST /api/thiyen/admin/products` - Tạo sản phẩm mới
- `PUT /api/thiyen/admin/products` - Cập nhật sản phẩm
- `DELETE /api/thiyen/admin/products/{id}` - Xóa sản phẩm

**Lưu ý**: Tất cả admin APIs (trừ login) cần JWT token trong header:
```
Authorization: Bearer <token>
```

## Response Format

Tất cả API responses theo format chuẩn:

```json
{
  "success": true,
  "data": {...},
  "message": "string"
}
```

## Authentication

### JWT Token

Sau khi đăng nhập thành công, bạn sẽ nhận được JWT token. Sử dụng token này trong header cho các request cần authentication:

```
Authorization: Bearer <your-jwt-token>
```

### Default Admin Account

- Username: `admin`
- Password: `thiyen1`

**Lưu ý**: Đổi mật khẩu mặc định trong môi trường production!

## Cấu hình Môi trường

### Development (DEV)
- **Database**: `ecommerce_dev`
- **Config**: `application-dev.yml`
- **Mặc định**: Profile này được active mặc định

### Test (TEST)
- **Database**: `ecommerce_test`
- **Config**: `application-test.yml`

### Production (PROD)
- **Database**: `ecommerce_prod`
- **Config**: `application-prod.yml`
- **BẮT BUỘC**: Set environment variables cho credentials

Xem chi tiết: [docs/03-build-deploy.md](./docs/03-build-deploy.md)

## CORS Configuration

API đã được cấu hình CORS để cho phép các origin sau:
- `http://localhost:3000`
- `http://localhost:5173`
- `https://debase.vn`
- `https://www.phodem.click`

## Testing

Chạy tests:

```bash
mvn test
```

Chạy tests với profile cụ thể:

```bash
mvn test -Dspring.profiles.active=dev
```

## Troubleshooting

### Lỗi kết nối database
- Kiểm tra PostgreSQL đã chạy chưa
- Kiểm tra thông tin database trong file config tương ứng
- Kiểm tra database đã được tạo chưa
- Xem chi tiết: [docs/02-database.md](./docs/02-database.md)

### Lỗi port đã được sử dụng
Thay đổi port trong `application.yml`:
```yaml
server:
  port: 5679
```

Hoặc dùng biến môi trường:
```bash
export PORT=5679
```

### Lỗi JWT
Kiểm tra JWT secret key trong `application.yml` hoặc biến môi trường `JWT_SECRET`

### Lỗi "release version 17 not supported"
- Đảm bảo đã cài JDK 17
- Chạy script setup Java trước khi build
- Xem chi tiết: [docs/01-setup.md](./docs/01-setup.md)

### Build failed
- Kiểm tra JDK 17 đã được cài đặt và cấu hình đúng
- Kiểm tra Maven version
- Xem log chi tiết: `mvn clean package -X`

## License

Private and proprietary.
