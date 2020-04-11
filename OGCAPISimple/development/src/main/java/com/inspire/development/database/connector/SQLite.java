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
    @JsonView(Views.Public.class)
    private String hostname;
    private Connection c;
    @JsonView(Views.Public.class)
    private String id;
    private HashMap<String, TableConfig> config;
    private HashMap<String, String> sqlString; // Table name, SQL String

    /**
     * Create sqlite connection
     * @param path sqlite file path
     * @param id connection id
     */
    @JsonCreator
    public SQLite(@JsonProperty("path")String path,@JsonProperty("id")String id) {
        this.id = id;
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

            for(String table:getAllTables())
                setTableExclude(table,true);

            log.info("Created SQL Connector with the id: " + id);
        } catch (SQLException e) {
            log.error("Error creating connector with the id: " + id + ". Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public HashMap<String, String> getSqlString() {
        return sqlString;
    }

    /**
     * {@inheritDoc}
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
            log.error("Error checking Connection for the connector: " + id);
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
            FeatureCollection fc = getFeatureCollectionByName(featureCollectionName,false,-1,0,null, null);
            if(check)
                sqlString.remove(featureCollectionName);
            return fc;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
    @JsonProperty
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public String updateConnector() {
        Connection oldCon = c;
        try {
            File sqliteFile = new File(hostname);

            if (sqliteFile.exists()) {
                Properties prop = new Properties();
                prop.setProperty("enable_shared_cache", "true");
                prop.setProperty("enable_load_extension", "true");
                prop.setProperty("enable_spatialite", "true");
                Connection connection = DriverManager.getConnection("jdbc:spatialite:" + hostname, prop);
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

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * Gets table config by alias
     * @param alias Table alias
     * @return table config if exists eles null
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
     * @return the geometry column name if one exists else null
     */
    public String getGeometry(String table) {
        log.debug("Get geometry for the table: " + table);
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
            log.warn("Error occurred while getting geometry for the table: "
                    + table
                    + ". Error: "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> getAllGeometry(String table) {
        ArrayList<String> names = new ArrayList<>();
        log.debug("Get geometry for the table: " + table);
        try {
            PreparedStatement ps = c.prepareStatement(
                    "select f_geometry_column from geometry_columns where f_table_name = ?");
            ps.setString(1, table);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                names.add(rs.getString(1).toUpperCase());
            }
        } catch (SQLException e) {
            log.warn("Error occurred while getting geometry for the table: "
                    + table
                    + ". Error: "
                    + e.getMessage());
            return null;
        }
        return names;
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
     * @throws Exception Thrown if any SQLException occurred.
     */
    public FeatureCollection getFeatureCollectionByName(String alias, boolean withSpatial, int limit, int offset, double[] bbox, Map<String,String> filterParams) throws Exception {
        if(c == null){
            return null;
        }

        if(c.isClosed()){
            updateConnector();
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




            log.debug("Converting table: " + queryName + " to featureCollection");
            ResultSet rs = SqlWhere(sql, filterParams, bbox, geoCol,queryName, limit, offset);
            //Creating featureCollection with given name
            FeatureCollection fs = new FeatureCollection(alias, withSpatial);
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
                                ColumnConfig columnConfig = tc.getMap().get(geoCol);
                                //Check if column is excluded
                                if(columnConfig == null || (columnConfig != null && !columnConfig.isExclude())) {
                                    String ewkb = rs.getString(x);
                                        if (ewkb != null) {
                                            Geometry geom = PGgeometry.geomFromString(ewkb);
                                            if (geom != null) {
                                                org.postgis.Point fp = geom.getFirstPoint();
                                                org.postgis.Point lp = geom.getLastPoint();
                                                f.setBbox(new double[]{fp.x, fp.y, lp.x, lp.y});
                                            }
                                        }
                                    }
                                } else {
                                    if (colName.equals("ogc_ewkb")) {
                                        ColumnConfig columnConfig = tc.getMap().get(geoCol);
                                        //Check if column is excluded
                                        if(columnConfig == null || (columnConfig != null && !columnConfig.isExclude())) {
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
                ResultSet resultSet = SqlBBox(sql,geoCol);

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
                            org.postgis.Point min = gm.getFirstPoint();
                            org.postgis.Point max = gm.getPoint(2);
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
     * Get primary key from table
     * @param table table name in db
     * @return the primary key column name
     */
    public String getPrimaryKey(String table) {
        log.debug("Get PrimaryKey for the table: " + table);
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

    /**
     * {@inheritDoc}
     */
    public ArrayList<String> getAllPrimaryKey(String table) {
        ArrayList<String> names = new ArrayList<>();
        log.debug("Get PrimaryKey for the table: " + table);
        try {
            DatabaseMetaData dm = c.getMetaData();
            ResultSet rs = dm.getIndexInfo(null, null, table, true, true);
            while(rs.next()) {
                names.add(rs.getString("column_name"));
            }
            if(names.size() == 0) {
                DatabaseMetaData md = c.getMetaData();
                ResultSet rs1 = md.getPrimaryKeys(null, null, table);
                while (rs1.next()) {
                    names.add(rs1.getString(4).toUpperCase());
                }
            }
        } catch (SQLException e) {
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
    public ResultSet SqlWhere(String sql, Map<String,String> filterParams, double[] bbox, String geoCol, String table, int limit ,int offset) throws SQLException{
        ResultSet rs;
        if((filterParams != null && filterParams.size() > 0) || bbox != null || geoCol != null){
            sql = "SELECT *, AsEWKB(Envelope(" + geoCol + ")) as ogc_bbox FROM (" + sql + ") as tabula";

            if(bbox != null || (filterParams != null && filterParams.size() > 0))
                sql += " where ";


            if(filterParams != null && filterParams.size() > 0) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    String col = getColumnByAlias(table, entry.getKey());
                    col = col == null ? entry.getKey() : col;
                    sql = sql + col + " like ? and ";
                }
            }

            if(bbox != null && geoCol != null) {
                sql += "Intersects(Envelope(" + geoCol + "),GeomFromEWKB(?))";
            }else {
                if(filterParams != null && filterParams.size() > 0)
                    sql = sql.substring(0, sql.length() - 4);
            }

            if(limit == -1){
                sql+=" OFFSET ?";
            }else{
                sql+=" LIMIT ? OFFSET ?";
            }

            PreparedStatement ps = c.prepareStatement(sql);

            int counter = 1;



            if(filterParams != null) {
                for (Map.Entry<String, String> entry : filterParams.entrySet()) {
                    ps.setString(counter, entry.getValue());
                    counter++;
                }
            }


            if(bbox != null && geoCol != null) {
                PGbox2d box = new org.postgis.PGbox2d(new org.postgis.Point(bbox[0], bbox[1]), new org.postgis.Point(bbox[2], bbox[3]));
                ps.setString(counter++, box.toString());
            }

            if(counter == -1){
                ps.setInt(counter++,limit);
            }

            ps.setInt(counter,offset);

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

    /**
     * Get BBOX for the whole table with given filters
     * @param sql    SQL to be executed
     * @param geoCol geometry column name
     * @return ResultSet with columns specified by filterparams
     * @throws SQLException if an error occurred while executing sql
     */
    public ResultSet SqlBBox(String sql, String geoCol) throws SQLException{
        ResultSet rs;
        sql ="SELECT AsEWKB(Extent("
                + geoCol
                + ")) as table_extent FROM ("
                + sql
                + ") as tabulana";
        sql = sql.replace("LIMIT ? OFFSET ?",  "");
        //Executing sql
        rs = c.createStatement().executeQuery(sql);
        return rs;
    }

    /**
     * Get column name by alias
     * @param table table name in db
     * @param alias column alias
     * @return column name from db if config exists else null
     */
    private String getColumnByAlias(String table, String alias){
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
     * Get config for connection
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
     * Get path of sqlite file
     * @return sqlite file path
     */
    @JsonProperty
    public String getHostname() {
        return hostname;
    }

    /**
     * Set sqlite path
     * @param path sqlite file path
     */
    public void setPath(String path) {
        this.hostname = path;
    }

    /**
     * Set connection id
     * @param id connection id
     */
    public void setConnectorId(String id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeSQL(String name){return sqlString.remove(name) != null;}
}
