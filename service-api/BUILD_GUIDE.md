# Hướng dẫn Build và Run cho từng môi trường

## 🚀 Build nhanh

### Windows (PowerShell)
```powershell
# DEV
.\build-dev.ps1

# TEST  
.\build-test.ps1

# PRODUCTION
.\build-prod.ps1
```

### Linux/Mac (Bash)
```bash
# DEV
./build-dev.sh

# TEST
./build-test.sh

# PRODUCTION
./build-prod.sh
```

## ▶️ Run nhanh

### Windows (PowerShell)
```powershell
# DEV
.\run-dev.ps1

# TEST
.\run-test.ps1

# PRODUCTION
.\run-prod.ps1
```

### Linux/Mac (Bash)
```bash
# DEV
./run-dev.sh

# TEST
./run-test.sh

# PRODUCTION
./run-prod.sh
```

## 📦 Chạy JAR đã build

```bash
# DEV
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=dev

# TEST
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=test

# PRODUCTION
java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod
```

## 🔧 Sử dụng Maven trực tiếp

```bash
# Build
mvn clean package -Pdev    # DEV
mvn clean package -Ptest    # TEST
mvn clean package -Pprod    # PRODUCTION

# Run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=test
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🌍 Sử dụng biến môi trường

### Windows PowerShell
```powershell
$env:SPRING_PROFILES_ACTIVE="test"
mvn spring-boot:run
```

### Linux/Mac
```bash
export SPRING_PROFILES_ACTIVE=test
mvn spring-boot:run
```

## 📊 Database cho từng môi trường

Mỗi môi trường kết nối đến database riêng:

| Môi trường | Database | Username | Password |
|-----------|----------|----------|----------|
| **DEV** | `ecommerce_dev` | `dragun` | `Picachu@123` |
| **TEST** | `ecommerce_test` | `postgres` | `123456` |
| **PROD** | `ecommerce_prod` | (env var) | (env var) |

**Lưu ý**: Cần tạo các database trước khi chạy. Xem `DATABASE_SETUP.md` để biết chi tiết.

## 📝 Lưu ý

- **DEV**: 
  - Database: `ecommerce_dev`
  - Mặc định, không cần set gì thêm
- **TEST**: 
  - Database: `ecommerce_test`
  - Username/password: `postgres/123456`
- **PRODUCTION**: 
  - Database: `ecommerce_prod`
  - **BẮT BUỘC** set environment variables:
    - `SPRING_DATASOURCE_URL`
    - `SPRING_DATASOURCE_USERNAME`
    - `SPRING_DATASOURCE_PASSWORD`
    - `JWT_SECRET`

## 🎯 Không cần sửa code!

Tất cả cấu hình đã được tách riêng trong các file:
- `application.yml` - Cấu hình chung (không có datasource)
- `application-dev.yml` - Database và cấu hình DEV
- `application-test.yml` - Database và cấu hình TEST  
- `application-prod.yml` - Database và cấu hình PRODUCTION

**Khi active môi trường nào, sẽ tự động connect database môi trường đó!** 🎉

Xem thêm: `DATABASE_SETUP.md` để biết cách setup database.


