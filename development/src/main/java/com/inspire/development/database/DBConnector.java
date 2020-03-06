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
package com.inspire.development.database;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@JsonTypeName("dbconnector")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface DBConnector {
    String database = "";
    String hostname = "";
    String password = "";
    String username = "";

    /**
     * Check connection
     * @return null if connection is good else the error
     */
    String checkConnection();

    /**
     * Delete feature collection from db
     * @param fc feature collection to be deleted
     */
    void delete(String fc);

    /**
     * Executes given SQL String
     * @param sql SQL String to be executed
     * @param fcName name to be used
     * @param check true if featureCollection should be kept
     * @return Feature Collection from SQL query result, null if error occurred.
     */
    FeatureCollection execute(String sql, String fcName, boolean check) throws Exception;

    /**
     * Get FeatureCollection with given name
     * @param collectionName FeatureCollection name
     * @param withSpatial true if spatial information should be included
     * @param limit size of feature array
     * @param offset offset to dataset beginning
     * @param bbox bounding box to be filtered by
     * @param filterParams parameters to be filtered by
     * @return FeatureCollection from given name. Returns null if collection doesnt exists or error occurred.
     */
    FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset,
                          double[] bbox, Map<String,String> filterParams);

    /**
     * Get all FeatureCollections from database that are not excluded
     * @return
     */
    FeatureCollection[] getAll();

    /**
     * Save featureCollection in db
     * @param fc featureCollection to be stored
     */
    void save(FeatureCollection fc);

    /**
     * Update FeatureCollection
     * @param fc featureCollection to be updated
     */
    void update(FeatureCollection fc);

    /**
     * Get id of db connection
     * @return id
     */
    String getId();

    /**
     * Get all tables and views
     * @return list
     */
    ArrayList<String> getAllTables();

    /**
     * Get all columns of table
     * @param table db table name or view name
     * @return list
     */
    ArrayList<String> getColumns(String table);

    /**
     * Rename table to new name
     * @param table table to be renamed
     * @param tableAlias new name
     */
    void renameTable(String table, String tableAlias);

    public ArrayList<String> getAllPrimaryKey(String table);

    public ArrayList<String> getAllGeometry(String table);

    void renameProp(String table, String feature, String featureAlias);

    void setGeo(String table, String column);

    void setId(String table, String column);

    String updateConnector();

    void setColumnExclude(String table, String column, boolean exclude);

    void setTableExclude(String table, boolean exclude);

    boolean removeSQL(String name);
}
