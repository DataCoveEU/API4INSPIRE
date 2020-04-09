# !/usr/bin/sh
echo "Docker Run of PostGIS"
docker run --name=acgsdi-postgis -d -t -e POSTGRES_USER=inspire -e POSTGRES_PASS=1nsp1r3_2#2# -e POSTGRES_DBNAME=inspire -e ALLOW_IP_RANGE=0.0.0.0/0 --network=postgisnet -p 25432:5432 kartoza/postgis
