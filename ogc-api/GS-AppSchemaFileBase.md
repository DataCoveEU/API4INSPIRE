---
layout: default
title: Mapping File Base
category: GC-AppSchema
order: 3
---

# Namespaces
All namespaces used in the App Schema Mapping must be declared together with their prefixes in the **namespaces** section.
```
<Namespace>
  <prefix>ex</prefix>
  <uri>https://schema.datacove.eu/Example</uri>
</Namespace>		
```

*Note: with All we really mean **ALL!** A common error is caused by missing a required namespace, this often happens with nested namespaces such as xlink or gmd*

# Database Configuration
GeoServer App Schema allows for various database configuration options. In order to allow great flexibility, the individual **DataStore** sections within the **sourceDataStores** utilize the Parameter Type that provides name/value pairs. The **name** element provides the individual configuration concept, while the **value** element provides the value to be used for this concept.

Regardless of configuration type, the identifier for a **DataStore** is always provided by the **id** element. This name must be provided within subsequent configuration sections to assign the correct data source to the attribute mapping.

Please note that in this tutorial we rely souly on PostGIS databases. For other configuration options, please see the full documentation available for [GeoServer](http://geoserver.org/).

## JNDI/JDBC Data Store
The simplest is to utilize an existing jdbc connection available on the server running GeoServer as a JNDI connection. 
For this purpose, the following parameters must be set within the **sourceDataStores DataStore**
* **dbtype**: For a JNDI datastore utilizing JDBC on PostGIS, specify the type 'postgisng'
* **jndiReferenceName**: Provide the name of the JDBC connection configured on the server
* **Expose primary keys**: This parameter must be set to true in order to enable getFeatureByID

```
<sourceDataStores>
  <DataStore>
    <id>idDataStoreInsp</id>
    <parameters>
      <Parameter>
        <name>dbtype</name>
        <value>postgisng</value>
      </Parameter>
      <Parameter>
        <name>jndiReferenceName</name>
        <value>java:comp/env/jdbc/tnw</value>
      </Parameter>
      <Parameter>
        <name>Expose primary keys</name>
        <value>true</value>
      </Parameter>
    </parameters>
  </DataStore>
</sourceDataStores>
```

## Direct Data Store
Alternatively, one can specify the database connection directly. The values can be either listed directly within the App Schema config file, or taken from a properties file for better security.
For this purpose, the following parameters must be set within the **sourceDataStores DataStore**
* **dbtype**: For a datastore on PostGIS, specify the type 'postgisng'
* **host**: Provide the host name or IP of the DB Server
* **port**: Provide the port the DB is listening on. Usual default for PostGres is 5432
* **database**: Provide the name of the database that this connection refers to
* **schema**: Provide the name of the database schema that this connection refers to
* **user**: Provide the name database user that this connection refers to
* **passwd**: Provide the password for the database name of the database user that this connection refers to
* **Expose primary keys**: This parameter must be set to true in order to enable getFeatureByID

*Note: in the example below, both options are shown, whereby the direct option has been commented out. More information on setting properties below.*

```
<sourceDataStores>
	<DataStore>
		<id>idDataStoreInsp</id>
		<parameters>
			<Parameter>
				<name>dbtype</name>
				<value>postgisng</value>
			</Parameter>
			<Parameter>
				<name>host</name>
				<!--<value>192.12.34.56</value>-->
				<value>${inspire.host}</value>
			</Parameter>
			<Parameter>
				<name>port</name>
				<value>5432</value>
			</Parameter>
			<Parameter>
				<name>database</name>
				<!--<value>my_inspire_database</value>-->
				<value>${inspire.database}</value>
			</Parameter>
			<Parameter>
				<name>schema</name>
				<value>public</value>
			</Parameter>
			<Parameter>
				<name>user</name>
				<!--<value>my_user</value>-->
				<value>${inspire.user}</value>
			</Parameter>
			<Parameter>
				<name>passwd</name>
				<!--<value>my_password</value>-->
				<value>${inspire.passwd}</value>
			</Parameter>
			<Parameter>
				<name>Expose primary keys</name>
				<value>true</value>
			</Parameter>
		</parameters>
	</DataStore>
</sourceDataStores>
```

### Properties

Under Debian, the app-schema.properties file can be found under the geoserver directory at the relative path ./WEB-INF/classes/app-schema.properties. The properties variables set in this file can be utilized within the app schema configuration as shown above.

```
inspire.host = 192.12.34.56
inspire.database = my_inspire_database
inspire.user = my_user
inspire.passwd = my_password

```

# Configure Source for Feature Types
The location of the xsd file where the description of the Feature Types to be mapped is provided in the **schemaUri** in the **targetTypes** section.

```
<targetTypes>
	<FeatureType>
		<schemaUri>https://schema.datacove.eu/Example.xsd</schemaUri>
	</FeatureType>
</targetTypes>
```

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

Further details on [Feature Mapping](https://github.com/DataCoveEU/API4INSPIRE/blob/gh-pages/ogc-api/GS-AppSchemaFeatureMapping.md)
