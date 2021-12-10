FROM amazoncorretto:11.0.13

CMD ["./gradlew", "clean", "build"]

COPY ./build/libs/ethereum-wallet-system-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080