# Hướng dẫn Build và Deploy

## Tổng quan

Project hỗ trợ 3 môi trường: **DEV**, **TEST**, và **PRODUCTION**. Mỗi môi trường có cấu hình riêng và kết nối đến database riêng.

## Quick Start

### Build

**Windows (PowerShell):**
```powershell
# DEV
.\scripts\build\build-dev.ps1

# TEST
.\scripts\build\build-test.ps1

# PRODUCTION
.\scripts\build\build-prod.ps1
```

**Linux/Mac (Bash):**
```bash
# DEV
./scripts/build/build-dev.sh

# TEST
./scripts/build/build-test.sh

# PRODUCTION
./scripts/build/build-prod.sh
```

### Run

**Windows (PowerShell):**
```powershell
# DEV
.\scripts\run\run-dev.ps1

# TEST
.\scripts\run\run-test.ps1

# PRODUCTION
.\scripts\run\run-prod.ps1
```

**Linux/Mac (Bash):**
```bash
# DEV
./scripts/run/run-dev.sh

# TEST
./scripts/run/run-test.sh

# PRODUCTION
./scripts/run/run-prod.sh
```

## Chi tiết Build

### Sử dụng Scripts (Khuyến nghị)

Scripts tự động:
- Clean project
- Build với profile tương ứng
- Skip tests (có thể sửa trong script nếu muốn chạy tests)
- Tạo JAR file trong `target/ecommerce-api-1.0.0.jar`

### Sử dụng Maven trực tiếp

```bash
# Build cho DEV (default)
mvn clean package -Pdev

# Build cho TEST
mvn clean package -Ptest

# Build cho PRODUCTION
mvn clean package -Pprod

# Build với tests
mvn clean package -Pdev

# Build và skip tests
mvn clean package -Pdev -DskipTests
```

## Chi tiết Run

### Sử dụng Scripts (Khuyến nghị)

Scripts tự động:
- Chạy với profile tương ứng
- Sử dụng Maven Spring Boot plugin

### Chạy JAR file đã build

```bash
# DEV
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=dev

# TEST
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=test

# PRODUCTION
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

### Sử dụng Maven Spring Boot plugin

```bash
# DEV
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# TEST
mvn spring-boot:run -Dspring-boot.run.profiles=test

# PRODUCTION
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Sử dụng biến môi trường

```bash
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE="test"
mvn spring-boot:run

# Linux/Mac
export SPRING_PROFILES_ACTIVE=test
mvn spring-boot:run
```

## Cấu hình Môi trường

### DEV Environment

- **Database**: `ecommerce_dev`
- **Username**: `dragun`
- **Password**: `Picachu@123`
- **Config file**: `application-dev.yml`
- **Mặc định**: Profile này được active mặc định

### TEST Environment

- **Database**: `ecommerce_test`
- **Username**: `postgres`
- **Password**: `123456`
- **Config file**: `application-test.yml`

### PRODUCTION Environment

- **Database**: `ecommerce_prod`
- **Username**: Từ environment variable
- **Password**: Từ environment variable
- **Config file**: `application-prod.yml`
- **BẮT BUỘC**: Set các biến môi trường:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET`

## Application URL

Sau khi chạy, application sẽ available tại:
- **URL**: `http://localhost:5678`
- **Port**: Có thể thay đổi trong `application.yml`

## Production Deployment

### Yêu cầu

1. **Database đã được setup** (xem [Database Setup](./02-database.md))
2. **Environment variables đã được set**
3. **JWT_SECRET đã được cấu hình**

### Bước 1: Build Production

```bash
# Windows
.\scripts\build\build-prod.ps1

# Linux/Mac
./scripts/build/build-prod.sh
```

### Bước 2: Set Environment Variables

```bash
# Windows PowerShell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://prod-server:5432/ecommerce_prod"
$env:SPRING_DATASOURCE_USERNAME="prod_user"
$env:SPRING_DATASOURCE_PASSWORD="secure_password"
$env:JWT_SECRET="your_jwt_secret_key"

# Linux/Mac
export SPRING_DATASOURCE_URL="jdbc:postgresql://prod-server:5432/ecommerce_prod"
export SPRING_DATASOURCE_USERNAME="prod_user"
export SPRING_DATASOURCE_PASSWORD="secure_password"
export JWT_SECRET="your_jwt_secret_key"
```

### Bước 3: Run Production

```bash
# Windows
.\scripts\run\run-prod.ps1

# Linux/Mac
./scripts/run/run-prod.sh

# Hoặc chạy JAR trực tiếp
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

### Bước 4: Run as Service (Linux)

Tạo systemd service file `/etc/systemd/system/ecommerce-api.service`:

```ini
[Unit]
Description=Thi Yen E-commerce API
After=network.target

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/service-api
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce_prod"
Environment="SPRING_DATASOURCE_USERNAME=prod_user"
Environment="SPRING_DATASOURCE_PASSWORD=secure_password"
Environment="JWT_SECRET=your_jwt_secret_key"
ExecStart=/usr/bin/java -jar /path/to/service-api/target/ecommerce-api-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable và start service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable ecommerce-api
sudo systemctl start ecommerce-api
sudo systemctl status ecommerce-api
```

## Testing

### Chạy tất cả tests

```bash
mvn test
```

### Chạy tests với profile cụ thể

```bash
mvn test -Dspring.profiles.active=dev
```

### Chạy test cụ thể

```bash
mvn test -Dtest=ClassName#methodName
```

## Troubleshooting

### Lỗi kết nối database
- Kiểm tra PostgreSQL đã chạy chưa
- Kiểm tra thông tin database trong file config tương ứng
- Kiểm tra database đã được tạo chưa

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

### Build failed
- Kiểm tra JDK 17 đã được cài đặt và cấu hình đúng
- Kiểm tra Maven version
- Xem log chi tiết: `mvn clean package -X`

## Lưu ý

- **DEV**: Mặc định, không cần set gì thêm
- **TEST**: Database riêng để test không ảnh hưởng đến dev
- **PRODUCTION**: **BẮT BUỘC** set environment variables, không hardcode credentials!

