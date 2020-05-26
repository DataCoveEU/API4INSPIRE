---
layout: default
title: Urban Data Platform Hamburg
category: data
order: 1
---

# Urban Data Platform Hamburg

The Urban Data Platform of Hamburg Germany is supporting us through provision of their Smart City Sensors - deployed on SensorThings API.
These range from car charging stations over the bike stations of “StadtRad” to various data of the Energy Campus of Hamburg University of Applied Science.
A small selection:

* Charging Stations:
    ```
    https://iot.hamburg.de/v1.0/Things?$filter=substringof(%27Lade%27,name)&$expand=Locations,Datastreams/Observations($orderby=phenomenonTime%20desc;$top=1)&$count=true
    ```
* StadtRad Locations and available Bikes:
    ```
    https://iot.hamburg.de/v1.0/Things?$filter=properties/serviceName%20eq%20%27STA%20StadtRad%27&$expand=Locations($select=location),Datastreams($expand=Observations($select=phenomenonTime,result;$orderby=phenomenonTime%20desc;$top=1);$filter=properties/type%20eq%202)&$count=true
    ```
* Data from the Energy Campus:
    ```
    https://iot.hamburg.de/v1.0/Things?$filter=name%20eq%20%27Energie%20Campus%20Hamburg%20University%20of%20Applied%20Sciences%27&$expand=Locations,Datastreams
    ```

In addition, the City of Hamburg maintains over 400 spatial datasets, many already available online in various formats.
As the Smart City Sensors would be very interesting in combination with routing information (and while INSPIRE TN-RO is available,
 there are no routing algorithms to date), we propose to complement the APIs provided with data stemming from [OpenStreetMap (OSM)](https://ec.europa.eu/jrc/en/publication/comparing-inspire-and-openstreetmap-data-how-make-most-out-two-worlds).

Conclusion: Lots of data on the bottom, cool applications on the top, and some juicy APIs in between.

Potential Use Cases in this context pertain to Smart City applications, e.g. providing routing information to the nearest charging station. In addition, users can experiment with concepts stemming from building management systems utilizing the data from the Energy Campus.

## Currently available endpoints:
* STA: [https://iot.hamburg.de/v1.0](https://iot.hamburg.de/v1.0) 
* Viewer: [https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/150/](https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/150/) 



