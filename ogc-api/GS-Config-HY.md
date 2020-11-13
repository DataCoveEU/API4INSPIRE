---
layout: default
title: GeoServer Black Box Configuration for HY-N and TN-W
category: GC-BB
topCategory: ogc-api
order: 1
---

# Configuration information for HY-N, HY-P and TN-W

## Implemented Classes
While the INSPIRE HY-N and TN-W cover many classes, within this project, we have only implemented the classes we need, that are as follows:
* hy-n:HydroNode
* hy-n:WatercourseLink
* hy-p:Watercourse
* tn-w:PortNode
* tn-w:WaterwayLink
* tn-w:InlandWaterway

The following image provides an overview of how these classes are interlinked
![Implemented Classes and Links HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/tnw-classes-pidc.png)

A note of warning - as both hy-n and tn-w are based on the underlying network model, some aspects of this configuration pertain to both namespaces

## Database Creation SQL
The following sql will create all tables required for the provision of the HY-N and TN-W FTs being described here

[SQL for HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/hy-n.sql)

[SQL for HY-P](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/hyp_watercourse_create.sql)


## App Schema Config

As this configuration provides features under both the HY-N and TN-W namespaces, two App Schema configuration files are required. In addition, you must make sure that the base namespaces HY and TN are correctly configured on your GeoServer

[App Schema Config HY-N](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingHYN.xml)

[App Schema Config TN-P](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingHYP.xml)

[App Schema Config TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingTNW.xml)

## Example XML Files

[hy-n:HydroNode](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/HydroNode.xml)

[hy-n:WatercourseLink](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/WatercourseLink.xml)

[hy-p:Watercourse](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/hy-p_Watercourse.xml)


## UML Overview of HY-N and TN-W

As it is often difficult to gain an overview of class attributes and associations within HY-N and TN-W due to the multi-level model, I've often found this UML overview that brings all levels together useful. The coloring pertains to the source model.

[UML Overview of HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Water%20Transport.png)

![UML Overview of HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Water%20Transport.png)

## Warning to XMLSpy users
A note of warning to XMLSpy users, while usually this SW is quite stable, it does NOT like the HY-N schema. 
This has to do with the fact that the underlying UML data models utilize multiple inheritance, that is not supported in XML.
GML Mixin technology enables provision of such UML models, but requires cyclic includes.
Result is that it will claim that your files are invalid, but in this case, it's XMLSpy that's being invalid.
