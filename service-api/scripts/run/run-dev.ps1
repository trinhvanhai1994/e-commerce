# Run script cho môi trường DEV
# Usage: .\run-dev.ps1
# Loads service-api/.env into process env before starting Spring Boot.

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ApiRoot = Resolve-Path (Join-Path $ScriptDir "..\..")
$EnvFile = Join-Path $ApiRoot ".env"

Write-Host "Running application in DEV mode..." -ForegroundColor Green

if (-not (Test-Path $EnvFile)) {
    Write-Host "Missing .env at $EnvFile" -ForegroundColor Red
    Write-Host "Copy .env.example to .env and fill in values." -ForegroundColor Yellow
    exit 1
}

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    $eq = $line.IndexOf("=")
    if ($eq -lt 1) {
        return
    }
    $name = $line.Substring(0, $eq).Trim()
    $value = $line.Substring($eq + 1).Trim()
    if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
        $value = $value.Substring(1, $value.Length - 2)
    }
    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}

Write-Host "Loaded env from $EnvFile" -ForegroundColor Cyan

Set-Location $ApiRoot
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
