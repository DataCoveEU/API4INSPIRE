# !/usr/bin/sh
clear
echo "Docker Build Image of OGC API Simple based on Tomcat"
cd ~/Documents/Computer/Docker/tomcat/
docker build -t tomcat:ogcapisimple .
