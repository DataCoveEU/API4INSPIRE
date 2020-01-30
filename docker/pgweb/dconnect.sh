# !/usr/bin/sh
echo "Docker connect to bash of pgweb container"
cd /home/sdidocker/pgweb
docker exec -it pgweb /bin/bash