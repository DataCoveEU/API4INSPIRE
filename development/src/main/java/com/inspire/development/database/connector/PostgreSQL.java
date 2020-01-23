package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.*;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.TableConfig;
import com.inspire.development.database.DBConnector;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * DBConnector for a PostgreSQL database
 */
@JsonTypeName("postgresql")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,visible = true)
public class PostgreSQL implements DBConnector {
    private ArrayList<String> errorBuffer;
    @JsonProperty("hostname")
    private String hostname;

    private String database;
    private int port;
    private String schema;

    private Connection c;
    @JsonProperty("id")
    private String id;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;


    public PostgreSQL(String hostname, int port, String database, String schema, String id) {
        this.id = id;
        errorBuffer = new ArrayList<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        tableNames = new ArrayList<>();
        config = new HashMap<>();

        Connection connection = null;
        try {
            // create a database connection
            //jdbc:postgresql://host:port/database
            Properties prop = new Properties();
            prop.setProperty("user", "inspire");
            prop.setProperty("password", "1nsp1r3_2#2#");
            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database, prop);
            c = connection;
            ((org.postgresql.PGConnection)c).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection)c).addDataType("box3d", (Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            errorBuffer.add(e.getMessage());
        }

    }

    @JsonCreator
    public PostgreSQL(@JsonProperty("hostname")String hostname, @JsonProperty("id")String id, @JsonProperty("config")HashMap<String,TableConfig> config, @JsonProperty("port")int port, @JsonProperty("schema")String schema, @JsonProperty("database")String database) {
        this.config = config;
        this.id = id;
        errorBuffer = new ArrayList<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        tableNames = new ArrayList<>();



        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database);
            c = connection;
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
     * @param featureCollectionName
     * @return Feature Collection from SQL query result, null if error occurred. Error is stored in errorBuffer. See {@link PostgreSQL#getErrorBuffer()}.
     */
    @JsonIgnore
    @Override
    public FeatureCollection execute(String sql, String featureCollectionName) {
        try {
            Statement stmt = c.createStatement();
            stmt.execute("CREATE VIEW " + schema + "." + featureCollectionName + " as " + sql);
            return this.get(featureCollectionName,true,false);
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
    public FeatureCollection get(String collectionName, boolean withProps, boolean withSpatial) {
        try {
                String queryName = getNameByAlias(collectionName);
                if (queryName == null) {
                    queryName = collectionName;
                }
                Statement stmt = c.createStatement();
                ResultSet rs = null;
                rs = stmt.executeQuery("SELECT * FROM " + schema + "." + queryName + "");
                return resultSetToFeatureCollection(rs, queryName, collectionName, withProps, withSpatial);
        } catch (SQLException e) {
                return null;
            }
    }

    /**
     * Returns all FeatureCollections for the Database
     * @param withProps boolean with Properties shall be included
     * @return FeatureCollection Array, null if error occurred.
     */
    @JsonIgnore
    @Override
    public FeatureCollection[] getAll(boolean withProps) {
        ArrayList<FeatureCollection> fc = new ArrayList<>();
        for (String table : getAllTables()) {
            try {
                Statement stmt = c.createStatement();
                ResultSet rs;
                rs = stmt.executeQuery("SELECT * FROM " + schema + "." + table + "");
                String alias = table;
                if (config.containsKey(table)) {
                    alias = config.get(table).getAlias();
                }
                FeatureCollection fs = resultSetToFeatureCollection(rs, table, alias, withProps, true);
                if (fs != null)
                    fc.add(fs);
            } catch (SQLException e) {

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
     * @param withProps boolean if Properties shall be returned
     * @param withSpatial boolean if BoundingBox shall be added
     * @return  ResultSet with content of table
     */
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table, String alias, boolean withProps, boolean withSpatial) {
        try {
            FeatureCollection fs = new FeatureCollection(alias);
            if(withProps) {
                while (rs.next()) {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();
                    if (withProps) {
                        for (int x = 1; x <= md.getColumnCount(); x++) {
                            if (md.getColumnLabel(x).contains("localid")) {
                                //ID
                                f.setId(rs.getString(x));
                            } else {
                                //Normal Feature
                                if (!md.getColumnName(x).contains("geom")) {
                                    String col = md.getColumnName(x);
                                    //Check if there is a config for that table and if it has a column rename
                                    if (config.containsKey(table) && config.get(table).getMap().containsKey(col)) {
                                        col = config.get(table).getMap().get(col);
                                    }
                                    Object o = rs.getObject(x);
                                    prop.put(col, o);
                                }
                            }
                        }
                    }

                    if (hasGeometry(table)) {
                        String geometry = rs.getString("geom");
                        mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geometry);
                        if (geo != null) {
                            f.setGeometry(geo);
                            f.setBbox(geo.getBbox());
                        }
                    }
                    f.setProperties(prop);
                    fs.addFeature(f);

                }
            }
            if(hasGeometry(table)) {
                Statement stmt = c.createStatement();
                ResultSet resultSet = stmt.executeQuery("SELECT ST_SetSRID(ST_Extent(geom), 4326) as table_extent FROM " + schema + "." + table + "");
                if (resultSet.next()) {
                    mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(resultSet.getString(1));
                    if(geo != null) {
                        double[] bounding = geo.getBbox();
                        if(bounding != null && withSpatial)
                            fs.setBB(DoubleStream.of(bounding).boxed().collect(Collectors.toList()));
                    }
                }
            }
            return fs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Rename a Feature
     * @param table Table of Feature
     * @param feature Feature original name
     * @param featureAlias Feature alias name
     */
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

    /**
     * Rename a FeatureCollection id
     * @param table Original ID
     * @param tableAlias Alias name to be used
     */
    public void renameTable(String table, String tableAlias){
        if(config.containsKey(table)){
            config.get(table).setAlias(tableAlias);
        }else{
            TableConfig tc = new TableConfig(table,tableAlias);
            config.put(table,tc);
        }
    }

    /**
     * Get Config
     * @return config
     */
    @JsonProperty
    public HashMap<String,TableConfig> getConfig(){
        return config;
    }

    @JsonProperty
    public String getHostname() {
        return hostname;
    }


    @JsonProperty
    public String getDatabase() {
        return database;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public String getSchema() {
        return schema;
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
                if(!table.contains("pg_"))
                    out.add(rs.getString(3));
            }
            rs = md.getTables(null, null, null, new String[]{"VIEW"});
            while (rs.next()) {
                out.add(rs.getString("TABLE_NAME"));
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
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, "geom");
            return rs.next();
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

    /**
     * Converts Extended Well Known Binary to a Geometry Object
     * @param ewkb EWKB String
     * @return Geometry object if string is valid, else null
     */
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
                            ymax = p.getY();

                        if(p.getY() < ymin)
                            ymin = p.getY();

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

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @JsonProperty
    public String getId(){
        return id;
    }

    /**
     * Get all columns from a Table
     * @param table Original Table name
     * @return ArrayList with all names. Null if an error occurred.
     */
    public ArrayList<String> getColumns(String table){
        ArrayList<String> result = new ArrayList<>();
        try {
            DatabaseMetaData md = c.getMetaData();
            ResultSet rset = md.getColumns(null, null, table, null);

            while (rset.next()) {
                result.add(rset.getString(4));
            }
        }catch (SQLException e){

        }
        return result;
    }

}
