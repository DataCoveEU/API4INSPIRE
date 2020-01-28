package com.inspire.development.core;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.Link;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.PostgreSQL;
import com.inspire.development.database.connector.SQLite;
import mil.nga.sf.geojson.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.core.DB;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class Core {
    DBConnectorList connectors;
    static Logger log = LogManager.getLogger(Core.class.getName());

    public Core(){
        connectors = new DBConnectorList();
    }

    public static DBConnectorList parseConfig(){
        log.info("Parsing config");
        File f = new File("config/config.json");
        if(f.exists()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(f, DBConnectorList.class);
            } catch (IOException e) {

            }
        }
        return null;
    }

    public void writeConfig(){
        log.info("Writing config to file");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(SQLite.class, DBConnector.class);
        try {
            objectMapper.writeValue(new File("config/config.json"), connectors);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DBConnectorList getConnectors(){
        return connectors;
    }


    public void setConnectors(DBConnectorList l){
        this.connectors = l;
    }

    public void addConnector(DBConnector d){
        this.connectors.add(d);
    }

    public FeatureCollection get(String featureCollection, boolean withSpatial, int limit, int offset, double[] bbox){
        log.info("Getting Collection: " + featureCollection);
        for(DBConnector db:connectors){
            FeatureCollection f = db.get(featureCollection, withSpatial, limit, offset, bbox);
            if(f != null)
                return f;
        }
        return null;
    }

    public FeatureCollection[] getAll(){
        String hostname = InetAddress.getLoopbackAddress().getHostName();
        ArrayList<FeatureCollection> fsl = new ArrayList<>();
        for(DBConnector db:connectors){
            FeatureCollection[] fca = db.getAll();
            for(FeatureCollection fc:fca){
                //Add required links
                fc.getLinks().add(new Link("http://" + hostname + "/collections/" + fc.getId(), "self", "application/json", "this document"));
                fc.getLinks().add(new Link("http://" + hostname + "/collections/" + fc.getId(), "alternate", "text/html", "this document as html"));
            }
            fsl.addAll(Arrays.asList(fca));
        }
        return fsl.toArray(new FeatureCollection[fsl.size()]);
    }

    public Feature getFeature(String collection, String feature){
        log.info("Getting feature: " + feature + " from collection: " + collection);
        FeatureCollection fs = get(collection, false, -1, 0,null);
        for(Object o: fs.getFeatures().toArray()){
            Feature f = (Feature)o;
            if(f.getId().equals(feature))
                return f;
        }
        return null;
    }

    public DBConnector getConnectorById(String id){
        for(DBConnector db:connectors){
            if(db.getId().equals(id))
                return db;
        }
        return null;
    }

}
