FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y maven

WORKDIR /app

# Copiamos todo de una
COPY backend/pom.xml .
COPY backend/src ./src

# La bala de plata: salteamos la compilación de los tests
RUN mvn clean package -Dmaven.test.skip=true

ENTRYPOINT ["java", "-jar", "target/quizforge-backend-0.0.1-SNAPSHOT.jar"]