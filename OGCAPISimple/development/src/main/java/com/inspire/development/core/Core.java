/*
 * The OGC API Simple provides environmental data
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
package com.inspire.development.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.FeatureWithLinks;
import com.inspire.development.collections.ImportantLinkList;
import com.inspire.development.config.*;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.SQLite;

import java.io.File;
import java.io.IOException;
import java.util.*;

import mil.nga.sf.geojson.Feature;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class ConfigMap extends HashMap<String, ConfigSql>{

}

public class Core {
    private static Logger log = LogManager.getLogger(Core.class.getName());
    private Config config;
    private FileAlterationObserver observer;
    private FileAlterationMonitor monitor;

    /**
     * Create new core instance
     */
    public Core() {
        config = new Config();

        Config conf = parseConfig(config.getConfigPath(), config.getConnectionPath());
        if(conf != null){
            this.config = conf;
        }

        File folder = new File(config.getSqlitePath());
        if (!folder.exists()) {
            folder.mkdirs();
        }



        observer = new FileAlterationObserver(config.getSqlitePath());

        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                try {
                    config.getConnectors().add(new SQLite(file.getPath(), file.getName()));
                    writeConfig(config.getConfigPath(), config.getConnectionPath());
                }catch (Exception e){
                    log.error("An exception occurred while adding a sqlite database", e);
                }
            }

            @Override
            public void onFileDelete(File file) {
                try {
                    deleteByPath(file.getPath());
                    writeConfig(config.getConfigPath(), config.getConnectionPath());
                }catch (Exception e){
                    log.error("An exception occurred while adding a sqlite database", e);
                }
            }
        });
        monitor = new FileAlterationMonitor(500, observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        File[] listOfFiles = new File(config.getSqlitePath()).listFiles();
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    File f = listOfFiles[i];
                    if (!checkIfConnectorExists(f.getName())) {
                        config.getConnectors().add(new SQLite(f.getPath(), f.getName()));
                    }
                }
            }
        }

        writeConfig(config.getConfigPath(), config.getConnectionPath());
    }

    /**
     * Get all important links
     * @return important links
     */
    public ImportantLinkList getLinks() {
        return config.getImportantLinks();
    }


    /**
     * Parse config instance from file
     * @param configPath config file path
     * @param connectionPath connection file path
     * @return config instance
     */
    public static Config parseConfig(String configPath, String connectionPath) {
        log.info("Parsing config");
        File fConfig = new File(configPath);
        File fConnection = new File(connectionPath);
        if (fConfig.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Config c = objectMapper.readValue(fConnection, Config.class);
                HashMap<String, ConfigSql> configSqlHashMap = objectMapper.readValue(fConfig,ConfigMap.class);
                for(DBConnector db:c.getConnectors()){
                     ConfigSql configSql = configSqlHashMap.get(db.getId());
                    if(configSql != null){
                        db.setSqlString(configSql.getSqlString());
                        db.setConfig(configSql.getConfig());
                    }
                }
                return c;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Add a new important link
     * @param link link to be used
     * @param name name to be displayed by
     */
    public void addLink(String link, String name){
        config.getImportantLinks().add(new ImportantLink(link,name));
        writeConfig(config.getConfigPath(), config.getConnectionPath());
    }

    /**
     * Remove a important link
     * @param name link name
     * @return true if removed. Else if not found
     */
    public boolean removeLink(String name){
        for(ImportantLink link:config.getImportantLinks()){
            if(link.getName().equals(name)){
                config.getImportantLinks().remove(link);
                writeConfig(config.getConfigPath(), config.getConnectionPath());
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a connector by the id
     * @param id connector id
     * @return true if found. Else false.
     */
    public boolean removeConnector(String id){
        for(DBConnector db:config.getConnectors()){
            if(db.getId().equals(id)){
                config.getConnectors().remove(db);
                writeConfig(config.getConfigPath(), config.getConnectionPath());
                return true;
            }
        }
        return false;
    }

    /**
     * Delete sqlite by the path. Used for the folder listener.
     * @param path
     */
    private void deleteByPath(String path) {
        for (int i = 0; i < config.getConnectors().size(); i++) {
            DBConnector db = config.getConnectors().get(i);
            if(db instanceof SQLite) {
                SQLite sqLite = (SQLite)db;
                if (sqLite.getHostname().equals(path)) {
                    config.getConnectors().remove(i);
                    writeConfig(config.getConfigPath(), config.getConnectionPath());
                    break;
                }
            }
        }
    }

    /**
     * Check if a connection exists
     * @param id id to be checked by
     * @return true if exists else false.
     */
    private boolean checkIfConnectorExists(String id) {
        for (DBConnector db : config.getConnectors()) {
            if (db.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Write the config to file
     * @param configPath config path
     * @param connectionPath connection path
     */
    public void writeConfig(String configPath, String connectionPath) {
        log.info("Writing config to file");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        HashMap<String, ConfigSql> configs = new HashMap<>();
        ArrayList<DBConnector> dbConnectors = config.getConnectors();
        for(DBConnector db:dbConnectors) {
            configs.put(db.getId(), new ConfigSql(db.getConfig(), db.getSqlString()));
        }
        try {
            File fConnections = new File(connectionPath);
            File fConfig = new File(configPath);
            objectMapper.writeValue(fConnections, config);
            objectMapper.writeValue(fConfig,configs);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all stored connectors.
     * @return list of all connections
     */
    public DBConnectorList getConnectors() {
        return config.getConnectors();
    }


    /**
     * Add a new connector
     * @param d connector to be added
     */
    public void addConnector(DBConnector d) {
        if (!checkIfConnectorExists(d.getId())) {
            config.getConnectors().add(d);
            writeConfig(config.getConfigPath(), config.getConnectionPath());
        }
    }

    /**
     * Get all FeatureCollections from all tables
     * @return Array containing all FeatureCollections
     */
    public FeatureCollection[] getAll() {
        ArrayList<FeatureCollection> fsl = new ArrayList<>();
        for (DBConnector db : config.getConnectors()) {
            FeatureCollection[] fca = db.getAll();
            fsl.addAll(Arrays.asList(fca));
        }
        return fsl.toArray(new FeatureCollection[fsl.size()]);
    }

    /**
     * Get a feature from a specified connection
     * @param collection collection name the feature is located in
     * @param feature feature name
     * @param host hostname to be used in the links
     * @return Feature
     */
    public FeatureWithLinks getFeature(String collection, String feature, String host) {
        log.info("Getting feature: " + feature + " from collection: " + collection);
        FeatureCollection fs = get(collection, false, -1, 0, null, null, host);
        for (Object o : fs.getFeatures().toArray()) {
            Feature f = (Feature) o;
            if(f != null) {
                if (f.getId().equals(feature)) {
                    f = replaceFk(f, host);
                    FeatureWithLinks fl = new FeatureWithLinks(f);
                    fl.collection = fs.getId();
                    return fl;
                }
            }
        }
        return null;
    }

    /**
     * Get items from a specified FeatureCollection with filter parameters
     * @param featureCollection name
     * @param withSpatial true if an extent property should be included.
     * @param limit response item limit
     * @param offset offset to the first result
     * @param bbox if set the FeatureCollections only contains geometries which intersect the given bbox
     * @param filterParams custom parameters to be filtered by
     * @param host host to be used for the foreign key links.
     * @return FeatureCollection
     */
    public FeatureCollection get(String featureCollection, boolean withSpatial, int limit, int offset,
                                 double[] bbox, Map<String,String> filterParams, String host) {
        log.info("Getting Collection: " + featureCollection);
        for (DBConnector db : config.getConnectors()) {
            FeatureCollection f = db.get(featureCollection, withSpatial, limit, offset, bbox, filterParams);
            if (f != null) {
                f.setFeatures(replaceFkFromList(f.getFeatures(), host));
                return f;
            }
        }
        return null;
    }

    /**
     * Get a connection by its id
     * @param id connection id
     * @return DBConnector with the specified id, null if no connection was found
     */
    public DBConnector getConnectorById(String id) {
        for (DBConnector db : config.getConnectors()) {
            if (db.getId().equals(id)) {
                return db;
            }
        }
        return null;
    }

    /**
     * Get the specified paging limit. Used for the landing page map
     * @return number formatted as a string
     */
    public String getPagingLimit(){
        return config.getPagingLimit();
    }

    /**
     * Get the specified config path
     * @return config path
     */
    public String getConfigPath(){
        return config.getConfigPath();
    }

    /**
     * Replace all foreign keys in a list of items
     * @param features feature list
     * @param host hostname to be used while replacing
     * @return the given features list but with all foreign keys replaced
     */
    private List<Feature> replaceFkFromList(List<Feature> features, String host){
        ArrayList<Feature> out = new ArrayList<>();
        for(Feature f:features) {
            out.add(replaceFk(f, host));
        }
        return out;
    }

    /**
     * Replace all foreign keys in the given Feature
     * @param f Feature to be used
     * @param host hostname the links should be set to
     * @return Feature with all foreign keys replaced
     */
    private Feature replaceFk(Feature f, String host){
            Map<String,Object> props = f.getProperties();
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String value = (String) entry.getValue();
                    if (value.contains("ogc_fk")) {
                        String[] strings = value.split(";");
                        entry.setValue("http://" + host + "/ogcapisimple/collections/" + strings[1] + "/items?" + strings[2] + "=" + strings[3]);
                    }
                }
            }
        return f;
    }

    /**
     * Delete an sql view by its name
     * @param name name
     * @return true if one could be deleted. False if not found.
     */
    public boolean deleteSQL(String name){
        for(DBConnector db:config.getConnectors()){
            if(db.removeSQL(name)){
                writeConfig(config.getConfigPath(), config.getConnectionPath());
                return true;
            }
        }
        return false;
    }

    /**
     * Get the connection save path
     * @return path
     */
    public String getConnectionPath(){
        return config.getConnectionPath();
    }
}
