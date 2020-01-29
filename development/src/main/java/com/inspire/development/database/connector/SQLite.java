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

import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
    @JsonProperty("id")
    private String id;
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;

    private String zwHostname;

    static Logger log = LogManager.getLogger(SQLite.class.getName());

    /**
     * Craete DBConnector for SQLite Database
     *
     * @param path Path to the SQLite File
     * @return true if it worked false if error occurred. Error is stored in errorBuffer. See {@link SQLite#getErrorBuffer()}.
     */
    public SQLite(String path, String id) {
        this.id = id;
        errorBuffer = new ArrayList<>();
        hostname = path;
        config = new HashMap<>();

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
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error creating connector with id: " + id + ". Error: " + e.getMessage());
            errorBuffer.add(e.getMessage());
        }

    }

    @JsonCreator
    public SQLite(@JsonProperty("path")String path, @JsonProperty("id")String id, @JsonProperty("config")HashMap<String,TableConfig> config) {
        this.config = config;
        this.id = id;
        errorBuffer = new ArrayList<>();
        hostname = path;

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
     * @return Feature Collection from SQL query result, null if error occurred. Error is stored in errorBuffer. See {@link PostgreSQL#getErrorBuffer()}.
     */
    @JsonIgnore
    @Override
    public FeatureCollection execute(String sql, String featureCollectionName) {
        try {
            log.debug("Executing sql: " + sql + ", with collectionName: " + featureCollectionName);
            Statement stmt = c.createStatement();
            stmt.execute("CREATE VIEW " + featureCollectionName + " as " + sql);
            //Adding view to geometry_columns
            PreparedStatement st = c.prepareStatement("INSERT INTO geometry_columns\n" +
                    "    (f_table_name, f_geometry_column, geometry_type, coord_dimension, srid, spatial_index_enabled)\n" +
                    "  VALUES (?, 'geometry', 0, 2, 4326, 1);");
            st.setString(1,featureCollectionName);
            st.execute();
            return this.get(featureCollectionName,false,-1,0, null);
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
            log.warn("Error executing sql: " + sql);
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
    public FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset, double[] bbox) {
        try {
            log.debug("Getting FeatureCollection: " + collectionName);
            TableConfig conf = getConfByAlias(collectionName);
            String queryName = collectionName;

            String geoCol = getGeometry(queryName);
            String idCol = null;

            if(conf != null) {
                queryName = conf.getTable();
                if (conf.getGeoCol() != null)
                    geoCol = conf.getGeoCol();

                if(conf.getIdCol() != null)
                    idCol = conf.getIdCol();
            }

            Statement stmt = c.createStatement();
            ResultSet rs = null;

            if(geoCol != null) {
                rs = stmt.executeQuery("SELECT *,AsEWKB(" + geoCol + ") from [" + queryName + "]");
            }else{
                rs = stmt.executeQuery("SELECT * FROM [" + queryName + "]");
            }
            return resultSetToFeatureCollection(rs, queryName, collectionName, idCol, geoCol, withSpatial, limit,offset,bbox);
        } catch (SQLException e) {
            log.warn("Error getting FeatureCollection: " + collectionName);
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

                Statement stmt = c.createStatement();
                ResultSet rs = null;
                String geoCol = getGeometry(table);
                String idCol = null;
                String alias = table;
                if (config.containsKey(table)) {
                    TableConfig c = config.get(table);
                    alias = c.getAlias();
                    if(c.getGeoCol() != null)
                        geoCol = c.getGeoCol();

                    if(c.getIdCol() != null)
                        idCol = c.getIdCol();
                }
                if (geoCol != null) {
                    rs = stmt.executeQuery("SELECT *,AsEWKB(" + geoCol  + ") FROM [" + table + "]");
                } else {
                    rs = stmt.executeQuery("SELECT * FROM [" + table + "]");
                }
                FeatureCollection fs = resultSetToFeatureCollection(rs, table, alias, idCol, geoCol, true, 0,0, null);
                if (fs != null)
                    fc.add(fs);
            } catch (SQLException e) {
                log.warn("Error occurred while converting table: " + table + " to FeatureCollection.");
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
     * @param alias Alias of the Table
     * @param geoCol geoColumn of the table
     * @param idCol idColumn of the table
     * @param withSpatial boolean if spatial data shall be included
     * @param limit limit on how many features shall be included
     * @param offset offset to the start of features
     * @param bbox optional, if given only features are returned if there bbox intersects the given one
     * @return  ResultSet with content of table
     */
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table, String alias, String idCol, String geoCol, boolean withSpatial, int limit, int offset, double[] bbox) {
        try {
            log.debug("Converting table: " + table + " to FeatureCollection");
            FeatureCollection fs = new FeatureCollection(alias);
            if(idCol == null)
                idCol = getPrimaryKey(table);
            //Creating offset
            for(int i = 0;i< offset;i++){
                rs.next();
            }
                int counter = 0;
                while (rs.next() && (counter < limit || limit == -1)) {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();
                    for (int x = 1; x <= md.getColumnCount(); x++) {
                        if (md.getColumnLabel(x).contains("OGC_FID") && idCol == null || md.getColumnName(x).equals(idCol)) {
                            //ID
                            f.setId(rs.getString(x));
                            log.debug("ID set");
                        } else {
                            //Normal Feature
                            if (!md.getColumnName(x).contains("AsEWKB(") && !md.getColumnName(x).contains(geoCol)) {
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
                    if (geoCol != null) {
                        log.debug("Set Geometry");
                        String geometry = rs.getString("AsEWKB(" + geoCol + ")");
                        if(geometry != null){
                        Geometry geometr = PGgeometry.geomFromString(geometry);
                        if (geometr.getSrid() == 0)
                            log.warn("SRID is 0, assuming that the format used 4326! Collection: " + alias);
                        else {
                            if (geometr.getSrid() != 4326) {
                                geometry = "'" + geometry + "'";
                                log.warn("SRID for collection: " + alias + " is not set to 4326!");
                                //Converting to 4326
                                ResultSet convSet = c.createStatement().executeQuery("SELECT AsEwkb(ST_Transform(GeomFromEWKB(" + geometry + "),4326)) FROM " + table);
                                if (convSet.next()) {
                                    String e = convSet.getString(1);
                                    if(e != null)
                                        geometr = PGgeometry.geomFromString(e);
                                }
                            }
                        }
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
                    if(intersect) {
                        f.setProperties(prop);
                        fs.addFeature(f);
                    }
                    counter++;
                }
            }
            if(geoCol != null) {
                Statement stmt = c.createStatement();
                ResultSet resultSet = stmt.executeQuery("SELECT AsEWKB(Extent(" + geoCol + ")) as table_extent FROM [" + table + "]");
                if (resultSet.next()) {
                    String ewkb = resultSet.getString(1);
                    if (ewkb != null) {
                        Geometry geometr = PGgeometry.geomFromString(ewkb);
                        if (geometr.getSrid() == 0)
                            log.warn("SRID is 0, assuming that the format used 4326! Collection: " + alias);
                        else {
                            if (geometr.getSrid() != 4326) {
                                ewkb = "'" + ewkb + "'";
                                //Converting to 4326.
                                ResultSet convSet = c.createStatement().executeQuery("SELECT AsEwkb(ST_Transform(GeomFromEWKB(" + ewkb + "),4326)) FROM " + table);
                                if (convSet.next()) {
                                    String e = convSet.getString(1);
                                    if (e != null)
                                        geometr = PGgeometry.geomFromString(e);
                                }
                            }
                        }
                        mil.nga.sf.geojson.Geometry g = EWKBtoGeo(geometr);
                        if (g != null) {
                            double[] array = g.getBbox();
                            if (array != null && withSpatial)
                                fs.setBB(DoubleStream.of(array).boxed().collect(Collectors.toList()));
                        }
                    }
                }
            }
            return fs;
        } catch (SQLException e) {
            log.warn("Error occured while converting table: " + table + " to FeatureCollection");
            return null;
        }
    }

    /**
     * Rename propertie of a table
     * @param table table the feature is conatained in
     * @param feature feature to be renamed
     * @param featureAlias alias to be used
     */
    public void renameProp(String table, String feature, String featureAlias){
        log.info("Renaming propertie: " + feature + " to " + featureAlias + ", in table " + table);
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
        log.info("Renaming table: " + table + " to: " + tableAlias);
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
    public String getId(){
        return id;
    }

    /**
     * Gets all tables from connector
     * @return ArrayList with table names
     */
    @JsonIgnore
    public ArrayList<String> getAllTables(){
        log.debug("Get all table names");
        try {
            ArrayList<String> out = new ArrayList<>();
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                String table = rs.getString(3);
                if(!table.contains("spatial_"))
                    out.add(table);
            }
            rs = md.getTables(null, null, null, new String[]{"VIEW"});
            while (rs.next()) {
                 out.add(rs.getString("TABLE_NAME"));
            }
            return out;
        }catch (SQLException e){
            log.warn("Error occurred while getting all tables. Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if table has a GEOMETRY column
     * @param table Table name to check
     * @return true if GEOMETRY exists, else false
     */
    public String getGeometry(String table){
        log.debug("Get geometry for table: " + table);
        try {
            PreparedStatement ps = c.prepareStatement("select f_geometry_column from geometry_columns where f_table_name = ?");
            ps.setString(1,table);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1).toUpperCase();
            }else{
                return null;
            }
        } catch (SQLException e) {
            log.warn("Error occurred while getting geometry for table: " + table + ". Error: " + e.getMessage() );
            return null;
        }
    }

    public String getPrimaryKey(String table){
        log.debug("Get PrimaryKey for table: " + table);
        try {
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getPrimaryKeys(null,null,table);
            if(rs.next()){
                return rs.getString(4).toUpperCase();
            }else{
                return null;
            }
        } catch (SQLException e) {
            log.error("Error occurred while getting primary key for table: " + table);
            return null;
        }
    }

    /**
     * Gets table name of alias
     * @param alias Table alias
     * @return real table name
     */
    public TableConfig getConfByAlias(String alias){
        log.debug("Getting config by alias for alias: " + alias);
        Iterator it = config.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            TableConfig t = (TableConfig) pair.getValue();
            if(t.getAlias().equals(alias)){
                return t;
            }
        }
        return null;
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
                    double x = geom.getFirstPoint().getX();
                    double y = geom.getFirstPoint().getY();
                    Point p = new mil.nga.sf.geojson.Point(new Position(x,y));
                    p.setBbox(new double[]{x,x,y,y});
                    return p;
                }
            return null;
    }


    public void setPath(String path){
        this.zwHostname = path;
    }

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
        if(zwHostname == null){
            return null;
        }
        try {
            File sqliteFile = new File(zwHostname);

            if(sqliteFile.exists()) {
                Properties prop = new Properties();
                prop.setProperty("enable_shared_cache", "true");
                prop.setProperty("enable_load_extension", "true");
                prop.setProperty("enable_spatialite", "true");
                Connection connection = DriverManager.getConnection("jdbc:spatialite:" + zwHostname, prop);
                c = connection;
                hostname = zwHostname;
                zwHostname = null;
                return null;
            }else{
                c = oldCon;
                return  "File does not exit";
            }
        } catch (SQLException e) {
            //Reset Connector to old params if error occurred
            c = oldCon;
            return e.getMessage();
        }
    }

    public void setGeo(String table, String column){
        if(config.containsKey(table)){
            config.get(table).setGeoCol(column);
        }else{
            TableConfig tc = new TableConfig(table,table);
            tc.setGeoCol(column);
            config.put(table,tc);
        }
    }

    public void setId(String table, String column){
        if(config.containsKey(table)){
            config.get(table).setIdCol(column);
        }else{
            TableConfig tc = new TableConfig(table,table);
            tc.setIdCol(column);
            config.put(table,tc);
        }
    }

}
