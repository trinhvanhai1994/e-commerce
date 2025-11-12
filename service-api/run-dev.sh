#!/bin/bash
# Run script cho môi trường DEV
# Usage: ./run-dev.sh

echo "Running application in DEV mode..."

mvn spring-boot:run -Dspring-boot.run.profiles=dev


