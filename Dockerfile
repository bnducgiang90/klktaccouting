#FROM openjdk:17-jdk-alpine
#COPY target/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
#EXPOSE 8080

FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Copy JAR
COPY --from=build /build/target/*.jar app.jar

# Run as non-root user
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
  "-Xmx450m", \
  "-Xms256m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=85.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=100", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.port=${PORT}", \
  "-jar", \
  "app.jar"]


## Kiểm tra logs

#Sau khi deploy, vào Render Dashboard → Logs để xem quá trình build và run:
#```
#==> Building Docker image
#==> Pushing Docker image
#==> Starting service
#==> Your service is live 🎉