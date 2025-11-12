# Build script cho môi trường TEST
# Usage: .\build-test.ps1

Write-Host "Building for TEST environment..." -ForegroundColor Yellow

mvn clean package -Ptest -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful! JAR file: target\ecommerce-api-1.0.0.jar" -ForegroundColor Green
    Write-Host "`nTo run the application:" -ForegroundColor Yellow
    Write-Host "  java -jar target\ecommerce-api-1.0.0.jar --spring.profiles.active=test" -ForegroundColor Cyan
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
    exit 1
}


