#!/bin/bash
# Build script cho môi trường PRODUCTION
# Usage: ./build-prod.sh

echo "Building for PRODUCTION environment..."

mvn clean package -Pprod -DskipTests

if [ $? -eq 0 ]; then
    echo -e "\nBuild successful! JAR file: target/ecommerce-api-1.0.0.jar"
    echo -e "\nTo run the application:"
    echo "  java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=prod"
    echo -e "\nIMPORTANT: Make sure to set JWT_SECRET environment variable for production!"
else
    echo -e "\nBuild failed!"
    exit 1
fi


