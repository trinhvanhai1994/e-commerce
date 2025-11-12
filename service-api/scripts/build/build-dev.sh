#!/bin/bash
# Build script cho môi trường DEV
# Usage: ./build-dev.sh

echo "Building for DEV environment..."

mvn clean package -Pdev -DskipTests

if [ $? -eq 0 ]; then
    echo -e "\nBuild successful! JAR file: target/ecommerce-api-1.0.0.jar"
    echo -e "\nTo run the application:"
    echo "  java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=dev"
else
    echo -e "\nBuild failed!"
    exit 1
fi


