---
layout: default
title: GeoServer Black Box Configuration for AM
category: GC-BB
order: 3
---

# Configuration information for AM

## Implemented Classes
The INSPIRE AM data spec only contains the following class:
* am:ManagementRestrictionOrRegulationZone

## Database Creation SQL
The following sql will create all tables required for the provision of the AM FT being described here. It also creates a table under the base2 namespace for the relatedParty information:

[SQL for AM](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/am.sql)

## App Schema Config

As this configuration provides features under the AM namespace.

[App Schema Config AM](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingAM.xml)

As this configuration provides features under the BASE2 namespace.

[App Schema Config AM](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingBS.xml)


## Example XML Files

[am:ManagementRestrictionOrRegulationZone](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/ManagementRestrictionOrRegulationZone.xml)
