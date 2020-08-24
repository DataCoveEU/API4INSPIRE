---
layout: default
title: GeoServer App Schema Mapping
category: GC-AppSchema
order: 1
---

In order to simplify the often painful process of App Schema configuration, we have created a tutorial based on a simple example that serves to illustrate the various options available without burdoning the user with a complex data model as often utilized in such tutorials. The individual sections of this tutorial are described below

# Example FeatureType and Database

For the purpose of this tutorial, we have created a set of GML featureTypes and dataTypes with various associations between them. These serve to illustrate the various options available within GeoServer App Schema configuration.

This example consists of:
* The UML Specification for the GML featureTypes and dataTypes with their associations
* SQL for the creation of a database providing the data for these GML featureTypes and dataTypes
* An example XML output for these GML featureTypes including the nested featureTypes and dataTypes
* The underlying XML Schema file

All information available under (Example FeatureType and Database)[https://github.com/DataCoveEU/API4INSPIRE/blob/gh-pages/ogc-api/GS-AppSchemaExample.md]

# App Schema Base

The base information within the App Schema configuration always follows the same pattern, providing information on the following concepts:
* Namespaces: all namespaces used within the final output XML must be configured
* Database: location and authentication information for the source database
* Source: source schema for the featureTypes to be provided
* Feature Mapping: mapping information for each individual featureType to be provided

All information available under (App Schema Base)[https://github.com/DataCoveEU/API4INSPIRE/blob/gh-pages/ogc-api/GS-AppSchemaFileBase.md]


# AttributeMapping

Within the Feature Mapping section of the App Schema configuration one must specify the source for each individual element and attribute within the target XML. GeoServer App Schema configuration supports a wide range of options depending on the user requirements, e.g. pertaining to the cardinality of nested types.

All information available under (Attribute Mapping)[https://github.com/DataCoveEU/API4INSPIRE/blob/gh-pages/ogc-api/GS-AppSchemaFeatureMapping.md]





