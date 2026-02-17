FROM eclipse-temurin:17-jdk-alpine-3.22

RUN ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

COPY ./build/libs/daruda-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
