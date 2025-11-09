#!/bin/bash
# Script to set up JAVA_HOME for this project

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "JAVA_HOME set to: $JAVA_HOME"
echo "Java version:"
java -version

echo ""
echo "To make this permanent, add these lines to your ~/.bashrc:"
echo "export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
echo "export PATH=\$JAVA_HOME/bin:\$PATH"


