# Run script cho môi trường TEST
# Usage: .\run-test.ps1

Write-Host "Running application in TEST mode..." -ForegroundColor Yellow

mvn spring-boot:run -Dspring-boot.run.profiles=test


