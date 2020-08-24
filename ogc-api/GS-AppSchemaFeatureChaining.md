---
layout: default
title: Feature Chaining Mapping
category: GC-AppSchema
order: 5
---

# Feature Chaining

Feature Chaining is one of the more complex aspects of GeoServer App Schema configuration. It is utilized in those cases where a nested featureType or dataType can have a cardinality of more than one. In such cases, an additional database table will be required for the provision of the data pertaining to the nested featureType or dataType. Feature Chaining allows one to configure the mapping for each individual featureType or dataType separately within a **FeatureTypeMapping** section of the configuration file, then reference this complete feature mapping for the content of the attribute providing the nested type.

## Feature Chaining of dataTypes

In our example, the dataType **NestedDT** is provided by the nestedDtMultiple attribute of **MainFT** with a cardinality of 0..* . The data for the dataType **NestedDT** is stored in the database table ex_nesteddt. The **FeatureTypeMapping** for this nested dataType has the same structure as that done for the **MainFT**, with a few additions. In the header a **mappingName** is provided that allows us to reference this feature mapping. 

In the example below, the **mappingName** is defined as \'\_Nested_DT\'

```
<FeatureTypeMapping>
  <mappingName>_Nested_DT</mappingName>
  <sourceDataStore>idDataStoreInsp</sourceDataStore>
  <sourceType>ex_nesteddt</sourceType>
  <targetElement>ex:NestedDT</targetElement>
```

In addition, we must provide an **AttributeMapping** section that specifies the database column to be utilized as a foreign key to the main table; the name provided under **targetAttribute** for this section must be the same as used when referencing this dataType. In the example below the database column main_ft_id references the id column of the main table as specified under **sourceExpression**, while the name for the **targetAttribute** is provided as \'FEATURE_LINK[1]\'

```
<AttributeMapping>
  <targetAttribute>FEATURE_LINK[1]</targetAttribute>
  <sourceExpression>
    <OCQL>main_ft_id</OCQL>
  </sourceExpression>
</AttributeMapping>
```




