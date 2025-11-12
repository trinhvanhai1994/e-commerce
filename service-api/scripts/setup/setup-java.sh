#!/bin/bash
# Script to set up JAVA_HOME for JDK 17 on Windows (Git Bash)
# Usage: source setup-java.sh [11|17]
# Or: . setup-java.sh 17

VERSION=${1:-17}

# Convert Windows path to Git Bash path format
win_to_unix_path() {
    local win_path="$1"
    # Convert C:\Users\... to /c/Users/...
    echo "$win_path" | sed 's|^\([A-Za-z]\):|/\1|' | tr '\\' '/'
}

# Find JDK in Windows paths
find_jdk() {
    local version=$1
    local jdk_path=""
    
    # Get Windows-style USERPROFILE and convert to Git Bash path
    if [ -n "$USERPROFILE" ]; then
        # USERPROFILE is usually in Windows format (C:\Users\...), convert it
        local user_profile=$(echo "$USERPROFILE" | sed 's|^\([A-Za-z]\):|/\1|' | tr '\\' '/')
    else
        local user_profile="$HOME"
    fi
    
    # Common JDK installation paths on Windows (Git Bash format)
    local search_paths=(
        "$user_profile/.jdks/temurin-17.0.17"
        "$user_profile/.jdks/jdk-$version"
        "/c/Program Files/Java/jdk-$version"
        "/c/Program Files/Eclipse Adoptium/jdk-$version"
        "/c/Program Files/Microsoft/jdk-$version"
        "/c/Program Files/OpenJDK/jdk-$version"
        "/c/Program Files (x86)/Java/jdk-$version"
    )
    
    # Try exact paths first
    for path in "${search_paths[@]}"; do
        # Check if directory exists
        if [ -d "$path" ]; then
            # Check if java.exe exists (Windows) or java (Unix)
            if [ -f "$path/bin/java.exe" ] || [ -f "$path/bin/java" ]; then
                jdk_path="$path"
                break
            fi
        fi
    done
    
    # If not found, try to search in common directories
    if [ -z "$jdk_path" ]; then
        local base_dirs=(
            "$user_profile/.jdks"
            "/c/Program Files/Java"
            "/c/Program Files/Eclipse Adoptium"
            "/c/Program Files/Microsoft"
            "/c/Program Files/OpenJDK"
        )
        
        for base_dir in "${base_dirs[@]}"; do
            if [ -d "$base_dir" ]; then
                # Find directories matching jdk-$version*
                local found_dirs=$(find "$base_dir" -maxdepth 1 -type d -name "jdk-$version*" 2>/dev/null)
                for dir in $found_dirs; do
                    if [ -f "$dir/bin/java.exe" ] || [ -f "$dir/bin/java" ]; then
                        jdk_path="$dir"
                        break 2
                    fi
                done
            fi
        done
    fi
    
    echo "$jdk_path"
}

# Find JDK
JDK_PATH=$(find_jdk $VERSION)

if [ -z "$JDK_PATH" ]; then
    echo ""
    echo "ERROR: JDK $VERSION not found!"
    echo ""
    echo "Please download and install JDK $VERSION from one of these sources:"
    echo "  1. Eclipse Adoptium (Temurin): https://adoptium.net/temurin/releases/?version=$VERSION"
    echo "  2. Oracle JDK: https://www.oracle.com/java/technologies/downloads/#java$VERSION"
    echo "  3. Microsoft Build of OpenJDK: https://learn.microsoft.com/en-us/java/openjdk/download"
    echo ""
    echo "After installation, run this script again."
    return 1 2>/dev/null || exit 1
fi

# Convert Windows path to Unix-style path for Git Bash if needed
# Git Bash usually handles this automatically, but just in case
if [[ "$JDK_PATH" =~ ^[A-Za-z]: ]]; then
    # It's a Windows path (C:\), convert it
    JDK_PATH=$(win_to_unix_path "$JDK_PATH")
fi

# Set JAVA_HOME and PATH
export JAVA_HOME="$JDK_PATH"
export PATH="$JAVA_HOME/bin:$PATH"

echo ""
echo "SUCCESS: JAVA_HOME has been set for JDK $VERSION"
echo "  Path: $JAVA_HOME"
echo ""
echo "Java version:"
java -version 2>&1 | head -3

echo ""
echo "Maven version:"
mvn -version 2>&1 | head -3

echo ""
echo "To make this permanent, add these lines to your ~/.bashrc:"
echo "export JAVA_HOME=\"$JAVA_HOME\""
echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
