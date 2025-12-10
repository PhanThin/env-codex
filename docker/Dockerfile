# syntax=10.254.144.152/docker.io/docker/dockerfile:1
# Stage 1: JDK is required to provide compiler
FROM 10.254.144.152/docker.io/library/maven:3.9.9-eclipse-temurin-21-alpine  as build-libraries

COPY docker/mvn/settings.xml /root/.m2/
COPY docker/pom.xml .
COPY docker/libraries/pom.xml libraries/pom.xml

COPY docker/libraries/core/pom.xml libraries/core/pom.xml
COPY docker/libraries/core/src libraries/core/src

COPY docker/libraries/auth/pom.xml libraries/auth/pom.xml
COPY docker/libraries/auth/src libraries/auth/src

COPY docker/libraries/dto/pom.xml libraries/dto/pom.xml
COPY docker/libraries/dto/src libraries/dto/src

COPY docker/libraries/jpa/pom.xml libraries/jpa/pom.xml
COPY docker/libraries/jpa/src libraries/jpa/src

COPY docker/libraries/minio-client/pom.xml libraries/minio-client/pom.xml
COPY docker/libraries/minio-client/src libraries/minio-client/src

COPY docker/libraries/redis-client/pom.xml libraries/redis-client/pom.xml
COPY docker/libraries/redis-client/src libraries/redis-client/src

COPY docker/libraries/gen-code/pom.xml libraries/gen-code/pom.xml
COPY docker/libraries/gen-code/src libraries/gen-code/src

RUN mvn clean install -DskipTests

# Stage 2: JDK is required to provide compiler
FROM 10.254.144.152/docker.io/library/maven:3.9.9-eclipse-temurin-21-alpine as build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src
COPY --from=build-libraries /root/.m2 /root/.m2

# Every build where the source code changes is slow because the Maven cache has to be re-created
RUN mvn clean install -DskipTests

# Unpack Spring Boot fat JAR
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Stage 3
FROM 10.254.144.152/docker.io/library/eclipse-temurin:21-jre-alpine

# Create the log directory and set permissions
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/logs-backend && \
    chown -R spring:spring /app/logs-backend && \
    chmod -R 755 /app/logs-backend

USER spring:spring
ENV JAVA_OPTS=""

# /tmp (inside the container) will be a volume
# When running the container, a random numbers and characters named directory will be created in /var/lib/docker/volumes
#VOLUME /tmp

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -cp app:app/lib/* vn.com.viettel.ServiceApplication ${0} ${@}"]
