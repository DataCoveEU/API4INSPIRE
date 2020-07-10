---
layout: default
title: Franco-Germanic Flow
category: data
order: 2
---

# Franco-Germanic Flow

For our third data nest, we’ve pulled together data from both the German and French sides of the Rhine River,
 integrating data from the German Bundesland of Baden-Württemberg (LUBW) with that stemming from the French Geological Survey (BRGM)
 and the French Office for Biodiversity (OFB) (via its environmental information systems research center - INSIDE).
This data covers spatial sources ranging from the basic river network information covered by the INSPIRE Theme Hydrography with additional
 river features supplied by the INSPIRE Theme Transport Networks - Water over water measurement stations provided in accordance with
 the INSPIRE Theme Environmental Monitoring Facilities to known flood risk zones provided under the INSPIRE Theme Natural Risk Zones,
 that will be exposed via OGC API - Features.
This data will be complemented by dynamic data provided via SensorThings pertaining to both water quality and quantity.

Groundwater data will also be provided in the form of Hydrogeological units, their monitoring facilities served by OGC API - Features
 and their associated raw quantity observations provided by SensorThings API. 

One interesting aspect of this combination of datasets will pertain to the overlaps in data maintained by different MS,
 with the French River Networks and Aquifers extending into Germany. A further duplication will be created pertaining to
 Environmental Monitoring Facilities, as we aim to provide these via both the OGC API - Features as well as SensorThings API;
 as far as possible these parallel datasets will provide cross references.

Potential Use Cases in this context pertain mostly to flooding within the Rhine catchment area, including both surface and ground water,
 but can also extend to navigability of French water transport networks based on water levels.

## Currently available endpoints:
* Viewer: [https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/102/](https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/102/)
* France
  * Ground Water STA: [https://sensorthings.brgm-rec.fr/SensorThingsGroundWater/v1.0](https://sensorthings.brgm-rec.fr/SensorThingsGroundWater/v1.0) 
  * Surface Quantity STA: [https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1](https://sta4hydrometry.brgm-rec.fr/FROST-Server/v1.1)
  * Surface Quality STA: [https://sensorthings-wq.brgm-rec.fr/FROST-Server/v1.0](https://sensorthings-wq.brgm-rec.fr/FROST-Server/v1.0)
* Germany
  * Water STA: [https://lubw-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1](https://lubw-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1)



