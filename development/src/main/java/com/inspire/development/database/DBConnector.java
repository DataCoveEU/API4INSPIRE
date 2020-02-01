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

    public FeatureCollection execute(String sql, String fcName, boolean check);

    public FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset, double[] bbox);

    public FeatureCollection[] getAll();

    public void save(FeatureCollection fc);

    public void update(FeatureCollection fc);

    public String getId();

    public ArrayList<String> getAllTables();

    public ArrayList<String> getColumns(String table);

    public void renameTable(String table, String tableAlias);

    public void renameProp(String table, String feature, String featureAlias);

    public void setGeo(String table, String column);

    public void setId(String table, String column);

    public String updateConnector();

    public void setExeclude(String table, boolean execlude);
}
