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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.PrintWriter;
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
        c.execute("select * from tna_insp_airspacearea","TobiasIsJustATest");
        c.renameTable("tna_insp_navaids", "Tobias");
        c.renameProp("tna_insp_navaids", "metadataproperty", "meta");
        PostgreSQL p = new PostgreSQL("localhost",25432,"inspire", "tna", "Postgres","inspire", "1nsp1r3_2#2#");
        core.getConnectors().add(c);
        core.getConnectors().add(p);

        core.writeConfig();
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
        return core.get(id, false, true, 0,0, null);
    }

    /**
     * Gets the items of a special feature collection
     *
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @GetMapping("/collections/{collectionId}/items")
    public FeatureCollection getCollectionItems(@PathVariable("collectionId") String id, @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(10000) int limit, @RequestParam(required = false) double[] bbox, @RequestParam(required = false, defaultValue = "0") @Min(0) @Max(10000) int offset) {
        FeatureCollection fc = core.get(id, true, false, limit, offset, bbox);

        return fc;
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
    public ResponseEntity<Object> addConnector(@RequestBody Map<String, ?> input){
        try {
            String classe = (String) input.get("class");
            if (classe.equals("postgres")) {
                String database = (String) input.get("database");
                String schema = (String) input.get("schema");
                String hostname = (String) input.get("hostname");
                String username = (String) input.get("username");
                String password = (String) input.get("password");
                int port = (Integer) input.get("port");
                String id = (String) input.get("id");
                Object o = input.get("isTest");
                boolean test = false;
                if(o != null){
                    test = (Boolean)o;
                }
                PostgreSQL s = new PostgreSQL(hostname, port, database, schema, id,username,password);
                String error = s.checkConnection();
                if(error == null) {
                    if (!test) {
                        core.addConnector(s);
                    }
                    return new ResponseEntity<>("OK", HttpStatus.OK);
                }else{
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
                //if(s.checkConnection());

            }
            if (classe.equals("sqlite")) {
                String path = (String)input.get("path");
                String id = (String) input.get("id");
                if(new File(id).exists()) {
                    core.addConnector(new SQLite(path, id));
                }else{
                    return new ResponseEntity<>("Path does not exist", HttpStatus.OK);
                }
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
    public ResponseEntity<Object> changeConnectorProperties(@RequestBody Map<String, ?> input) {
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
                            postgreSQL.setDatabase(database);

                            String schema = (String) input.get("schema");
                            postgreSQL.setSchema(schema);

                            String hostname = (String) input.get("hostname");
                            postgreSQL.setHostname(hostname);

                            String portString = (String) input.get("port");
                            if(portString != null) {
                                int port = Integer.parseInt(portString);
                                postgreSQL.setPort(port);
                            }

                            String username = (String) input.get("username");
                            postgreSQL.setUsername(username);

                            String password = (String) input.get("password");
                            postgreSQL.setPassword(password);

                            String error = db.updateConnector();
                            if(error == null){
                                return new ResponseEntity<>("OK", HttpStatus.OK);
                            }else{
                                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                        }
                    }

                }
                    if (classe.equals("sqlite")) {
                        //Index of connector to be changed
                        String id = (String) input.get("id");
                        if (id != null) {
                            //Get Connector by ID
                            DBConnector db = core.getConnectorById(id);
                            if (db != null) {
                                //Cast to SQLite Connector to set Path
                                SQLite sqLite = (SQLite) db;
                                String path = (String)input.get("path");
                                if(path != null){
                                    sqLite.setPath(path);
                                    String error = db.updateConnector();
                                    if(error == null){
                                        return new ResponseEntity<>("OK", HttpStatus.OK);
                                    }else{
                                        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
                                    }
                                }
                            }
                        } else {
                            return new ResponseEntity<>("Connector id is null", HttpStatus.OK);
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
    public ResponseEntity<Object> getTable(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            DBConnector db = core.getConnectorById(id);
            if(db != null) {
                return new ResponseEntity<>(db.getAllTables(), HttpStatus.OK);
            }else{
                return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
            }
        }else{
            return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        }
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

    /**
     * Execute SQL String and store it in DB
     * @param input se APIDoc
     * @return  see APIDoc
     */
    @RequestMapping(value = "/api/executeSQL", method = RequestMethod.POST)
    public ResponseEntity<Object> executeSQL(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            String sql = (String)input.get("sql");
            if(sql != null){
                String collectionName = (String)input.get("collectionName");
                if(collectionName != null){
                    DBConnector db = core.getConnectorById(id);
                    if(db != null){
                        return new ResponseEntity<>(db.execute(sql,collectionName), HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
                    }
                }else{
                    return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
                }
            }else{
                return new ResponseEntity<>("SQL string missing", HttpStatus.BAD_REQUEST);
            }
        }else{
            return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Rename a featureCollection
      * @param input se APIDoc
     * @return  see APIDoc
     */
    @RequestMapping(value = "/api/renameCollection", method = RequestMethod.POST)
    public ResponseEntity<Object> renameCollection(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            String orgName = (String)input.get("orgName");
            if(orgName != null){
                String alias = (String)input.get("alias");
                if(alias != null){
                    DBConnector db = core.getConnectorById(id);
                    if(db != null){
                        db.renameTable(orgName,alias);
                        return new ResponseEntity<>("OK", HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
                    }
                }else{
                    return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
                }
            }else{
                return new ResponseEntity<>("Original Name missing", HttpStatus.BAD_REQUEST);
            }
        }else{
            return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Rename a featureCollection
     * @param input se APIDoc
     * @return  see APIDoc
     */
    @RequestMapping(value = "/api/renameProp", method = RequestMethod.POST)
    public ResponseEntity<Object> renameProp(@RequestBody Map<String, ?> input){
        String id = (String)input.get("id");
        if(id != null){
            String table = (String)input.get("table");
            if(table != null){
                String orgName = (String)input.get("orgName");
                if(orgName != null){
                    DBConnector db = core.getConnectorById(id);
                    if(db != null){
                        String alias = (String)input.get("alias");
                        if(alias != null) {
                            db.renameProp(table, orgName, alias);
                            return new ResponseEntity<>("OK", HttpStatus.OK);
                        }else{
                            return new ResponseEntity<>("Alias is missing", HttpStatus.BAD_REQUEST);
                        }
                    }else{
                        return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
                    }
                }else{
                    return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
                }
            }else{
                return new ResponseEntity<>("Original Name missing", HttpStatus.BAD_REQUEST);
            }
        }else{
            return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Change admin Password hashed to a file
     * @param input password to be saved
     * @return
     */
    @RequestMapping(value = "/api/changePwd", method = RequestMethod.POST)
    public ResponseEntity<Object> changePassword(@RequestBody Map<String, ?> input){
        String pwd = (String)input.get("pwd");
        if(pwd != null){
            try (PrintWriter out = new PrintWriter("./config/admin.pw")) {
                out.print(BCrypt.hashpw(pwd, BCrypt.gensalt(12)));
            }catch (Exception e){

            }
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("No password provided missing", HttpStatus.BAD_REQUEST);
        }
    }

}
