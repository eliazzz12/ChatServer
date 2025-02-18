FROM amazoncorretto:23
COPY . /app
EXPOSE 10101
WORKDIR /app/out/artifacts/ChatServer_jar
CMD java -jar ChatServer.jar