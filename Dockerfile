FROM openjdk:21-slim AS base

FROM base AS app_builder
WORKDIR /app
COPY . .
RUN ./gradlew build

FROM base AS docker_builder
WORKDIR /app
COPY --from=app_builder /app/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM base AS runtime

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

WORKDIR /app

COPY --from=docker_builder /app/dependencies/ ./
COPY --from=docker_builder /app/spring-boot-loader/ ./
COPY --from=docker_builder /app/snapshot-dependencies/ ./
COPY --from=docker_builder /app/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
