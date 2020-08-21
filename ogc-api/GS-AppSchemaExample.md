---
layout: default
title: GeoServer App Schema Mapping
category: GC-AppSchema
order: 2
---

## Example FeatureType and Database

### UML for Dummy1 FeatureType
![Dummy1 UML](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Example_Types.png)

### ER Diagram of table dummy1
![Dummy1 ER](https://raw.githubusercontent.com/DataCoveEU/API4INSPIRE/gh-pages/images/Example1_ER.png)

### Example XML Output
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
  <ex:code xlink:href="http://codes.datacove.eu/cpde"/>
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
</ex:MainFT>
```

### Schema file

The Schema file for Dummy1 is available at [https://schema.datacove.eu/Example.xsd](https://schema.datacove.eu/Example.xsd)
