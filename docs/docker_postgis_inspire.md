# Docker PostGIS for ACG SDI INSPIRE
### __do not use this data for operational use!!!__

This PostGIS docker image is based on the PostGIS image from kartoza (see below)

## __Ressources__

kartoza/postgis (PostGIS) Image on
[Docker Hub](https://hub.docker.com/r/kartoza/postgis/)

[Dockerfile on GitHub](https://github.com/kartoza/docker-postgis)

[PostGIS Database With Docker](https://alexurquhart.com/post/set-up-postgis-with-docker/)

### __open issues__ 
* The docker network "--network=postgisnet" must exist before starting this container in order to be able to be seen by pgweb (see below) and can be initialized with 
    ```sh
    docker network create postgisnet
    ```

## __Image creation__ 
Use the dbuild.sh script to create the local image.

## __Running the PostGIS container__ 
Use the drun.sh script to start the PostGIS container.


# Docker pgweb for ACG SDI INSPIRE
### __testing purpose only !!!__

This pgweb docker image is based on the pgweb image from sosedoff (see below)

## __Ressources__

sosedoff/pgweb Image on

[Docker Hub](https://hub.docker.com/r/sosedoff/pgweb)

[Dockerfile on GitHub](https://github.com/sosedoff/pgweb)

### __open issues__ 
* The docker network "--network=postgisnet" must exist before starting this container  in order to be able to connect to the PostGIS Container (see above) and can be initialized with 
    ```sh
    docker network create postgisnet
    ```

## __Image creation__ 

not applicable at the moment

## __Running the pgweb container__ 
The pgweb container can be started using the following docker run command:

Use the drun.sh script to start the pgweb container.

## __Connecting to the PostGIS container__ 
Starting the pgweb in the browser it is necessary to open on the following site:
http://HostOrIPwithPGWEB:8090

Inside the GUI the following fields are to be filled:
* Type: Standard
* Host: is the hostname or IP of the docker container holding the PostgreSQL DB
* Port: the internal port of the PostGIS container (usually 5432) with the container above use 25432
* Username: the user of the DB (here: inspire)
* Password: the password of the DB user (here: see the drun.sh of PostgreSQL for the password of the inspire user)
* Database: the name of the database which should be connected to (here: inspire)
* SSL Mode: disable (unless otherwise configured)