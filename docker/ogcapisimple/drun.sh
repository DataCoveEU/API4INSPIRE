# !/usr/bin/sh
echo "Docker Run --> OGC API Simple based on Tomcat"
docker run -p 8086:8080 -d --name ogcapisimple-tomcat tomcat:ogcapisimple
