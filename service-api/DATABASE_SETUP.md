# Hướng dẫn Setup Database cho từng môi trường

## 📊 Cấu hình Database theo môi trường

Mỗi môi trường sẽ kết nối đến database riêng:

| Môi trường | Database | Username | Password | File config |
|-----------|----------|----------|----------|-------------|
| **DEV** | `ecommerce_dev` | `dragun` | `Picachu@123` | `application-dev.yml` |
| **TEST** | `ecommerce_test` | `postgres` | `123456` | `application-test.yml` |
| **PROD** | `ecommerce_prod` | (từ env) | (từ env) | `application-prod.yml` |

## 🗄️ Tạo Database

### PostgreSQL Command Line

```sql
-- Tạo database cho DEV
CREATE DATABASE ecommerce_dev;

-- Tạo database cho TEST
CREATE DATABASE ecommerce_test;

-- Tạo database cho PRODUCTION
CREATE DATABASE ecommerce_prod;
```

### Hoặc sử dụng psql

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

## 🔧 Cấu hình User và Permissions

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

## 🚀 Chạy Migration

Sau khi tạo database, chạy Flyway migration:

```bash
# DEV
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_dev -Dflyway.user=dragun -Dflyway.password=Picachu@123

# TEST
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_test -Dflyway.user=postgres -Dflyway.password=123456

# PROD
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce_prod -Dflyway.user=prod_user -Dflyway.password=your_password
```

## 🔐 Production - Sử dụng Environment Variables

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

## ✅ Kiểm tra kết nối

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

## 📝 Lưu ý

1. **DEV**: Có thể dùng database `ecommerce` thay vì `ecommerce_dev` nếu muốn
2. **TEST**: Database riêng để test không ảnh hưởng đến dev
3. **PROD**: **BẮT BUỘC** phải set environment variables, không hardcode!

