/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.*;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.ColumnConfig;
import com.inspire.development.config.TableConfig;
import com.inspire.development.database.DBConnector;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgis.Geometry;
import org.postgis.PGbox2d;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * DBConnector for a PostgreSQL database
 */
@JsonTypeName("postgresql")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public class PostgreSQL implements DBConnector {
    static Logger log = LogManager.getLogger(PostgreSQL.class.getName());
    private HashMap<String, String> errorBuffer;
    @JsonProperty("hostname")
    private String hostname;
    private String database;
    private int port;
    private String schema;
    private String username;
    private String password;
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
            errorBuffer.put(getUUID(), e.getMessage());
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
            errorBuffer.put(getUUID(), e.getMessage());
            log.error("Error creating connector. Error: " + e.getMessage());
        }
    }

    /**
     * Get a random UUID
     *
     * @return random uuid
     */
    private static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, String> getSqlString() {
        return sqlString;
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
                return "an error occurred while creating the connection";
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
    public FeatureCollection execute(String sql, String featureCollectionName, boolean check) throws Exception {
        try {
            c.createStatement().executeQuery(sql);
            //SQL Executed
            sqlString.put(featureCollectionName, sql);
            FeatureCollection fc = getFeatureCollectionByName(featureCollectionName, false, -1, 0, null, null);
            if (check)
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
                                 double[] bbox, Map<String, String> filterParams) {
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
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Update a connector with the parameters setted with the setter methods
     *
     * @return null if everything worked else the Error
     */
    public String updateConnector() {
        Connection oldCon = c;
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
            return null;
        } catch (SQLException | ClassNotFoundException e) {
            errorBuffer.put(getUUID(), e.getMessage());
            c = oldCon;
            return e.getMessage();
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
                } catch (Throwable t) {
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

    /**
     * Set the column to be used for the geometry
     *
     * @param table  table the column is contained in
     * @param column column to be used
     */
    public void setGeo(String table, String column) {
        if (config.containsKey(table)) {
            config.get(table).setGeoCol(column);
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.setGeoCol(column);
            config.put(table, tc);
        }
    }

    /**
     * Set the id column to be used in Features
     *
     * @param table  table the id is contained in
     * @param column column to be used
     */
    public void setId(String table, String column) {
        if (config.containsKey(table)) {
            config.get(table).setIdCol(column);
        } else {
            TableConfig tc = new TableConfig(table, table);
            tc.setIdCol(column);
            config.put(table, tc);
        }
    }


    /**
     * Exclude a column from the api
     *
     * @param table   table the column is contained in
     * @param column  column to be excluded
     * @param exclude true if it should be excluded else false
     */
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

    /**
     * Exclude table from the api
     *
     * @param table   table to be excluded
     * @param exclude true if it should be excluded else false
     */
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


    /**
     * Converts a featureCollectionId to a FeatureCollection object
     *
     * @param alias        featureCollectionId from the API
     * @param withSpatial  true if spatial info shall be provided in the response
     * @param limit        limit on how many items shall be included in the response
     * @param offset       offset to the data in the database
     * @param bbox         array in the form of [xmin, ymin, xmax, ymax]. Only data with an intersecting boundingbox is included in the response
     * @param filterParams Params to be filtered by. Null if nothing should be filtered by
     * @return FeatureCollection with data specified by the params
     * @throws Exception Thrown if any SQLException occurred
     */
    public FeatureCollection getFeatureCollectionByName(String alias, boolean withSpatial, int limit, int offset, double[] bbox, Map<String, String> filterParams) throws Exception {
        if (c == null) {
            return null;
        }

        TableConfig tc = getConfByAlias(alias);
        String queryName = alias;
        String geoCol;
        String idCol;
        if (tc != null) {
            queryName = tc.getTable();
            //Setting geoCol and idCol if it is set in the config
            geoCol = tc.getGeoCol() != null ? tc.getGeoCol() : getGeometry(queryName);
            idCol = tc.getIdCol() != null ? tc.getIdCol() : getPrimaryKey(queryName);
        } else {
            geoCol = getGeometry(queryName);
            idCol = getPrimaryKey(queryName);
        }

        if (config.containsKey(queryName) && config.get(queryName).isExclude()) {
            return null;
        }

        //Checking if table is a view
        String sql = sqlString.get(queryName);
        boolean isView = sql != null;
        sql = sql != null ? sql : "SELECT * FROM " + schema + "." + queryName;

        sql += " LIMIT ? OFFSET ?";

        log.debug("Converting table: " + queryName + " to featureCollection");
        //Executing sql
        ResultSet rs = SqlWhere(sql, filterParams, bbox, geoCol, queryName, limit, offset);

        ResultSet foreignKeys = c.getMetaData().getImportedKeys(null, null, queryName);

        HashMap<String, String> fk = new HashMap<>();

        if (foreignKeys.next()) {
            //Key in current table
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
            //Table to link to
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            fk.put(fkColumnName, (pkTableName + ";" + pkColumnName));
        }
        //Creating featureCollection with given name
        FeatureCollection fs = new FeatureCollection(alias);

        while (rs.next()) {
            try {
                Feature f = new Feature();
                HashMap<String, Object> prop = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();
                for (int x = 1; x <= md.getColumnCount(); x++) {
                    String colName = md.getColumnName(x);
                    if (colName.equals("ogc_bbox")) {
                        org.postgis.PGgeometry box = (org.postgis.PGgeometry) rs.getObject(x);
                        if (box != null) {
                            Point fp = box.getGeometry().getFirstPoint();
                            Point lp = box.getGeometry().getLastPoint();
                            f.setBbox(new double[]{fp.x, fp.y, lp.x, lp.y});
                        }
                        continue;
                    }
                    if (colName.equals(idCol)) {
                        //ID
                        f.setId(rs.getString(x));
                        continue;
                    }
                    //Normal Feature
                    if (colName.equals(geoCol)) {
                        PGgeometry geom = (PGgeometry) rs.getObject(x);
                        if (geom != null) {
                            mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geom.getGeometry());
                            f.setGeometry(geo);
                        }
                        continue;
                    }
                    String col = md.getColumnName(x);
                    Object o = rs.getObject(x);
                    if (fk.containsKey(colName)) o = "ogc_fk;" + fk.get(colName) + ";" + o;
                    if (tc != null) {
                        ColumnConfig columnConfig = tc.getMap().get(col);
                        if (columnConfig != null) {
                            if (!columnConfig.isExclude()) {
                                if (columnConfig.getAlias() == null) {
                                    prop.put(colName, o);
                                } else {
                                    prop.put(columnConfig.getAlias(), o);
                                }
                            }
                            continue;
                        }
                    }
                    prop.put(colName, o);
                    continue;
                }
                f.setProperties(prop);
                fs.addFeature(f);
            } catch (Exception e) {
                log.error("An error occurred while converting feature collection, table" + queryName);
            }
        }

        if (geoCol != null && withSpatial) {
            log.debug("Getting Bounding Box for Table: " + queryName);
            Statement stmt = c.createStatement();
            //ST_SetSRID -> transforms Box to Polygon
            ResultSet resultSet = SqlBBox(sql, geoCol);

            if (resultSet.next()) {
                String ewkb = resultSet.getString(1);
                if (ewkb != null) {
                    Geometry gm = PGgeometry.geomFromString(ewkb);
                    if (gm.getSrid() == 0) {
                        log.warn("SRID is 0, assuming that the format used is 4326! Collection: " + alias);
                    }
                    if (gm.getSrid() != 4326 || gm.getSrid() != 0) {
                        log.warn("SRID for collection: " + alias + " is not set to 4326!");
                    } else {
                        mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(gm);
                        if (geo != null) {
                            double[] bounding = geo.getBbox();
                            if (bounding != null) {
                                fs.setBB(DoubleStream.of(bounding).boxed().collect(Collectors.toList()));
                            }
                        }
                    }
                }
            }
        }
        return fs;
    }

    /**
     * Get the Resultset with the given filters
     *
     * @param sql          sql to be executed
     * @param filterParams Parameters to be filtered by
     * @param bbox         bbox to be used while filtering
     * @param geoCol       geometry column to be used
     * @param table        the table name
     * @return ResultSet with columns matching the params
     * @throws Exception
     */
    public ResultSet SqlWhere(String sql, Map<String, String> filterParams, double[] bbox, String geoCol, String table, int limit, int offset) throws Exception {
        ResultSet rs;
        if ((filterParams != null && filterParams.size() > 0) || bbox != null || geoCol != null) {
            sql = "SELECT *, ST_Envelope(" + geoCol + ") as ogc_bbox FROM (" + sql + ") as tabula";

            if (bbox != null || (filterParams != null && filterParams.size() > 0))
                sql += " where ";

            if (filterParams != null && filterParams.size() > 0) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    String col = getConfigByAlias(table, entry.getKey());
                    col = col == null ? entry.getKey() : col;
                    sql = sql + col + "::varchar = ? and ";
                }
            }
            if (bbox != null && geoCol != null) {
                sql += "ST_Intersects(ST_Envelope(" + geoCol + "),?)";
            } else {
                if (filterParams != null && filterParams.size() > 0)
                    sql = sql.substring(0, sql.length() - 4);
            }
            PreparedStatement ps = c.prepareStatement(sql);
            int counter = 1;

            if (counter == -1) {
                //Should be ALL
                ps.setInt(counter++, limit);
            } else {
                ps.setInt(counter++, limit);
            }

            ps.setInt(counter++, offset);

            if (filterParams != null) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    ps.setString(counter, entry.getValue());
                    counter++;
                }
            }

            if (bbox != null && geoCol != null) {
                PGbox2d box = new org.postgis.PGbox2d(new Point(bbox[0], bbox[1]), new Point(bbox[2], bbox[3]));
                ps.setObject(counter, box);
            }

            rs = ps.executeQuery();
        } else {
            //Executing sql
            rs = c.createStatement().executeQuery(sql);
        }
        return rs;
    }

    /**
     * Get BBOX for the whole table with fiven filters
     *
     * @param sql    SQL to be executed
     * @param geoCol geometry column name
     * @return ResultSet with columns specified by filterparams
     * @throws SQLException
     */
    public ResultSet SqlBBox(String sql, String geoCol) throws SQLException {
        ResultSet rs;
        sql = "SELECT ST_SetSRID(ST_Extent("
                + geoCol
                + "), 4326) as table_extent FROM ("
                + sql
                + ") as tabulana";
        //Executing sql
        PreparedStatement ps = c.prepareStatement(sql);

        ps.setInt(1, 0);
        ps.setInt(2, 0);

        rs = ps.executeQuery();
        return rs;
    }

    private String getConfigByAlias(String table, String alias) {
        TableConfig tc = config.get(table);
        if (tc == null) return null;
        for (Map.Entry<String, ColumnConfig> entry : tc.getMap().entrySet()) {
            if (entry.getValue().getAlias().equals(alias)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get the geometry column name of a table
     *
     * @param table Table name to get the column from
     * @return name if one exists else null
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
     * Converts Geometry Object to a Geometry Object
     *
     * @param geom Geometry Object
     * @return Geometry object if string is valid, else null
     */
    public mil.nga.sf.geojson.Geometry EWKBtoGeo(Geometry geom) {
        if (geom != null) {
            log.debug("Converting EWKB to Geometry");

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
                return new mil.nga.sf.geojson.Point(
                        new Position(geom.getFirstPoint().getX(), geom.getFirstPoint().getY()));
            }
        }
        return null;
    }

    /**
     * Get all errors of the database connection
     *
     * @return Array with all error Messages
     */
    @JsonIgnore
    public HashMap<String, String> getErrorBuffer() {
        return errorBuffer;
    }

    /**
     * Remove an error
     *
     * @param uuid UUID to remove
     * @return true if removed else false
     */
    public boolean removeError(String uuid) {
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

    /**
     * Hostname of the connector
     *
     * @return
     */
    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the hostname, not used till updateConnector was called
     *
     * @param hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Get Primary Key for table
     *
     * @param table table name to get from
     * @return the primary key name
     */
    public String getPrimaryKey(String table) {
        log.debug("Get PrimaryKey for table: " + table);
        try {
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getPrimaryKeys(null, schema, table);
            if (rs.next()) {
                return rs.getString(4).toLowerCase();
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean removeSQL(String name) {
        return sqlString.remove(name) != null;
    }
}
