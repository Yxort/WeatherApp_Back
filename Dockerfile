# Этап сборки
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Копируем весь проект
COPY . .

# Собираем Spring Boot fat JAR
RUN gradle clean bootJar --no-daemon

# Этап runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Копируем готовый JAR из предыдущего этапа
COPY --from=build /app/build/libs/*.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java","-jar","app.jar"]
