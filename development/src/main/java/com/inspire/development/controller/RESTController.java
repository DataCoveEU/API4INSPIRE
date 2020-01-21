package com.inspire.development.controller;

import com.inspire.development.collections.Collections;
import com.inspire.development.collections.FeatureCollection;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.core.Core;
import com.inspire.development.database.connector.PostgreSQL;
import com.inspire.development.database.connector.SQLite;
import com.sun.jdi.connect.Connector;
import mil.nga.sf.geojson.Feature;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
public class RESTController {
    private Core core;

    public RESTController(){
        core = new Core();
        SQLite c = new SQLite("inspireDB.sqlite","Inspire");
        c.renameTable("tna_insp_navaids", "Tobias");
        c.renameFeature("tna_insp_navaids", "metadataproperty", "meta");
       // PostgreSQL p = new PostgreSQL("localhost",25432,"inspire", "tna", "Postgres");
        core.getConnectors().add(c);
        //core.getConnectors().add(p);
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
        //TODO Remove props
        return core.get(id, false);
    }

    /**
     * Gets the items of a special feature collection
     *
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @GetMapping("/collections/{collectionId}/items")
    public FeatureCollection getCollectionItems(@PathVariable("collectionId") String id) {
        return core.get(id, true);
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


}
