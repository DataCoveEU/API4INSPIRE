#!/usr/bin/sh
echo "Docker Run of pgweb"

# create the network with: docker network create postgisnet
docker run --name=pgweb -d --network=postgisnet -p 8090:8081 sosedoff/pgweb
#connect on the gui with
#Type: Standard
#HOST: IP of the docker container holding the postgreSQL DB
#Port: 5432
#Username: docker
#PWD: docker
#DB: gis
#SSL Mode: disable
