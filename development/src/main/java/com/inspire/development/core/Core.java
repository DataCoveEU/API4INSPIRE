package com.inspire.development.core;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.ImportantLinkList;
import com.inspire.development.config.Config;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.config.ImportantLink;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.SQLite;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mil.nga.sf.geojson.Feature;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Core {
    static Logger log = LogManager.getLogger(Core.class.getName());
    Config config;
    FileAlterationObserver observer;
    FileAlterationMonitor monitor;

    public Core() {
        config = new Config();

        Config conf = parseConfig();
        if(conf != null){
            this.config = conf;
        }

        String logPath = System.getProperty("log4j.saveDirectory");

        if(logPath == null){
            System.setProperty("log4j.saveDirectory",config.getLogPath());
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
                writeConfig();
            }

            @Override
            public void onFileDelete(File file) {
                deleteByPath(file.getName());
                writeConfig();
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
            writeConfig();
        }
    }

    public ImportantLinkList getLinks() {
        return config.getImportantLinks();
    }

    public void setLogDirectory(String dir){
        config.setLogPath(dir);
    }

    public void setSqlitePath(String path){
        try {
            monitor.stop();
        }catch (Exception e){

        }

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        observer = new FileAlterationObserver(path);

        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                config.getConnectors().add(new SQLite(file.getPath(), file.getName()));
            }

            @Override
            public void onFileDelete(File file) {
                deleteByPath(file.getPath());
            }
        });

        monitor = new FileAlterationMonitor(500, observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        config.setSqlitePath(path);

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
            writeConfig();
        }
    }

    public HashMap<String, String> getErrors(){
        HashMap<String,String> errors = new HashMap<>();
        for(DBConnector db:config.getConnectors()){
            errors.putAll(db.getErrorBuffer());
        }
        return errors;
    }

    public boolean removeError(String UUID){
        for(DBConnector db:config.getConnectors()){
            if(db.removeError(UUID)){
                return true;
            }
        }
        return false;
    }

    public static Config parseConfig() {
        log.info("Parsing config");
        File f = new File("./config.json");
        if (f.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(f, Config.class);
            } catch (IOException e) {

            }
        }
        return null;
    }



    public void addLink(String link, String name){
        config.getImportantLinks().add(new ImportantLink(link,name));
        writeConfig();
    }

    public boolean removeLink(String name){
        for(ImportantLink link:config.getImportantLinks()){
            if(link.getName().equals(name)){
                config.getImportantLinks().remove(link);
                return true;
            }
        }
        return false;
    }

    public boolean removeConnector(String id){
        for(DBConnector db:config.getConnectors()){
            if(db.getId().equals(id)){
                config.getConnectors().remove(db);
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


    public void writeConfig() {
        log.info("Writing config to file");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File f = new File("./config.json");
            objectMapper.writeValue(f, config);
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

    public Feature getFeature(String collection, String feature) {
        log.info("Getting feature: " + feature + " from collection: " + collection);
        FeatureCollection fs = get(collection, false, -1, 0, null, null);
        for (Object o : fs.getFeatures().toArray()) {
            Feature f = (Feature) o;
            if (f.getId().equals(feature)) {
                return f;
            }
        }
        return null;
    }

    public FeatureCollection get(String featureCollection, boolean withSpatial, int limit, int offset,
                                 double[] bbox, Map<String,String> filterParams) {
        log.info("Getting Collection: " + featureCollection);
        for (DBConnector db : config.getConnectors()) {
            FeatureCollection f = db.get(featureCollection, withSpatial, limit, offset, bbox, filterParams);
            if (f != null) {
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
}
