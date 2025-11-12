# Script to set JAVA_HOME for JDK 17 in current PowerShell session
# Usage: . .\setup-java-current-session.ps1
# Note: The dot (.) at the beginning is important to run in current session

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("11", "17")]
    [string]$Version = "17"
)

# Common JDK installation paths on Windows
$JDK11_PATHS = @(
    "$env:USERPROFILE\.jdks\jdk-11",
    "C:\Program Files\Java\jdk-11",
    "C:\Program Files\Java\jdk-11.0.x",
    "C:\Program Files (x86)\Java\jdk-11"
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
    return
}

# Set JAVA_HOME for current session (this will persist in the current shell)
$env:JAVA_HOME = $jdkPath
# Remove old Java from PATH and add new one
$env:PATH = "$jdkPath\bin;" + ($env:PATH -replace '[^;]*java[^;]*;?', '')

Write-Host ""
Write-Host "SUCCESS: JAVA_HOME has been set for JDK $Version in current session" -ForegroundColor Green
Write-Host "  Path: $jdkPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Java version:" -ForegroundColor Yellow
java -version
Write-Host ""
Write-Host "Maven version:" -ForegroundColor Yellow
mvn -version 2>&1 | Select-Object -First 3

