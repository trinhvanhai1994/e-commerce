# Build script cho môi trường DEV
# Usage: .\build-dev.ps1

Write-Host "Building for DEV environment..." -ForegroundColor Green

mvn clean package -Pdev -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful! JAR file: target\ecommerce-api-1.0.0.jar" -ForegroundColor Green
    Write-Host "`nTo run the application:" -ForegroundColor Yellow
    Write-Host "  java -jar target\ecommerce-api-1.0.0.jar --spring.profiles.active=dev" -ForegroundColor Cyan
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
    exit 1
}


