---
layout: default
title: GeoServer App Schema Mapping
category: GC-AppSchema
order: 1
---

# GeoServer App Schema Configuration
In order to simplify the often painful process of App Schema configuration, we have created a tutorial based on a simple example that serves to illustrate the various options available without burdoning the user with a complex data model as often utilized in such tutorials. The individual sections of this tutorial are described below

## Example FeatureType and Database

For the purpose of this tutorial, we have created a set of GML featureTypes and dataTypes with various associations between them. These serve to illustrate the various options available within GeoServer App Schema configuration.

This example consists of:
* The UML Specification for the GML featureTypes and dataTypes with their associations
* SQL for the creation of a database providing the data for these GML featureTypes and dataTypes
* An example XML output for these GML featureTypes including the nested featureTypes and dataTypes
* The underlying XML Schema file

All information available under [Example FeatureType and Database](https://datacoveeu.github.io/API4INSPIRE/ogc-api/GS-AppSchemaExample.html)

Here two endpoints providing the OGC API - Features output of the Example types:
* MainFT: [https://service.datacove.eu/geoserver/ogc/features/collections/ex:MainFT/items](https://service.datacove.eu/geoserver/ogc/features/collections/ex:MainFT/items?f=application%2Fgeo%2Bjson&limit=50)
* OtherFT: [https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items](https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items?f=application%2Fgeo%2Bjson&limit=50)

## App Schema Base

The base information within the App Schema configuration always follows the same pattern, providing information on the following concepts:
* Namespaces: all namespaces used within the final output XML must be configured
* Database: location and authentication information for the source database
* Source: source schema for the featureTypes to be provided
* Feature Mapping: mapping information for each individual featureType to be provided

All information available under [App Schema Base](https://datacoveeu.github.io/API4INSPIRE/ogc-api/GS-AppSchemaFileBase.html)


## Attribute Mapping

Within the Feature Mapping section of the App Schema configuration one must specify the source for each individual element and attribute within the target XML. GeoServer App Schema configuration supports a wide range of options depending on the user requirements, e.g. pertaining to the cardinality of nested types. In the following section, we provide basic configuration options.

All information for basic feature mapping available under [Basic Feature Mapping](https://datacoveeu.github.io/API4INSPIRE/ogc-api/GS-AppSchemaFeatureMapping.html)


## Feature Chaining

Feature Chaining is the mechanism for more complex mapping within GeoServer App Schema. This is utilized when one feature includes or references multiple instances of a different featureType or dataType as well as in those cases where two featureTypes are provided individually, but with reference to the other.

All information for mapping utilizing feature chaining available under [Feature Chaining Mapping](https://datacoveeu.github.io/API4INSPIRE/ogc-api/GS-AppSchemaFeatureChaining.html)

## Linking Features and Multiple xlinks

Usually, feature chaining is used to include additional features with a multiplicity above one within a feature. However, this functionality can also be used to provide a list of xlinks referencing a related feature or alternative resource such as a codelist. 

All information for linking features and providing multiple xlinks available under [Linking Features and Multiple xlinks](https://datacoveeu.github.io/API4INSPIRE/ogc-api/GS-AppSchemaLinkingFeatures.html)

## Identifiers

In order to assure that references to other features resolve, care must be taken in the underlying URL scheme. For more information on this topic, please see:

[Identifiers](https://datacoveeu.github.io/API4INSPIRE/ogc-api/Identifiers.html)



