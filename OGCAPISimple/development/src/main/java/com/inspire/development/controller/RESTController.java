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
package com.inspire.development.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.collections.Collections;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.collections.FeatureWithLinks;
import com.inspire.development.collections.Link;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.config.TableConfig;
import com.inspire.development.core.Core;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.PostgreSQL;
import com.inspire.development.database.connector.SQLite;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


class ConformanceDeclaration {

    private String[] conformsTo;

    /**
     * Helper class for the conformance declaration
     * @param conformsTo array of links
     */
    public ConformanceDeclaration(String[] conformsTo) {
        this.conformsTo = conformsTo;
    }

    public String[] getConformsTo() {
        return this.conformsTo;
    }
}

@RestController
public class RESTController {

    private String hostEnv;

    @Value("classpath:openapi.json")
    Resource resourceFile;
    @Value("classpath:index.html")
    Resource indexFile;
    private Core core;


    public RESTController() {
        core = new Core();
        hostEnv = System.getenv("HOST_OGCAPISIMPLE");
    }

    @CrossOrigin(maxAge = 3600)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path = "/openapi.json")
    public @ResponseBody
    Object getApiDef() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(resourceFile.getInputStream(), Object.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @CrossOrigin(maxAge = 3600)
    @RequestMapping(path = "/api", produces = {"text/html", "application/json"})
    @ResponseBody
    public Object index(@RequestParam(required = false, defaultValue = "text/html") String f) {
        if (f.equals("text/html")) {
            try {
                return new String(Files.readAllBytes(Paths.get(indexFile.getURI())));
            } catch (Exception e) {
                return null;
            }
        } else {
            if (f.equals("application/json")) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/json");
                    return ResponseEntity.ok().headers(headers).body(mapper.readValue(resourceFile.getInputStream(), Object.class));
                } catch (IOException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @CrossOrigin(maxAge = 3600)
    @GetMapping("/collections")
    public Object Collections(@RequestHeader("Accept") String content,
                              @RequestHeader(name = "Host", required = false) String host,
                              @RequestParam(required = false, defaultValue = "application/json") String f) {
        host = hostEnv != null ? hostEnv : host;
        if (f.equals("application/json")) {
            Collections c = new Collections(Arrays.asList(core.getAll()));
            c.setCollections(c.getCollections().stream().collect(Collectors.toList()));
            c.getCollections().removeIf(Objects::isNull);
            for (FeatureCollection fc : c.getCollections()) {
                if (fc == null) {

                } else {
                    //Add required links
                    fc.getLinks()
                            .add(new Link(
                                    "http://" + host + "/ogcapisimple/collections/" + fc.getId(),
                                    "self", "application/json", "this document"));
                    fc.getLinks()
                            .add(new Link(
                                    "http://" + host + "/ogcapisimple/collections/" + fc.getId(),
                                    "alternate", "text/html", "this document as html"));
                    fc.getLinks()
                            .add(new Link(
                                    "http://" + host + "/ogcapisimple/collections/" + fc.getId() + "/items",
                                    "items", "application/json", "this document as html"));
                }
            }

            c.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/",
                            "self", "application/json", "this document"));

            c.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections?f=text%2Fhtml",
                            "self", "text/html", "this document"));
            return new ResponseEntity(c, HttpStatus.OK);
        } else if (f.equals("text/html")) {
            try {
                File f1 = new File(this.getClass().getClassLoader().getResource("static/index.html").getFile());
                return new Scanner(f1).useDelimiter("\\Z").next();
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets the conformance declaration
     * @return the json file which contains the conformance declaration
     */
    @CrossOrigin(maxAge = 3600)
    @GetMapping("/conformance")
    public ConformanceDeclaration getConformance() {
        String[] links = {
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30",
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson",
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html"
        };
        return new ConformanceDeclaration(links);
    }

    /**
     * Gets a special feature collection from the database
     * @param host host header
     * @param id the id of the feature Collection
     * @return the collection with the id
     */
    @CrossOrigin(maxAge = 3600)
    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<Object> getCollections(@PathVariable("collectionId") String id,
                                                 @RequestHeader(name = "Host", required = false) String host) {
        host = hostEnv != null ? hostEnv : host;
        FeatureCollection fc = core.get(id, true, 0, 0, null, null, host);
        if (fc != null) {
            fc.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/" + fc.getId(),
                            "self", "application/json", "this document"));
            fc.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/" + fc.getId(),
                            "alternate", "text/html", "this document as html"));
            fc.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/" + fc.getId() + "/items",
                            "items", "application/json", "this document as html"));
            return new ResponseEntity<>(fc, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the items of a special feature collection
     * @param limit request param limit
     * @param bbox request param bbox
     * @param offset request param offset
     * @param f request param content type
     * @param filterParams additional request parameters
     * @param host host header
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @CrossOrigin(maxAge = 3600)
    @RequestMapping(path = "/collections/{collectionId}/items", method = RequestMethod.GET)
    @ResponseBody
    public Object getCollectionItems(@PathVariable("collectionId") String id,
                                     @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(10000) int limit,
                                     @RequestParam(required = false) double[] bbox,
                                     @RequestParam(required = false, defaultValue = "0") @Min(0) @Max(10000) int offset,
                                     @RequestParam(required = false) Map<String, String> filterParams,
                                     @RequestHeader(name = "Host", required = false) String host,
                                     @RequestParam(required = false, defaultValue = "application/json") String f) {
        host = hostEnv != null ? hostEnv : host;
        if (f.equals("application/json")) {
            Map<String,String> linkParams = new HashMap();

            //Clone Map
            for (Map.Entry<String,String> entry: filterParams.entrySet()) {
                linkParams.put(entry.getKey(), entry.getValue());
            }
            //Removing offset and limit param and bbox
            filterParams.remove("offset");
            filterParams.remove("limit");
            filterParams.remove("bbox");
            filterParams.remove("f");
            FeatureCollection fc = core.get(id, false, limit, offset, bbox, filterParams, host);
            if (fc != null) {

                String params = linkParams.entrySet().stream()
                    .map(p -> urlEncodeUTF8(p.getKey()) + "=" + urlEncodeUTF8(p.getValue()))
                    .reduce((p1, p2) -> p1 + "&" + p2)
                    .orElse("");

                if(params != "")
                    params = "&" + params;


                fc.getLinks()
                        .add(new Link(
                                "http://" + host + "/ogcapisimple/collections/" + fc.getId() + "/items?limit=" + limit + "&offset=" + offset + params,
                                "self", "application/json", "this document"));
                //Test if any data is available after the current selected data
                FeatureCollection featureCollectionNext = core.get(id, false, 1, limit + offset, bbox, filterParams, host);
                if (featureCollectionNext.getFeatures().size() == 1) {
                    fc.getLinks()
                            .add(new Link(
                                    "http://" + host + "/ogcapisimple/collections/" + fc.getId() + "/items?limit=" + limit + "&offset=" + (offset + limit) + params,
                                    "next", "application/json", "this document"));
                }

                if (offset > 0) {
                    fc.getLinks()
                            .add(new Link(
                                    "http://" + host + "/ogcapisimple/collections/" + fc.getId() + "/items?limit=" + limit + "&offset=" + (offset - limit < 0 ? 0 : offset - limit) + params,
                                    "prev", "application/json", "this document"));
                }
                return new ResponseEntity<>(fc, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else if (f.equals("text/html")) {
            try {
                File f1 = new File(this.getClass().getClassLoader().getResource("static/index.html").getFile());
                return new Scanner(f1).useDelimiter("\\Z").next();
            } catch (Exception e) {
                return e.getMessage();
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }



    /**
     * Gets the items of a special item (=feature) from a special feature collection
     * @param host host header
     * @param collectionId the id of the feature Collection
     * @param featureId    the id of the item (=feature)
     * @return a special item (=feature) from a collection
     */
    @CrossOrigin(maxAge = 3600)
    @GetMapping(path="/collections/{collectionId}/items/{featureId}", produces = "application/geo+json")
    public ResponseEntity<Object> getItemFromCollection(
            @PathVariable("collectionId") String collectionId,
            @PathVariable("featureId") String featureId,
            @RequestHeader(name = "Host", required = false) String host) {
        host = hostEnv != null ? hostEnv : host;
        FeatureWithLinks f = core.getFeature(collectionId, featureId, host);
        if (f != null) {
            f.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/" + f.collection + "/items/" + f.getId(),
                            "self", "application/json", "this document"));
            f.getLinks()
                    .add(new Link(
                            "http://" + host + "/ogcapisimple/collections/" + f.collection,
                            "collection", "application/json", "the collection the feature is located in"));
            return new ResponseEntity<>(f, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/api/getConnectors", method = RequestMethod.POST)
    public DBConnectorList getConnectors() {
        return core.getConnectors();
    }

    /**
     * Adds a new Database Connector
     * @param input see APIDoc
     * @return false if error occurred else false
     */
    @RequestMapping(value = "/api/addConnector", method = RequestMethod.POST)
    public ResponseEntity<Object> addConnector(@RequestBody Map<String, ?> input) {
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
                if (o != null) {
                    test = (Boolean) o;
                }
                PostgreSQL s = new PostgreSQL(hostname, port, database, schema, id, username, password);
                String error = s.checkConnection();
                if (error == null) {
                    if (!test) {
                        core.addConnector(s);
                        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
                    }
                    return new ResponseEntity<>("OK", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                }
            }
            if (classe.equals("sqlite")) {
                String path = (String) input.get("path");
                String id = (String) input.get("id");
                if (new File(id).exists()) {
                    core.addConnector(new SQLite(path, id));
                } else {
                    return new ResponseEntity<>("Path does not exist", HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * change Connector Properties
     *
     * @param input See APIDoc
     * @return false if error occurred else true
     */

    @RequestMapping(value = "/api/setConnectorProps", method = RequestMethod.POST)
    public ResponseEntity<Object> changeConnectorProperties(@RequestBody Map<String, ?> input) {
        try {
            String classe = (String) input.get("class");
            //Check if classe parameter exits else BAD_REQUEST
            if (classe != null) {
                if (classe.equals("postgres")) {
                    String id = (String) input.get("id");
                    if (id != null) {
                        //Get Connector by ID
                        DBConnector db = core.getConnectorById(id);
                        if (db != null && db instanceof PostgreSQL) {
                            //Cast Connector
                            PostgreSQL postgreSQL = (PostgreSQL) db;

                            String newid = (String) input.get("newid");
                            if(newid != null)
                                postgreSQL.setName(newid);

                            String database = (String) input.get("database");
                            if(database != null)
                                postgreSQL.setDatabase(database);

                            String schema = (String) input.get("schema");
                            if(schema != null)
                                postgreSQL.setSchema(schema);

                            String hostname = (String) input.get("hostname");
                            if(hostname != null)
                                postgreSQL.setHostname(hostname);

                            Integer port = (Integer) input.get("port");
                            if (port != null) {
                                postgreSQL.setPort(port);
                            }

                            String username = (String) input.get("username");
                            if(username != null)
                                postgreSQL.setUsername(username);

                            String password = (String) input.get("password");
                            if(password != null)
                                postgreSQL.setPassword(password);

                            String error = postgreSQL.updateConnector();
                            if (error == null) {
                                core.writeConfig(core.getConfigPath(), core.getConnectionPath());
                                return new ResponseEntity<>(HttpStatus.OK);
                            } else {
                                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
                            }
                        }
                    }
                }
                if (classe.equals("sqlite")) {
                    //Index of connector to be changed
                    String orgId = (String) input.get("orgid");
                    String id = (String) input.get("id");
                    if (orgId != null && id != null) {
                        DBConnector db = core.getConnectorById(orgId);
                        if (db != null) {
                            if (db instanceof SQLite) {
                                SQLite sqLite = (SQLite) db;
                                sqLite.setConnectorId(id);
                                core.writeConfig(core.getConfigPath(), core.getConnectionPath());
                                return new ResponseEntity<>(HttpStatus.OK);
                            } else {
                                return new ResponseEntity<>("Given connector is not a sqlite connector", HttpStatus.BAD_REQUEST);
                            }
                        } else {
                            return new ResponseEntity<>("Connector not found", HttpStatus.BAD_REQUEST);
                        }
                    }
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }

    /**
     * Get tables based on the Connector id
     *
     * @param input Connector id
     * @return Array with all Names
     */
    @RequestMapping(value = "/api/getTables", method = RequestMethod.POST)
    public ResponseEntity<Object> getTable(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            DBConnector db = core.getConnectorById(id);
            if (db != null) {
                return new ResponseEntity<>(db.getAllTables(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get features based on the Connector id and featureCollection id
     *
     * @param input Connector id
     * @return Array with all Names
     */
    @RequestMapping(value = "/api/getColumns", method = RequestMethod.POST)
    public ResponseEntity<ArrayList<String>> getFeatures(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            DBConnector db = core.getConnectorById(id);
            if (db != null) {
                String table = (String) input.get("table");
                if (table != null) {
                    ArrayList<String> list = db.getColumns(table);
                    if (list != null) {
                        return new ResponseEntity<>(list, HttpStatus.OK);
                    }
                }
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    /**
     * Execute SQL String and store it in DB
     *
     * @param input se APIDoc
     * @return see APIDoc
     */
    @RequestMapping(value = "/api/executeSQL", method = RequestMethod.POST)
    public ResponseEntity<Object> executeSQL(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            String sql = (String) input.get("sql");
            if (sql != null) {
                String collectionName = (String) input.get("collectionName");
                if (collectionName != null) {
                    DBConnector db = core.getConnectorById(id);
                    if (db != null) {
                        boolean check = (Boolean) input.get("check");
                        try {
                            FeatureCollection fc = db.execute(sql, collectionName, check);
                            core.writeConfig(core.getConfigPath(), core.getConnectionPath());
                            return new ResponseEntity<>(fc, HttpStatus.OK);
                        } catch (Exception e) {
                            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("SQL string missing", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Rename a featureCollection
     *
     * @param input se APIDoc
     * @return see APIDoc
     */
    @RequestMapping(value = "/api/renameCollection", method = RequestMethod.POST)
    public ResponseEntity<Object> renameCollection(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            String orgName = (String) input.get("orgName");
            if (orgName != null) {
                String alias = (String) input.get("alias");
                if (alias != null) {
                    DBConnector db = core.getConnectorById(id);
                    if (db != null) {
                        db.renameTable(orgName, alias);
                        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
                        return new ResponseEntity<>("OK", HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Original Name missing", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Rename a featureCollection
     *
     * @param input se APIDoc
     * @return see APIDoc
     */
    @RequestMapping(value = "/api/renameProp", method = RequestMethod.POST)
    public ResponseEntity<Object> renameProp(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id missing", HttpStatus.BAD_REQUEST);
        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Original Name missing", HttpStatus.BAD_REQUEST);
        String orgName = (String) input.get("orgName");
        if (orgName == null) return new ResponseEntity<>("CollectionName missing", HttpStatus.BAD_REQUEST);
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not existing", HttpStatus.BAD_REQUEST);
        String alias = (String) input.get("alias");
        if (alias == null) return new ResponseEntity<>("Alias is missing", HttpStatus.BAD_REQUEST);

        db.renameProp(table, orgName, alias);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    /**
     * Change admin Password hashed to a file
     * @param input password to be saved
     * @return sett APIDoc
     */
    @RequestMapping(value = "/api/changePwd", method = RequestMethod.POST)
    public ResponseEntity<Object> changePassword(@RequestBody Map<String, ?> input) {
        String pwd = (String) input.get("pwd");
        if (pwd == null) return new ResponseEntity<>("No password provided missing", HttpStatus.BAD_REQUEST);

        try (PrintWriter out = new PrintWriter("./config/admin.pw")) {
            out.print(BCrypt.hashpw(pwd, BCrypt.gensalt(12)));
        } catch (Exception e) {

        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/api/setGeo", method = RequestMethod.POST)
    public ResponseEntity<Object> setGeo(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Database Connector id missing", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Database Table missing", HttpStatus.BAD_REQUEST);

        String column = (String) input.get("column");
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("No connector found for the id: " + id, HttpStatus.BAD_REQUEST);

        db.setGeo(table, column);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return null;
    }

    @RequestMapping(value = "/api/setId", method = RequestMethod.POST)
    public ResponseEntity<Object> setId(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Database Connector id missing", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Database Table missing", HttpStatus.BAD_REQUEST);

        String column = (String) input.get("column");
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("No connector found for the id: " + id, HttpStatus.BAD_REQUEST);

        db.setId(table, column);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return null;
    }

    @RequestMapping(value = "/api/excludeTable", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeTable(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);

        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);

        Object exclude = input.get("exclude");
        if (exclude == null) return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);

        boolean excludeVal = (Boolean) exclude;
        db.setTableExclude(table, excludeVal);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/excludeColumn", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeColumn(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);

        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);

        String column = (String) input.get("column");
        if (column == null) return new ResponseEntity<>("Column is null", HttpStatus.BAD_REQUEST);

        Object exclude = input.get("exclude");
        if (exclude == null) return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);

        boolean excludeVal = (Boolean) exclude;
        db.setColumnExclude(table, column, excludeVal);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @RequestMapping(value = "/api/excludeAllTables", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeAllTables(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);

        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
        Object exl = input.get("exclude");
        if (exl == null) return new ResponseEntity<>("Excluded Value is null", HttpStatus.BAD_REQUEST);
        boolean exclude = (Boolean) exl;
        for (int i = 0; i < db.getAllTables().size(); i++)
            db.setTableExclude(db.getAllTables().get(i), exclude);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/excludeAllColumns", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeAllColumns(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Table is null", HttpStatus.BAD_REQUEST);
        Object exc = input.get("exclude");
        if (exc == null) return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);
        boolean exclude = (Boolean) exc;
        for (int i = 0; i < db.getColumns(table).size(); i++)
            db.setColumnExclude(table, db.getColumns(table).get(i), exclude);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/addImportantLink", method = RequestMethod.POST)
    public ResponseEntity<Object> addImportantLink(@RequestBody Map<String, ?> input) {
        String link = (String) input.get("link");
        if (link == null) return new ResponseEntity<>("Link is null", HttpStatus.BAD_REQUEST);
        String name = (String) input.get("name");
        if (name == null) return new ResponseEntity<>("Name is null", HttpStatus.BAD_REQUEST);
        core.addLink(link, name);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @RequestMapping(value = "/getImportantLinks", method = RequestMethod.POST)
    public ResponseEntity<Object> getImportantLinks() {
        return new ResponseEntity<>(core.getLinks(), HttpStatus.OK);
    }

    @RequestMapping(value = "/api/removeImportantLink", method = RequestMethod.POST)
    public ResponseEntity<Object> removeLink(@RequestBody Map<String, ?> input) {
        String name = (String) input.get("name");
        if (name == null) return new ResponseEntity<>("Name is null", HttpStatus.BAD_REQUEST);

        core.removeLink(name);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/api/deleteConnector", method = RequestMethod.POST)
    public ResponseEntity<Object> deleteConnector(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
            core.removeConnector(id);
            core.writeConfig(core.getConfigPath(), core.getConnectionPath());
            return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/editSQL", method = RequestMethod.POST)
    public ResponseEntity<Object> editSQL(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
        String sql = (String )input.get("sql");
        if(sql == null) return new ResponseEntity<>("SQL is null", HttpStatus.BAD_REQUEST);
        String sqlName = (String )input.get("sqlName");
        if(sqlName == null) return new ResponseEntity<>("SQLName is null", HttpStatus.BAD_REQUEST);
        String newName = (String )input.get("newName");
        if(newName == null){
            db.getSqlString().put(sqlName,sql);
        }else {
            db.getSqlString().remove(sqlName);
            TableConfig obj = db.getConfig().remove(sqlName);
            obj.setAlias(newName);
            obj.setTable(newName);
            db.getConfig().put(newName, obj);
            db.getSqlString().put(newName,sql);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/deleteSQL", method = RequestMethod.POST)
    public ResponseEntity<Object> deleteSQL(@RequestBody Map<String, ?> input) {
        String name = (String) input.get("name");
        if (name == null) return new ResponseEntity<>("SQL name is null", HttpStatus.BAD_REQUEST);
        Object o = core.deleteSQL(name);
        core.writeConfig(core.getConfigPath(), core.getConnectionPath());
        return new ResponseEntity<>(o, HttpStatus.OK);
    }


    @RequestMapping(value = "/api/checkConnection", method = RequestMethod.POST)
    public ResponseEntity<Object> checkConnection(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Database Connector id missing", HttpStatus.BAD_REQUEST);
        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connection not found", HttpStatus.BAD_REQUEST);
        String error = db.checkConnection();
        return error != null ? new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR) : new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/getGeo", method = RequestMethod.POST)
    public ResponseEntity<Object> getGeo(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);

        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(db.getAllGeometry(table),HttpStatus.OK);
    }

    @RequestMapping(value = "/api/getId", method = RequestMethod.POST)
    public ResponseEntity<Object> getId(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id == null) return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);

        DBConnector db = core.getConnectorById(id);
        if (db == null) return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);

        String table = (String) input.get("table");
        if (table == null) return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(db.getAllPrimaryKey(table),HttpStatus.OK);
    }

    @RequestMapping("/getPagingLimit")
    public String checkConnection() {
        return core.getPagingLimit();
    }
}