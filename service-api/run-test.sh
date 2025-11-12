#!/bin/bash
# Run script cho môi trường TEST
# Usage: ./run-test.sh

echo "Running application in TEST mode..."

mvn spring-boot:run -Dspring-boot.run.profiles=test


