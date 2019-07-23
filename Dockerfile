FROM openjdk:11-jdk-slim
ARG jar
RUN groupadd -g 986 fwmtacceptancetests && \
     useradd -r -u 986 -g fwmtacceptancetests fwmtacceptancetests
USER fwmtacceptancetests
COPY $jar /opt/fwmtacceptancetests.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "java",  "-jar", "/opt/fwmtacceptancetests.jar" ]
