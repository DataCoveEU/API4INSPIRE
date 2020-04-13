#!/usr/bin/sh
clear
echo "Docker Build Image of OGC API Simple based on Tomcat"
docker build -t tomcat:ogcapisimple .
