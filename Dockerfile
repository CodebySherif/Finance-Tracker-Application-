# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy only the POM first so dependency downloads are cached
# as their own Docker layer and skipped on later rebuilds unless
# pom.xml itself changes.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the rest of the source (backend + static frontend) and build.
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /build/target/finance-tracker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
