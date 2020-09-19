FROM openjdk:14-alpine
COPY target/teambalance-*.jar teambalance.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "teambalance.jar"]