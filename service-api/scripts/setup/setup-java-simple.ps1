# Simple script to set JAVA_HOME for JDK 17
# Run this with: powershell -ExecutionPolicy Bypass -File .\setup-java-simple.ps1

$Version = "17"

# Common JDK 17 paths
$JDK17_PATHS = @(
    "$env:USERPROFILE\.jdks\temurin-17.0.17",
    "$env:USERPROFILE\.jdks\jdk-17",
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Eclipse Adoptium\jdk-17",
    "C:\Program Files\Microsoft\jdk-17",
    "C:\Program Files\OpenJDK\jdk-17"
)

$jdkPath = $null

# Try exact paths first
foreach ($path in $JDK17_PATHS) {
    if (Test-Path $path) {
        $javaExe = Join-Path $path "bin\java.exe"
        if (Test-Path $javaExe) {
            $jdkPath = $path
            break
        }
    }
}

# Try to find with wildcard
if ($null -eq $jdkPath) {
    $searchPaths = @(
        "$env:USERPROFILE\.jdks",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft",
        "C:\Program Files\OpenJDK"
    )
    
    foreach ($basePath in $searchPaths) {
        if (Test-Path $basePath) {
            $jdkDirs = Get-ChildItem -Path $basePath -Directory -Filter "jdk-17*" -ErrorAction SilentlyContinue
            foreach ($jdkDir in $jdkDirs) {
                $javaExe = Join-Path $jdkDir.FullName "bin\java.exe"
                if (Test-Path $javaExe) {
                    $jdkPath = $jdkDir.FullName
                    break
                }
            }
            if ($jdkPath) { break }
        }
    }
}

if ($null -eq $jdkPath) {
    Write-Host "JDK 17 not found! Please install JDK 17 first." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Yellow
    exit 1
}

# Set JAVA_HOME for current session
$env:JAVA_HOME = $jdkPath
$env:PATH = "$jdkPath\bin;$env:PATH"

Write-Host "JAVA_HOME set to: $jdkPath" -ForegroundColor Green
Write-Host "Java version:" -ForegroundColor Yellow
java -version

