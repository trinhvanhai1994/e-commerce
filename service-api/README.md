# Thi Yen E-commerce API

Spring Boot REST API cho nền tảng thương mại điện tử Thi Yen.

## Công nghệ sử dụng

- **Java 17** (LTS)
- **Spring Boot 3.2.0** (ổn định, tương thích JDK 17)
- **PostgreSQL** (database)
- **Maven** (build tool)
- **Spring Security** với JWT authentication
- **Spring Data JPA** (ORM)
- **Flyway** (database migration)
- **OAuth2 Client** (chuẩn bị cho tích hợp bên thứ 3)

## Yêu cầu hệ thống

- JDK 17 hoặc cao hơn
- Maven 3.6+
- PostgreSQL 12+ hoặc cao hơn

## Cài đặt và chạy

### 1. Cài đặt PostgreSQL

Đảm bảo PostgreSQL đã được cài đặt và chạy trên máy của bạn.

### 2. Tạo database

```sql
CREATE DATABASE ecommerce;
```

### 3. Cấu hình database

Cấu hình database đã được thiết lập sẵn trong `application.yml` và `application-dev.yml`.

**Mặc định:**
- Database: `ecommerce`
- Username: `dragun`
- Password: `Picachu@123`

Bạn có thể override bằng biến môi trường:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=dragun
export SPRING_DATASOURCE_PASSWORD=Picachu@123
```

Hoặc tạo file `.env` và load nó (xem file `.env.example`).

### 4. Build project

#### Cách 1: Sử dụng build scripts (Khuyến nghị)

**Windows (PowerShell):**
```powershell
# Build cho DEV
.\build-dev.ps1

# Build cho TEST
.\build-test.ps1

# Build cho PRODUCTION
.\build-prod.ps1
```

**Linux/Mac (Bash):**
```bash
# Build cho DEV
./build-dev.sh

# Build cho TEST
./build-test.sh

# Build cho PRODUCTION
./build-prod.sh
```

#### Cách 2: Sử dụng Maven trực tiếp

```bash
# Build cho DEV (default)
mvn clean package -Pdev

# Build cho TEST
mvn clean package -Ptest

# Build cho PRODUCTION
mvn clean package -Pprod
```

### 5. Chạy application

#### Cách 1: Sử dụng run scripts (Khuyến nghị)

**Windows (PowerShell):**
```powershell
# Chạy DEV
.\run-dev.ps1

# Chạy TEST
.\run-test.ps1

# Chạy PRODUCTION
.\run-prod.ps1
```

**Linux/Mac (Bash):**
```bash
# Chạy DEV
./run-dev.sh

# Chạy TEST
./run-test.sh

# Chạy PRODUCTION
./run-prod.sh
```

#### Cách 2: Chạy JAR file đã build

```bash
# Chạy với profile DEV
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=dev

# Chạy với profile TEST
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=test

# Chạy với profile PRODUCTION
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

#### Cách 3: Sử dụng Maven Spring Boot plugin

```bash
# Chạy DEV
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Chạy TEST
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Chạy PRODUCTION
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Cách 4: Sử dụng biến môi trường

```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="test"
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=test
mvn spring-boot:run
```

Application sẽ chạy tại: `http://localhost:5678`

## Cấu trúc Project

```
service-api/
├── src/
│   ├── main/
│   │   ├── java/com/thiyen/ecommerce/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/          # Business logic
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── model/            # Entities và DTOs
│   │   │   ├── security/         # Security components
│   │   │   ├── exception/        # Exception handling
│   │   │   └── util/             # Utilities
│   │   └── resources/
│   │       ├── application.yml
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

## Database Migration

Project sử dụng Flyway để quản lý database schema. Các migration scripts nằm trong `src/main/resources/db/migration/`.

Flyway sẽ tự động chạy migrations khi application khởi động.

## Environment Variables

### Development
Sử dụng `application-dev.yml` (mặc định)

### Production
Sử dụng `application-prod.yml` với các biến môi trường:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecommerce_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000
export PORT=5678
```

## CORS Configuration

API đã được cấu hình CORS để cho phép các origin sau:
- `http://localhost:3000`
- `http://localhost:5173`
- `https://debase.vn`
- `https://www.phodem.click`

## OAuth2 Integration

Project đã được chuẩn bị sẵn cho việc tích hợp OAuth2 với các nhà cung cấp bên thứ 3 (Google, Facebook, GitHub, etc.).

Để kích hoạt OAuth2, cấu hình trong `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
```

## Testing

Chạy tests:

```bash
mvn test
```

## Build và Deploy cho từng môi trường

### Môi trường DEV
```bash
# Windows
.\build-dev.ps1

# Linux/Mac
./build-dev.sh
```

### Môi trường TEST
```bash
# Windows
.\build-test.ps1

# Linux/Mac
./build-test.sh
```

### Môi trường PRODUCTION
```bash
# Windows
.\build-prod.ps1

# Linux/Mac
./build-prod.sh
```

**Lưu ý quan trọng cho PRODUCTION:**
- Đảm bảo set biến môi trường `JWT_SECRET` trước khi chạy
- Không commit thông tin nhạy cảm vào code
- Sử dụng biến môi trường cho tất cả credentials

JAR file sẽ được tạo trong `target/ecommerce-api-1.0.0.jar` cho tất cả các môi trường.

## Troubleshooting

### Lỗi kết nối database
- Kiểm tra PostgreSQL đã chạy chưa
- Kiểm tra thông tin database trong `application-dev.yml`
- Kiểm tra database `ecommerce` đã được tạo chưa

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

## License

Private and proprietary.


