---
layout: default
title: GeoServer App Schema Mapping File Base
category: GC-AppSchema
order: 3
---

# Namespaces
All namespaces used in the App Schema Mapping must be declared together with their prefixes in the **namespaces** section.
```
<Namespace>
	<prefix>dm1</prefix>
	<uri>https://schema.datacove.eu/Dummy1</uri>
</Namespace>		
```

# Database Configuration
Tricky! Later!!!

# Configure Source for Feature Types
The location of the xsd file where the description of the Feature Types to be mapped is provided in the **schemaUri** in the **targetTypes** section.

```
<targetTypes>
	<FeatureType>
		<schemaUri>https://schema.datacove.eu/Dummy1.xsd</schemaUri>
	</FeatureType>
</targetTypes>
```

# Feature Mapping
For each Feature Type being mapped, basic information on the data source must be provided as follows.

Under **sourceDataStore**, the name of the data store configured in the Database Configuration must be provided.

Under **sourceType**, the name of the data table in which the data for the Feature Type is stored must be provided.

Under **targetElement**, the name of the Feature Type to be configured must be provided.

Under **defaultGeometry**, the location of the Feature geometry can be be provided. While this is not essential, it is recommended in cases where the geometry is nested within the Feature.

```
<FeatureTypeMapping>
	<sourceDataStore>idDataStoreInsp</sourceDataStore>
	<sourceType>dummy1</sourceType>
	<targetElement>dm1:Dummy1</targetElement>
	<defaultGeometry>dm1:geometry</defaultGeometry> 
```

Further details on [Feature Mapping](https://github.com/DataCoveEU/API4INSPIRE/blob/gh-pages/ogc-api/GS-AppSchemaFeatureMapping.md)
