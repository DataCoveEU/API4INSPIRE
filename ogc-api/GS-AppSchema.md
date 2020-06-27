---
layout: default
title: GeoServer App Schema Mapping
category: GC-AppSchema
order: 1
---

## AttributeMapping

### Mapping gml:id

Under **targetAttribute**, provide the name of the FeatureType you're mapping to including the namespace

Under **idExpression**, provide the DB Column name which provides the value for the gml:id.

*Note of caution pertaining to gml:id, gml:id cannot start with a number. It must be a letter or underscore “_”, after this characters may be letters, numbers or one of “_”, “-“, “.”*

```
<AttributeMapping>
	<targetAttribute>dm1:Dummy1</targetAttribute>
	<idExpression>
		<OCQL>id</OCQL>
	</idExpression>
</AttributeMapping>
```


### Mapping simple element

Under **targetAttribute**, provide the name of the element you're mapping to including the namespace

Under **sourceExpression**, provide the DB Column name which provides the value for this element.

```
<AttributeMapping>
	<targetAttribute>dm1:name</targetAttribute>
	<sourceExpression>
		<OCQL>name</OCQL>
	</sourceExpression>
</AttributeMapping>
```


### Mapping to attributes such as xlink:href

Under **targetAttribute**, provide the name of the element you're mapping to including the namespace

Include **encodeIfEmpty** set to true if only the attributes will contain content. Otherwise GeoServer will only encode this element if the element itself contains content.

Under **ClientProperty**, under **name** provide the name of the attribute to be mapped to, under **value** provide the DB Column name which provides the value for this element.

```
<AttributeMapping>
	<targetAttribute>dm1:code</targetAttribute>
	<encodeIfEmpty>true</encodeIfEmpty>
	<ClientProperty>
		<name>xlink:href</name>
		<value>strconcat('http://codes.datacove.eu/', code)</value>
	</ClientProperty>
</AttributeMapping>					
```

### Mapping to geometry

Under **targetAttribute**, provide the name of the geometry element you're mapping to including the namespace. Do **NOT** include the geometry type, Geoserver will figure this out itself depending on the database geometry type.

Under **idExpression**, provide the DB Column name which provides the value for the gml:id of the geometry.

Under **sourceExpression**, provide the DB Column name which provides the value for the geometry.

```
<AttributeMapping>
	<targetAttribute>dm1:geometry</targetAttribute>
	<idExpression>
		<OCQL>strconcat('PT_', id)</OCQL>
	</idExpression>
	<sourceExpression>
		<OCQL>geom</OCQL>
	</sourceExpression>
</AttributeMapping>	
```
