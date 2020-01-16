package com.inspire.development.controller;

import com.inspire.development.database.connector.SQLite;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTController {

    @GetMapping("/test")
    public String test() {
        SQLite c = new SQLite("/Users/pressler/Downloads/inspireDB.sqlite");
        //c.checkConnection();
        c.getAll();
        return "Hello World";
    }
}
