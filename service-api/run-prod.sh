#!/bin/bash
# Run script cho môi trường PRODUCTION
# Usage: ./run-prod.sh

echo "Running application in PRODUCTION mode..."
echo "WARNING: Make sure JWT_SECRET is set!"

mvn spring-boot:run -Dspring-boot.run.profiles=prod


