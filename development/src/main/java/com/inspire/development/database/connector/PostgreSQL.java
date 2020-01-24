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

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
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
    private String username;
    private String password;

    private String zwPassword;
    private String zwSchema;
    private int zwPort = 0;
    private String zwDatabase;
    private String zwUsername;
    private String zwHostname;

    private Connection c;
    @JsonProperty("id")
    private String id;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;


    public PostgreSQL(String hostname, int port, String database, String schema, String id, String username, String password) {
        this.id = id;
        errorBuffer = new ArrayList<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        tableNames = new ArrayList<>();
        config = new HashMap<>();
        this.username = username;
        this.password = password;

        Connection connection = null;
        // create a database connection
        //jdbc:postgresql://host:port/database
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database, prop);
            c = connection;
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
        }
        //((org.postgresql.PGConnection)c).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        //((org.postgresql.PGConnection)c).addDataType("box3d", (Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));

    }

    public void setUsername(String username) {
        this.zwUsername = username;
    }

    public void setPassword(String password) {
        this.zwPassword = password;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonCreator
    public PostgreSQL(@JsonProperty("hostname")String hostname, @JsonProperty("id")String id, @JsonProperty("config")HashMap<String,TableConfig> config, @JsonProperty("port")int port, @JsonProperty("schema")String schema, @JsonProperty("database")String database, @JsonProperty("username")String username,@JsonProperty("password")String password) {
        this.config = config;
        this.id = id;
        errorBuffer = new ArrayList<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        this.username = username;
        this.password = password;
        tableNames = new ArrayList<>();



        Connection connection = null;
        // create a database connection
        //jdbc:postgresql://host:port/database
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database, prop);
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
            if(c == null){
                if(errorBuffer.size() > 0)
                    return errorBuffer.get(errorBuffer.size()-1);
                else
                    return "some error occurred";
            }
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
            return this.get(featureCollectionName,true,false, -1, 0,null);
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
    public FeatureCollection get(String collectionName, boolean withProps, boolean withSpatial, int limit, int offset, double[] bbox) {
        try {
                String queryName = getNameByAlias(collectionName);
                if (queryName == null) {
                    queryName = collectionName;
                }
                Statement stmt = c.createStatement();
                ResultSet rs = null;
                rs = stmt.executeQuery("SELECT * FROM " + schema + "." + queryName + "");
                return resultSetToFeatureCollection(rs, queryName, collectionName, withProps, withSpatial, limit, offset,bbox);
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
                FeatureCollection fs = resultSetToFeatureCollection(rs, table, alias, withProps, true,0,0, null);
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
     * @param limit limit on how many features shall be included
     * @param offset offset to the start of features
     * @param bbox optional, if given only features are returned if there bbox intersects the given one
     * @return  ResultSet with content of table
     */
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table, String alias, boolean withProps, boolean withSpatial, int limit, int offset, double[] bbox) {
        try {
            FeatureCollection fs = new FeatureCollection(alias);
            if(withProps) {
                //Create offset
                for(int i = 0;i< offset;i++){
                    rs.next();
                }
                int counter = 0;
                while (rs.next() && (counter < limit || limit == -1)) {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();
                        for (int x = 1; x <= md.getColumnCount(); x++) {
                            System.out.println(md.getColumnLabel(x));
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

                    boolean intersect = true;
                    if (hasGeometry(table)) {
                        String geometry = rs.getString("geom");
                        mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geometry);
                        if (geo != null) {
                            f.setGeometry(geo);
                            double[] bboxFeature = geo.getBbox();
                            f.setBbox(bboxFeature);
                            if(bbox != null) {
                                Rectangle a = rectFromBBox(bboxFeature);
                                Rectangle b = rectFromBBox(bbox);
                                intersect = a.intersects(b);
                            }
                        }
                    }
                    if(intersect) {
                        f.setProperties(prop);
                        fs.addFeature(f);
                    }
                    counter++;

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
    public void renameProp(String table, String feature, String featureAlias){
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
            ResultSet rs = md.getTables(null, schema, "%", null);
            while (rs.next()) {
                String table = rs.getString(3);
                if(!table.contains("pg_"))
                    out.add(rs.getString(3));
            }
            /**rs = md.getTables(null, schema, null, new String[]{"VIEW"});
            while (rs.next()) {
                out.add(rs.getString("TABLE_NAME"));
            }**/
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
                    p1.setBbox(new double[]{xmin,ymin,xmax,ymax});
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
        this.zwHostname = hostname;
    }

    public void setDatabase(String database) {
        this.zwDatabase = database;
    }

    public void setPort(int port) {
        this.zwPort = port;
    }

    public void setSchema(String schema) {
        this.zwSchema = schema;
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

    public Rectangle rectFromBBox(double[] bbox){
        return new Rectangle((int)bbox[0],(int)bbox[3], (int)(bbox[2]-bbox[0]), (int)(bbox[3]-bbox[1]));
    }

    public String updateConnector(){
        Connection oldCon = c;
        try{
            Properties prop = new Properties();
            String un = zwUsername;
            if(un == null){
                un = username;
            }
            String pw = zwPassword;
            if(pw == null){
                pw = password;
            }

            prop.setProperty("user", un);
            prop.setProperty("password", pw);


            String hn = zwHostname;
            if(hn == null)
                hn = hostname;

            int pt = zwPort;
            if(pt == 0)
                pt = port;

            String db = zwDatabase;
            if(db == null)
                db = database;




            Connection connection = DriverManager.getConnection("jdbc:postgresql://" + hn + ":" + pt + "/" + db, prop);
            c = connection;
            if(zwSchema != null)
                schema = zwSchema;
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

}
