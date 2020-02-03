package com.inspire.development.database;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;
import java.util.ArrayList;

@JsonTypeName("dbconnector")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface DBConnector {
  String database = "";
  String hostname = "";
  String password = "";
  String username = "";

  String checkConnection();

  void delete(String fc);

  FeatureCollection execute(String sql, String fcName, boolean check);

  FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset,
      double[] bbox);

  FeatureCollection[] getAll();

  void save(FeatureCollection fc);

  void update(FeatureCollection fc);

  String getId();

  ArrayList<String> getAllTables();

  ArrayList<String> getColumns(String table);

  void renameTable(String table, String tableAlias);

  void renameProp(String table, String feature, String featureAlias);

  void setGeo(String table, String column);

  void setId(String table, String column);

  String updateConnector();

  void setColumnExclude(String table, String column, boolean exclude);

  void setTableExclude(String table, boolean exclude);
}
