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
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

/**
 * DBConnector for a PostgreSQL database
 */
@JsonTypeName("postgresql")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public class PostgreSQL implements DBConnector {
    static Logger log = LogManager.getLogger(PostgreSQL.class.getName());
    private HashMap<String,String> errorBuffer;
    @JsonProperty("hostname")
    private String hostname;
    private String database;
    private int port;
    private String schema;
    private String username;
    private String password;
    private String zwPassword;
    private String zwSchema;
    private int zwPort = 0;
    private String zwDatabase;
    private String zwUsername;
    private String zwHostname;
    private HashMap<String, String> sqlString; // Table name, SQL String
    private Connection c;
    @JsonProperty("id")
    private String id;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column
    @JsonProperty("config")
    private HashMap<String, TableConfig> config;

    public PostgreSQL(String hostname, int port, String database, String schema, String id,
                      String username, String password) {
        this.id = id;
        errorBuffer = new HashMap<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        tableNames = new ArrayList<>();
        config = new HashMap<>();
        this.username = username;
        this.password = password;
        this.sqlString = new HashMap<>();

        Connection connection = null;
        // create a database connection
        //jdbc:postgresql://host:port/database
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        try {
            Class.forName("org.postgresql.Driver");
            connection =
                    DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database,
                            prop);
            c = connection;
            log.info("Postgres Connector created for path: " + hostname);
        } catch (SQLException | ClassNotFoundException e) {
            errorBuffer.put(getUUID(),e.getMessage());
            log.error("Error creating connector. Error: " + e.getMessage());
        }
    }

    @JsonCreator
    public PostgreSQL(@JsonProperty("hostname") String hostname, @JsonProperty("id") String id,
                      @JsonProperty("config") HashMap<String, TableConfig> config, @JsonProperty("port") int port,
                      @JsonProperty("schema") String schema, @JsonProperty("database") String database,
                      @JsonProperty("username") String username, @JsonProperty("password") String password,
                      @JsonProperty("sqlString") HashMap<String, String> sqlString) {
        this.config = config;
        this.id = id;
        errorBuffer = new HashMap<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        this.username = username;
        this.password = password;
        tableNames = new ArrayList<>();
        if (sqlString != null) {
            this.sqlString = sqlString;
        } else {
            this.sqlString = new HashMap<>();
        }

        Connection connection = null;
        // create a database connection
        //jdbc:postgresql://host:port/database
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        try {
            Class.forName("org.postgresql.Driver");
            connection =
                    DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database,
                            prop);
            c = connection;
            log.info("Postgres Connector created from config for path: " + hostname);
        } catch (SQLException | ClassNotFoundException e) {
            errorBuffer.put(getUUID(),e.getMessage());
            log.error("Error creating connector. Error: " + e.getMessage());
        }
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.zwUsername = username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.zwPassword = password;
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
            if (c == null) {
                if (errorBuffer.size() > 0) {
                    return errorBuffer.get(errorBuffer.size() - 1);
                } else {
                    return "some error occurred";
                }
            }
            if (!c.isClosed()) {
                return null;
            } else {
                return "Connection to " + hostname + " is closed";
            }
        } catch (SQLException e) {
            log.warn("Error checking connection for connector: " + hostname);
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
            FeatureCollection fc = getFeatureCollectionByName(featureCollectionName,false,-1,0,null,null);
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
                                 double[] bbox, Map<String,String> filterParams){
            log.info("Requesting Collection: "
                    + collectionName
                    + "with settings: limit="
                    + limit
                    + ", offset="
                    + offset
                    + ", bbox="
                    + Arrays.toString(bbox)
                    + ", witSpatial="
                    + withSpatial);
            try {
                return getFeatureCollectionByName(collectionName, withSpatial, limit, offset, bbox, filterParams);
            }catch (Exception e){
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
        log.info("Get all Collections.");
        log.debug("Iterating through all tables:");
        for (String table : getAllTables()) {
            if (!(config.containsKey(table) && config.get(table).isExclude())) {
                log.debug("Table: " + table);
                try {
                    fc.add(getFeatureCollectionByName(config.get(table) != null ? config.get(table).getAlias() : table, true, 0, 0, null, null));
                }catch (Throwable t){
                    //DO NOTHING
                }
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
        try {
            ArrayList<String> out = new ArrayList<>();
            PreparedStatement pr = c.prepareStatement("select * from information_schema.tables where table_schema = ?");
            pr.setString(1, schema);
            ResultSet rs = pr.executeQuery();
            while (rs.next()) {
                out.add(rs.getString(3));
            }
            for (Map.Entry<String, String> entry : sqlString.entrySet())
                out.add(entry.getKey());
            return out;
        } catch (SQLException e) {
            log.warn("Failde to get all tables. Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all columns from a Table
     *
     * @param table Original Table name
     * @return ArrayList with all names. Null if an error occurred.
     */
    public ArrayList<String> getColumns(String table) {
        log.debug("Getting all Collumns for table: " + table);
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

    /**
     * Rename a FeatureCollection id
     *
     * @param table      Original ID
     * @param tableAlias Alias name to be used
     */
    public void renameTable(String table, String tableAlias) {
        if (config.containsKey(table)) {
            config.get(table).setAlias(tableAlias);
        } else {
            TableConfig tc = new TableConfig(table, tableAlias);
            config.put(table, tc);
        }
    }

    /**
     * Rename a Feature
     *
     * @param table        Table of Feature
     * @param feature      Feature original name
     * @param featureAlias Feature alias name
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
        log.debug("Updating Connector");
        Connection oldCon = c;
        try {
            Properties prop = new Properties();
            String un = zwUsername;
            if (un == null) {
                un = username;
            }
            String pw = zwPassword;
            if (pw == null) {
                pw = password;
            }

            prop.setProperty("user", un);
            prop.setProperty("password", pw);

            String hn = zwHostname;
            if (hn == null) {
                hn = hostname;
            }

            int pt = zwPort;
            if (pt == 0) {
                pt = port;
            }

            String db = zwDatabase;
            if (db == null) {
                db = database;
            }

            Connection connection =
                    DriverManager.getConnection("jdbc:postgresql://" + hn + ":" + pt + "/" + db, prop);
            c = connection;
            if (zwSchema != null) {
                schema = zwSchema;
            }
            //Set properties
            hostname = hn;
            password = pw;
            username = un;
            port = pt;
            hostname = hn;

            //Reset zw
            zwPassword = null;
            zwHostname = null;
            zwUsername = null;
            zwPort = 0;
            zwSchema = null;
            zwDatabase = null;
            return null;
        } catch (SQLException e) {
            //Reset zw
            zwPassword = null;
            zwHostname = null;
            zwPort = 0;
            zwSchema = null;
            zwDatabase = null;
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
        log.debug("Getting table by alias: " + alias);
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


    public FeatureCollection getFeatureCollectionByName(String alias, boolean withSpatial, int limit, int offset, double[] bbox, Map<String,String> filterParams) throws Exception {
        TableConfig tc = getConfByAlias(alias);
        String queryName = alias;
        String geoCol;
        String idCol = "localid";
        if(tc != null){
            queryName = tc.getTable();
            //Setting geoCol and idCol if it is set in the config
            geoCol = tc.getGeoCol() != null ? tc.getGeoCol() : getGeometry(queryName);
            idCol = tc.getIdCol() != null ? tc.getIdCol() : "localid";
        }else {
            geoCol = getGeometry(queryName);
        }
        //Checking if table is a view
        String sql = sqlString.get(queryName);
        boolean isView = sql != null;
        sql = sql != null ? sql : "SELECT * FROM " + schema + "." + queryName;

            log.debug("Converting table: " + queryName + " to featureCollection");
            //Executing sql
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
                    if (colName.equals(idCol)) {
                        //ID
                        f.setId(rs.getString(x));
                    } else {
                        //Normal Feature
                        if (!colName.equals(geoCol)) {
                            String col = md.getColumnName(x);
                            //Check if there is a config for that table and if it has a column rename
                            if (config.containsKey(queryName) && config.get(queryName).getMap().containsKey(col)) {
                                col = config.get(queryName).getMap().get(col).getAlias();
                            }
                            Object o = rs.getObject(x);
                            if (o instanceof PGgeometry && geoCol == null && isView) {
                                //Auto detecting geo column if the table is a view
                                geoCol = colName;
                                setGeo(queryName, colName);
                            } else {
                                if (tc != null) {
                                    ColumnConfig columnConfig = tc.getMap().get(col);
                                    if (columnConfig != null && !columnConfig.isExclude()) {
                                        prop.put(columnConfig.getAlias(), o);
                                    }
                                }else{
                                    prop.put(colName, o);
                                }
                            }
                        }
                    }
                }
                boolean intersect = true;
                if (geoCol != null) {
                    String geometry = new String(rs.getBytes(geoCol));
                    if (geometry != null) {
                        Geometry geometr = PGgeometry.geomFromString(geometry);
                        if (geometr != null) {
                            if (geometr.getSrid() == 0) {
                                log.warn("SRID is 0, assuming that the format used 4326! Collection: " + alias);
                            }
                            if (geometr.getSrid() != 4326 && geometr.getSrid() != 0) {
                                log.warn("SRID for collection: " + alias + " is not set to 4326!");
                            } else {
                                mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geometr);
                                if (geo != null) {
                                    f.setGeometry(geo);
                                    double[] bboxFeature = geo.getBbox();
                                    f.setBbox(bboxFeature);
                                    if (bbox != null) {
                                        Rectangle a = rectFromBBox(bboxFeature);
                                        Rectangle b = rectFromBBox(bbox);
                                        intersect = a.intersects(b);
                                    }
                                }
                            }
                        }
                    }else{
                        log.error("Error converting database geoColumn to valid geometry at table: " + queryName);
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

    public ResultSet SqlWhere(String sql, Map<String,String> filterParams) throws Exception{
        ResultSet rs;
        if(filterParams != null && filterParams.size() > 0){
            sql = "SELECT * FROM (" + sql + ") as tabula where ";
            for(Map.Entry<String,String> entry:filterParams.entrySet()){
                sql = sql + entry.getKey() + "::varchar = ? and";
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
                sql = sql + entry.getKey() + "::varchar = ? and";
            }
            sql = sql.substring(0,sql.length()-4);
            sql = "SELECT ST_SetSRID(ST_Extent("
                    + geoCol
                    + "), 4326) as table_extent FROM ("
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
            sql = "SELECT ST_SetSRID(ST_Extent("
                    + geoCol
                    + "), 4326) as table_extent FROM ("
                    + sql
                    + ") as tabulana";
            //Executing sql
            rs = c.createStatement().executeQuery(sql);
        }
        return rs;
    }

    /**
     * Checks if table has a GEOMETRY column
     *
     * @param table Table name to check
     * @return true if GEOMETRY exists, else false
     */
    public String getGeometry(String table) {
        try {
            log.debug("Getting geometry columns for table: " + table);
            PreparedStatement ps = c.prepareStatement(
                    "select f_geometry_column from geometry_columns where f_table_schema = ? and f_table_name = ?");
            ps.setString(1, schema);
            ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            log.warn("Error getting Geometry for table: " + table);
            return null;
        }
    }

    /**
     * Converts Extended Well Known Binary to a Geometry Object
     *
     * @param geom Geometry Object
     * @return Geometry object if string is valid, else null
     */
    public mil.nga.sf.geojson.Geometry EWKBtoGeo(Geometry geom) {
        if (geom != null) {
            log.debug("Converting EWKB to Geometry");
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
                return new mil.nga.sf.geojson.Point(
                        new Position(geom.getFirstPoint().getX(), geom.getFirstPoint().getY()));
            }
        }
        return null;
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
    public HashMap<String,String> getErrorBuffer() {
        return errorBuffer;
    }

    public boolean removeError(String uuid){
        return errorBuffer.remove(uuid) != null;
    }

    /**
     * Get Config
     *
     * @return config
     */
    @JsonProperty
    public HashMap<String, TableConfig> getConfig() {
        return config;
    }

    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.zwHostname = hostname;
    }

    @JsonProperty
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.zwDatabase = database;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.zwPort = port;
    }

    @JsonProperty
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.zwSchema = schema;
    }
}
