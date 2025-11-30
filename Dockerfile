FROM clojure:temurin-17-tools-deps-alpine AS builder

WORKDIR /app
COPY . .

RUN clj -T:build uber

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar api.jar

CMD ["java", "-jar", "api.jar"]
