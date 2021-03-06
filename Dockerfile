FROM openjdk:11

VOLUME /tmp

EXPOSE 8080

ARG JAR_FILE

COPY ${JAR_FILE} docker-k8s-helm-demo.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","docker-k8s-helm-demo.jar"]
