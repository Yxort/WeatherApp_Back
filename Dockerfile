# Используем официальный образ с Java 17
FROM eclipse-temurin:17-jdk-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем build/libs/*.jar в контейнер
COPY build/libs/*.jar app.jar

# Открываем порт 8080
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java","-jar","app.jar"]
