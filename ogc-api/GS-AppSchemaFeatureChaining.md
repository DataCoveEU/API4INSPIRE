---
layout: default
title: Feature Chaining Mapping
category: GC-AppSchema
order: 5
---

# Feature Chaining

Feature Chaining is one of the more complex aspects of GeoServer App Schema configuration. It is utilized in those cases where a nested featureType or dataType can have a cardinality of more than one. In such cases, an additional database table will be required for the provision of the data pertaining to the nested featureType or dataType. Feature Chaining allows one to configure the mapping for each individual featureType or dataType separately within a **FeatureTypeMapping** section of the configuration file, then reference this complete feature mapping for the content of the attribute providing the nested type.

## Feature Chaining of dataTypes

### Provision of a chained dataType

In our example, the dataType **NestedDT** is provided by the nestedDtMultiple attribute of **MainFT** with a cardinality of 0..* . The data for the dataType **NestedDT** is stored in the database table ex_nesteddt. The **FeatureTypeMapping** for this nested dataType has the same structure as that done for the **MainFT**, with a few additions. In the header a **mappingName** is provided that allows us to reference this feature mapping. 

In the example below, the **mappingName** is defined as \'\_Nested_DT\'

```
<FeatureTypeMapping>
  <mappingName>_Nested_DT</mappingName>
  <sourceDataStore>idDataStoreInsp</sourceDataStore>
  <sourceType>ex_nesteddt</sourceType>
  <targetElement>ex:NestedDT</targetElement>
```

In addition, we must provide an **AttributeMapping** section that specifies the database column to be utilized as a foreign key to the main table; the name provided under **targetAttribute** for this section must be the same as used when referencing this dataType. In the example below the database column main_ft_id references the id column of the main table as specified under **sourceExpression**, while the name for the **targetAttribute** is provided as \'FEATURE_LINK\'

```
<AttributeMapping>
  <targetAttribute>FEATURE_LINK</targetAttribute>
  <sourceExpression>
    <OCQL>main_ft_id</OCQL>
  </sourceExpression>
</AttributeMapping>
```

*Note: in cases where the same dataType is referenced from multiple locations, whereby different foreign keys should be utilized in associating the correct data, multiple such **AttributeMapping** sections can be provided, each with a different name for **targetAttribute**, e.g. \'FEATURE_LINK[1]\' and \'FEATURE_LINK[2]\'

### Referencing of a chained dataType

Once the dataType to be nested has been configured as described above, one must configure the reference to it within the MainFT as shown below. The **targetAttribute** is the name of the XML element as in basic feature mapping. In the **sourceExpression** the linkage to the chained dataType is provided via the following elements:
* OCQL: the column of the main database table referenced by the foreign key of the linked feature, in our example \'id\'
* linkElement: the **mappingName** of the **FeatureTypeMapping** to be linked, in our example \'\_Nested_DT\'
* linkField: the name provided as **targetAttribute** of the **AttributeMapping** providing the correct foreign key association, in our example \'FEATURE_LINK\'

```
<AttributeMapping>
  <targetAttribute>ex:nestedDTMultiple</targetAttribute>
  <sourceExpression>
    <OCQL>id</OCQL>
    <linkElement>_Nested_DT</linkElement>
    <linkField>FEATURE_LINK</linkField>
  </sourceExpression>
</AttributeMapping>	
```


## Feature Chaining of featureTypes
The same principle as described above can also be applied to featureTypes. The only addition is the provision of an **idExpression** within the **FeatureTypeMapping** of the linked featureType.

```
<AttributeMapping>
  <targetAttribute>ex:NestedFT</targetAttribute>
  <idExpression>
    <OCQL>gmlid</OCQL>
  </idExpression>
</AttributeMapping>
```
