# Tài liệu Dự án

Thư mục này chứa tất cả tài liệu chi tiết về dự án Thi Yen E-commerce API.

## 📚 Mục lục Tài liệu

### 1. [Cài đặt Môi trường](./01-setup.md)
Hướng dẫn cài đặt và cấu hình môi trường phát triển:
- Cài đặt JDK 17
- Cài đặt Maven
- Cài đặt PostgreSQL
- Thiết lập JAVA_HOME
- Troubleshooting

### 2. [Setup Database](./02-database.md)
Hướng dẫn setup database và migration:
- Tạo database cho các môi trường
- Cấu hình user và permissions
- Chạy Flyway migration
- Cấu hình production với environment variables
- Troubleshooting

### 3. [Build và Deploy](./03-build-deploy.md)
Hướng dẫn build và deploy cho các môi trường:
- Build cho DEV, TEST, PRODUCTION
- Run application
- Deploy production
- Testing
- Troubleshooting

### 4. [Tích hợp Pancake POS](./04-pancake-integration.md)
Hướng dẫn tích hợp với Pancake POS:
- Lấy API Key và Warehouse ID
- Cấu hình integration
- Test kết nối
- Chạy test tích hợp
- Troubleshooting

### 5. [Tích hợp MISA MeInvoice (Thiyen)](./05-meinvoice-integration-conclusion.md)
Luồng V1 nháp + V2 phát hành (HSM/MTT), cấu hình `application-*.yml`, API admin, đối chiếu MISA UI. Spec gốc: [V1](./Tài%20liệu%20API%20Tạp%20hóa%20đơn%20Misa.md) · [V2](./Tài%20liệu%20API%20Tạo%20hóa%20đơn%20Misa_V2.md).

### 6. [Báo cáo đồ án Lập trình Java](./BAO-CAO-DO-AN-LAP-TRINH-JAVA.md)
Mẫu báo cáo đồ án (Markdown chuẩn Pandoc → DOCX, font Times New Roman 13pt, line spacing 1.5) — nộp môn Java.

## 🚀 Quick Start

Nếu bạn mới bắt đầu, hãy làm theo thứ tự:

1. **[Cài đặt Môi trường](./01-setup.md)** - Cài đặt JDK, Maven, PostgreSQL
2. **[Setup Database](./02-database.md)** - Tạo database và chạy migration
3. **[Build và Deploy](./03-build-deploy.md)** - Build và chạy application

## 📝 Lưu ý

- Tất cả các scripts tiện ích nằm trong thư mục `scripts/`
- Cấu hình application nằm trong `src/main/resources/application*.yml`
- Migration files nằm trong `src/main/resources/db/migration/`

## 🔗 Liên kết

- [README chính](../README.md) - Tổng quan về project
- [Scripts](../scripts/) - Scripts tiện ích

