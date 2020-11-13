---
layout: default
title: GeoServer Black Box Configuration for NZ
category: GC-BB
topCategory: ogc-api
order: 4
---

# Configuration information for NZ

## Implemented Classes
At present, we only support the following class from the INSPIRE NZ data spec:
* nz:HazardArea

## Database Creation SQL
The following sql will create the table required for the provision of the NZ FT being described here. 

[SQL for NZ](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/NZ_create.sql)

## App Schema Config

As this configuration provides features under the NZ namespace.

[App Schema Config NZ](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingNZ.xml)

## Example XML Files

[nz:HazardArea](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/HazardArea.xml)
