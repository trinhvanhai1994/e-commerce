# Run script cho môi trường PRODUCTION
# Usage: .\run-prod.ps1

Write-Host "Running application in PRODUCTION mode..." -ForegroundColor Red
Write-Host "WARNING: Make sure JWT_SECRET is set!" -ForegroundColor Yellow

mvn spring-boot:run -Dspring-boot.run.profiles=prod


