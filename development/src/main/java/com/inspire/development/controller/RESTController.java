package com.inspire.development.controller;

import com.inspire.development.database.connector.SQLite;
import com.inspire.development.featureCollection.FeatureCollection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {

    @GetMapping("/test")
    public FeatureCollection test() {
        SQLite c = new SQLite("/Users/pressler/Downloads/inspireDB.sqlite");
        //c.checkConnection();
        return c.getAll()[1];
    }
}
