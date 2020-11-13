---
layout: default
title: GeoServer Black Box Configuration for SU-V
category: GC-BB
topCategory: ogc-api
order: 2
---

# Configuration information for SU-V

## Implemented Classes
While the INSPIRE SU-V covers many classes, within this project, we have only implemented the classes following class:
* suv:VectorStatisticalUnit

## Database Creation SQL
The following sql will create all tables required for the provision of the HY-N and TN-W FTs being described here

[SQL for SU-V](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/hy-n.sql)


## App Schema Config

As this configuration provides features under the SU-V namespace.

[App Schema Config SU-V](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingSUV.xml)


## Example XML Files

[suv:VectorStatisticalUnit](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/StatisticalUnitVector.xml)
