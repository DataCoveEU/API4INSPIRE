#!/bin/bash
set -e -x -v
#------------------# !/usr/bin/sh
# prepare environment for Tomcat 
#--------------------- Docker Container environment -------------------------------
echo  "----------->entrypoint.sh started ....";
echo  "----------->entrypoint.sh starting tomcat ....";
exec /opt/tc-${ACGSDI}/bin/catalina.sh run

