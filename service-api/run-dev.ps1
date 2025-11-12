# Run script cho môi trường DEV
# Usage: .\run-dev.ps1

Write-Host "Running application in DEV mode..." -ForegroundColor Green

mvn spring-boot:run -Dspring-boot.run.profiles=dev


