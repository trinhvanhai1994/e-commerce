# Quick Setup Guide

## Setting JAVA_HOME

### Option 1: For Current Terminal Session
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### Option 2: Reload Your Shell Configuration
After adding JAVA_HOME to ~/.bashrc, reload it:
```bash
source ~/.bashrc
```

### Option 3: Use the Setup Script
```bash
source setup-java.sh
```

## Verify Setup
```bash
echo $JAVA_HOME
java -version
mvn -version
```

## Build Project
```bash
cd service-api
mvn clean install
```

## Run Application
```bash
cd service-api
mvn spring-boot:run
```


