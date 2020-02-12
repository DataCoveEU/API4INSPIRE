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

import java.awt.Rectangle;
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
     * @param sql                   SQL String to be executed
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
     *
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
                    //DO NOTHING
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
        if (zwHostname == null) {
            return null;
        }
        try {
            File sqliteFile = new File(zwHostname);

            if (sqliteFile.exists()) {
                Properties prop = new Properties();
                prop.setProperty("enable_shared_cache", "true");
                prop.setProperty("enable_load_extension", "true");
                prop.setProperty("enable_spatialite", "true");
                Connection connection = DriverManager.getConnection("jdbc:spatialite:" + zwHostname, prop);
                c = connection;
                hostname = zwHostname;
                zwHostname = null;
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
        //Checking if table is a view
        String sql = sqlString.get(queryName);
        if(geoCol != null){
            sql = sql != null ? sql : "SELECT *, AsEWKB(" + geoCol + ") FROM " + queryName;
        }else {
            sql = sql != null ? sql : "SELECT * FROM " + queryName;
        }


            log.debug("Converting table: " + queryName + " to featureCollection");
            ResultSet rs = SqlWhere(sql, filterParams);
            //Creating featureCollection with given name
            FeatureCollection fs = new FeatureCollection(alias);
            //Create offset
            for (int i = 0; i < offset; i++) {
                rs.next();
            }

            while (rs.next() && (fs.getFeatures().size() < limit || limit == -1)) {
                Feature f = new Feature();
                HashMap<String, Object> prop = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();
                for (int x = 1; x <= md.getColumnCount(); x++) {
                    String colName = md.getColumnName(x);
                    if (md.getColumnLabel(x).contains("OGC_FID") && idCol == null || colName.equals(idCol)) {
                        //ID
                        f.setId(rs.getString(x));
                        log.debug("ID set");
                    } else {
                        //Normal Feature
                        if (!(colName.contains("AsEWKB(")
                                || (geoCol != null && colName.equals(geoCol))
                                || colName.equals("GEOMETRY"))) {
                            String col = md.getColumnName(x);
                            //Check if there is a config for that table and if it has a column rename
                            if (tc != null) {
                                ColumnConfig columnConfig = tc.getMap().get(col);
                                if (columnConfig != null && !columnConfig.isExclude()) {
                                    prop.put(columnConfig.getAlias(), rs.getObject(x));
                                }
                            } else {
                                prop.put(col, rs.getObject(x));
                            }
                        }
                    }
                }
                boolean intersect = true;
                if (geoCol != null) {
                    log.debug("Set Geometry");
                    String geometry = rs.getString("AsEWKB(" + geoCol + ")");
                    if (geometry != null) {
                        Geometry geometr = PGgeometry.geomFromString(geometry);
                        if (geometr.getSrid() == 0) {
                            log.warn("SRID is 0, assuming that the format used 4326! Collection: " + alias);
                        }
                        if (geometr.getSrid() != 4326) {
                            log.warn("SRID for collection: " + alias + " is not set to 4326!");
                        } else {
                            mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geometr);
                            if (geo != null) {
                                f.setGeometry(geo);
                                double[] bboxFeature = geo.getBbox();
                                f.setBbox(bboxFeature);
                                //If bbox is given
                                if (bbox != null) {
                                    //Check if intersects
                                    Rectangle a = rectFromBBox(bboxFeature);
                                    Rectangle b = rectFromBBox(bbox);
                                    intersect = a.intersects(b);
                                }
                            }
                        }
                    }
                }

                if (intersect) {
                    f.setProperties(prop);
                    fs.addFeature(f);
                }
            }

            if (geoCol != null) {
                log.debug("Getting Bounding Box for Table: " + queryName);
                Statement stmt = c.createStatement();
                //ST_SetSRID -> transforms Box to Polygon

                ResultSet resultSet = SqlBBox(sql,filterParams,geoCol);

                if (resultSet.next()) {
                    String ewkb = resultSet.getString(1);
                    if (ewkb != null) {
                        Geometry gm = PGgeometry.geomFromString(ewkb);
                        if (gm.getSrid() == 0) {
                            log.warn("SRID is 0, assuming that the format used is 4326! Collection: " + alias);
                        }
                        if (gm.getSrid() != 4326) {
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
        double xmin = Integer.MAX_VALUE;
        double xmax = Integer.MIN_VALUE;
        double ymin = Integer.MAX_VALUE;
        double ymax = Integer.MIN_VALUE;

        //Type is Polygon
        if (geom.getType() == 3) {
            List<List<Position>> l = new ArrayList<>();
            ArrayList<Position> li = new ArrayList<>();

            int x = 1;
            org.postgis.Point p = geom.getFirstPoint();
            do {
                if (p.getX() > xmax) {
                    xmax = p.getX();
                }

                if (p.getX() < xmin) {
                    xmin = p.getX();
                }

                if (p.getY() > ymax) {
                    ymax = p.getY();
                }

                if (p.getY() < ymin) {
                    ymin = p.getY();
                }

                li.add(new Position(p.getX(), p.getY()));
                p = geom.getPoint(x);
                x++;
            } while ((!p.equals(geom.getLastPoint())));
            l.add(li);
            Polygon p1 = new Polygon(l);
            p1.setBbox(new double[]{xmin, ymin, xmax, ymax});
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

    public ResultSet SqlWhere(String sql, Map<String,String> filterParams) throws SQLException{
        ResultSet rs;
        if(filterParams != null && filterParams.size() > 0){
            sql = "SELECT * FROM (" + sql + ") as tabula where ";
            for(Map.Entry<String,String> entry:filterParams.entrySet()){
                sql = sql + entry.getKey() + " = ? and";
            }
            sql = sql.substring(0,sql.length()-4);
            PreparedStatement ps = c.prepareStatement(sql);
            int counter = 1;
            for(Map.Entry<String,String> entry:filterParams.entrySet()){
                ps.setString(counter,entry.getValue());
                counter++;
            }
            rs = ps.executeQuery();
        }else {
            //Executing sql
            rs = c.createStatement().executeQuery(sql);
        }
        return rs;
    }

    public ResultSet SqlBBox(String sql, Map<String,String> filterParams, String geoCol) throws SQLException{
        ResultSet rs;
        if(filterParams != null && filterParams.size() > 0){
            sql = "SELECT * FROM (" + sql + ") as tabula where ";
            for(Map.Entry<String,String> entry:filterParams.entrySet()){
                sql = sql + entry.getKey() + " = ? and";
            }
            sql = sql.substring(0,sql.length()-4);
            sql = "SELECT AsEWKB(Extent("
                    + geoCol
                    + ")) as table_extent FROM ("
                    + sql
                    + ") as tabulana";
            PreparedStatement ps = c.prepareStatement(sql);
            int counter = 1;
            for(Map.Entry<String,String> entry:filterParams.entrySet()){
                ps.setString(counter,entry.getValue());
                counter++;
            }
            rs = ps.executeQuery();
        }else {
            //Executing sql
            rs = c.createStatement().executeQuery(sql);
        }
        return rs;
    }

    public Rectangle rectFromBBox(double[] bbox) {
        return new Rectangle((int) bbox[0], (int) bbox[3], (int) (bbox[2] - bbox[0]),
                (int) (bbox[3] - bbox[1]));
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
}
