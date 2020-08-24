---
layout: default
title: GeoServer App Schema Mapping Feature Mapping
category: GC-AppSchema
order: 4
---

# Feature Mapping
For each Feature Type being mapped, basic information on the data source must be provided as follows.

Under **sourceDataStore**, the name of the data store configured in the Database Configuration must be provided.

Under **sourceType**, the name of the data table in which the data for the Feature Type is stored must be provided.

Under **targetElement**, the name of the Feature Type to be configured must be provided.

Under **defaultGeometry**, the location of the Feature geometry can be be provided. While this is not essential, it is recommended in cases where the geometry is nested within the Feature.


```
<FeatureTypeMapping>
	<sourceDataStore>idDataStoreInsp</sourceDataStore>
	<sourceType>ex_mainft</sourceType>
	<targetElement>ex:MainFT</targetElement>
	<defaultGeometry>ex:geometry</defaultGeometry> 
```

# AttributeMapping
In the **attributeMappings** section, the data source for each element of the Feature Type being mapped is configured within a **AttributeMapping** section.
```
<attributeMappings>
	<AttributeMapping>
		...
	</AttributeMapping>
</attributeMappings>
```

## Mapping gml:id

Under **targetAttribute**, provide the name of the FeatureType you're mapping to including the namespace

Under **idExpression**, provide the DB Column name which provides the value for the gml:id.

*Note of caution pertaining to gml:id, gml:id cannot start with a number. It must be a letter or underscore “_”, after this characters may be letters, numbers or one of “_”, “-“, “.”*

```
<AttributeMapping>
	<targetAttribute>ex:MainFT</targetAttribute>
	<idExpression>
		<OCQL>gmlid</OCQL>
	</idExpression>
</AttributeMapping>
```


## Mapping simple element

Under **targetAttribute**, provide the name of the element you're mapping to including the namespace

Under **sourceExpression**, provide the DB Column name which provides the value for this element.

*Note: in this example the type of the attribute is string, the DB column of type varchar. The same principle works for other datatypes, e.g. date, whereby the DB column must be of the appropriate type*

```
<AttributeMapping>
	<targetAttribute>ex:name</targetAttribute>
	<sourceExpression>
		<OCQL>ft_name</OCQL>
	</sourceExpression>
</AttributeMapping>
```


## Mapping to attributes such as xlink:href

Under **targetAttribute**, provide the name of the element you're mapping to including the namespace

Include **encodeIfEmpty** set to true if only the attributes will contain content. Otherwise GeoServer will only encode this element if the element itself contains content.

Under **ClientProperty**, under **name** provide the name of the attribute to be mapped to, under **value** provide the DB Column name which provides the value for this element.

*Note: both **sourceExpression** and **ClientProperty** can be utilized in the same **AttributeMapping** section if one wants to provide content both to the XML Element itself as well as to attributes of this element*

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

## Mapping to geometry

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


## Mapping to elements of a featureType or dataType (singular)
When mapping to elements of featureTypes or dataTypes with a maximum cardinality of one, one can explicitely map to the individual elements of this included type by specifying the full xpath to this element. Both **sourceExpression** for element content as well as **ClientProperty** for mapping to element attributes can be utilized in this context. As already mentioned above under **Mapping to attributes**, if the element itself may be empty, one must set **encodeIfEmpty** to **true**.

This approach to mapping can be utilized regardless of if the included type is a featureType or a dataType.

Under **targetAttribute**, provide the name of the element you're mapping to including the namespace

Under **sourceExpression**, provide the DB Column name which provides the value for this element.

Include **encodeIfEmpty** set to **true** if only the attributes will contain content. Otherwise GeoServer will only encode this element if the element itself contains content.

Under **ClientProperty**, under **name** provide the name of the attribute to be mapped to, under **value** provide the DB Column name which provides the value for this element.

*Note: in this example the type of the attribute is string, the DB column of type varchar. The same principle works for other datatypes, e.g. date, whereby the DB column must be of the appropriate type*

The following configuration segment assigns the content of the database column nested_dt_name to the ex:name element of the nested dataType ex:NestedDT, that is provided under the ex:nestedDT element of ex:mainFT.
```
<AttributeMapping>
	<targetAttribute>ex:nestedDT/ex:NestedDT/ex:name</targetAttribute>
	<sourceExpression>
		<OCQL>nested_dt_name</OCQL>
	  </sourceExpression>
</AttributeMapping>
```

The following configuration section assigns the content of the database column nested_ft_name to the ex:name element of the nested dataType ex:NestedFT, that is provided under the ex:nestedFT element of ex:mainFT. As ex:nestedFT requires the provision of a gml:id for this featureType, an additional **AttributeMapping** section is required to provide this under the gml:id attribute of ex:NestedFT

```
<AttributeMapping>
	<targetAttribute>ex:nestedFT/ex:NestedFT</targetAttribute>
	<ClientProperty>
		<name>gml:id</name>
		<value>strconcat('nestedFT_', id)</value>
	</ClientProperty>
</AttributeMapping>

<AttributeMapping>
	<targetAttribute>ex:nestedFT/ex:NestedFT/ex:name</targetAttribute>
	<sourceExpression>
		<OCQL>nested_ft_name</OCQL>
	</sourceExpression>
</AttributeMapping>
```


