# Preconfigured GeoServer "Black Boxes"
In order to ease uptake of GeoServer utilizing App Schema, we will be providing the configurations created within the API4INSPIRE project for wider reuse.

A short note to the background of this approach. 
While GeoServer App Schema allows for mapping between database columns and elements of the XML Schema of the target data model being configured against, 
these DB tables must be aligned with the structure of the target data model; 
App Schema does not allow for element data on the same level of the target data model to be taken from multiple tables.
As this caveat usually requires at least some modification of the underlying data source, 
our experience has shown that it is usually easiest to model the database tables based on the target data model, 
then either import the source data into these tables, or alternatively define views mimicking these tables
(depending on the size of the data source, one may wish to utilize materialized views in order to reach performance goals)

Each configuration consists of the following two sections:
* SQL for creating the DB tables in accordance with the target data model
* App Schema configuration files aligning these DB tables with the target data model

This undertaking is still a work-in-progress - first configs will appear here shortly!
