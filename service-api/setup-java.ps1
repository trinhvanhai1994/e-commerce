# Script to set up JAVA_HOME for JDK 17 on Windows
# This script helps you switch between JDK 11 and JDK 17

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("11", "17")]
    [string]$Version = "17"
)

# Common JDK installation paths on Windows
$JDK11_PATHS = @(
    "C:\Program Files\Java\jdk-11",
    "C:\Program Files\Java\jdk-11.0.x",
    "C:\Program Files (x86)\Java\jdk-11",
    "$env:JAVA_HOME"  # Current JAVA_HOME if it's JDK 11
)

$JDK17_PATHS = @(
    "$env:USERPROFILE\.jdks\temurin-17.0.17",
    "$env:USERPROFILE\.jdks\jdk-17",
    "$env:USERPROFILE\.jdks\jdk-17.0.x",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-17.0.x",
    "C:\Program Files\Eclipse Adoptium\jdk-17",
    "C:\Program Files\Microsoft\jdk-17",
    "C:\Program Files\OpenJDK\jdk-17",
    "C:\Program Files (x86)\Java\jdk-17"
)

function Find-JDK {
    param([string]$Version)
    
    $searchPaths = if ($Version -eq "11") { $JDK11_PATHS } else { $JDK17_PATHS }
    
    foreach ($path in $searchPaths) {
        if ($path -and (Test-Path $path)) {
            $javaExe = Join-Path $path "bin\java.exe"
            if (Test-Path $javaExe) {
                return $path
            }
        }
    }
    
    # Try to find in Program Files with wildcard
    $programFilesPaths = @(
        "$env:USERPROFILE\.jdks",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\OpenJDK"
    )
    
    foreach ($basePath in $programFilesPaths) {
        if (Test-Path $basePath) {
            $jdkDirs = Get-ChildItem -Path $basePath -Directory -Filter "jdk-$Version*" -ErrorAction SilentlyContinue
            foreach ($jdkDir in $jdkDirs) {
                $javaExe = Join-Path $jdkDir.FullName "bin\java.exe"
                if (Test-Path $javaExe) {
                    return $jdkDir.FullName
                }
            }
        }
    }
    
    return $null
}

$jdkPath = Find-JDK -Version $Version

if ($null -eq $jdkPath) {
    Write-Host ""
    Write-Host "ERROR: JDK $Version not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please download and install JDK $Version from one of these sources:" -ForegroundColor Yellow
    Write-Host "  1. Eclipse Adoptium (Temurin): https://adoptium.net/temurin/releases/?version=$Version" -ForegroundColor Cyan
    Write-Host "  2. Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java$Version" -ForegroundColor Cyan
    Write-Host "  3. Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "After installation, run this script again." -ForegroundColor Yellow
    exit 1
}

# Set JAVA_HOME for current session
$env:JAVA_HOME = $jdkPath
$env:PATH = "$jdkPath\bin;$env:PATH"

Write-Host ""
Write-Host "SUCCESS: JAVA_HOME has been set for JDK $Version" -ForegroundColor Green
Write-Host "  Path: $jdkPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Java version:" -ForegroundColor Yellow
java -version

Write-Host ""
Write-Host "To set permanently for PowerShell:" -ForegroundColor Yellow
Write-Host "  1. Open PowerShell as Administrator" -ForegroundColor White
Write-Host "  2. Run the following command (change path if needed):" -ForegroundColor White
$setJAVA_HOME = "[System.Environment]::SetEnvironmentVariable('JAVA_HOME', '$jdkPath', 'User')"
Write-Host "     $setJAVA_HOME" -ForegroundColor Cyan
Write-Host "  3. Add to PATH (if not already added):" -ForegroundColor White
Write-Host "     `$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'User')" -ForegroundColor Cyan
$setPath = "[System.Environment]::SetEnvironmentVariable('Path', `$currentPath + ';$jdkPath\bin', 'User')"
Write-Host "     $setPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Or use this script each time you open a new terminal:" -ForegroundColor Yellow
Write-Host "  .\setup-java.ps1 -Version $Version" -ForegroundColor Cyan
