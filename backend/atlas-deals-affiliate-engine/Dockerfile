FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY backend/atlas-deals-common /build/backend/atlas-deals-common
COPY backend/atlas-deals-affiliate-engine /build/backend/atlas-deals-affiliate-engine
RUN apk add --no-cache maven && mvn -f /build/pom.xml clean package -pl backend/atlas-deals-affiliate-engine -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S atlas && adduser -S atlas -G atlas
USER atlas
WORKDIR /app
COPY --from=builder /build/backend/atlas-deals-affiliate-engine/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
