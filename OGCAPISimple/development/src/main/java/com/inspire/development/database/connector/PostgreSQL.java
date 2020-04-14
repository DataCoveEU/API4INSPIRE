/*
 * The OGC API Simple provides enviromental data
 * Created on Wed Feb 26 2020
 * @author Tobias Pressler
 * Copyright (c) 2020 - Tobias Pressler
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.

 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */

package com.inspire.development.database.connector;

import com.fasterxml.jackson.annotation.*;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.ColumnConfig;
import com.inspire.development.config.TableConfig;
import com.inspire.development.config.Views;
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



/**
 * DBConnector for a PostgreSQL database
 */
@JsonTypeName("postgresql")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public class PostgreSQL implements DBConnector {
    static Logger log = LogManager.getLogger(PostgreSQL.class.getName());
    @JsonView(Views.Public.class)
    private String hostname;
    @JsonView(Views.Public.class)
    private String database;
    @JsonView(Views.Public.class)
    private int port;
    @JsonView(Views.Public.class)
    private String schema;
    @JsonView(Views.Public.class)
    private String username;
    @JsonView(Views.Public.class)
    private String password;

    private HashMap<String, String> sqlString; // Table name, SQL String
    private Connection c;
    @JsonView(Views.Public.class)
    private String id;
    private HashMap<String, TableConfig> config;

    /**
     * Create a new PostgeSQL connection
     * @param hostname hostname
     * @param port port
     * @param database database name
     * @param schema schema name
     * @param id connection id
     * @param username username
     * @param password password
     */
    public PostgreSQL(String hostname, int port, String database, String schema, String id,
                      String username, String password) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        config = new HashMap<>();
        this.username = username;
        this.password = password;
        this.sqlString = new HashMap<>();

        Connection connection = null;
        Properties prop = new Properties();
        prop.setProperty("user", username);
        prop.setProperty("password", password);
        try {
            Class.forName("org.postgresql.Driver");
            connection =
                    DriverManager.getConnection("jdbc:postgresql://" + hostname + ":" + port + "/" + database,
                            prop);
            c = connection;

            for(String table:getAllTables())
                setTableExclude(table,true);
            log.info("Postgres Connector created for the path: " + hostname);
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error creating connector. Error: ", e);
        }
    }

    @JsonCreator
    public PostgreSQL(@JsonProperty("hostname") String hostname, @JsonProperty("id") String id, @JsonProperty("port") int port,
                      @JsonProperty("schema") String schema, @JsonProperty("database") String database,
                      @JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schema = schema;
        this.username = username;
        this.password = password;
        this.sqlString = new HashMap<>();
        this.config = new HashMap<>();


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

            for(String table:getAllTables())
                setTableExclude(table,true);

            log.info("Postgres Connector created from config for the path: " + hostname);
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error creating connector. Error: ", e);
        }
    }

    /**
     * Get used username for database connection
     * @return username
     */
    @JsonProperty
    public String getUsername() {
        return username;
    }

    /**
     * Set username
     * @param username username to be used
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get used password for database connection
     * @return password
     */
    @JsonProperty
    public String getPassword() {
        return password;
    }

    /**
     * Set password
     * @param password password to be used
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get mapped sql views
     * @return FeatureCollection name and sql statement
     */
    public HashMap<String, String> getSqlString() {
        return sqlString;
    }

    /**
     * {@inheritDoc}
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
            log.warn("Error checking connection for the connector: " + hostname);
            return e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String fc) {
        //Not used
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public FeatureCollection execute(String sql, String featureCollectionName, boolean check){
        try {
            sql = sql.replace(";","");
            c.createStatement().executeQuery(sql);
            //SQL Executed
            sqlString.put(featureCollectionName, sql);
            FeatureCollection fc = getFeatureCollectionByName(featureCollectionName, false, -1, 0, null, null);
            if (check)
                sqlString.remove(featureCollectionName);
            return fc;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * {@inheritDoc}
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
            log.error("Error occurred while getting FeatureCollection " + collectionName + ". Error: ", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
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
            c = oldCon;
            return e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
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
                } catch (Exception e) {
                    log.error("An error occurred converting table " + table + ". Exception: ", e);
                }
            }
        }
        return fc.toArray(new FeatureCollection[fc.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(FeatureCollection fc) {
        //Not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(FeatureCollection fc) {
        //Not used
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            log.error("Failed to get all tables. Error: ", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
   @Override
    public ArrayList<String> getColumns(String table) {
        log.debug("Getting all Columns for the table: " + table);
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
     * {@inheritDoc}
     */
    @Override
    public void renameTable(String table, String tableAlias) {
        if (config.containsKey(table)) {
            config.get(table).setAlias(tableAlias);
        } else {
            TableConfig tc = new TableConfig(table, tableAlias);
            config.put(table, tc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     */
   @Override
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
     * {@inheritDoc}
     */
   @Override
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
     * {@inheritDoc}
     */
    @Override
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
     * Gets table config by alias
     * @param alias Table alias
     * @return TableConfig
     */
    public TableConfig getConfByAlias(String alias) {
        log.debug("Getting the table by alias: " + alias);
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
     * @param alias        featureCollectionId from the API
     * @param withSpatial  true if spatial info shall be provided in the response
     * @param limit        limit on how many items shall be included in the response
     * @param offset       offset to the data in the database
     * @param bbox         array in the form of [xmin, ymin, xmax, ymax]. Only data with an intersecting boundingbox is included in the response
     * @param filterParams Params to be filtered by. Null if nothing should be filtered by
     * @return FeatureCollection with data specified by the params
     * @throws Exception Thrown if any SQLException occurred.
     */
    public FeatureCollection getFeatureCollectionByName(String alias, boolean withSpatial, int limit, int offset, double[] bbox, Map<String, String> filterParams) throws Exception {
        if (c == null) {
            return null;
        }

        if(c.isClosed()){
            updateConnector();
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


        log.debug("Converting table: " + queryName + " to featureCollection");
        //Executing sql
        ResultSet rs = SqlWhere(sql, filterParams, bbox, geoCol, queryName, limit, offset);

        HashMap<String, String> fk = new HashMap<>();

        try{
            ResultSet foreignKeys = c.getMetaData().getImportedKeys(null, schema, queryName);

            if (foreignKeys.next()) {
                //Key in current table
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                if(config.containsKey(pkTableName)){
                    pkTableName = config.get(pkTableName).getAlias();
                }
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                //Table to link to
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                fk.put(fkColumnName, (pkTableName + ";" + pkColumnName));
            }
        }catch (SQLException e){
            log.warn("Error getting foreign keys in the table: "+ queryName);
        }
        //Creating featureCollection with given name
        FeatureCollection fs = new FeatureCollection(alias, withSpatial);

        while (rs.next()) {
            try {
                Feature f = new Feature();
                HashMap<String, Object> prop = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();
                for (int x = 1; x <= md.getColumnCount(); x++) {
                    String colName = md.getColumnName(x);
                    if (colName.equals("ogc_bbox")) {
                        ColumnConfig columnConfig = tc.getMap().get( md.getColumnName(x));
                        //Check if column is excluded
                        if(columnConfig == null || (columnConfig != null && !columnConfig.isExclude())) {
                            org.postgis.PGgeometry box = (org.postgis.PGgeometry) rs.getObject(x);
                            if (box != null) {
                                Point fp = box.getGeometry().getFirstPoint();
                                Point lp = box.getGeometry().getLastPoint();
                                f.setBbox(new double[]{fp.x, fp.y, lp.x, lp.y});
                            }
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
                        ColumnConfig columnConfig = tc.getMap().get( md.getColumnName(x));
                        //Check if column is excluded
                        if(columnConfig == null || (columnConfig != null && !columnConfig.isExclude())) {
                            PGgeometry geom = (PGgeometry) rs.getObject(x);
                            if (geom != null) {
                                mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geom.getGeometry());
                                f.setGeometry(geo);
                            }
                        }
                        continue;
                    }
                    String col = md.getColumnName(x);
                    Object o = rs.getObject(x);
                    if(o instanceof PGgeometry){
                        o = o.toString();
                        //Auto use geo in view if nothing is set
                        if(isView && geoCol == null){
                            setGeo(queryName, col);
                            PGgeometry geom = (PGgeometry) rs.getObject(x);
                            if (geom != null) {
                                mil.nga.sf.geojson.Geometry geo = EWKBtoGeo(geom.getGeometry());
                                f.setGeometry(geo);
                            }
                            continue;
                        }
                    }
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
            ResultSet resultSet = SqlBBox(sql, geoCol);

            if (resultSet.next()) {
                String ewkb = resultSet.getString(1);
                if (ewkb != null) {
                    Geometry gm = PGgeometry.geomFromString(ewkb);
                    if (gm.getSrid() == 0) {
                        log.warn("SRID is 0, assuming the format used is 4326! Collection: " + alias);
                    }
                    if (gm.getSrid() != 4326 && gm.getSrid() != 0) {
                        log.warn("SRID for collection: " + alias + " is not set to 4326!");
                    } else {
                        ArrayList<Double> bb = new ArrayList<>();
                        Point min = gm.getFirstPoint();
                        Point max = gm.getPoint(2);
                        bb.add(min.getX());
                        bb.add(min.getY());
                        bb.add(max.getX());
                        bb.add(max.getY());
                        fs.setBB(bb);
                    }
                }
            }
        }
        return fs;
    }

    /**
     * Get the Resultset with the given filters
     * @param sql          sql to be executed
     * @param filterParams Parameters to be filtered by
     * @param bbox         bbox to be used while filtering
     * @param geoCol       geometry column to be used
     * @param table        the table name
     * @param limit         limit to be used
     * @param offset        offset to start data
     * @return ResultSet with columns matching the params
     * @throws Exception an error occurred while executing sql
     */
    public ResultSet SqlWhere(String sql, Map<String, String> filterParams, double[] bbox, String geoCol, String table, int limit, int offset) throws Exception {
        ResultSet rs;
        if ((filterParams != null && filterParams.size() > 0) || bbox != null || geoCol != null) {
            sql = "SELECT *, ST_Envelope(" + geoCol + ") as ogc_bbox FROM (" + sql + ") as tabula";

            if (bbox != null || (filterParams != null && filterParams.size() > 0))
                sql += " where ";

            if (filterParams != null && filterParams.size() > 0) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    String col = getColumnByAlias(table, entry.getKey());
                    col = col == null ? entry.getKey() : col;
                    sql = sql + col + "::varchar like ? and ";
                }
            }
            if (bbox != null && geoCol != null) {
                sql += "ST_Intersects(ST_Envelope(" + geoCol + "),?)";
            } else {
                if (filterParams != null && filterParams.size() > 0)
                    sql = sql.substring(0, sql.length() - 4);
            }

            if(limit == -1){
                sql+=" OFFSET ?";
            }else{
                sql+=" LIMIT ? OFFSET ?";
            }

            PreparedStatement ps = c.prepareStatement(sql);
            int counter = 1;


            if (filterParams != null) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    String replaced = entry.getValue().replace("*","%");
                    ps.setString(counter, replaced);
                    counter++;
                }
            }

            if (bbox != null && geoCol != null) {
                PGbox2d box = new PGbox2d(new Point(bbox[0], bbox[1]), new Point(bbox[2], bbox[3]));
                ps.setObject(counter++, box);
            }

            if (limit != -1) {//Should be ALL
                ps.setInt(counter++, limit);
            }

            ps.setInt(counter, offset);

            rs = ps.executeQuery();
        } else {

            if(limit == -1){
                sql+=" OFFSET ?";
            }else{
                sql+=" LIMIT ? OFFSET ?";
            }
            //Executing sql
            PreparedStatement ps = c.prepareStatement(sql);

            if(limit == -1){
                ps.setInt(1,offset);
            }else{
                ps.setInt(1,limit);
                ps.setInt(2,offset);
            }
            //Executing sql
            rs = ps.executeQuery();
        }
        return rs;
    }

    /**
     * Get BBOX for the whole table with given filters
     * @param sql    SQL to be executed
     * @param geoCol geometry column name
     * @return ResultSet with columns specified by filterparams
     * @throws SQLException if an error occurred while executing sql
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

        rs = ps.executeQuery();
        return rs;
    }

    /**
     * Get column name by alias
     * @param table table the column is located in
     * @param alias column alias
     * @return column name if config exists. Null if no config is set.
     */
    private String getColumnByAlias(String table, String alias) {
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
     * @param table Table name from db
     * @return name if one exists else null
     */
    public String getGeometry(String table) {
        try {
            log.debug("Getting geometry columns for the table: " + table);
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
            log.warn("Error getting Geometry for the table: " + table);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<String> getAllGeometry(String table) {
        ArrayList<String> names = new ArrayList<>();
        try {
            log.debug("Getting geometry columns for the table: " + table);
            PreparedStatement ps = c.prepareStatement(
                    "select f_geometry_column from geometry_columns where f_table_schema = ? and f_table_name = ?");
            ps.setString(1, schema);
            ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        } catch (SQLException e) {
            log.warn("Error getting Geometry for the table: " + table);
            return null;
        }
        return names;
    }

    /**
     * Converts Geometry Object to a Geometry Object
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
     * Get Config
     * @return config
     */
    public HashMap<String, TableConfig> getConfig() {
        return config;
    }

    @Override
    public void setConfig(HashMap<String, TableConfig> c) {
        this.config = c;
    }

    @Override
    public void setSqlString(HashMap<String, String> sqlString) {
        this.sqlString = sqlString;
    }

    /**
     * Hostname of the connector
     * @return hostname
     */
    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the hostname, not used till updateConnector was called
     * @param hostname hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Get daabase name
     * @return database name
     */
    @JsonProperty
    public String getDatabase() {
        return database;
    }

    /**
     * Set database name
     * @param database database name
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Get port
     * @return port
     */
    @JsonProperty
    public int getPort() {
        return port;
    }

    /**
     * Set database port
     * @param port db port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get schema name
     * @return schema name
     */
    @JsonProperty
    public String getSchema() {
        return schema;
    }

    /**
     * Set schema name
     * @param schema schema name
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Get Primary Key for table
     * @param table table name to get from
     * @return the primary key name
     */
    public String getPrimaryKey(String table) {
        log.debug("Get PrimaryKey for the table: " + table);
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

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> getAllPrimaryKey(String table) {
        ArrayList<String> names = new ArrayList<>();
        log.debug("Get PrimaryKey for the table: " + table);
        try {
            DatabaseMetaData dm = c.getMetaData();
            ResultSet rs = dm.getIndexInfo(null, schema, table, true, true);
            while(rs.next()) {
                names.add(rs.getString("column_name"));
            }
            if(names.size() == 0){
                //Fallback
                DatabaseMetaData md = c.getMetaData();
                ResultSet rs1 = md.getPrimaryKeys(null, schema, table);
                while (rs1.next()) {
                    names.add(rs1.getString(4).toLowerCase());
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeSQL(String name) {
        return sqlString.remove(name) != null;
    }
}