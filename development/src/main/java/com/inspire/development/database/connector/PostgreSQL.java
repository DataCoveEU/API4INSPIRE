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
    @JsonProperty("name")
    private String name;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column
    @JsonProperty("config")
    private HashMap<String,TableConfig> config;
    @JsonProperty("SQLString")
    private HashMap<String,String> sqlList; //FCName, SQL


    public PostgreSQL(String hostname, int port, String database, String schema, String name) {
        this.name = name;
        errorBuffer = new ArrayList<>();
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        tableNames = new ArrayList<>();
        config = new HashMap<>();
        sqlList = new HashMap<>();

        Connection connection = null;
        try {
            // create a database connection
            //jdbc:postgresql://host:port/database
            Properties prop = new Properties();
            prop.setProperty("user", "inspire");
            prop.setProperty("password", "1nsp1r3_2#2#");
            connection = DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database + "?currentSchema=" + schema, prop);
            c = connection;
            ((org.postgresql.PGConnection)c).addDataType("geometry", (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
            ((org.postgresql.PGConnection)c).addDataType("box3d", (Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            errorBuffer.add(e.getMessage());
        }

    }

    @JsonCreator
    public PostgreSQL(@JsonProperty("hostname")String hostname, @JsonProperty("name")String name, @JsonProperty("config")HashMap<String,TableConfig> config, @JsonProperty("SQLString") HashMap<String,String> sql, @JsonProperty("port")int port, @JsonProperty("schema")String schema, @JsonProperty("database")String database) {
        this.config = config;
        this.name = name;
        this.sqlList = sql;
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
     * @return Feature Collection Array from SQL query result, null if error occurred. Error is stored in errorBuffer. See {@link PostgreSQL#getErrorBuffer()}.
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
                    rs = stmt.executeQuery("SELECT * from " + queryName);
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
                    rs = stmt.executeQuery("SELECT * FROM " + table);
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
            FeatureCollection fs = new FeatureCollection(alias);
            if(withProps) {
                while (rs.next()) {
                    Feature f = new Feature();
                    HashMap<String, Object> prop = new HashMap<>();
                    ResultSetMetaData md = rs.getMetaData();


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
                                /*if(o == null){
                                       errorBuffer.add("Propertie null at: " + table + ", Id: " + f.getId());
                                   }*/
                                prop.put(col, o);
                            }
                        }
                    }

                    String geometry = rs.getString("geom");
                    if(geometry != null) {
                        Geometry geom = PGgeometry.geomFromString(geometry);
                        if (geom.getType() == 3) {
                            List<List<Position>> l = new ArrayList<>();
                            ArrayList<Position> li = new ArrayList<>();

                            int x = 1;
                            Point p = geom.getFirstPoint();
                            do {

                                li.add(new Position(p.getX(), p.getY()));
                                p = geom.getPoint(x);
                                x++;
                            } while ((!p.equals(geom.getLastPoint())));
                            l.add(li);
                            f.setGeometry(new Polygon(l));
                        }
                        if (geom.getType() == 1) {
                            f.setGeometry(new mil.nga.sf.geojson.Point(new Position(geom.getFirstPoint().getX(), geom.getFirstPoint().getY())));
                        }
                    }
                    f.setProperties(prop);
                    fs.addFeature(f);
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
            PreparedStatement stmt = c.prepareStatement("select * from information_schema.tables where table_schema =?");
            stmt.setString(1,schema);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                //String table = rs.getString(3);
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
            stmt.executeQuery("SELECT geom FROM " + table);
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
}
