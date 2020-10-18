#!/usr/bin/sh
echo "Docker Run --> OGC API Simple based on Tomcat"
docker container rm -f ogcapisimple-tomcat
#docker run -p 8643:8443 -it --name ogcapisimple-tomcat tomcat:ogcapisimple
docker run -p 8443:8443 -d --name ogcapisimple-tomcat tomcat:ogcapisimple
