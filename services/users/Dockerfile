FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built jar
COPY build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/app.jar"]
