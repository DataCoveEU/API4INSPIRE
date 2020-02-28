/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.ColumnConfig;
import com.inspire.development.config.TableConfig;
import com.inspire.development.database.DBConnector;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Point;
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgis.Geometry;
import org.postgis.PGbox2d;
import org.postgis.PGgeometry;

/**
 * DBConnector for a SQLite database
 */
@JsonTypeName("sqlite")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public class SQLite implements DBConnector {
    static Logger log = LogManager.getLogger(SQLite.class.getName());
    private HashMap<String,String> errorBuffer;
    @JsonProperty("path")
    private String hostname;
    private Connection c;
    @JsonProperty("id")
    private String id;
    @JsonProperty("config")
    private HashMap<String, TableConfig> config;
    private HashMap<String, String> sqlString; // Table name, SQL String
    private String zwHostname;

    /**
     * Craete DBConnector for SQLite Database
     *
     * @param path Path to the SQLite File
     * @return true if it worked false if error occurred. Error is stored in errorBuffer. See {@link
     * SQLite#getErrorBuffer()}.
     */
    public SQLite(String path, String id) {
        this.id = id;
        errorBuffer = new HashMap<>();
        hostname = path;
        config = new HashMap<>();
        sqlString = new HashMap<>();

        Connection connection = null;
        try {
            Class.forName("org.spatialite.JDBC");
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
            log.info("Created SQL Connector with id: " + id);
        } catch (SQLException e) {
            log.error("Error creating connector with id: " + id + ". Error: " + e.getMessage());
            e.printStackTrace();
            errorBuffer.put(getUUID(),e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @JsonCreator
    public SQLite(@JsonProperty("path") String path, @JsonProperty("id") String id,
                  @JsonProperty("config") HashMap<String, TableConfig> config,
                  @JsonProperty("sqlString") HashMap<String, String> sqlString) {
        this.config = config;
        this.id = id;
        errorBuffer = new HashMap<>();
        hostname = path;
        this.sqlString = sqlString;

        Connection connection = null;
        try {
            Class.forName("org.spatialite.JDBC");
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
            log.info("Created SQL Connector with path: " + hostname);
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error creating connector with id: " + id + ". Error: " + e.getMessage());
            errorBuffer.put(getUUID(),e.getMessage());
        }
    }

    public HashMap<String, String> getSqlString() {
        return sqlString;
    }

    private static String getUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * Checks for the connectivity to the given Database File
     *
     * @return null if successful else the error string
     */
    @Override
    public String checkConnection() {
        try {
            if(c == null)
                return "an error occurred while creating the connection";
            if (!c.isClosed()) {
                return null;
            } else {
                return "Connection to " + hostname + " is closed";
            }
        } catch (SQLException e) {
            log.error("Error checking Connection for connector: " + id);
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
     * @param featureCollectionName
     * @return Feature Collection from SQL query result, null if error occurred. Error is stored in
     * errorBuffer. See {@link PostgreSQL#getErrorBuffer()}.
     */
    @JsonIgnore
    @Override
    public FeatureCollection execute(String sql, String featureCollectionName, boolean check) throws Exception{
        try {
            c.createStatement().executeQuery(sql);
            //SQL Executed
            sqlString.put(featureCollectionName, sql);
            FeatureCollection fc = getFeatureCollectionByName(featureCollectionName,false,-1,0,null, null);
            if(check)
                sqlString.remove(featureCollectionName);
            return fc;
        } catch (SQLException e) {
            throw e;
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
    public FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset,
                                 double[] bbox, Map<String,String> filterParams) {
        try {
            return getFeatureCollectionByName(collectionName, withSpatial, limit, offset, bbox, filterParams);
        }catch (Throwable t){
            return null;
        }
    }

    /**
     * Returns all FeatureCollections for the Database
     * @return FeatureCollection Array, null if error occurred.
     */
    @JsonIgnore
    @Override
    public FeatureCollection[] getAll() {

        log.info("Get all FeatureCollections");
        ArrayList<FeatureCollection> fc = new ArrayList<>();
        for (String table : getAllTables()) {
                log.debug("Table: " + table);
                try {
                    fc.add(getFeatureCollectionByName(config.get(table) != null ? config.get(table).getAlias() : table, true, 0, 0, null, null));
                }catch (Throwable t){
                    t.printStackTrace();
                }
        }
        return fc.toArray(new FeatureCollection[fc.size()]);
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

    @JsonProperty
    public String getId() {
        return id;
    }

    /**
     * Gets all tables from connector
     *
     * @return ArrayList with table names
     */
    @JsonIgnore
    public ArrayList<String> getAllTables() {
        log.debug("Get all table names");
        try {
            ArrayList<String> out = new ArrayList<>();
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String table = rs.getString(3);
                if (!table.contains("spatial_")) {
                    out.add(table);
                }
            }
            for (Map.Entry<String, String> entry : sqlString.entrySet())
                out.add(entry.getKey());
            return out;
        } catch (SQLException e) {
            log.warn("Error occurred while getting all tables. Error: " + e.getMessage());
            return null;
        }
    }

    public ArrayList<String> getColumns(String table) {
        ArrayList<String> result = new ArrayList<>();
        try {
            if (sqlString.containsKey(table)) {
                ResultSet rs = c.createStatement().executeQuery(sqlString.get(table));
                ResultSetMetaData md = rs.getMetaData();
                for (int x = 1; x <= md.getColumnCount(); x++)
                    result.add(md.getColumnName(x));
            } else {
                DatabaseMetaData md = c.getMetaData();
                ResultSet rset = md.getColumns(null, null, table, null);

                while (rset.next()) {
                    result.add(rset.getString(4));
                }
            }
        } catch (SQLException e) {

        }
        return result;
    }

    public void renameTable(String table, String tableAlias) {
        log.info("Renaming table: " + table + " to: " + tableAlias);
        if (config.containsKey(table)) {
            config.get(table).setAlias(tableAlias);
        } else {
            TableConfig tc = new TableConfig(table, tableAlias);
            config.put(table, tc);
        }
    }

    /**
     * Rename propertie of a table
     *
     * @param table        table the feature is conatained in
     * @param feature      feature to be renamed
     * @param featureAlias alias to be used
     */
    public void renameProp(String table, String feature, String featureAlias) {
        log.info("Renaming propertie: " + feature + " to " + featureAlias + ", in table " + table);
        if (config.containsKey(table)) {
            TableConfig conf = config.get(table);
            conf.getMap().put(feature, new ColumnConfig(featureAlias, false));
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.getMap().put(feature, new ColumnConfig(featureAlias, false));
            config.put(table, tc);
        }
    }

    public void setGeo(String table, String column) {
        if (config.containsKey(table)) {
            config.get(table).setGeoCol(column);
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.setGeoCol(column);
            config.put(table, tc);
        }
    }

    public void setId(String table, String column) {
        if (config.containsKey(table)) {
            config.get(table).setIdCol(column);
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.setIdCol(column);
            config.put(table, tc);
        }
    }

    public String updateConnector() {
        Connection oldCon = c;
        try {
            File sqliteFile = new File(hostname);

            if (sqliteFile.exists()) {
                Properties prop = new Properties();
                prop.setProperty("enable_shared_cache", "true");
                prop.setProperty("enable_load_extension", "true");
                prop.setProperty("enable_spatialite", "true");
                Connection connection = DriverManager.getConnection("jdbc:spatialite:" + zwHostname, prop);
                c = connection;
                return null;
            } else {
                c = oldCon;
                return "File does not exit";
            }
        } catch (SQLException e) {
            //Reset Connector to old params if error occurred
            c = oldCon;
            return e.getMessage();
        }
    }

    public void setColumnExclude(String table, String column, boolean exclude) {
        if (config.containsKey(table)) {
            TableConfig conf = config.get(table);
            conf.getMap().put(column, new ColumnConfig(column, exclude));
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.getMap().put(column, new ColumnConfig(column, exclude));
            config.put(table, tc);
        }
    }

    public void setTableExclude(String table, boolean exclude) {
        if (config.containsKey(table)) {
            config.get(table).setExclude(exclude);
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.setExclude(exclude);
            config.put(table, tc);
        }
    }

    /**
     * Gets table name of alias
     *
     * @param alias Table alias
     * @return real table name
     */
    public TableConfig getConfByAlias(String alias) {
        log.debug("Getting config by alias for alias: " + alias);
        Iterator it = config.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            TableConfig t = (TableConfig) pair.getValue();
            if (t.getAlias().equals(alias)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Checks if table has a GEOMETRY column
     *
     * @param table Table name to check
     * @return true if GEOMETRY exists, else false
     */
    public String getGeometry(String table) {
        log.debug("Get geometry for table: " + table);
        try {
            PreparedStatement ps = c.prepareStatement(
                    "select f_geometry_column from geometry_columns where f_table_name = ?");
            ps.setString(1, table);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1).toUpperCase();
            } else {
                return null;
            }
        } catch (SQLException e) {
            log.warn("Error occurred while getting geometry for table: "
                    + table
                    + ". Error: "
                    + e.getMessage());
            return null;
        }
    }


    /**
     * Converts a featureCollectionId to a FeatureCollection object
     * @param alias featureCollectionId from the API
     * @param withSpatial true if spatial info shall be provided in the response
     * @param limit limit on how many items shall be included in the response
     * @param offset offset to the data in the database
     * @param bbox array in the form of [xmin, ymin, xmax, ymax]. Only data with an intersecting boundingbox is included in the response
     * @param filterParams Params to be filtered by. Null if nothing should be filtered by
     * @return FeatureCollection with data specified by the params
     * @throws Exception Thrown if any SQLException occurred
     */
    public FeatureCollection getFeatureCollectionByName(String alias, boolean withSpatial, int limit, int offset, double[] bbox, Map<String,String> filterParams) throws Exception {
        if(c == null){
            return null;
        }

        TableConfig tc = getConfByAlias(alias);
        String queryName = alias;
        String geoCol;
        String idCol = getPrimaryKey(queryName);
        if(tc != null){
            if(tc.isExclude())
                return null;
            queryName = tc.getTable();
            //Setting geoCol and idCol if it is set in the config
            geoCol = tc.getGeoCol() != null ? tc.getGeoCol() : getGeometry(queryName);
            idCol = tc.getIdCol() != null ? tc.getIdCol() : idCol;
        }else {
            geoCol = getGeometry(queryName);
        }

        if (config.containsKey(queryName) && config.get(queryName).isExclude()) {
            return null;
        }

        //Checking if table is a view
        String sql = sqlString.get(queryName);
        if(geoCol != null){
            sql = sql != null ? sql : "SELECT *, AsEWKB(" + geoCol + ") as ogc_ewkb FROM " + queryName;
        }else {
            sql = sql != null ? sql : "SELECT * FROM " + queryName;
        }

        sql+=" LIMIT ? OFFSET ?";


            log.debug("Converting table: " + queryName + " to featureCollection");
            ResultSet rs = SqlWhere(sql, filterParams, bbox, geoCol,queryName, limit, offset);
            //Creating featureCollection with given name
            FeatureCollection fs = new FeatureCollection(alias);
            while (rs.next()) {
                try {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();
                    for (int x = 1; x <= md.getColumnCount(); x++) {
                        String colName = md.getColumnName(x);
                        if (colName.equals(idCol)) {
                            //ID
                            f.setId(rs.getString(x));
                            log.debug("ID set");
                        } else {
                            if (colName.equals("ogc_bbox")) {
                                String ewkb = rs.getString(x);
                                if (ewkb != null) {
                                    Geometry geom = PGgeometry.geomFromString(ewkb);
                                    if (geom != null) {
                                        org.postgis.Point fp = geom.getFirstPoint();
                                        org.postgis.Point lp = geom.getLastPoint();
                                        f.setBbox(new double[]{fp.x, fp.y, lp.x, lp.y});
                                    }
                                }
                            } else {
                                if (colName.equals("ogc_ewkb")) {
                                    String ewkb = rs.getString(x);
                                    if (ewkb != null) {
                                        Geometry geom = PGgeometry.geomFromString(ewkb);
                                        if (geom != null) {
                                            mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geom);
                                            if (geo != null) {
                                                f.setGeometry(geo);
                                            }
                                        }
                                    }
                                } else {
                                    //Normal Feature
                                    if (!colName.equals(geoCol)) {
                                        String col = md.getColumnName(x);

                                        Object o = rs.getObject(x);
                                        //Check if there is a config for that table and if it has a column rename
                                        if (tc != null) {
                                            ColumnConfig columnConfig = tc.getMap().get(col);
                                            if (columnConfig == null) {
                                                prop.put(colName, o);
                                            } else {
                                                if (!columnConfig.isExclude()) {
                                                    if (columnConfig.getAlias() == null) {
                                                        prop.put(colName, o);
                                                    } else {
                                                        prop.put(columnConfig.getAlias(), o);
                                                    }
                                                }
                                            }
                                        } else {
                                            prop.put(colName, o);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    f.setProperties(prop);
                    fs.addFeature(f);
                }catch (Exception e){
                    log.error("Error occurred while converting sqlite table to feature collection.  Table: " + queryName);
                }
            }

            if (geoCol != null) {
                log.debug("Getting Bounding Box for Table: " + queryName);
                Statement stmt = c.createStatement();
                //ST_SetSRID -> transforms Box to Polygon

                ResultSet resultSet = SqlBBox(sql,filterParams,bbox, queryName,geoCol);

                if (resultSet.next()) {
                    String ewkb = resultSet.getString(1);
                    if (ewkb != null) {
                        Geometry gm = PGgeometry.geomFromString(ewkb);
                        if (gm.getSrid() == 0) {
                            log.warn("SRID is 0, assuming that the format used is 4326! Collection: " + alias);
                        }
                        if (gm.getSrid() != 4326 && gm.getSrid() != 0) {
                            log.warn("SRID for collection: " + alias + " is not set to 4326!");
                        } else {
                            mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(gm);
                            if (geo != null) {
                                double[] bounding = geo.getBbox();
                                if (bounding != null && withSpatial) {
                                    fs.setBB(DoubleStream.of(bounding).boxed().collect(Collectors.toList()));
                                }
                            }
                        }
                    }
                }
            }
            return fs;
    }


    public String getPrimaryKey(String table) {
        log.debug("Get PrimaryKey for table: " + table);
        try {
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getPrimaryKeys(null, null, table);
            if (rs.next()) {
                return rs.getString(4).toUpperCase();
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public mil.nga.sf.geojson.Geometry EWKBtoGeo(Geometry geom) {

        //Type is Polygon
        if (geom.getType() == 3) {
            List<List<Position>> l = new ArrayList<>();
            ArrayList<Position> li = new ArrayList<>();

            int x = 1;
            org.postgis.Point p = geom.getFirstPoint();
            do {
                li.add(new Position(p.getX(), p.getY()));
                p = geom.getPoint(x);
                x++;
            } while ((!p.equals(geom.getLastPoint())));
            l.add(li);
            Polygon p1 = new Polygon(l);
            return p1;
        }
        //Type is Point
        if (geom.getType() == 1) {
            double x = geom.getFirstPoint().getX();
            double y = geom.getFirstPoint().getY();
            Point p = new mil.nga.sf.geojson.Point(new Position(x, y));
            p.setBbox(new double[]{x, y, x, y});
            return p;
        }
        return null;
    }

    public ResultSet SqlWhere(String sql, Map<String,String> filterParams, double[] bbox, String geoCol, String table, int limit ,int offset) throws SQLException{
        ResultSet rs;
        if((filterParams != null && filterParams.size() > 0) || bbox != null || geoCol != null){
            sql = "SELECT *, AsEWKB(Envelope(" + geoCol + ")) as ogc_bbox FROM (" + sql + ") as tabula";

            if(bbox != null || (filterParams != null && filterParams.size() > 0))
                sql += " where ";


            if(filterParams != null && filterParams.size() > 0) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    String col = getConfigByAlias(table, entry.getKey());
                    col = col == null ? entry.getKey() : col;
                    sql = sql + col + " = ? and ";
                }
            }

            if(bbox != null && geoCol != null) {
                sql += "Intersects(Envelope(" + geoCol + "),GeomFromEWKB(?))";
            }else {
                if(filterParams != null && filterParams.size() > 0)
                    sql = sql.substring(0, sql.length() - 4);
            }

            PreparedStatement ps = c.prepareStatement(sql);

            int counter = 1;

            if(counter == -1){
                //Should be ALL
                ps.setInt(counter++,limit);
            }else{
                ps.setInt(counter++,limit);
            }

            ps.setInt(counter++,offset);

            if(filterParams != null) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    ps.setString(counter, entry.getValue());
                    counter++;
                }
            }


            if(bbox != null && geoCol != null) {
                PGbox2d box = new org.postgis.PGbox2d(new org.postgis.Point(bbox[0], bbox[1]), new org.postgis.Point(bbox[2], bbox[3]));
                ps.setString(counter, box.toString());
            }

            rs = ps.executeQuery();
        }else {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1,0);
            ps.setInt(2,0);
            //Executing sql
            rs = ps.executeQuery();
        }
        return rs;
    }

    public ResultSet SqlBBox(String sql, Map<String,String> filterParams, double[] bbox, String table, String geoCol) throws SQLException{
        ResultSet rs;
        sql ="SELECT AsEWKB(Extent("
                + geoCol
                + ")) as table_extent FROM ("
                + sql
                + ") as tabulana";
        //Executing sql
        PreparedStatement ps = c.prepareStatement(sql);

        ps.setInt(1,0);
        ps.setInt(2,0);

        rs = ps.executeQuery();
        return rs;
    }



    private String getConfigByAlias(String table, String alias){
        TableConfig tc =  config.get(table);
        if(tc == null) return null;
        for(Map.Entry<String,ColumnConfig> entry : tc.getMap().entrySet()){
            if(entry.getValue().getAlias().equals(alias)){
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all errors of the database connector
     *
     * @return Array with all error Messages
     */
    @JsonIgnore
    public HashMap<String, String> getErrorBuffer() {
        return errorBuffer;
    }

    @Override
    public boolean removeError(String UUID) {
        return errorBuffer.remove(UUID) != null;
    }

    @JsonProperty
    public HashMap<String, TableConfig> getConfig() {
        return config;
    }

    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    public void setPath(String path) {
        this.zwHostname = path;
    }

    public void setConnectorId(String id) {
        this.id = id;
    }

    public boolean removeSQL(String name){return sqlString.remove(name) != null;}
}
