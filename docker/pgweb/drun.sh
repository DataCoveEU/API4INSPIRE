# !/usr/bin/sh
echo "Docker Run of pgweb"
cd /home/sdidocker/pgweb
#if started from the local registry
#docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -v /home/sdidocker/elk/data/:/opt/data -it --name elk localhost:5000/elk_sebp:latest
#docker run -p 8081:8080 -t -d --name acgsdi-geoserver localhost:5000/geoserver:acgsdi
#docker run -p 8081:8080 -it --name acgsdi-geoserver localhost:5000/geoserver:acgsdi
#docker run -p 8082:8080 -it --name acgsdi-geonetwork-test localhost:5000/geonetwork:acgsdi-test

#docker run --name "postgis" -e POSTGRES_PASS=postgres -p 25432:5432 -d -t kartoza/postgis
#docker run --name=postgis -d -e POSTGRES_USER=alex -e POSTGRES_PASS=password -e POSTGRES_DBNAME=gis -e ALLOW_IP_RANGE=0.0.0.0/0 -p 5432:5432 -v pg_data:/var/lib/postgresql --restart=always kartoza/postgis:9.6-2.4
#docker run --name=postgis -d -t -e POSTGRES_USER=docker -e POSTGRES_PASS=docker -e POSTGRES_DBNAME=gis -e ALLOW_IP_RANGE=0.0.0.0/0 -p 25432:5432 -v pg_data:/var/lib/postgresql kartoza/postgis

# create the network with: docker network create postgisnet
docker run --name=pgweb --network=postgisnet -p 8090:8081 sosedoff/pgweb
#connect on the gui with
#Type: Standard
#HOST: IP of the docker container holding the postgreSQL DB
#Port: 5432
#Username: docker
#PWD: docker
#DB: gis
#SSL Mode: disable
