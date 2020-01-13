# !/usr/bin/sh
clear
echo "Docker Build Image of PostGIS"
cd /home/sdidocker/pg
docker build -t kartoza/postgis .
