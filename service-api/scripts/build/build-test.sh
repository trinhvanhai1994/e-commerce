#!/bin/bash
# Build script cho môi trường TEST
# Usage: ./build-test.sh

echo "Building for TEST environment..."

mvn clean package -Ptest -DskipTests

if [ $? -eq 0 ]; then
    echo -e "\nBuild successful! JAR file: target/ecommerce-api-1.0.0.jar"
    echo -e "\nTo run the application:"
    echo "  java -jar target/ecommerce-api-1.0.0.jar --spring.profiles.active=test"
else
    echo -e "\nBuild failed!"
    exit 1
fi


