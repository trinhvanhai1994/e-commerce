### BUILD STAGE
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package spring-boot:repackage -DskipTests

### RUNTIME STAGE
FROM eclipse-temurin:17-jre

ENV APP_HOME=/app
WORKDIR $APP_HOME

RUN mkdir -p $APP_HOME/log /var/www/html/images/thiyen \
    && chmod -R 777 $APP_HOME /var/www/html/images/thiyen

EXPOSE 5678

COPY --from=builder /build/target/ecommerce-api-*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
