---
layout: default
title: Airy Austria
category: data
order: 0
---

# Airy Austria

In Austria, we have two quite advanced data providers integrated within API4INSPIRE, providing both spatial data via various OGC service types as well as dynamic measurement data, allowing users to integrate over both API types. Potential Use Cases in this context pertain to linking locations from air transport networks with data stemming from meteorological and air quality monitoring stations.


## Austro Control

Austro Control (ACG), responsible for air traffic control within Austria, has long had nearly all datasets relevant for the INSPIRE Theme Transport Networks - Air online via WFS in strict accordance with the INSPIRE data specifications.
In addition, they have defined a solid URI based identifier scheme, allowing for direct resolution of the identifier URI to the underlying data object.
Within API4INSPIRE, an OGC API endpoint will be configured in parallel to the WFS endpoint;
additionally, the stand-alone OGC API - Features implementation for provision of simple features corresponding to SF-0 will be deployed on this data source.
Thus, participants will be able to experiment with diverse provision options and evaluate the strengths and weaknesses of each of the three different systems serving the same dataset.

### Currently available endpoints

* WFS2: [https://sdigeo-free.austrocontrol.at/geoserver/tn-a/wfs?service=WFS&version=2.0.0&request=GetCapabilities](https://sdigeo-free.austrocontrol.at/geoserver/tn-a/wfs?service=WFS&version=2.0.0&request=GetCapabilities)
* OGC API: [https://inspire.austrocontrol.at/ogcapi/ogc/features](https://inspire.austrocontrol.at/ogcapi/ogc/features) 


## Zentralanstalt für Meteorologie und Geodynamik

Zentralanstalt für Meteorologie und Geodynamik (ZAMG), the Austrian agency for meteorology and geodynamics is responsible for the continuous measurement of meteorological parameters, as well as the provision of this data to the World Meteorological Organization (WMO).
They are currently finalizing services for INSPIRE compliant WFS provision of this data, and will add an additional OGC API - Features endpoint to this configuration.
In addition, the same dataset will also be provided in parallel via SensorThings API, underscoring the strengths of sensor centric services for the provision of dynamic measurement data.

