---
layout: default
title: Mapping - Example Overview
category: GC-AppSchema
order: 2
---

# Example FeatureType and Database

## UML for Example FeatureTypes

The UML diagram below shows the featureType and dataTypes defined for this tutorial, together with the associations defined between these types.

![Example UML](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Example_Types.png)

## ER Diagram of DB Tables

The ER diagram below shows the database tables required for provision of the Example FeatureTypes

![Example ER](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Example_ER.png)

## Example XML Output MainFT
```
<ex:MainFT gml:id="gmlid1">
  <ex:inspireId>
	<base:Identifier>
	  <base:localId>localid 1</base:localId>
	  <base:namespace>namespace 1</base:namespace>
	  <base:versionId>version 1</base:versionId>
	</base:Identifier>
  </ex:inspireId>
  <ex:name>ft_name</ex:name>
  <ex:code xlink:href="http://codes.datacove.eu/code"/>
  <ex:codeMultiple xlink:href="http://codes.datacove.eu/codeMultiple1"/>
  <ex:codeMultiple xlink:href="http://codes.datacove.eu/codeMultiple2"/>
  <ex:nestedDT>
	<ex:NestedDT>
	  <ex:name>nested_dt_name</ex:name>
	</ex:NestedDT>
  </ex:nestedDT>
  <ex:nestedDTMultiple>
	<ex:NestedDT>
	  <ex:name>nesteddt_name1</ex:name>
	</ex:NestedDT>
  </ex:nestedDTMultiple>
  <ex:nestedDTMultiple>
	<ex:NestedDT>
	  <ex:name>nesteddt_name2</ex:name>
	</ex:NestedDT>
  </ex:nestedDTMultiple>
  <ex:countedDT>
	<ex:OtherDT>
	  <ex:name>counted_dt_name1</ex:name>
	</ex:OtherDT>
  </ex:countedDT>
  <ex:countedDT>
	<ex:OtherDT>
	  <ex:name>counted_dt_name2</ex:name>
	</ex:OtherDT>
  </ex:countedDT>
  <ex:nestedFT>
	<ex:NestedFT gml:id="nestedFT_gmlid1">
	  <ex:name>nested_ft_name</ex:name>
	</ex:NestedFT>
  </ex:nestedFT>
  <ex:nestedFTMultiple>
	<ex:NestedFT gml:id="nestedft_gmlid1">
	  <ex:name>nestedft_name1</ex:name>
	</ex:NestedFT>
  </ex:nestedFTMultiple>
  <ex:nestedFTMultiple>
	<ex:NestedFT gml:id="nestedft_gmlid2">
	  <ex:name>nestedft_name2</ex:name>
	</ex:NestedFT>
  </ex:nestedFTMultiple>
  <ex:geometry>
	<gml:Point gml:id="PT_gmlid1" srsDimension="2" srsName="urn:ogc:def:crs:EPSG::4326">
	  <gml:pos>48.022078 -1.505727</gml:pos>
	</gml:Point>
  </ex:geometry>
  <ex:other xlink:href="http://service.datacove.eu/example/other/ID2"/>
</ex:MainFT>
```

## Example XML Output MainFT
```
<ex:OtherFT gml:id="ID2">
  <ex:name>OtherFT</ex:name>
  <ex:main xlink:href="http://service.datacove.eu/example/main/ID1"/>
  <ex:main xlink:href="http://service.datacove.eu/example/main/ID3"/>
</ex:OtherFT>

```

## Schema file

The Schema file for the types used in this example is available at [https://schema.datacove.eu/Example.xsd](https://schema.datacove.eu/Example.xsd)

## SQL and App Schema Mapping files for Example DB

[SQL for Example DB](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/Example.sql)

[App Schema Mapping for Example DB](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/ogc-api/configs/MappingEX.xml)

## Example Endpoints (OGC API - Features)
Here two endpoints providing the OGC API - Features output of the Example types:
* MainFT: [https://service.datacove.eu/geoserver/ogc/features/collections/ex:MainFT/items](https://service.datacove.eu/geoserver/ogc/features/collections/ex:MainFT/items?f=application%2Fgeo%2Bjson&limit=50)
* OtherFT: [https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items](https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items?f=application%2Fgeo%2Bjson&limit=50)
