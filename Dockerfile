# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Tải trước dependencies để cache (giúp build các lần sau nhanh hơn)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# File jar sinh ra từ pom.xml của bạn sẽ có tên cf-manager-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/cf-manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]