# Simple OGC API Development
The development documentation:

* [Kickoff Simple OGC API - 2020/01/10](./.pdf)
* [Connection between Frontend (ReactJS) and Backend (Java Spring)](https://github.com/kantega/react-and-spring/)
* [Trello (To-Do List Export)](./Trello-Export.html)
* [Milestones](./SimpleOGCAPI_milestones.md)
* [UML Diagram](./InspireUML.pdf)

* __[Docker for PostGIS & pgweb & ogcapisimple](./docker_postgis_inspire.md)__

* [OGC API Examples](https://github.com/opengeospatial/ogcapi-features/blob/master/implementations.md)
* [Admin design mockup](./Admin-Design.pdf)
* [User design mockup](./User-Design.pdf) 
* [Backend Docmentation](./Server.md)


## Run the application
### Requirements
* Java 8
* Apache tomcat (optional) 
* Maven (just for the build) 

## How to build
In /development you have to run the following command:
````
mvn clean install
````
After running this, the .war file is created

### How to run
In /development/target there is a .war file. This war file can be deployed by a tomcat web server.

## Login (default credentials)
Username: admin
Password: admin

