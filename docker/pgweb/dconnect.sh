# !/usr/bin/sh
echo "Docker connect to bash of pgweb container"
cd /home/sdidocker/pgweb
#if started from the local registry
#docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -v /home/sdidocker/elk/data/:/opt/data -it --name elk localhost:5000/elk_sebp:latest
docker exec -it pgweb /bin/bash