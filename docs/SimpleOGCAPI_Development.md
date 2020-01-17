# Simple OGC API Development
The development documentation:

* [Kickoff Simple OGC API - 2020/01/10](./.pdf)
* [Connection between Frontend (ReactJS) and Backend (Java Spring)](https://github.com/kantega/react-and-spring/)
* [Trello (To-Do List Export)](./Trello-Export.html)
* [Milestones](./SimpleOGCAPI_milestones.md)
* [UML Diagram](./InspireUML.pdf)

* __[PostGIS & pgweb Docker](./docker_postgis_inspire.md)__


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
In unix a .war file can also be deployed using the following command:
````
java -jar /development/target/development-0.0.1.war
````
