# Hướng dẫn cài đặt JDK 17 trên Windows

## Bước 1: Tải và cài đặt JDK 17

### Tùy chọn 1: Eclipse Adoptium (Temurin) - Khuyến nghị
1. Truy cập: https://adoptium.net/temurin/releases/?version=17
2. Chọn:
   - **Operating System**: Windows
   - **Architecture**: x64
   - **Package Type**: JDK
3. Tải file `.msi` và cài đặt
4. Ghi nhớ đường dẫn cài đặt (thường là `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`)

### Tùy chọn 2: Oracle JDK
1. Truy cập: https://www.oracle.com/java/technologies/downloads/#java17
2. Tải Windows x64 Installer
3. Cài đặt và ghi nhớ đường dẫn

### Tùy chọn 3: Microsoft Build of OpenJDK
1. Truy cập: https://learn.microsoft.com/en-us/java/openjdk/download
2. Tải JDK 17 cho Windows
3. Cài đặt và ghi nhớ đường dẫn

## Bước 2: Sử dụng script tự động

### ⚠️ Lưu ý về Execution Policy

Nếu gặp lỗi "running scripts is disabled", bạn có 3 cách giải quyết:

#### Giải pháp 1: Chạy với Bypass (Khuyến nghị - Không cần quyền Admin)
```powershell
cd service-api
powershell -ExecutionPolicy Bypass -File .\setup-java.ps1 -Version 17
```

#### Giải pháp 2: Sử dụng script đơn giản
```powershell
cd service-api
powershell -ExecutionPolicy Bypass -File .\setup-java-simple.ps1
```

#### Giải pháp 3: Chạy trong cùng session (Khuyến nghị cho build)
```powershell
cd service-api
. .\setup-java-current-session.ps1 -Version 17
# Lưu ý: Dấu chấm (.) ở đầu là quan trọng để chạy trong session hiện tại
# Sau đó bạn có thể chạy mvn ngay trong cùng terminal này
```

#### Giải pháp 4: Thay đổi Execution Policy (Cần quyền Admin)
```powershell
# Mở PowerShell với quyền Administrator, sau đó chạy:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Cách 1: Sử dụng script trong cùng session

#### Nếu dùng PowerShell:
```powershell
# Chuyển đến thư mục service-api
cd service-api

# Thiết lập JDK 17 cho session hiện tại (dấu chấm ở đầu là quan trọng!)
. .\setup-java-current-session.ps1 -Version 17

# Kiểm tra version
java -version
mvn -version

# Build project
mvn clean install
```

#### Nếu dùng Git Bash (MINGW):
```bash
# Chuyển đến thư mục service-api
cd service-api

# Thiết lập JDK 17 cho session hiện tại (dấu chấm ở đầu là quan trọng!)
. ./setup-java.sh 17
# Hoặc: source ./setup-java.sh 17

# Kiểm tra version
java -version
mvn -version

# Build project
mvn clean install
```

### Cách 2: Sử dụng script PowerShell (Sau khi xử lý Execution Policy)
```powershell
# Chuyển đến thư mục service-api
cd service-api

# Thiết lập JDK 17 (chạy trong subprocess)
powershell -ExecutionPolicy Bypass -File .\setup-java.ps1 -Version 17

# Lưu ý: JAVA_HOME chỉ có hiệu lực trong subprocess, không ảnh hưởng đến shell hiện tại
# Để sử dụng trong shell hiện tại, dùng Cách 1 hoặc thiết lập thủ công
```

### Cách 3: Thiết lập thủ công cho session hiện tại
```powershell
# Thay đổi đường dẫn theo nơi bạn cài JDK 17
# Ví dụ với JDK trong thư mục .jdks (IntelliJ IDEA):
$env:JAVA_HOME = "C:\Users\haitv\.jdks\temurin-17.0.17"
# Hoặc với JDK cài đặt thông thường:
# $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.9+9-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Kiểm tra
java -version
```

## Bước 3: Thiết lập vĩnh viễn (Tùy chọn)

### Thiết lập JAVA_HOME vĩnh viễn
```powershell
# Mở PowerShell với quyền Administrator
# Thay đổi đường dẫn theo nơi bạn cài JDK 17
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.0.9+9-hotspot', 'User')

# Thêm vào PATH
$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'User')
$jdkBinPath = "$env:JAVA_HOME\bin"
if ($currentPath -notlike "*$jdkBinPath*") {
    [System.Environment]::SetEnvironmentVariable('Path', "$currentPath;$jdkBinPath", 'User')
}
```

### Hoặc sử dụng GUI
1. Nhấn `Win + R`, gõ `sysdm.cpl`, nhấn Enter
2. Chọn tab **Advanced** → **Environment Variables**
3. Trong **User variables**, tạo hoặc sửa biến `JAVA_HOME`:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.0.9+9-hotspot` (thay đổi theo đường dẫn của bạn)
4. Tìm biến `Path` trong User variables, chọn **Edit**
5. Thêm mới: `%JAVA_HOME%\bin`
6. Nhấn **OK** để lưu
7. **Đóng và mở lại PowerShell** để áp dụng thay đổi

## Bước 4: Chuyển đổi giữa JDK 11 và JDK 17

### Sử dụng script
```powershell
# Chuyển sang JDK 17
.\setup-java.ps1 -Version 17

# Chuyển sang JDK 11
.\setup-java.ps1 -Version 11
```

### Kiểm tra version hiện tại
```powershell
java -version
echo $env:JAVA_HOME
```

## Bước 5: Build và chạy project

```Bash
# Đảm bảo đang dùng JDK 17
. ./setup-java.sh 17

# Build project
mvn clean install

# Chạy application
mvn spring-boot:run
```

## Troubleshooting

### Lỗi: "release version 17 not supported"
- Đảm bảo đã cài JDK 17
- Chạy `.\setup-java.ps1 -Version 17` trước khi build
- Kiểm tra: `java -version` phải hiển thị version 17

### Lỗi: Script không tìm thấy JDK
- Kiểm tra đường dẫn cài đặt JDK 17
- Sửa script `setup-java.ps1` và thêm đường dẫn của bạn vào mảng `$JDK17_PATHS`

### Muốn giữ JDK 11 làm mặc định
- Không thiết lập JAVA_HOME vĩnh viễn
- Chỉ chạy `.\setup-java.ps1 -Version 17` khi cần build project này

