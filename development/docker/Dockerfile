FROM java:8-jdk-alpine
RUN mkdir /usr/app
COPY ./target/development-0.0.1.war /usr/app/
WORKDIR /usr/app/
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "development-0.0.1.war" ]