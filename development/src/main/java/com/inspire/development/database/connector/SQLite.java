package com.inspire.development.database.connector;

import com.inspire.development.database.DBConnector;
import com.inspire.development.featureCollection.FeatureCollection;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.Point;
import mil.nga.sf.geojson.Position;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.sqlite.SQLiteConfig;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * DBConnector for a SQLite database
 */
public class SQLite implements DBConnector {
    private ArrayList<String> errorBuffer;
    private String hostname;
    private Connection c;
    private ArrayList<String> tableNames; // Stores table names that contain a GEOMETRY column


    /**
     * Craete DBConnector for SQLite Database
     *
     * @param path Path to the SQLite File
     * @return true if it worked false if error occurred. Error is stored in errorBuffer. See {@link SQLite#getErrorBuffer()}.
     */
    public SQLite(String path) {
        errorBuffer = new ArrayList<>();
        hostname = path;
        tableNames = new ArrayList<>();

        Connection connection = null;
        try {
            // create a database connection
            Properties prop = new Properties();
            prop.setProperty("enable_shared_cache", "true");
            prop.setProperty("enable_load_extension", "true");
            prop.setProperty("enable_spatialite", "true");
            connection = DriverManager.getConnection("jdbc:spatialite:" + hostname, prop);
            c = connection;
            Statement stat = c.createStatement();
            stat.execute("SELECT InitSpatialMetaData()");
            stat.close();
            updateTablesArray();
            System.out.println(tableNames);
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
        }

    }

    /**
     * Updates tableNames array to contain table names, that contain a GEOMETRY column
     */
    private void updateTablesArray() {
        try {
            tableNames = new ArrayList<>();
            DatabaseMetaData md = c.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                try {
                    Statement s = c.createStatement();
                    ResultSetMetaData rsm = s.executeQuery("SELECT  * FROM " + rs.getString(3)).getMetaData();
                    boolean contains = false;
                    //Iterate backwards because the GEOMETRY column is most of the time the last
                    for (int x = rsm.getColumnCount(); x > 0; x++) {
                        if (rsm.getColumnTypeName(x).contains("GEOMETRY")) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        tableNames.add(rs.getString(3));
                    }
                } catch (SQLException e) {
                    //some table names dont exist so do nothing
                }
            }
        } catch (SQLException e) {
            //Connector is closed
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
            if (c.isValid(10)) {
                return null;
            } else {
                return "Connection to " + hostname + " is closed or credentials are invalid";
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
        //TODO Implement delete
    }

    /**
     * Executes given SQL String
     *
     * @param sql SQL String to be executed
     * @return Feature Collection Array from SQL query result, null if error occurred. Error is stored in errorBuffer. See {@link SQLite#getErrorBuffer()}.
     */
    @Override
    public FeatureCollection[] execute(String sql) {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {

            }
            return new FeatureCollection[0];
        } catch (SQLException e) {
            errorBuffer.add(e.getMessage());
            return null;
        }
    }

    /**
     * Get FeatureCollection with given name
     *
     * @param collectionName FeatureCollection name from inside database
     * @return FeatureCollection from given name. Returns null if collection doesnt exists.
     */
    @Override
    public FeatureCollection get(String collectionName) {
        try {
            PreparedStatement stmt = c.prepareStatement("SELECT * from ?");
            stmt.setString(1, collectionName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

            }
            return new FeatureCollection("");
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Returns all FeatureCollections for the Database
     *
     * @return FeatureCollection Array, null if error occurred.
     */
    @Override
    public FeatureCollection[] getAll() {
        ArrayList<FeatureCollection> fc = new ArrayList<>();
        try {
            for (String table : tableNames) {

                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT *, ST_Y(GEOMETRY), ST_X(GEOMETRY) FROM " + table);
                FeatureCollection fs = resultSetToFeatureCollection(rs, table);
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
        //TODO implement save
    }

    /**
     * Update FeatureCollection in database
     *
     * @param fc fc to be updated
     */
    @Override
    public void update(FeatureCollection fc) {

    }

    /**
     * Get all errors of the database connector
     *
     * @return Array with all error Messages
     */
    public String[] getErrorBuffer() {
        return errorBuffer.toArray(new String[errorBuffer.size()]);
    }

    /**
     * Converts a ResultSet from a Table Query to a FeatureCollection
     * @param rs ResultSet from Table query
     * @param table Table name of query
     * @return  ResultSet with content of table
     */
    private FeatureCollection resultSetToFeatureCollection(ResultSet rs, String table) {
        try {
            FeatureCollection fs = new FeatureCollection(table);
            while (rs.next()) {
                Feature f = new Feature();
                HashMap<String, Object> prop = new HashMap<>();
                ResultSetMetaData md = rs.getMetaData();

                GeometryFactory gm = new GeometryFactory(new PrecisionModel(), 4326);
                WKBReader wkbr = new WKBReader(gm);
                double xP = -1;
                double yP = -1;

                for (int x = 1; x <= md.getColumnCount(); x++) {
                    if (md.getColumnName(x).contains("ST_X")) {
                        //Geometry Feature X
                       xP = rs.getFloat(x);
                    } else {
                        if (md.getColumnName(x).contains("ST_Y")) {
                            //Geometry Feature Y
                            yP = rs.getFloat(x);
                        }else {
                            if (md.getColumnLabel(x).contains("OGC_FID")) {
                                //ID
                                f.setId(rs.getString(x));
                            } else {
                                //Normal Feature
                                if(!md.getColumnTypeName(x).contains("GEOMETRY"))
                                    prop.put(md.getColumnName(x), rs.getObject(x));
                            }
                        }
                    }
                }
                if(xP != -1 && yP != -1)
                    f.setGeometry(new Point(new Position(xP,yP)));
                f.setProperties(prop);
                fs.addFeature(f);
            }
            return fs;
        } catch (SQLException e) {
            return null;
        }
    }
}
