---
layout: default
title: GeoServer Black Box Configuration for HY-N and TN-W
category: GC-BB
order: 1
---

# Configuration information for HY-N and TN-W

## Implemented Classes
While the INSPIRE HY-N and TN-W cover many classes, within this project, we have only implemented the classes we need, that are as follows:
* hy-n:HydroNode
* hy-n:WatercourseLink
* tn-w:PortNode
* tn-w:WaterwayLink
* tn-w:InlandWaterway

The following image provides an overview of how these classes are interlinked
![Implemented Classes and Links HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/tnw-classes-pidc.png)

A note of warning - as both hy-n and tn-w are based on the underlying network model, some aspects of this configuration pertain to both namespaces

## Database Creation SQL
The following sql will create all tables required for the provision of the HY-N and TN-W FTs being described here

[SQL for HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/hy-n.sql)


## App Schema Config

## UML Overview of HY-N and TN-W

As it is often difficult to gain an overview of class attributes and associations within HY-N and TN-W due to the multi-level model, I've often found this UML overview that brings all levels together useful. The coloring pertains to the source model.

![UML Overview of HY-N and TN-W](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Water%20Transport.png)
