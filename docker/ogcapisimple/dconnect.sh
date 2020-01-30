# !/usr/bin/sh
echo "Docker connect to bash of OGC API Simple based on Tomcat Container"
cd ~/Documents/Computer/Docker/tomcat
docker exec -it ogcapisimple-tomcat /bin/bash
