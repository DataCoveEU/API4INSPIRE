---
layout: default
title: Implementations
category: STA
order: 100
---

# Implementations & Demo Services

Here are some server and client implementations, and some demo services to test your query foo!


## Demo Services

* FROST-Server demo serice:
  - https://ogctest.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/
  - Access: Read & Write
* European air quality:
  - https://airquality-frost.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1
  - Access: Read Only
* Rivers & Measuring points in Baden-Württemberg:  
  This service has many non-point (line, polygon) Locations
  - https://lubw.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/
  - Access: Read Only


## Implementations

### FROST-Server & FROST-Client

![FROST-Server](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/master/docs/images/FROST-Server-darkgrey.png)

[FROST-Server](https://github.com/FraunhoferIOSB/FROST-Server) is an Open Source server implementation of the OGC SensorThings API. FROST-Server implements the entire specification, including all extensions.
It is written in Java and can run in Tomcat or Wildfly and is available as a Docker image.
Among its many features is the ability to use String or UUID based entity IDs.

![FROST-Client](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Client/master/images/FROST-Client-darkgrey.png)

[FROST-Client](https://github.com/FraunhoferIOSB/FROST-Client) is a Java client library for communicating with a SensorThings API compatible server. 


### GOST

[GOST](https://www.gostserver.xyz/) is an open source implementation of the SensorThings API in the Go programming language initiated by Geodan.
It contains easily deployable server software and a JavaScript client.

