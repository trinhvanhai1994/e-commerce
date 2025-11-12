# Hướng dẫn Cài đặt Môi trường

## Yêu cầu Hệ thống

- **JDK 17** hoặc cao hơn (LTS)
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Git** (để clone repository)

## 1. Cài đặt JDK 17

### Windows

#### Cách 1: Sử dụng Script Tự động (Khuyến nghị)

**PowerShell:**
```powershell
cd service-api
# Thiết lập cho session hiện tại (khuyến nghị)
. .\scripts\setup\setup-java-current-session.ps1 -Version 17

# Hoặc chạy script đơn giản
powershell -ExecutionPolicy Bypass -File .\scripts\setup\setup-java-simple.ps1
```

**Git Bash:**
```bash
cd service-api
source ./scripts/setup/setup-java.sh 17
```

#### Cách 2: Cài đặt Thủ công

1. **Tải JDK 17:**
   - Eclipse Adoptium (Temurin): https://adoptium.net/temurin/releases/?version=17
   - Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java17
   - Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download

2. **Cài đặt và thiết lập JAVA_HOME:**
   ```powershell
   # Thiết lập cho session hiện tại
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
   $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
   
   # Thiết lập vĩnh viễn (cần quyền Admin)
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot', 'User')
   ```

3. **Kiểm tra:**
   ```powershell
   java -version
   mvn -version
   ```

### Linux/Mac

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Mac (với Homebrew)
brew install openjdk@17

# Thiết lập JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Thêm vào ~/.bashrc hoặc ~/.zshrc để vĩnh viễn
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
```

## 2. Cài đặt Maven

### Windows

1. Tải Maven từ: https://maven.apache.org/download.cgi
2. Giải nén vào `C:\Program Files\Apache\maven`
3. Thêm vào PATH:
   ```powershell
   $env:PATH = "C:\Program Files\Apache\maven\bin;$env:PATH"
   ```

### Linux/Mac

```bash
# Ubuntu/Debian
sudo apt-get install maven

# Mac
brew install maven
```

## 3. Cài đặt PostgreSQL

### Windows

1. Tải từ: https://www.postgresql.org/download/windows/
2. Cài đặt và ghi nhớ password cho user `postgres`
3. Khởi động PostgreSQL service

### Linux

```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib

# Khởi động service
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### Mac

```bash
brew install postgresql@14
brew services start postgresql@14
```

## 4. Kiểm tra Cài đặt

```bash
# Kiểm tra Java
java -version
# Kết quả mong đợi: openjdk version "17.x.x"

# Kiểm tra Maven
mvn -version
# Kết quả mong đợi: Apache Maven 3.x.x

# Kiểm tra PostgreSQL
psql --version
# Kết quả mong đợi: psql (PostgreSQL) 12.x hoặc cao hơn
```

## 5. Chuyển đổi giữa JDK 11 và JDK 17

Nếu bạn cần làm việc với nhiều project sử dụng các phiên bản JDK khác nhau:

```powershell
# Chuyển sang JDK 17
. .\scripts\setup\setup-java-current-session.ps1 -Version 17

# Chuyển sang JDK 11
. .\scripts\setup\setup-java-current-session.ps1 -Version 11
```

## Troubleshooting

### Lỗi: "release version 17 not supported"
- Đảm bảo đã cài JDK 17
- Chạy script setup Java trước khi build
- Kiểm tra: `java -version` phải hiển thị version 17

### Lỗi: Script không tìm thấy JDK
- Kiểm tra đường dẫn cài đặt JDK 17
- Sửa script và thêm đường dẫn của bạn vào danh sách tìm kiếm

### Lỗi PowerShell Execution Policy
```powershell
# Giải pháp 1: Chạy với Bypass (không cần Admin)
powershell -ExecutionPolicy Bypass -File .\scripts\setup\setup-java.ps1

# Giải pháp 2: Thay đổi Execution Policy (cần Admin)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

