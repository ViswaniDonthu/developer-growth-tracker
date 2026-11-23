# ==============================================================
# Stage 1: Build Frontend (React + Vite)
# ==============================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ==============================================================
# Stage 2: Build Backend with embedded Frontend assets (Spring Boot)
# ==============================================================
FROM maven:3.9.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app/backend

# Cache dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy backend source
COPY backend/src ./src

# Copy built frontend into Spring Boot's static resources folder
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

# Build unified executable JAR
RUN mvn clean package -DskipTests

# ==============================================================
# Stage 3: Lightweight Production Runtime
# ==============================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=backend-builder /app/backend/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
