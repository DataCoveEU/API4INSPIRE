package com.inspire.development.database;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.database.connector.SQLite;

@JsonTypeName("dbconnector")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DBConnector {
    String database = "";
    String hostname = "";
    String password = "";
    String username = "";

    public String checkConnection();

    public void delete(String fc);

    public FeatureCollection[] execute(String sql);

    public FeatureCollection get(String collectionName);

    public FeatureCollection[] getAll();

    public void save(FeatureCollection fc);

    public void update(FeatureCollection fc);
}
