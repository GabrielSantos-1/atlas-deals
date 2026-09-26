FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY backend/pom.xml /build/backend/pom.xml
COPY backend/atlas-deals-common /build/backend/atlas-deals-common
COPY backend/atlas-deals-image-generator /build/backend/atlas-deals-image-generator
RUN apk add --no-cache maven && mvn -f /build/pom.xml clean package -pl backend/atlas-deals-image-generator -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S atlas && adduser -S atlas -G atlas
USER atlas
WORKDIR /app
COPY --from=builder /build/backend/atlas-deals-image-generator/target/*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
