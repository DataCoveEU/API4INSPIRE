package com.inspire.development.controller;

import com.inspire.development.collections.Collections;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.conformance.ConformanceDeclaration;
import com.inspire.development.core.Core;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.PostgreSQL;
import com.inspire.development.database.connector.SQLite;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import mil.nga.sf.geojson.Feature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {
    private Core core;

    public RESTController() {
        core = new Core();

        //SQLite c = new SQLite("inspireDB.sqlite","Inspire");
       /* //c.execute("select * from tna_insp_airspacearea","TobiasIsJustATest");
        //c.renameTable("tna_insp_navaids", "Tobias");
        PostgreSQL p = new PostgreSQL("localhost",25432,"inspire", "tna", "Postgres","inspire", "1nsp1r3_2#2#");
        core.getConnectors().add(p);;*/
        // core.getConnectors().add(c);

    }

    @GetMapping("/collections")
    public Collections Collections() {
        return new Collections(Arrays.asList(core.getAll()));
    }

    /**
     * Gets the conformance declaration
     *
     * @return the json file which contains the conformance declaration
     */
    @GetMapping("/conformance")
    public ConformanceDeclaration getConformance() {
        //When calling there is a Code 500
        String[] links = {"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
                //"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/oas30", ==> API Definition not yet implemented
                //"http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/html", ==> Parameter to choose html no implemented yet
                "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/geojson"};
        return new ConformanceDeclaration(links);
    }

    /**
     * Gets a special feature collection from the database
     *
     * @param id the id of the feature Collection
     * @return the collection with the id
     */
    @GetMapping("/collections/{collectionId}")
    public ResponseEntity<Object> getCollections(@PathVariable("collectionId") String id) {
        FeatureCollection fc = core.get(id, true, 0, 0, null);
        if (fc != null) {
            return new ResponseEntity<>(fc, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the items of a special feature collection
     *
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @GetMapping("/collections/{collectionId}/items")
    public ResponseEntity<Object> getCollectionItems(@PathVariable("collectionId") String id,
                                                     @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(10000) int limit,
                                                     @RequestParam(required = false) double[] bbox,
                                                     @RequestParam(required = false, defaultValue = "0") @Min(0) @Max(10000) int offset) {
        FeatureCollection fc = core.get(id, false, limit, offset, bbox);
        if (fc != null) {
            return new ResponseEntity<>(fc, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Gets the items of a special item (=feature) from a special feature collection
     *
     * @param collectionId the id of the feature Collection
     * @param featureId    the id of the item (=feature)
     * @return a special item (=feature) from a collection
     */
    @GetMapping("/collections/{collectionId}/items/{featureId}")
    public ResponseEntity<Object> getItemFromCollection(
            @PathVariable("collectionId") String collectionId,
            @PathVariable("featureId") String featureId) {
        //TODO: implement the method to return a special feature from a special collection
        Feature f = core.getFeature(collectionId, featureId);
        if (f != null) {
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
     *
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
                        core.writeConfig();
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
                            if (portString != null) {
                                int port = Integer.parseInt(portString);
                                postgreSQL.setPort(port);
                            }

                            String username = (String) input.get("username");
                            postgreSQL.setUsername(username);

                            String password = (String) input.get("password");
                            postgreSQL.setPassword(password);

                            String error = db.updateConnector();
                            if (error == null) {
                                return new ResponseEntity<>("OK", HttpStatus.OK);
                            } else {
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
                            String path = (String) input.get("path");
                            if (path != null) {
                                sqLite.setPath(path);
                                String error = db.updateConnector();
                                if (error == null) {
                                    return new ResponseEntity<>("OK", HttpStatus.OK);
                                } else {
                                    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
                                }
                            }
                        }
                    } else {
                        return new ResponseEntity<>("Connector id is null", HttpStatus.OK);
                    }
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
     * Gets tables based on the Connector id
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
     * Gets features based on the Connector id and featureCollection id
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
                            return new ResponseEntity<>(db.execute(sql, collectionName, check), HttpStatus.OK);
                        }catch (Exception e){
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
        if (id != null) {
            String table = (String) input.get("table");
            if (table != null) {
                String orgName = (String) input.get("orgName");
                if (orgName != null) {
                    DBConnector db = core.getConnectorById(id);
                    if (db != null) {
                        String alias = (String) input.get("alias");
                        if (alias != null) {
                            db.renameProp(table, orgName, alias);
                            return new ResponseEntity<>("OK", HttpStatus.OK);
                        } else {
                            return new ResponseEntity<>("Alias is missing", HttpStatus.BAD_REQUEST);
                        }
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
     * Change admin Password hashed to a file
     *
     * @param input password to be saved
     * @return
     */
    @RequestMapping(value = "/api/changePwd", method = RequestMethod.POST)
    public ResponseEntity<Object> changePassword(@RequestBody Map<String, ?> input) {
        String pwd = (String) input.get("pwd");
        if (pwd != null) {
            try (PrintWriter out = new PrintWriter("./config/admin.pw")) {
                out.print(BCrypt.hashpw(pwd, BCrypt.gensalt(12)));
            } catch (Exception e) {

            }
            return new ResponseEntity<>("OK", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No password provided missing", HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/api/setGeo", method = RequestMethod.POST)
    public ResponseEntity<Object> setGeo(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            String table = (String) input.get("table");
            if (table != null) {
                String column = (String) input.get("column");
                DBConnector db = core.getConnectorById(id);
                if (db != null) {
                    db.setGeo(table, column);
                } else {
                    return new ResponseEntity<>("No connector found for id: " + id, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Database Table missing", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Database Connector Id missing", HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @RequestMapping(value = "/api/setId", method = RequestMethod.POST)
    public ResponseEntity<Object> setId(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if (id != null) {
            String table = (String) input.get("table");
            if (table != null) {
                String column = (String) input.get("column");
                DBConnector db = core.getConnectorById(id);
                    if (db != null) {
                        db.setId(table, column);
                    } else {
                        return new ResponseEntity<>("No connector found for id: " + id, HttpStatus.BAD_REQUEST);
                    }

            } else {
                return new ResponseEntity<>("Database Table missing", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Database Connector Id missing", HttpStatus.BAD_REQUEST);
        }
        return null;
    }

  @RequestMapping(value="/api/excludeTable", method = RequestMethod.POST)
  public ResponseEntity<Object> excludeTable(@RequestBody Map<String, ?> input) {
    String id = (String) input.get("id");
    if (id != null) {
      DBConnector db = core.getConnectorById(id);
      if (db != null) {
        String table = (String) input.get("table");
        if(table != null) {
          Object exclude = input.get("exclude");
          if(exclude != null) {
            boolean excludeVal = (Boolean) exclude;
            db.setTableExclude(table, excludeVal);
            return new ResponseEntity<>(HttpStatus.OK);
          } else {
            return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);
          }
        } else {
          return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);
        }
      } else {
        return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/excludeColumn", method = RequestMethod.POST)
  public ResponseEntity<Object> excludeColumn(@RequestBody Map<String, ?> input) {
    String id = (String) input.get("id");
    if (id != null) {
      DBConnector db = core.getConnectorById(id);
      if (db != null) {
        String table = (String) input.get("table");
        if(table != null) {
          String column = (String) input.get("column");
          if(column != null) {
            Object exclude = input.get("exclude");
            if(exclude != null) {
              boolean excludeVal = (Boolean) exclude;
              db.setColumnExclude(table, column, excludeVal);
              return new ResponseEntity<>(HttpStatus.OK);
            } else {
              return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);
            }
          } else {
            return new ResponseEntity<>("Column is null", HttpStatus.BAD_REQUEST);
          }

        } else {
          return new ResponseEntity<>("Table name is null", HttpStatus.BAD_REQUEST);
        }
      } else {
        return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/api/excludeAllTables", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeAllTables(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if(id != null) {
            DBConnector db = core.getConnectorById(id);
            if(db != null) {
                Object exl = input.get("exclude");
                if(exl != null) {
                    boolean exclude = (Boolean) exl;
                    for(int i = 0; i < db.getAllTables().size(); i++) {
                        db.setTableExclude(db.getAllTables().get(i), exclude);
                    }
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Exclued Value is null", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
            }
        } else {
            return  new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        }
  }

    @RequestMapping(value = "/api/excludeAllColumns", method = RequestMethod.POST)
    public ResponseEntity<Object> excludeAllColumns(@RequestBody Map<String, ?> input) {
        String id = (String) input.get("id");
        if(id != null) {
            DBConnector db = core.getConnectorById(id);
            if(db != null) {
                String table = (String) input.get("table");
                if(table != null) {
                    Object exc = input.get("exclude");
                    if(exc != null) {
                        boolean exclude = (Boolean) exc;
                        for(int i = 0; i < db.getColumns(table).size(); i++) {
                            db.setColumnExclude(table, db.getColumns(table).get(i), exclude);
                        }
                        return new ResponseEntity<>(HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Exclude value is null", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>("Table is null", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Connector id not found", HttpStatus.BAD_REQUEST);
            }
        } else {
            return  new ResponseEntity<>("Connector id is null", HttpStatus.BAD_REQUEST);
        }
    }


}
