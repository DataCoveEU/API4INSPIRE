---
layout: default
title: Ad-Hoc Sources
category: data
order: 3
---

# Ad-Hoc Sources

During the start of the Corona-Crisis, we read various articles on the connection between intensity of Covid-19 cases and the ambient air quality. 
While we were aware of the availablility of European air quality data, we also knew just how hard it was to access without prior knowledge of the sources and access points.
As we knew that with SensorThings API we had the necessary technology in our hands, we initially started harvesting the information both on the air quality stations as 
well as the hourly data from the OGC WFS and SOS provided by the Austrian Environment Agency (Umweltbundesamt). Based on popular demand, this API endpoint was 
rapidly extended across central Europa via access to data from the European Environment Agency. 

Additional data sources have been aggregating around this initial ad-hoc activity. Covid-19 case data had been put online in real-time by the Technical University in Stuttgart.

A further ad-hoc SensorThings endpoint was then made available for demographic data accessed from Eurostat, as information on the population in an area is essential for
guaging the impact of air quality, or putting the number of Covid-19 cases into context.

These ad-hoc activities have served to highlight just how easy it is to enable access to existing data sources via SensorThings API.

## Ad-hoc Austrian Air-Quality API

While not formally planned, this additional source has been triggered by the Corona virus outbreak and the relationship with air quality.
As near-real-time Austrian air-quality data was already available in accordance with INSPIRE and European reporting requirements via existing OGC WFS and SOS services,
 it was a fairly simple task to harvest these endpoints, transform the data as required and insert into a SensorThings API instance.
This endpoint is being dynamically extended as additional data sources become available from sources such as the European Environment Agency.
Details are available at http://datacove.eu/ad-hoc-air-quality/ 

### Currently available endpoints
* Viewer: [https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/121/](https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/121/)
* STA: [https://airquality-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1](https://airquality-frost.docker01.ilt-dmz.iosb.fraunhofer.de/v1.1) 
* OGC API - Features: [https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/ldpmanager/manager/#/services/luft-umweltbundesamt-at](https://api4inspire.docker01.ilt-dmz.iosb.fraunhofer.de/ldpmanager/manager/#/services/luft-umweltbundesamt-at)
* SOS for Measurements: [http://luft.umweltbundesamt.at/inspire/sos?service=SOS&version=2.0.0&request=getCapabilities](http://luft.umweltbundesamt.at/inspire/sos?service=SOS&version=2.0.0&request=getCapabilities) 
* WFS2 for Stations and the like: [http://luft.umweltbundesamt.at/inspire/wfs?service=WFS&version=2.0.0&request=getCapabilities](http://luft.umweltbundesamt.at/inspire/wfs?service=WFS&version=2.0.0&request=getCapabilities) 

## Covid-STA
The Covid-STA endpoint was created by Joe Thunyathep Santhanavanich from the Hochschule für Technik Stuttgart. 
The data is accessed from various sources including the repository provided by Johns Hopkins University and the Robert Koch Institute.

### Currently available endpoints
* Viewer: [https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/114/](https://wg-brgm.docker01.ilt-dmz.iosb.fraunhofer.de/servlet/is/114/)
* STA: [http://covidsta.hft-stuttgart.de/server/v1.1](http://covidsta.hft-stuttgart.de/server/v1.1) 

## Demography Things

Based on our recent success in reproviding European air quality data via the OGC SensorThings API, 
and further inspired by recent work on enabling access to Covid-19 data via this API Standard, 
we created a first prototype of DemographyThings, exposing European Demography data pertaining to NUTS regions from Eurostat in a simple and reusable manner.

* STA: [http://service.datacove.eu/DemographyThings/v1.1](http://service.datacove.eu/DemographyThings/v1.1) 

Details of how to formulate a request to this API are available from the API4INSPIRE project web site, but this one, providing the population for all European countries as GeoJSON is just too cool not to share:

```
http://service.datacove.eu/DemographyThings/v1.1/Things
    ?$top=10
    &$filter=length(name) eq 2
    &$select=name
    &$expand=Locations($select=location),
        Datastreams(
            $filter=ObservedProperty/name eq 'demo_r_pjanaggr3';
            $select=name;
            $expand=ObservedProperty($select=name, definition),
                Sensor($select=metadata),
                Observations(
                    $select=result,phenomenonTime;
                    $orderby=phenomenonTime desc;
                    $top=1
                )
        )
    &$resultFormat=geojson
```

Alternatively, if you prefer your values as CSV, try the following link (same data as above, different format, no geometries, just NUTS codes):

```
http://service.datacove.eu/DemographyThings/v1.1/Things
    ?$top=10
    &$filter=length(name)%20eq%202
    &$select=name
    &$select=name
    &$expand=Datastreams(
        $top=1;
        $filter=ObservedProperty/name%20eq%20%27demo_r_pjanaggr3%27;
        $select=name;
        $expand=ObservedProperty($select=name,%20definition),
            Sensor($select=metadata),
            Observations(
                $select=result,phenomenonTime;
                $orderby=phenomenonTime%20desc;
                $top=1
            )
    )
    &$resultFormat=csv
```
