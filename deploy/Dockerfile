FROM frolvlad/alpine-oraclejdk8

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH

RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

ENV TOMCAT_MAJOR 8
ENV TOMCAT_VERSION 8.5.34
ENV TOMCAT_TGZ_URL http://apache.mirrors.ovh.net/ftp.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz

RUN apk add curl

RUN set -x && curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz \
    && tar -xvf tomcat.tar.gz --strip-components=1 \
    && rm bin/*.bat \
    && rm tomcat.tar.gz*

RUN apk del curl

RUN rm $CATALINA_HOME/webapps/* -rf

ADD ./ROOT $CATALINA_HOME/webapps/ROOT

EXPOSE 8080
CMD ["catalina.sh", "run"]

