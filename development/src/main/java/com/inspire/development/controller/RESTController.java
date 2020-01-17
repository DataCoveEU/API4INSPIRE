package com.inspire.development.controller;

import com.inspire.development.database.connector.SQLite;
import com.inspire.development.featureCollection.FeatureCollection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {

    @GetMapping("/collections")
    public FeatureCollection test() {
        SQLite c = new SQLite("/home/lukas/Documents/inspireDB.sqlite");
        //c.checkConnection();
        return c.getAll()[1];
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
    public String getCollections(@PathVariable("collectionId") String id) {
        //TODO: implement the method to return the feature collection with the matching collection ID
        return "Collection: " + id;
    }

    /**
     * Gets the items of a special feature collection
     *
     * @param id the id of the feature Collection
     * @return all of the items of the matching feature collection
     */
    @GetMapping("/collections/{collectionId}/items")
    public String getCollectionItems(@PathVariable("collectionId") String id) {
        //TODO: implement the method to return all the intems with the matching collection ID
        return "Features from the collection: " + id;
    }

    /**
     * Gets the items of a special item (=feature) from a special feature collection
     *
     * @param collectionId the id of the feature Collection
     * @param featureId the id of the item (=feature)
     * @return a special item (=feature) from a collection
     */
    @GetMapping("/collections/{collectionId}/items/{featureId}")
    public String getItemFromCollection(@PathVariable("collectionId") String collectionId, @PathVariable("featureId") String featureId) {
        //TODO: implement the method to return a special feature from a special collection
        return "Collection: " + collectionId + "; Feature: " + featureId;

    }
}
