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

```
<FeatureTypeMapping>
  <mappingName>_main_to_other_</mappingName>
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
    <OCQL>gmlid</OCQL>
    <linkElement>_main_to_other_</linkElement>
    <linkField>FEATURE_LINK</linkField>
  </sourceExpression>
  <isMultiple>true</isMultiple>
  <ClientProperty>
    <name>xlink:href</name>
    <value>strconcat(strconcat('https://service.datacove.eu/geoserver/ogc/features/collections/ex:OtherFT/items/', otherid), '?f=application%2Fgeo%2Bjson')</value>
  </ClientProperty>
</AttributeMapping>					
```
