FROM openjdk:14-alpine

RUN apk update && apk add bash curl busybox-extras
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein

COPY ./ /src
WORKDIR /src
RUN lein uberjar

#ADD target/pf-foundry-0.0.1-SNAPSHOT-standalone.jar /pf-foundry/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/src/target/pf-foundry-0.0.1-SNAPSHOT-standalone.jar"]
#CMD ["/bin/bash", "-c", "'sleep 1000'"]
