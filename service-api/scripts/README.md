# Scripts Tiện ích

Thư mục này chứa các scripts tiện ích để build, run và setup môi trường cho project.

## 📁 Cấu trúc

```
scripts/
├── setup/              # Scripts setup môi trường (JDK)
├── build/              # Scripts build cho các môi trường
├── run/                # Scripts run cho các môi trường
├── copy-images.ps1    # Script copy ảnh từ service-ui sang service-api (Windows)
└── copy-images.sh     # Script copy ảnh từ service-ui sang service-api (Linux/Mac)
```

## 🔧 Setup Scripts

### Windows (PowerShell)

**setup-java-current-session.ps1** (Khuyến nghị)
```powershell
# Thiết lập JDK cho session hiện tại
. .\scripts\setup\setup-java-current-session.ps1 -Version 17
```

**setup-java.ps1**
```powershell
# Thiết lập JDK (chạy trong subprocess)
powershell -ExecutionPolicy Bypass -File .\scripts\setup\setup-java.ps1 -Version 17
```

**setup-java-simple.ps1**
```powershell
# Script đơn giản, chỉ hỗ trợ JDK 17
powershell -ExecutionPolicy Bypass -File .\scripts\setup\setup-java-simple.ps1
```

### Linux/Mac/Git Bash

**setup-java.sh**
```bash
# Thiết lập JDK cho session hiện tại
source ./scripts/setup/setup-java.sh 17
# Hoặc
. ./scripts/setup/setup-java.sh 17
```

## 🏗️ Build Scripts

### Windows (PowerShell)

```powershell
# DEV
.\scripts\build\build-dev.ps1

# TEST
.\scripts\build\build-test.ps1

# PRODUCTION
.\scripts\build\build-prod.ps1
```

### Linux/Mac (Bash)

```bash
# DEV
./scripts/build/build-dev.sh

# TEST
./scripts/build/build-test.sh

# PRODUCTION
./scripts/build/build-prod.sh
```

## ▶️ Run Scripts

### Windows (PowerShell)

```powershell
# DEV
.\scripts\run\run-dev.ps1

# TEST
.\scripts\run\run-test.ps1

# PRODUCTION
.\scripts\run\run-prod.ps1
```

### Linux/Mac (Bash)

```bash
# DEV
./scripts/run/run-dev.sh

# TEST
./scripts/run/run-test.sh

# PRODUCTION
./scripts/run/run-prod.sh
```

## 📸 Copy Images Script

Script này copy ảnh từ `service-ui/public/images/products` sang `service-api/src/main/resources/public/images/products` để server có thể đọc/ghi ảnh.

### Windows (PowerShell)

```powershell
.\scripts\copy-images.ps1
```

### Linux/Mac (Bash)

```bash
chmod +x ./scripts/copy-images.sh
./scripts/copy-images.sh
```

**Lưu ý**: Chạy script này sau khi thêm/sửa ảnh trong service-ui để đảm bảo server có ảnh mới nhất.

## 📝 Lưu ý

- Tất cả scripts đều hỗ trợ cả Windows và Linux/Mac
- Scripts build sẽ tạo JAR file trong `target/ecommerce-api-1.0.0.jar`
- Scripts run sử dụng Maven Spring Boot plugin
- Đảm bảo đã setup JDK 17 trước khi build/run
- Chạy `copy-images.ps1` hoặc `copy-images.sh` sau khi thêm/sửa ảnh trong service-ui

## 🔗 Liên kết

- [Tài liệu chi tiết](../docs/README.md)
- [README chính](../README.md)

