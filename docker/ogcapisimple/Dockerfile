ARG IMAGE_VERSION=8u232-jdk-stretch
FROM openjdk:$IMAGE_VERSION

#--------- OGC API Simple Environment settings ----------------
#test or prod
ARG ACG_SDI_ENV=dev
ARG TC_MAJOR=8
ARG TC_VER=8.5.51
ENV ACGSDI=${ACG_SDI_ENV}
ENV CATALINA_HOME="/opt/tc-${ACGSDI}"
#download links
ENV TOMCAT_MAJOR=$TC_MAJOR
ENV TOMCAT_VERSION=${TC_VER}
ENV TOMCAT_DOWNLOAD=http://mirror.klaus-uwe.me/apache/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz
#--------- Tomcat Base image for OGC API Simple ------------
LABEL maintainer="Klaus Gäbler <klaus.gaebler@austrocontrol.at>"
WORKDIR /opt
#ACGSDI usr configuration
RUN groupadd tomcat \
    && useradd -g tomcat -M donked

# TOMCAT
#download, unpack, remove unnecessary files
RUN wget $TOMCAT_DOWNLOAD \
    && mkdir tc-$ACGSDI \
    && tar xzf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /opt/tc-$ACGSDI --strip-components 1 \
    && rm apache-tomcat-$TOMCAT_VERSION.tar.gz \
    && cd tc-$ACGSDI \
    && rm -r webapps && mkdir webapps \
    && mkdir /opt/log \
    && mkdir /data && mkdir /data/ogcapisimple && mkdir /data/ogcapisimple/logs && mkdir /data/ogcapisimple/logs/tc

# directories /data, /data/ogcapisimple and /data/ogcapisimple/tc-logs are meant for mounting docker volumes to be able to store
# logs and other files

#--------- Tomcat Base configuration for OGC API Simple------------
ENV CATALINA_OUT=/data/ogcapisimple/logs/tc/catalina.out
ADD logging.properties /opt/tc-$ACGSDI/conf/logging.properties
ADD server.xml /opt/tc-$ACGSDI/conf/server.xml
ADD web.xml /opt/tc-$ACGSDI/conf/web.xml
ADD ogcapisimple.war /opt/tc-$ACGSDI/webapps/ogcapisimple.war

#--------- Tomcat Service config start for OGC API Simple------------
ADD scripts /opt/scripts
RUN chmod +x /opt/scripts/*.sh
# scripts should contain all required script files for final Tomcat and OGC API Simple
RUN chown --verbose --recursive donked:tomcat /opt/tc-${ACGSDI} \
    && chown --verbose --recursive donked:tomcat /data/ogcapisimple \
    && chown --verbose --recursive donked:tomcat /opt/log

USER donked
CMD ["/opt/scripts/entrypoint.sh"]
