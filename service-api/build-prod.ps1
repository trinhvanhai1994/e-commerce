# Build script cho môi trường PRODUCTION
# Usage: .\build-prod.ps1

Write-Host "Building for PRODUCTION environment..." -ForegroundColor Red

mvn clean package -Pprod -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful! JAR file: target\ecommerce-api-1.0.0.jar" -ForegroundColor Green
    Write-Host "`nTo run the application:" -ForegroundColor Yellow
    Write-Host "  java -jar target\ecommerce-api-1.0.0.jar --spring.profiles.active=prod" -ForegroundColor Cyan
    Write-Host "`nIMPORTANT: Make sure to set JWT_SECRET environment variable for production!" -ForegroundColor Red
} else {
    Write-Host "`nBuild failed!" -ForegroundColor Red
    exit 1
}


