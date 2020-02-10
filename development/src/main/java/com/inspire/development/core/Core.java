package com.inspire.development.core;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.Link;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.SQLite;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import mil.nga.sf.geojson.Feature;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Core {
    static Logger log = LogManager.getLogger(Core.class.getName());
    DBConnectorList connectors;
    int port = 8080;

    public Core() {
        connectors = new DBConnectorList();
        File folder = new File("./../ogcapisimple/sqlite");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        FileAlterationObserver observer = new FileAlterationObserver("./../ogcapisimple/sqlite");

        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                connectors.add(new SQLite(file.getPath(), file.getName()));
            }

            @Override
            public void onFileDelete(File file) {
                deleteByName(file.getName());
            }
        });
        FileAlterationMonitor monitor = new FileAlterationMonitor(500, observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DBConnectorList list = parseConfig();
        if (list != null) {
            connectors = list;
        }

        File[] listOfFiles = new File("./../ogcapisimple/sqlite").listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                File f = listOfFiles[i];
                if (!checkIfConnectorExists(f.getName())) {
                    connectors.add(new SQLite(f.getPath(), f.getName()));
                }
            }
        }
        writeConfig();
    }

    public static DBConnectorList parseConfig() {
        log.info("Parsing config");
        File f = new File("../ogcapisimple/config.json");
        if (f.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(f, DBConnectorList.class);
            } catch (IOException e) {

            }
        }
        return null;
    }

    private void deleteByName(String id) {
        for (int i = 0; i < connectors.size(); i++) {
            DBConnector db = connectors.get(i);
            if (db.getId().equals(id)) {
                connectors.remove(i);
                break;
            }
        }
    }

    private boolean checkIfConnectorExists(String id) {
        for (DBConnector db : connectors) {
            if (db.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void writeConfig() {
        log.info("Writing config to file");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(SQLite.class, DBConnector.class);
        try {
            File f = new File("../ogcapisimple/config.json");
            objectMapper.writeValue(f, connectors);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DBConnectorList getConnectors() {
        return connectors;
    }

    public void setConnectors(DBConnectorList l) {
        this.connectors = l;
    }

    public void addConnector(DBConnector d) {
        if (!checkIfConnectorExists(d.getId())) {
            this.connectors.add(d);
        }
    }

    public FeatureCollection[] getAll() {
        String hostname = InetAddress.getLoopbackAddress().getHostName();
        ArrayList<FeatureCollection> fsl = new ArrayList<>();
        for (DBConnector db : connectors) {
            FeatureCollection[] fca = db.getAll();
            for (FeatureCollection fc : fca) {
                //Add required links
                fc.getLinks()
                        .add(new Link(
                                "http://" + hostname + ":" + port + "/ogcapisimple/collections/" + fc.getId(),
                                "self", "application/json", "this document"));
                fc.getLinks()
                        .add(new Link(
                                "http://" + hostname + ":" + port + "/ogcapisimple/collections/" + fc.getId(),
                                "alternate", "text/html", "this document as html"));
            }
            fsl.addAll(Arrays.asList(fca));
        }
        return fsl.toArray(new FeatureCollection[fsl.size()]);
    }

    public Feature getFeature(String collection, String feature) {
        log.info("Getting feature: " + feature + " from collection: " + collection);
        FeatureCollection fs = get(collection, false, -1, 0, null);
        for (Object o : fs.getFeatures().toArray()) {
            Feature f = (Feature) o;
            if (f.getId().equals(feature)) {
                return f;
            }
        }
        return null;
    }

    public FeatureCollection get(String featureCollection, boolean withSpatial, int limit, int offset,
                                 double[] bbox) {
        log.info("Getting Collection: " + featureCollection);
        for (DBConnector db : connectors) {
            FeatureCollection f = db.get(featureCollection, withSpatial, limit, offset, bbox);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    public DBConnector getConnectorById(String id) {
        for (DBConnector db : connectors) {
            if (db.getId().equals(id)) {
                return db;
            }
        }
        return null;
    }
}
