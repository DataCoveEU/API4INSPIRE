# !/usr/bin/sh
echo "Docker connect to bash of PostGIS container"
cd /home/sdidocker/pg
docker exec -it acgsdi-postgis /bin/bash