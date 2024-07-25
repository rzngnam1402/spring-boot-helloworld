FROM openjdk:17-oracle

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
