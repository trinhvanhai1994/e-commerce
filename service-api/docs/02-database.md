# Hướng dẫn Setup Database

## Tổng quan

Project sử dụng PostgreSQL với Flyway để quản lý database migration. Mỗi môi trường (DEV, TEST, PROD) sử dụng database riêng biệt.

## Cấu hình Database theo Môi trường

| Môi trường | Database | Username | Password | File Config |
|-----------|----------|----------|----------|-------------|
| **DEV** | `ecommerce_dev` | `dragun` | `Picachu@123` | `application-dev.yml` |
| **TEST** | `ecommerce_test` | `postgres` | `123456` | `application-test.yml` |
| **PROD** | `ecommerce_prod` | (env var) | (env var) | `application-prod.yml` |

## 1. Tạo Database

### Sử dụng psql

```bash
# Kết nối PostgreSQL
psql -U postgres

# Tạo các database
CREATE DATABASE ecommerce_dev;
CREATE DATABASE ecommerce_test;
CREATE DATABASE ecommerce_prod;

# Thoát
\q
```

### Sử dụng SQL Script

```sql
-- Kết nối PostgreSQL
psql -U postgres

-- Tạo database cho DEV
CREATE DATABASE ecommerce_dev;

-- Tạo database cho TEST
CREATE DATABASE ecommerce_test;

-- Tạo database cho PRODUCTION
CREATE DATABASE ecommerce_prod;
```

## 2. Cấu hình User và Permissions

### DEV Environment

```sql
-- Tạo user (nếu chưa có)
CREATE USER dragun WITH PASSWORD 'Picachu@123';

-- Cấp quyền
GRANT ALL PRIVILEGES ON DATABASE ecommerce_dev TO dragun;
```

### TEST Environment

```sql
-- Sử dụng user postgres mặc định hoặc tạo user riêng
CREATE USER test_user WITH PASSWORD '123456';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_test TO test_user;
```

### PRODUCTION Environment

```sql
-- Tạo user riêng cho production (KHÔNG dùng default)
CREATE USER prod_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_prod TO prod_user;
```

## 3. Chạy Database Migration

### Tự động (Khuyến nghị)

Flyway sẽ tự động chạy migration khi bạn start Spring Boot application:

```bash
# DEV
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# TEST
mvn spring-boot:run -Dspring-boot.run.profiles=test

# PROD
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Thủ công qua Maven

```bash
# DEV
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_dev \
  -Dflyway.user=dragun \
  -Dflyway.password=Picachu@123

# TEST
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_test \
  -Dflyway.user=postgres \
  -Dflyway.password=123456

# PROD
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_prod \
  -Dflyway.user=prod_user \
  -Dflyway.password=your_password
```

### Các lệnh Flyway khác

```bash
# Xem thông tin migration
mvn flyway:info

# Xem lịch sử migration
mvn flyway:history

# Validate migration (kiểm tra tính hợp lệ)
mvn flyway:validate

# Repair migration (sửa lỗi checksum)
mvn flyway:repair

# Clean database (XÓA TẤT CẢ DỮ LIỆU - CẨN THẬN!)
mvn flyway:clean
```

## 4. Cấu hình Production với Environment Variables

**QUAN TRỌNG**: Production không nên hardcode credentials trong code!

### Windows PowerShell

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://prod-server:5432/ecommerce_prod"
$env:SPRING_DATASOURCE_USERNAME="prod_user"
$env:SPRING_DATASOURCE_PASSWORD="secure_password"
$env:JWT_SECRET="your_jwt_secret_key"

java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

### Linux/Mac

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://prod-server:5432/ecommerce_prod"
export SPRING_DATASOURCE_USERNAME="prod_user"
export SPRING_DATASOURCE_PASSWORD="secure_password"
export JWT_SECRET="your_jwt_secret_key"

java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

## 5. Kiểm tra Kết nối

Sau khi chạy application, kiểm tra log để xác nhận kết nối đúng database:

```
# DEV sẽ thấy:
HikariPool-1 - Starting... 
HikariPool-1 - Start completed.
Connected to: jdbc:postgresql://localhost:5432/ecommerce_dev

# TEST sẽ thấy:
Connected to: jdbc:postgresql://localhost:5432/ecommerce_test

# PROD sẽ thấy:
Connected to: jdbc:postgresql://prod-server:5432/ecommerce_prod
```

## 6. Cấu trúc Migration Files

Migration files nằm trong: `src/main/resources/db/migration/`

**Naming convention:** `V{version}__{description}.sql`

Ví dụ:
- `V1__Initial_schema.sql`
- `V2__Fix_districts_schema.sql`
- `V3__Fix_wards_schema.sql`
- `V4__Insert_initial_products.sql`
- `V5__Update_product_id_52_to_5.sql`

## Troubleshooting

### Lỗi: "Unable to connect to the database"

1. **Kiểm tra PostgreSQL đã chạy chưa:**
   ```bash
   # Windows
   net start postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl status postgresql
   ```

2. **Kiểm tra database đã tồn tại chưa:**
   ```sql
   psql -U postgres
   \l  -- Liệt kê tất cả databases
   ```

3. **Kiểm tra thông tin kết nối:**
   ```bash
   psql -h localhost -U postgres -d ecommerce_dev
   ```

### Lỗi: "Migration checksum mismatch"

Chạy repair để sửa:
```bash
mvn flyway:repair
```

### Reset database và chạy lại migration

```bash
# CẢNH BÁO: Xóa tất cả dữ liệu!
mvn flyway:clean
mvn flyway:migrate
```

## Lưu ý

1. **DEV**: Có thể dùng database `ecommerce` thay vì `ecommerce_dev` nếu muốn
2. **TEST**: Database riêng để test không ảnh hưởng đến dev
3. **PROD**: **BẮT BUỘC** phải set environment variables, không hardcode!

