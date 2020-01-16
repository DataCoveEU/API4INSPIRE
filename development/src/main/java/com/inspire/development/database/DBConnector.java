package com.inspire.development.database;


import com.inspire.development.featureCollection.FeatureCollection;

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
