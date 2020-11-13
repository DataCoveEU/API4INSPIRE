---
layout: default
title: Linking Features and Multiple xlinks
category: GC-AppSchema
topCategory: ogc-api
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
  * targetAttribute: this element only needs the content "FEATURE_LINK".
* Second **AttributeMapping**
  * targetAttribute: the element from the main featureType that will be providing the xlink (*Note: this is the one tricky bit as usually **FeatureTypeMapping** only contains elements from the **nested** feature, whereas here the element provided is from the **main** feature*)

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
Once the link has been set up as described above, it only needs to be inserted as a **AttributeMapping** into the mapping of the **MainFT** as as shown below. 
The **targetAttribute** is the name of the XML element as in basic feature mapping, in this example ex:other. The elements **encodeIfEmpty** and **isMultiple** should be set to true. 

In the **sourceExpression** the linkage to the chained dataType is provided via the following elements:
* OCQL: the primary key of the main table, column the foreign key of the linked table is referencing. In this example "gmlid"
* linkElement: the **mappingName** of the link **FeatureTypeMapping**
* linkField: this element only needs the content "FEATURE_LINK".

In the **ClientProperty**, we provide the XML attribute to be mapped to, as well as the content of this attribute as follows:
* name: the name of the XML attribute to be mapped to, in this example xlink:href
* value: the content of the xlink:href attribute. There are two tricky bits in this entry:
  * In contrast to other App Schema modalities, the column names being referenced stem from the linked table, "otherid" is a column of the linked association table *ex_main_other_as*
  * In order to make the link actionable, the API URL has been prepended to the identifier of the linked feature. In addition, in this example the application type has been added as a suffix for easier exploration within a browser - this suffix should **NOT** be provided in real data endpoints, only provided for illustrative purposes here.

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

## A general note to the "FEATURE_LINK"
In most cases where we use the string "FEATURE_LINK", this simple string suffices. However, there are cases where different types are nested under the same element. In those cases, the FEATURE_LINK can be formulated as an array, the first instance as "FEATURE_LINK[1]", the second as "FEATURE_LINK[2]". Note that the index must be included in both mentions of FEATURE_LINK (so both in the main as well as in the nested feature mapping)
