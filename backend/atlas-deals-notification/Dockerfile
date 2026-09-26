FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY backend/atlas-deals-common /build/backend/atlas-deals-common
COPY backend/atlas-deals-notification /build/backend/atlas-deals-notification
RUN apk add --no-cache maven && mvn -f /build/pom.xml clean package -pl backend/atlas-deals-notification -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S atlas && adduser -S atlas -G atlas
USER atlas
WORKDIR /app
COPY --from=builder /build/backend/atlas-deals-notification/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
