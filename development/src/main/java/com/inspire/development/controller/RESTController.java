package com.inspire.development.controller;

import com.inspire.development.collections.Collections;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.core.Core;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.PostgreSQL;
import com.inspire.development.database.connector.SQLite;
import mil.nga.sf.geojson.Feature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@RestController
public class RESTController {
    private Core core;

    public RESTController(){
        core = new Core();
        SQLite c = new SQLite("inspireDB.sqlite","Inspire");
        c.renameTable("tna_insp_navaids", "Tobias");
        c.renameFeature("tna_insp_navaids", "metadataproperty", "meta");
        PostgreSQL p = new PostgreSQL("localhost",25432,"inspire", "tna", "Postgres");
        core.getConnectors().add(c);
        core.getConnectors().add(p);
        //DBConnectorList list = core.parseConfig();
        //if(list != null){
          //  core.setConnectors(list);
        //}
    }

    @GetMapping("/collections")
    public Collections Collections() {
        return new Collections(Arrays.asList(core.getAll(false)));
    }

    /**
     * Gets the conformance decalaration
     *
     * @return the yaml file which contains the conformance declaration
     */
    @GetMapping("/conformance")
    public String getConformance() {
        //TODO: implement the method to return the yaml file
        return "Conformance";
    }

    /**
     * Gets a special feature collection from the database
     *
     * @param id the id of the feature Collection
     * @return the collection with the id
     */
    @GetMapping("/collections/{collectionId}")
    public FeatureCollection getCollections(@PathVariable("collectionId") String id) {
        return core.get(id, false, true);
    }

    /**
     * Gets the items of a special feature collection
     *
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @GetMapping("/collections/{collectionId}/items")
    public FeatureCollection getCollectionItems(@PathVariable("collectionId") String id) {
        return core.get(id, true, false);
    }

    /**
     * Gets the items of a special item (=feature) from a special feature collection
     *
     * @param collectionId the id of the feature Collection
     * @param featureId the id of the item (=feature)
     * @return a special item (=feature) from a collection
     */
    @GetMapping("/collections/{collectionId}/items/{featureId}")
    public Feature getItemFromCollection(@PathVariable("collectionId") String collectionId, @PathVariable("featureId") String featureId) {
        //TODO: implement the method to return a special feature from a special collection
        return core.getFeature(collectionId,featureId);

    }

    @RequestMapping(value = "/api/getConnectors", method = RequestMethod.POST)
    public DBConnectorList getConnectors(){
        return core.getConnectors();
    }

    /**
     * Adds a new Database Connector
     * @param input see APIDoc
     * @return false if error occurred else false
     */
    @RequestMapping(value = "/api/addConnector", method = RequestMethod.POST)
    public ResponseEntity<Boolean> addConnector(@RequestBody Map<String, ?> input,@RequestBody MultipartFile file){
        try {
            String classe = (String) input.get("class");
            if (classe.equals("postgres")) {
                String database = (String) input.get("database");
                String schema = (String) input.get("schema");
                String hostname = (String) input.get("hostname");
                int port = Integer.parseInt((String) input.get("port"));
                String id = (String) input.get("id");
                core.addConnector(new PostgreSQL(hostname, port, database, schema, id));
            }
            if (classe.equals("sqlite")) {
                file.transferTo(new File("./db/" + file.getName()));
                String path =  "db/" + file.getName();
                String id = (String) input.get("id");
                core.addConnector(new SQLite(path, id));
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * change Connector Properties
     * @param input See APIDoc
     * @return false if error occurred else true
     */
    @RequestMapping(value = "/api/setConnectorProps", method = RequestMethod.POST)
    public ResponseEntity<Boolean> changeConnectorProperties(@RequestBody Map<String, ?> input, @RequestBody MultipartFile file) {
        try {
            String classe = (String) input.get("class");
            //Check if classe parameter exits else BAD_REQUEST
            if (classe != null){
                if (classe.equals("postgres")) {
                    String id = (String) input.get("id");
                    if (id != null) {
                        //Get Connector by ID
                        DBConnector db = core.getConnectorById(id);
                        if (db != null) {
                            //Cast Connector
                            PostgreSQL postgreSQL = (PostgreSQL) db;
                            String database = (String) input.get("database");
                            if (database != null) {
                                postgreSQL.setDatabase(database);
                            }
                            String schema = (String) input.get("schema");
                            if (schema != null) {
                                postgreSQL.setSchema(schema);
                            }
                            String hostname = (String) input.get("hostname");
                            if (hostname != null) {
                                postgreSQL.setHostname(hostname);
                            }
                            String portString = (String) input.get("port");
                            if (portString != null) {
                                int port = Integer.parseInt(portString);
                                postgreSQL.setPort(port);
                            }
                            return new ResponseEntity<>(true, HttpStatus.OK);
                        }
                    }

                }
                    if (classe.equals("sqlite")) {
                        //Index of connector to be changed
                        String id = (String) input.get("id");
                        if (file != null) {
                            //Transfer file to DB folder
                            file.transferTo(new File("./db/" + file.getName()));
                            String path = "db/" + file.getName();
                            //Get Connector by ID
                            DBConnector db = core.getConnectorById(id);
                            if (db != null) {
                                //Cast to SQLite Connector to set Path
                                SQLite sqLite = (SQLite) db;
                                sqLite.setPath(path);
                                return new ResponseEntity<>(true, HttpStatus.OK);
                            }
                        } else {
                            return new ResponseEntity<>(false, HttpStatus.OK);
                        }
                    }
            }else{
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gets tables based on the Connector id
     * @param input Connector id
     * @return Array with all Names
     */
    @RequestMapping(value = "/api/getTables", method = RequestMethod.POST)
    public ResponseEntity<ArrayList<String>> getTable(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            DBConnector db = core.getConnectorById(id);
            if(db != null) {
                return new ResponseEntity<>(db.getAllTables(), HttpStatus.OK);
            }
        }else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gets features based on the Connector id and featureCollection id
     * @param input Connector id
     * @return Array with all Names
     */
    @RequestMapping(value = "/api/getColumns", method = RequestMethod.POST)
    public ResponseEntity<ArrayList<String>> getFeatures(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            DBConnector db = core.getConnectorById(id);
            if(db != null) {
                String table = (String)input.get("table");
                if(table != null) {
                    ArrayList<String> list = db.getColumns(table);
                    if(list != null)
                        return new ResponseEntity<>(list, HttpStatus.OK);
                }
            }
        }else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

}
