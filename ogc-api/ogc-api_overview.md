---
title: OGC API - Features Overview
layout: default
category: ogc-api
order: 1
---

# OGC API - Features Overview

## OGC API Standard

The OGC API Standard consists of the following sections
* [OGC API-Common V.0.0.5 published 2019-11-18](http://docs.opengeospatial.org/is/17-069r3/17-069r3.html)
* [OGC API -Features -Part 1: Core V.1.0 published 2019-10-14](https://github.com/opengeospatial/oapi_common)
* [OGC API – Coverages Work-in-Progress](https://github.com/opengeospatial/ogc_api_coverages)

## OGC API - Features URIs

Landing page, Base URI for API:

`https://inspire.austrocontrol.at/ogcapi/ogc/features`

API Definition - Current swagger documentation:

`https://inspire.austrocontrol.at/ogcapi/ogc/features/api`

API Conformance - Conformance to specifications:

`https://inspire.austrocontrol.at/ogcapi/ogc/features/conformance`

API Collections - All Datasets:

`https://inspire.austrocontrol.at/ogcapi/ogc/features/collections`

Specific Collection –Dataset Description

`https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/{collectionId}`

```https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/tn-a:AerodromeNode```

All Items of a Collection –Dataset 

`https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/{collectionId}/items`

```https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/tn-a:AerodromeNode/items?f=application%2Fgeo%2Bjson&limit=50```

Specific Item – Feature 

`https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/{collectionId}/items/{featureId}`

```https://inspire.austrocontrol.at/ogcapi/ogc/features/collections/tn-a:AerodromeNode/items/AT.0012.6bed1778-d6bf-11e8-9f8b-f2801f1b9fd1.tn-a.AerodromeNode.115?f=application%2Fgeo%2Bjson```


