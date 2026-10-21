FROM eclipse-temurin:latest
ADD /target/pharmase-monolith.jar /pharmase-monolith.jar
ENTRYPOINT ["java", "-jar", "/pharmase-monolith.jar"]