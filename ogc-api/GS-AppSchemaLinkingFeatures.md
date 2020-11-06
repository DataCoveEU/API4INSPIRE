---
layout: default
title: Linking Features and Multiple xlinks
category: GC-AppSchema
order: 6
---

# Linking Features and Multiple xlinks

Usually, feature chaining is used to include additional features with a multiplicity above one within a feature. However, this functionality can also be used to provide a list of xlinks referencing a related feature or alternative resource such as a codelist. As with Feature Chaining, two database tables are required:
* the table providing the data for the main feature, in our example ex_mainft
* the table providing the data for the linked feature, in our example ex_main_other_as, the association table providing the link between MainFT and OtherFT

## Setting up the Link


In our example, links to the featureType **OtherFT** (data contained in *ex_mainft*) are provided by xlinks within the ex:other attribute of the **MainFT** (data contained in *ex_otherft*), with a cardinality of 0..* . The association table *ex_main_other_as* provides the links between between the tables, (2 attributes, mainid and otherid).

The header section of the **FeatureTypeMapping** is the same as for normal chained features, described in the previous section in more detail. In this example, the **mappingName** _main_to_other is provided. The association table *ex_main_other_as* providing information on the link between **MainFT** and **OtherFT** is provided in the **sourceType** element. The **targetElement** is the featureType being linked, in this case **OtherFT**.

Within the **attributeMappings**, two **AttributeMapping** blocks must be provided as follows:

* First **AttributeMapping**
  * sourceExpression/OCQL: in this element, the column providing the foreign key to the main table must be entered. In this example "mainid".
  * targetAttribute: this element one only needs to enter "FEATURE_LINK".
* Second **AttributeMapping**
  * targetAttribute: the element from the main featureType that will be providing the xlink

```
<FeatureTypeMapping>
  <mappingName>_main_to_other</mappingName>
  <sourceDataStore>idDataStoreInsp</sourceDataStore>
  <sourceType>ex_main_other_as</sourceType>
  <targetElement>ex:OtherFT</targetElement>
  <attributeMappings>
	<encodeIfEmpty>true</encodeIfEmpty>
	<AttributeMapping>
	  <targetAttribute>FEATURE_LINK</targetAttribute>
	  <!-- FK in linked table -->
	  <sourceExpression>
		<OCQL>mainid</OCQL>
	  </sourceExpression>
	</AttributeMapping>
	<AttributeMapping>
	  <targetAttribute>ex:other</targetAttribute>
	</AttributeMapping>
  </attributeMappings>
</FeatureTypeMapping>	
```

## Embedding the Link

```
<AttributeMapping>
  <targetAttribute>ex:other</targetAttribute>
  <encodeIfEmpty>true</encodeIfEmpty>
  <sourceExpression>
    <!-- PK in main table, what the FK in the linked table links to -->
    <OCQL>gmlid</OCQL>
    <linkElement>_main_to_other</linkElement>
    <linkField>FEATURE_LINK</linkField>
  </sourceExpression>
  <isMultiple>true</isMultiple>
  <ClientProperty>
    <name>xlink:href</name>
    <!-- here we can now access fields from the linked table. -->
    <!-- otherid is from the table ex_main_other_as referenced in the FeatureTypeMapping _main_to_other -->
    <value>strconcat(strconcat('https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items/', otherid), '?f=application%2Fgeo%2Bjson')</value>
  </ClientProperty>
</AttributeMapping>					
```
