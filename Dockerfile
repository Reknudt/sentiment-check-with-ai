FROM openjdk:25-ea-jdk-slim
WORKDIR /app
#COPY .env.properties env.properties
COPY target/sentiment-analysis-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]