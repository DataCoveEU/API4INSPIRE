# !/usr/bin/sh
echo "Docker Run --> OGC API Simple based on Tomcat"
cd ~/Documents/Computer/Docker/tomcat/
docker run -p 8086:8080 -it --name ogcapisimple-tomcat tomcat:ogcapisimple
