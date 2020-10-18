ARG IMAGE_VERSION=8u265-jdk-slim
FROM openjdk:$IMAGE_VERSION

#--------- OGC API Simple Environment settings ----------------
#test or prod
ARG ACG_SDI_ENV=dev
ARG TC_MAJOR=8
ARG TC_VER=8.5.58
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

# in the OpenJDK Image "slim" wget and unzip are not included
RUN apt-get update \
    && apt-get install -y wget unzip

# TOMCAT
#download, unpack, remove unnecessary files
RUN wget $TOMCAT_DOWNLOAD \
    && mkdir tc-$ACGSDI \
    && tar xzf apache-tomcat-$TOMCAT_VERSION.tar.gz -C /opt/tc-$ACGSDI --strip-components 1 \
    && rm apache-tomcat-$TOMCAT_VERSION.tar.gz \
    && cd tc-$ACGSDI \
    && rm -r webapps && mkdir webapps \
    && mkdir /opt/log \
    && mkdir /data && mkdir /data/ogcapisimple && mkdir /data/ogcapisimple/logs && mkdir /data/ogcapisimple/logs/tc && mkdir /data/ogcapisimple/logs/ogcapisimple

# directories /data, /data/ogcapisimple and /data/ogcapisimple/tc-logs are meant for mounting docker volumes to be able to store
# logs and other files

#--------- Certificate for Tomcat https port 8443------------
# local generated certificate
# RUN keytool -genkey -alias tomcat -keyalg RSA -keypass changeit -storepass changeit -keystore /opt/keystore -dname "CN=validating_usr, OU=validating_usr_unit, O=validating_org, L=docker, ST=Austria, C=AT"

#import into JKS (destination type jkcs12) for tomcat
RUN keytool -genkey -alias tomcat -keyalg RSA -keypass changeit -storepass changeit -keystore /opt/keystore.jks -dname "CN=ogcapisimple_usr, OU=ogcapisimple_unit, O=ogcapisimple_org, L=docker, ST=Austria, C=AT"

#Warning:
#The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12
#which is an industry standard format using
#"keytool -importkeystore -srckeystore /opt/keystore -destkeystore /opt/keystore -deststoretype pkcs12".


#--------- Tomcat Base configuration for OGC API Simple------------
ENV CATALINA_OUT=/data/ogcapisimple/logs/tc/catalina.out
ADD logging.properties /opt/tc-$ACGSDI/conf/logging.properties
ADD server.xml /opt/tc-$ACGSDI/conf/server.xml
ADD web.xml /opt/tc-$ACGSDI/conf/web.xml
ADD ogcapisimple.war /opt/tc-$ACGSDI/webapps/ogcapisimple.war

#--------- Tomcat Service config start for OGC API Simple------------
ENV LOG_OGCAPISIMPLE=/data/ogcapisimple/logs/ogcapisimple
ADD scripts /opt/scripts
RUN chmod +x /opt/scripts/*.sh
# scripts should contain all required script files for final Tomcat and OGC API Simple
RUN chown --verbose --recursive donked:tomcat /opt/tc-${ACGSDI} \
    && chown --verbose --recursive donked:tomcat /data/ogcapisimple \
    && chown --verbose --recursive donked:tomcat /opt/log

USER donked
CMD ["/opt/scripts/entrypoint.sh"]
