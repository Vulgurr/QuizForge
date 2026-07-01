FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y maven

WORKDIR /app

# Copiamos todo de una
COPY backend/pom.xml .
COPY backend/src ./src

# La bala de plata: salteamos la compilación de los tests
# Compilamos y luego renombramos CUALQUIER .jar que se haya generado a "app.jar"
RUN mvn clean package -Dmaven.test.skip=true && mv target/*.jar target/app.jar

# Ahora siempre sabemos exactamente qué archivo ejecutar
ENTRYPOINT ["java", "-jar", "target/app.jar"]