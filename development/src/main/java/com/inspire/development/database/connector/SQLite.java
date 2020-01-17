package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.*;
import com.inspire.development.config.TableConfig;
import com.inspire.development.database.DBConnector;
import com.inspire.development.collections.FeatureCollection;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Point;
import mil.nga.sf.geojson.Position;
import org.springframework.beans.factory.support.ManagedMap;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * DBConnector for a SQLite database
 */
@JsonTypeName("sqlite")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,visible = true)
public class SQLite implements DBConnector {
    private ArrayList<String> errorBuffer;
    @JsonProperty("path")
    private String hostname;
    private Connection c;
    @JsonProperty("name")
    private String name;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;
    @JsonProperty("SQLString")
    private ArrayList<String> sqlList;

    /**
     * Craete DBConnector for SQLite Database
     *
     * @param path Path to the SQLite File
     * @return true if it worked false if error occurred. Error is stored in errorBuffer. See {@link SQLite#getErrorBuffer()}.
     */
    public SQLite(String path, String name) {
        this.name = name;
        errorBuffer = new ArrayList<>();
        hostname = path;
        tableNames = new ArrayList<>();
        config = new HashMap<>();

        Connection connection = null;
        try {
            // create a database connection
            //Enable spatialite
            Properties prop = new Properties();
            prop.setProperty("enable_shared_cache", "true");
            prop.setProperty("enable_load_extension", "true");
            prop.setProperty("enable_spatialite", "true");
            connection = DriverManager.getConnection("jdbc:spatialite:" + hostname, prop);
            c = connection;
            //Enable spatialite for tables
            Statement stat = c.createStatement();
            stat.execute("SELECT InitSpatialMetaData()");
            stat.close();
            updateTablesArray();
            System.out.println(tableNames);
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
        }

    }

    @JsonCreator
    public SQLite(@JsonProperty("path")String path, @JsonProperty("name")String name, @JsonProperty("config")HashMap<String,TableConfig> config, @JsonProperty("SQLString") ArrayList<String> sql) {
        this.config = config;
        this.name = name;
        this.sqlList = sql;
        errorBuffer = new ArrayList<>();
        hostname = path;
        tableNames = new ArrayList<>();

        Connection connection = null;
        try {
            // create a database connection
            //Enable spatialite
            Properties prop = new Properties();
            prop.setProperty("enable_shared_cache", "true");
            prop.setProperty("enable_load_extension", "true");
            prop.setProperty("enable_spatialite", "true");
            connection = DriverManager.getConnection("jdbc:spatialite:" + hostname, prop);
            c = connection;
            //Enable spatialite for tables
            Statement stat = c.createStatement();
            stat.execute("SELECT InitSpatialMetaData()");
            stat.close();
            updateTablesArray();
            System.out.println(tableNames);
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
        }

    }

    /**
     * Updates tableNames array to contain table names, that contain a GEOMETRY column
     */
    private void updateTablesArray() {
        try {
            tableNames = new ArrayList<>();
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                try {
                    Statement s = c.createStatement();
                    ResultSetMetaData rsm = s.executeQuery("SELECT  * FROM " + rs.getString(3)).getMetaData();
                    boolean contains = false;
                    //Iterate backwards because the GEOMETRY column is most of the time the last
                    for (int x = rsm.getColumnCount(); x > 0; x++) {
                        if (rsm.getColumnTypeName(x).contains("GEOMETRY")) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        tableNames.add(rs.getString(3));
                    }
                } catch (SQLException e) {
                    //some table names dont exist so do nothing
                }
            }
        } catch (SQLException e) {
            //Connector is closed
            errorBuffer.add(e.getMessage());
        }
    }

    /**
     * Checks for the connectivity to the given Database File
     *
     * @return null if successful else the error string
     */
    @Override
    public String checkConnection() {
        try {
            if (!c.isClosed()) {
                return null;
            } else {
                return "Connection to " + hostname + " is closed";
            }
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Deletes Feature Collection with given name
     *
     * @param fc Feature Collection name
     */
    @Override
    public void delete(String fc) {
        //Not used
    }

    /**
     * Executes given SQL String
     *
     * @param sql SQL String to be executed
     * @return Feature Collection Array from SQL query result, null if error occurred. Error is stored in errorBuffer. See {@link SQLite#getErrorBuffer()}.
     */
    //TODO How is the sql GEOMETRY column handled
    @JsonIgnore
    @Override
    public FeatureCollection[] execute(String sql) {
        ArrayList<FeatureCollection> fs = new ArrayList<>();
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            sqlList.add(sql);
            if(rs.getMetaData().getColumnCount() >= 1) {
                String name = rs.getMetaData().getTableName(1);
                String alias = name;
                if(config.containsKey(name))
                    alias = config.get(name).getAlias();
                while (rs.next()) {
                    resultSetToFeatureCollection(rs, name, alias);
                }

                return fs.toArray(new FeatureCollection[fs.size()]);
            }else{
                errorBuffer.add("SQL has to contain at least 1 output");
                return null;
            }
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
            return null;
        }
    }

    /**
     * Get FeatureCollection with given name
     *
     * @param collectionName FeatureCollection name from inside database
     * @return FeatureCollection from given name. Returns null if collection doesnt exists.
     */
    @JsonIgnore
    @Override
    public FeatureCollection get(String collectionName) {
        try {
            String queryName = collectionName;
            if(config.containsKey(collectionName)){
                queryName = config.get(collectionName).getAlias();
            }
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT *,ST_Y(GEOMETRY), ST_X(GEOMETRY) from " + queryName);
            return resultSetToFeatureCollection(rs, queryName, collectionName);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Returns all FeatureCollections for the Database
     *
     * @return FeatureCollection Array, null if error occurred.
     */
    @JsonIgnore
    @Override
    public FeatureCollection[] getAll() {
        ArrayList<FeatureCollection> fc = new ArrayList<>();
        try {
            for (String table : tableNames) {
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT *, ST_Y(GEOMETRY), ST_X(GEOMETRY) FROM " + table);
                String alias = table;
                if(config.containsKey(table)){
                    alias = config.get(table).getAlias();
                }
                FeatureCollection fs = resultSetToFeatureCollection(rs, table,alias);
                fc.add(fs);
            }
            return fc.toArray(new FeatureCollection[fc.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves FeatureCollection in database
     *
     * @param fc FeatureCollection to be stored
     */
    @Override
    public void save(FeatureCollection fc) {
        //Not used
    }

    /**
     * Update FeatureCollection in database
     *
     * @param fc fc to be updated
     */
    @Override
    public void update(FeatureCollection fc) {
        //Not used
    }

    /**
     * Get all errors of the database connector
     *
     * @return Array with all error Messages
     */
    @JsonIgnore
    public String[] getErrorBuffer() {
        return errorBuffer.toArray(new String[errorBuffer.size()]);
    }

    /**
     * Converts a ResultSet from a Table Query to a FeatureCollection
     * @param rs ResultSet from Table query
     * @param table Table name of query
     * @return  ResultSet with content of table
     */
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table, String alias) {
        try {
            FeatureCollection fs = new FeatureCollection(alias);
            while (rs.next()) {
                Feature f = new Feature();
                HashMap<String, Object> prop = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();

                double xP = -1;
                double yP = -1;

                for (int x = 1; x <= md.getColumnCount(); x++) {
                    if (md.getColumnName(x).contains("ST_X")) {
                        //Geometry Feature X
                       xP = rs.getFloat(x);
                    } else {
                        if (md.getColumnName(x).contains("ST_Y")) {
                            //Geometry Feature Y
                            yP = rs.getFloat(x);
                        }else {
                            if (md.getColumnLabel(x).contains("OGC_FID")) {
                                //ID
                                f.setId(rs.getString(x));
                            } else {
                                //Normal Feature
                                if(!md.getColumnTypeName(x).contains("GEOMETRY")) {
                                    String col = md.getColumnName(x);
                                    //Check if there is a config for that table and if it has a column rename
                                    if(config.containsKey(table) && config.get(table).getMap().containsKey(col)){
                                        col = config.get(table).getMap().get(col);
                                    }
                                    Object o = rs.getObject(x);
                                    if(o == null){
                                        errorBuffer.add("Propertie null at: " + table + ", Id: " + f.getId());
                                    }
                                    prop.put(col,o);
                                }
                            }
                        }
                    }
                }
                if(xP != -1 && yP != -1) {
                    f.setGeometry(new Point(new Position(xP, yP)));
                    f.setProperties(prop);
                    fs.addFeature(f);
                }else{
                    errorBuffer.add("Geometry Format not fitting or null at FeatureCollection: " + table + ", Id: " + f.getId());
                }
            }
            return fs;
        } catch (SQLException e) {
            return null;
        }
    }

    public void renameFeature(String table, String feature, String featureAlias){
        if(config.containsKey(table)){
            TableConfig conf = config.get(table);
            conf.getMap().put(feature,featureAlias);
        }else{
            TableConfig tc = new TableConfig(table, table);
            tc.getMap().put(feature,featureAlias);
            config.put(table,tc);
        }
    }

    public void renameTable(String table, String tableAlias){
        if(config.containsKey(table)){
            config.get(table).setAlias(tableAlias);
        }else{
            TableConfig tc = new TableConfig(table,tableAlias);
            config.put(table,tc);
        }
    }

    @JsonProperty
    public HashMap<String,TableConfig> getConfig(){
        return config;
    }

    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    @JsonProperty
    public String getName(){
        return name;
    }
}
