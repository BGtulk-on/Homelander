FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy all files from schoolInventory directory
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew build -x test

EXPOSE 8082

# Вместо точното име, сложи това, за да хваща какъвто и да е JAR файл в папката
# Добавяме изрично да НЕ ползва -plain файла
ENTRYPOINT ["sh", "-c", "java -jar build/libs/*[!plain].jar"]