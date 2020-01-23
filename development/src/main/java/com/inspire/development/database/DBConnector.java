package com.inspire.development.database;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.database.connector.SQLite;

import java.util.ArrayList;

@JsonTypeName("dbconnector")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DBConnector {
    String database = "";
    String hostname = "";
    String password = "";
    String username = "";

    public String checkConnection();

    public void delete(String fc);

    public FeatureCollection execute(String sql, String fcName);

    public FeatureCollection get(String collectionName, boolean withProps, boolean withSpatial);

    public FeatureCollection[] getAll(boolean withProps);

    public void save(FeatureCollection fc);

    public void update(FeatureCollection fc);

    public String getId();

    public ArrayList<String> getAllTables();

    public ArrayList<String> getColumns(String table);

    public void renameTable(String table, String tableAlias);

    public void renameFeature(String table, String feature, String featureAlias);
}
