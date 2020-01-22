package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.*;
import com.inspire.development.config.TableConfig;
import com.inspire.development.database.DBConnector;
import com.inspire.development.collections.FeatureCollection;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Point;
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.springframework.beans.factory.support.ManagedMap;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.*;

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
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;
    @JsonProperty("SQLString")
    private HashMap<String,String> sqlList; //FCName, SQL

    static Logger log = LogManager.getLogger(SQLite.class.getName());

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
        config = new HashMap<>();
        sqlList = new HashMap<>();



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
            log.debug("Created SQL Connector with path: " + hostname);
        } catch (SQLException e) {
            log.error(e.getMessage());
            errorBuffer.add(e.getMessage());
        }

    }

    @JsonCreator
    public SQLite(@JsonProperty("path")String path, @JsonProperty("name")String name, @JsonProperty("config")HashMap<String,TableConfig> config, @JsonProperty("SQLString") HashMap<String,String> sql) {
        this.config = config;
        this.name = name;
        this.sqlList = sql;
        errorBuffer = new ArrayList<>();
        hostname = path;

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
            //updateTablesArray();
            //System.out.println(tableNames);
        } catch (SQLException e) {
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
    public FeatureCollection[] execute(String sql, String fcn) {
        ArrayList<FeatureCollection> fs = new ArrayList<>();
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            sqlList.put(fcn,sql);
            if(rs.getMetaData().getColumnCount() >= 1) {
                String name = rs.getMetaData().getTableName(1);
                while (rs.next()) {
                    resultSetToFeatureCollection(rs, name, name, true);
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
     * @param collectionName FeatureCollection name from inside database
     * @return FeatureCollection from given name. Returns null if collection doesnt exists.
     */
    @JsonIgnore
    @Override
    public FeatureCollection get(String collectionName, boolean withProps) {
        try {
        if(sqlList.containsKey(collectionName)){
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery(sqlList.get(collectionName));
                return resultSetToFeatureCollection(rs, collectionName, collectionName, withProps);
        }else {

                String queryName = getNameByAlias(collectionName);
                if (queryName == null) {
                    queryName = collectionName;
                }
                Statement stmt = c.createStatement();
                ResultSet rs = null;
                if(hasGeometry(queryName)) {
                    rs = stmt.executeQuery("SELECT *,AsEWKB(GEOMETRY) from " + queryName);
                }else{
                    rs = stmt.executeQuery("SELECT * FROM " + queryName);
                }
                return resultSetToFeatureCollection(rs, queryName, collectionName, withProps);
        }
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
    public FeatureCollection[] getAll(boolean withProps) {
        ArrayList<FeatureCollection> fc = new ArrayList<>();
        try {
            for (String table : getAllTables()) {
                Statement stmt = c.createStatement();
                ResultSet rs = null;
                if(hasGeometry(table)) {
                    rs = stmt.executeQuery("SELECT *,AsEWKB(GEOMETRY) FROM " + table);
                }else{
                    rs = stmt.executeQuery("SELECT * FROM " + table);
                }
                String alias = table;
                if(config.containsKey(table)){
                    alias = config.get(table).getAlias();
                }
                FeatureCollection fs = resultSetToFeatureCollection(rs, table,alias, withProps);
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
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table, String alias, boolean withProps) {
        try {
            double xmin = Integer.MAX_VALUE;
            double xmax = Integer.MIN_VALUE;
            double ymin = Integer.MAX_VALUE;
            double ymax = Integer.MIN_VALUE;
            FeatureCollection fs = new FeatureCollection(alias);

                while (rs.next()) {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();



                    for (int x = 1; x <= md.getColumnCount(); x++) {
                        if (md.getColumnLabel(x).contains("OGC_FID")) {
                            //ID
                            f.setId(rs.getString(x));
                        } else {
                            //Normal Feature
                            if (!md.getColumnName(x).equals("AsEWKB(GEOMETRY)") && !md.getColumnName(x).equals("GEOMETRY")) {
                                String col = md.getColumnName(x);
                                //Check if there is a config for that table and if it has a column rename
                                if (config.containsKey(table) && config.get(table).getMap().containsKey(col)) {
                                    col = config.get(table).getMap().get(col);
                                }
                                Object o = rs.getObject(x);
                                /*if(o == null){
                                       errorBuffer.add("Propertie null at: " + table + ", Id: " + f.getId());
                                   }*/
                                prop.put(col, o);
                            }
                        }
                    }
                    String geometry = rs.getString("AsEWKB(GEOMETRY)");
                    mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geometry);
                    if(geo != null){
                        f.setGeometry(geo);
                        f.setBbox(geo.getBbox());
                        if(f.getBbox() != null && f.getBbox().length == 4) {
                            if (f.getBbox()[0] > xmax)
                                xmax = f.getBbox()[0];

                            if (f.getBbox()[1] < xmin)
                                xmin = f.getBbox()[1];

                            if (f.getBbox()[2] > ymax)
                                ymax = f.getBbox()[2];

                            if (f.getBbox()[3] < ymin)
                                ymin = f.getBbox()[3];
                        }
                    }
                    if(withProps) {
                        f.setProperties(prop);
                        fs.addFeature(f);
                    }
                }
            fs.setBB(Arrays.asList(new Double[]{xmin,xmax,ymin,ymax}));
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

    /**
     * Gets all tables from connector
     * @return ArrayList with table names
     */
    @JsonIgnore
    public ArrayList<String> getAllTables(){
        try {
            ArrayList<String> out = new ArrayList<>();
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String table = rs.getString(3);
                if(!table.contains("spatial_"))
                    out.add(rs.getString(3));
            }
            return out;
        }catch (SQLException e){
            return null;
        }
    }

    /**
     * Checks if table has a GEOMETRY column
     * @param table Table name to check
     * @return true if GEOMETRY exists, else false
     */
    public boolean hasGeometry(String table){
        try {
            Statement stmt = c.createStatement();
            stmt.executeQuery("SELECT GEOMETRY FROM " + table);
            return  true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Gets table name of alias
     * @param alias Table alias
     * @return real table name
     */
    public String getNameByAlias(String alias){
        Iterator it = config.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            TableConfig t = (TableConfig) pair.getValue();
            if(t.getAlias().equals(alias)){
                return t.getTable();
            }
        }
        return null;
    }

    public mil.nga.sf.geojson.Geometry EWKBtoGeo(String ewkb) {
        try {
            if (ewkb != null) {
                double xmin = Integer.MAX_VALUE;
                double xmax = Integer.MIN_VALUE;
                double ymin = Integer.MAX_VALUE;
                double ymax = Integer.MIN_VALUE;

                Geometry geom = PGgeometry.geomFromString(ewkb);
                //Type is Polygon
                if (geom.getType() == 3) {
                    List<List<Position>> l = new ArrayList<>();
                    ArrayList<Position> li = new ArrayList<>();

                    int x = 1;
                    org.postgis.Point p = geom.getFirstPoint();
                    do {
                        if(p.getX() > xmax)
                            xmax = p.getX();

                        if(p.getX() < xmin)
                            xmin = p.getX();

                        if(p.getY() > ymax)
                            ymax = p.getX();

                        if(p.getY() < ymin)
                            ymin = p.getX();

                        li.add(new Position(p.getX(), p.getY()));
                        p = geom.getPoint(x);
                        x++;
                    } while ((!p.equals(geom.getLastPoint())));
                    l.add(li);
                    Polygon p1 = new Polygon(l);
                    p1.setBbox(new double[]{xmin,xmax,ymin,ymax});
                    return p1;
                }
                //Type is Point
                if (geom.getType() == 1) {
                    return new mil.nga.sf.geojson.Point(new Position(geom.getFirstPoint().getX(), geom.getFirstPoint().getY()));
                }
                return null;
            }else{
                return null;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

}
