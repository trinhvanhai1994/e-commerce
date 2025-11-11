# Hướng dẫn chạy Flyway Migration

## Cách 1: Chạy migration qua Maven (Khuyến nghị)

### Với cấu hình mặc định (từ application-dev.yml):
```bash
cd service-api
mvn flyway:migrate
```

### Với cấu hình tùy chỉnh:
```bash
# Sử dụng biến môi trường hoặc properties
mvn flyway:migrate \
  -Dflyway.url=jdbc:postgresql://localhost:5432/ecommerce \
  -Dflyway.user=postgres \
  -Dflyway.password=123456
```

### Các lệnh Flyway khác:
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

## Cách 2: Chạy migration tự động khi start ứng dụng

Flyway sẽ tự động chạy migration khi bạn start Spring Boot application:

```bash
cd service-api
mvn spring-boot:run
```

Hoặc:

```bash
cd service-api
mvn clean install
java -jar target/ecommerce-api-1.0.0.jar
```

## Cấu hình Database

### Mặc định (application-dev.yml):
- **URL**: `jdbc:postgresql://localhost:5432/ecommerce`
- **Username**: `postgres`
- **Password**: `123456`

### Production (application.yml):
- **URL**: `jdbc:postgresql://localhost:5432/ecommerce`
- **Username**: `dragun`
- **Password**: `Picachu@123`

### Override bằng biến môi trường:
```bash
# Windows PowerShell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/ecommerce"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="123456"
mvn flyway:migrate

# Windows CMD
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=123456
mvn flyway:migrate

# Linux/Mac/Git Bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=123456
mvn flyway:migrate
```

## Troubleshooting

### Lỗi: "Unable to connect to the database"
1. Kiểm tra PostgreSQL đã chạy chưa:
   ```bash
   # Windows
   net start postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl status postgresql
   ```

2. Kiểm tra database đã tồn tại chưa:
   ```sql
   -- Kết nối PostgreSQL
   psql -U postgres
   
   -- Tạo database nếu chưa có
   CREATE DATABASE ecommerce;
   ```

3. Kiểm tra thông tin kết nối:
   ```bash
   # Test kết nối
   psql -h localhost -U postgres -d ecommerce
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

## Cấu trúc Migration Files

Migration files nằm trong: `src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

Ví dụ:
- `V1__Initial_schema.sql`
- `V2__Fix_districts_schema.sql`
- `V3__Fix_wards_schema.sql`
- `V4__Insert_initial_products.sql`
- `V5__Update_product_id_52_to_5.sql`

