---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults
title: Home
layout: default
category: main
order: 1
---

# API4INSPIRE

The EC has a long history of promoting open access to public data across Europe, breaking down electronic barriers at national borders through the creation of common data and service models, as well as through the provision of accompanying legislation facilitating such endeavours.
The [INSPIRE Directive](https://inspire.ec.europa.eu/) has been a core building block in this work, which has been further elaborated within the [“European Union Location Framework (EULF)”](https://joinup.ec.europa.eu/collection/european-union-location-framework-eulf/about) and [“A Reusable INSPIRE Reference Platform (ARe3NA)”](https://joinup.ec.europa.eu/collection/are3na)

API4INSPIRE serves to investigate new developments in geospatial standards and technologies, foremost the new [OGC API – Features](https://www.ogc.org/standards/ogcapi-features) and [SensorThings API](https://www.ogc.org/standards/sensorthings) standards, together with the outcomes of the [INSPIRE MIG Action 2017.2 on alternative encodings for INSPIRE data](https://github.com/INSPIRE-MIF/2017.2).
For this purpose, an evaluation strategy has been developed suited to determine how these new and emerging standards can best be utilized to leverage existing investments by EU Member States in the INSPIRE implementation.

For the provision of data via OGC API – Features, our most common deployment option is [GeoServer](http://geoserver.org/) with the OGC API extension.
For cases where GeoServer is not used or where we do not have access to provider infrastructure, [LD-Proxy](https://interactive-instruments.github.io/ldproxy/) is utilized to transform data available via WFS2.
In addition, the OGC API – Simple server has been developed within the API4INSPIRE project; this simple implementation allows for provision of simple features in accordance with SF-0.
For the provision of SensorThings API, we utilize the [Fraunhofer Open Source SensorThings (FROST) Server](https://www.iosb.fraunhofer.de/servlet/is/82077/).


## Data Providers

Six data providers from Germany, France and Austria are contributing data, staff and infrastructure, and are well integrated with the core project team;
together, these data providers manage data from 14 INSPIRE themes.
Together with our data providers, we have provided various related datasets via OGC API – Features and SensorThings API endpoints.
This data is openly available for exploration and application development.

* Airy Austria
  * Air Transport Network plus Meteorological Conditions
  * Air Quality via SensorThings
  * Compare and merge OGC API - Features and SensorThings API
* Urban Data Plattform Hamburg
  * Lots of sensor and geo data on the bottom, cool applications on the top, and some juicy APIs in between.
  * One possible use-case here is combining the locations of the electro rollers with the road network data, i.e. determine the fastest route to a juicy burger.
* Franco-Germanic Flows
  * Cross-border Water data providing alternative perspectives on the Rhine
  * Various other complementary French data sources


## Deliverables

The following deliverables have been created during this project:

* [D0 Inception Report](files/D0-InceptionReport-v1.2.pdf)
* [D1 Evaluation Methodology](files/D1-EvaluationMethodology_1.1.pdf)
* [D2 Deployment Strategies](files/D2-DeploymentStrategy_1.1.pdf)


## ELISE

This on-going API4INSPIRE“ study is funded in the frame of the European Location Interoperability Solutions for e-Government action [ELISE](https://ec.europa.eu/isa2/actions/elise_en),
 part of the [ISA](https://ec.europa.eu/digital-single-market/en/european-egovernment-action-plan-2016-2020) Programme.
The ELISE Action supports Better Regulation and Digital Single Market Strategy goals,
 including specific actions of [the e-Government Action Plan](https://ec.europa.eu/digital-single-market/en/european-egovernment-action-plan-2016-2020) and the [European Interoperability Framework](https://ec.europa.eu/isa2/eif_en),
 which are reinforced by the [Tallinn Declaration](http://ec.europa.eu/newsroom/document.cfm?doc_id=47559) vision
 and the [Communications on Building the Data Economy](https://eur-lex.europa.eu/content/news/building_EU_data_economy.html)
 and on [Artificial Intelligence for Europe](https://ec.europa.eu/digital-single-market/en/news/communication-artificial-intelligence-europe).

In particular, ELISE studies explore the role of location information in digital government and the technologies involved in delivering innovative public services.
Is worth mentioning, the “[Digital Government Benchmark – API study](https://joinup.ec.europa.eu/collection/elise-european-location-interoperability-solutions-e-government/document/report-digital-government-benchmark-api-study)” providing an early-stage analysis of Web APIs as enablers for the digital transformation of government.
A multiple-case study comparative analysis has been applied to selected cases, with a particular focus on geospatial API.

![ELISE](images/ELISE-VI.png)

