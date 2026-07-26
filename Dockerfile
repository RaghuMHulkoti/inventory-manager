# ─────────────────────────────────────────────────────────────
# Stage 1 — Build
#   Uses the full JDK to compile and package the application.
#   Dependencies are downloaded in a separate layer so Docker
#   can cache them and only re-download when pom.xml changes.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and POM first — lets Docker cache the
# dependency download layer independently of source changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download all dependencies offline (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source and build the fat jar (tests already run in CI)
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2 — Runtime
#   Uses the slim JRE image (no compiler toolchain) which is
#   smaller and has a smaller attack surface.
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Create a dedicated non-root user/group for the process.
# --no-create-home keeps the image lean.
RUN groupadd --system spring \
 && useradd  --system --gid spring --no-create-home spring

# Copy only the built jar from the build stage.
# chown ensures the non-root user can read it.
COPY --from=build --chown=spring:spring \
     /app/target/inventory-manager-*.jar app.jar

# Switch to non-root user (security best-practice)
USER spring

# The application listens on 8080
EXPOSE 8080

# -XX:MaxRAMPercentage=75.0 lets the JVM use up to 75% of the
# container's memory limit, avoiding OOM kills.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
