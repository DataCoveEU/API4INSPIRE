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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.TableConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@JsonTypeName("dbconnector")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface DBConnector {

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
    FeatureCollection execute(String sql, String fcName, boolean check);

    /**
     * Get FeatureCollection with given name
     * @param collectionName FeatureCollection name
     * @param withSpatial true if spatial information should be included
     * @param limit size of feature array
     * @param offset offset to dataset beginning
     * @param bbox bounding box to be filtered by
     * @param filterParams parameters to be filtered by
     * @return FeatureCollection from given name. Returns null if collection doesn't exist or error occurred.
     */
    FeatureCollection get(String collectionName, boolean withSpatial, int limit, int offset,
                          double[] bbox, Map<String,String> filterParams);

    /**
     * Get all FeatureCollections from database that are not excluded
     * @return array with all FeatureCollections
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
     * Rename a table to new name
     * @param table table to be renamed
     * @param tableAlias new name
     */
    void renameTable(String table, String tableAlias);

    /**
     * Get all primary keys from a table
     * @param table table name in db
     * @return ArrayList containing all columns that are marked as primary keys in the db
     */
    ArrayList<String> getAllPrimaryKey(String table);

    /**
     * Get all geometry columns from a table
     * @param table table name in db
     * @return ArrayList containing all columns that contain geometries
     */
    ArrayList<String> getAllGeometry(String table);

    /**
     * Rename property in api. Only set virtually so no changes are written to db.
     * @param table table name from db
     * @param feature feature name to be renamed
     * @param featureAlias feature name to be used in api
     */
    void renameProp(String table, String feature, String featureAlias);

    /**
     * Set geometry column to be used in the api
     * @param table table name from db
     * @param column column to be used
     */
    void setGeo(String table, String column);

    /**
     * Set id column to be used in the api
     * @param table table name from db
     * @param column column to be used
     */
    void setId(String table, String column);

    /**
     * Update connector with new properties
     * @return null if successfully. A string if an error occurred.
     */
    String updateConnector();

    /**
     * Set column to be excluded in api
     * @param table table name in db
     * @param column column name in db
     * @param exclude true if excluded. False if column shall be included.
     */
    void setColumnExclude(String table, String column, boolean exclude);

    /**
     * Set table to be excluded in the api
     * @param table table name in db
     * @param exclude true if excluded. False if column shall be included.
     */
    void setTableExclude(String table, boolean exclude);

    /**
     * Remove sql view by the name.
     * @param name sql view name
     * @return true if view could be deleted. False not found.
     */
    boolean removeSQL(String name);

    /**
     * Get sql view config
     * @return Key as collection name. Value the sql string.
     */
    HashMap<String, String> getSqlString();

    /**
     * Get configuration
     * @return config
     */
    HashMap<String, TableConfig> getConfig();

    void setConfig(HashMap<String, TableConfig> c);

    void setSqlString(HashMap<String, String> sqlString);
}
