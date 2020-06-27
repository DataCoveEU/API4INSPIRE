---
layout: default
title: GeoServer App Schema Mapping
category: GC-AppSchema
order: 1
---

## AttributeMapping

### Mapping gml:id

Under targetAttribute, provide the name of the FeatureType you're mapping to including the namespace

Under idExpression, provide the DB Column name which provides the value for the gml:id.

*Note of caution pertaining to gml:id, gml:id cannot start with a number. It must be a letter or underscore “_”, after this characters may be letters, numbers or one of “_”, “-“, “.”*

```
<AttributeMapping>
	<targetAttribute>dm1:Dummy1</targetAttribute>
	<idExpression>
		<OCQL>id</OCQL>
	</idExpression>
</AttributeMapping>
```



