/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.FeatureWithLinks;
import com.inspire.development.collections.ImportantLinkList;
import com.inspire.development.config.*;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.SQLite;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import mil.nga.sf.geojson.Feature;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.stream.Collectors.toList;

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
                config.getConnectors().add(new SQLite(file.getPath(), file.getName()));
                writeConfig(config.getConfigPath(), config.getConnectionPath());
            }

            @Override
            public void onFileDelete(File file) {
                deleteByPath(file.getName());
                writeConfig(config.getConfigPath(), config.getConnectionPath());
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



    public void addLink(String link, String name){
        config.getImportantLinks().add(new ImportantLink(link,name));
        writeConfig(config.getConfigPath(), config.getConnectionPath());
    }

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

    private boolean checkIfConnectorExists(String id) {
        for (DBConnector db : config.getConnectors()) {
            if (db.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    public void writeConfig(String configPath, String connectionPath) {
        log.info("Writing config to file");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        HashMap<String, ConfigSql> configs = new HashMap<>();
        ArrayList<DBConnector> dbConnectors = config.getConnectors();
        for(DBConnector db:dbConnectors){
            configs.put(db.getId(),new ConfigSql(db.getConfig(), db.getSqlString()));
        }
        try {
            File fConnections = new File(connectionPath);
            File fConfig = new File(configPath);
            objectMapper.writeValue(fConnections, config);
            objectMapper.writerWithView(Views.Public.class).writeValue(fConfig,configs);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DBConnectorList getConnectors() {
        return config.getConnectors();
    }



    public void addConnector(DBConnector d) {
        if (!checkIfConnectorExists(d.getId())) {
            config.getConnectors().add(d);
            writeConfig(config.getConfigPath(), config.getConnectionPath());
        }
    }

    public FeatureCollection[] getAll() {
        String hostname = InetAddress.getLoopbackAddress().getHostName();
        ArrayList<FeatureCollection> fsl = new ArrayList<>();
        for (DBConnector db : config.getConnectors()) {
            FeatureCollection[] fca = db.getAll();
            fsl.addAll(Arrays.asList(fca));
        }
        return fsl.toArray(new FeatureCollection[fsl.size()]);
    }

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

    public DBConnector getConnectorById(String id) {
        for (DBConnector db : config.getConnectors()) {
            if (db.getId().equals(id)) {
                return db;
            }
        }
        return null;
    }

    public String getPagingLimit(){
        return config.getPagingLimit();
    }

    public String getConfigPath(){
        return config.getConfigPath();
    }

    public List<Feature> replaceFkFromList(List<Feature> features, String host){
        ArrayList<Feature> out = new ArrayList<>();
        for(Feature f:features) {
            out.add(replaceFk(f, host));
        }
        return out;
    }

    public Feature replaceFk(Feature f, String host){
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

    public boolean deleteSQL(String name){
        for(DBConnector db:config.getConnectors()){
            if(db.removeSQL(name)){
                writeConfig(config.getConfigPath(), config.getConnectionPath());
                return true;
            }
        }
        return false;
    }

    public String getConnectionPath(){
        return config.getConnectionPath();
    }
}
