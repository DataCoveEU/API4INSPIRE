# API4INSPIRE
Here we provide various resources pertaining to the usage of APIs within INSPIRE. In the sections below we provide an overview of the APIs we will be deploying as well as the data sources we will be deploying this data on.

For more resources on how to deploy such APIs, please also see our [Project Wiki](https://github.com/DataCoveEU/API4INSPIRE/wiki)

## APIs

### OGC API - Features
Pertaining to OGC API - Features, we will be providing this API in two versions

* Geoserver: GeoServer can provide OGC API - Features as an alternative access point on existing GML deployments utilizing App Schema via an extension. 
* [Simple OGC API Development](./OGCAPISimple/docs/SimpleOGCAPI_Development.md)

### OGC SensorThings API
* FROST: The Fraunhofer Open Source SensorThings API will be utilized for the provision of SensorThings API deployments

## Data Sources
At present, we are still in the process of deploying the APIs on data sources hosted by our data providers. As these resources become available, they will be added to the list below.

### ZAMG (AT) - Meteorological Data

### Austro Control (AT)

Data pertaining to INSPIRE Transport Networks - Air

* OGC API: https://inspire.austrocontrol.at/ogcapi/ogc/features
* WFS2: https://sdigeo-free.austrocontrol.at/geoserver/tn-a/wfs?service=WFS&version=2.0.0&request=GetCapabilities

### Umweltbundesamt (AT) & European Environment Agency (EU)

Air Quality data, transformed from the INSPIRE based European Air Quality Directive datasets

* STA: https://airquality-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1 
* Viewer: https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/121/ 

### Hamburg (DE) - Smart City Sensors

Various Smart City Sensors from the City of Hamburg

* STA: https://iot.hamburg.de/v1.0 
* Viewer: https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/110/ 

### BRGM (FR)

Groundwater Quantity, Hydrogeological Unit [GE & or OGC:GroundWaterML2], Flood Risk [NZ or AM]

* STA: https://sensorthings.brgm-rec.fr/SensorThingsGroundWater/v1.0/ 
* Viewer: https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/101/ 

### AFN (FR)

Surface water quantity and quality [EF & OM], Hydrological Networks [HY], Water Transport Networks [TN-W]

* STA Surface Quantity: https://sensorthings.brgm-rec.fr/SensorThingsFlood/v1.0
* STA Surface Quality: https://sensorthings-wq.brgm-rec.fr/FROST-Server/v1.0
* Viewer: https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/101/ 

### Baden Würtenberg Water (DE)

Hydrological Network [HY], Surface Water Quality [EF & OM]

* STA: https://lubw-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1 
* Viewer: https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/114/ 

**This work is developed under the European Location Interoperability Solutions for e-Government (ELISE) Action, financed by Interoperability solutions for public administrations, businesses and citizens (ISA²) Programme.**

