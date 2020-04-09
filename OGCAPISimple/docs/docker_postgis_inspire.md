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

# Docker ogcapisimple for OGC API 4 INSPIRE
### __currently for dev purpose only !!!__

This ogcapisimple docker image is based on the openjdk image (see below) and installs an Apache Tomcat which is used to supply the ogcapisimple web application.

## __Ressources__

openjdk Image on

[Docker Hub](https://hub.docker.com/_/openjdk)

### __open issues__ 
* tbd - currently app is in development

## __Image creation__ 

* if required, download the content of the "/API4INSPIRE/docker/ogcapisimple" directory (incl. the sub dir /scripts) to your directory of choice
* copy the ogcapisimple.war into the same directory (ogcapisimple.war will be included in this directory on Github when ready for further testing)
* give the following files permission to be executable:
    * dbuild.sh
    * drun.sh
    * dconnect.sh
* create the docker image with
```sh
sh ./dbuild.sh
```

## __Running the ogcapisimple container__ 
The ogcapisimple container can be started using the following docker run command from within the script:
* run the docker container with
```sh
sh ./drun.sh
```

## __Connecting to the ogcapisimple container__ 
The landing page can be opened with:
http://hostname/ogcapisimple

* For container inspection purpose connect in a terminal to the docker image with
```sh
sh ./dconnect.sh
```
## __Configuration possibilities__
### __OpenJDK image version__
in "Dockerfile"
```sh
ARG IMAGE_VERSION=8u232-jdk-stretch
```
### __Apache Tomcat configuration__
#### __Apache Tomcat version__
in "Dockerfile"
```sh
ARG TC_MAJOR=8
ARG TC_VER=8.5.50
```

#### __Apache Tomcat logging configuration__
in "logging.properties"
```sh
############################################################
# Handler specific properties.
# Describes specific configuration info for Handlers.
############################################################

1catalina.org.apache.juli.AsyncFileHandler.level = FINE
1catalina.org.apache.juli.AsyncFileHandler.directory = /data/ogcapisimple/logs/tc
1catalina.org.apache.juli.AsyncFileHandler.prefix = catalina.
1catalina.org.apache.juli.AsyncFileHandler.encoding = UTF-8

2localhost.org.apache.juli.AsyncFileHandler.level = FINE
2localhost.org.apache.juli.AsyncFileHandler.directory = /data/ogcapisimple/logs/tc
2localhost.org.apache.juli.AsyncFileHandler.prefix = localhost.
2localhost.org.apache.juli.AsyncFileHandler.encoding = UTF-8

3manager.org.apache.juli.AsyncFileHandler.level = FINE
3manager.org.apache.juli.AsyncFileHandler.directory = /data/ogcapisimple/logs/tc
3manager.org.apache.juli.AsyncFileHandler.prefix = manager.
3manager.org.apache.juli.AsyncFileHandler.encoding = UTF-8

4host-manager.org.apache.juli.AsyncFileHandler.level = FINE
4host-manager.org.apache.juli.AsyncFileHandler.directory = /data/ogcapisimple/logs/tc
4host-manager.org.apache.juli.AsyncFileHandler.prefix = host-manager.
4host-manager.org.apache.juli.AsyncFileHandler.encoding = UTF-8

java.util.logging.ConsoleHandler.level = FINE
java.util.logging.ConsoleHandler.formatter = org.apache.juli.OneLineFormatter
java.util.logging.ConsoleHandler.encoding = UTF-8
```

Configure access logs in "server.xml"
```sh
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="/data/ogcapisimple/logs/tc"
               prefix="localhost_access_log" suffix=".txt"
               pattern="%h %l %u %t &quot;%r&quot; %s %b" />
```


#### __Apache Tomcat CORS handling__
in "web.xml"
```sh
<filter>
  <filter-name>CorsFilter</filter-name>
  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
    <init-param>
    <param-name>cors.allowed.origins</param-name>
    <!-- <param-value>*</param-value> -->
    <param-value>https://stackpath.bootstrapcdn.com/bootstrap, https://code.jquery.com, https://cdn.jsdelivr.net/npm, https://stackpath.bootstrapcdn.com/bootstrap</param-value>
  </init-param>
  <init-param>
    <param-name>cors.exposed.headers</param-name>
    <param-value>Access-Control-Allow-Origin,Access-Control-Allow-Credentials</param-value>
  </init-param>  
</filter>
<filter-mapping>
  <filter-name>CorsFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

