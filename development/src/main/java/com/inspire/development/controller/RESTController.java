package com.inspire.development.controller;

import com.inspire.development.collections.Collections;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.core.Core;
import com.inspire.development.database.connector.SQLite;
import com.inspire.development.collections.FeatureCollection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class RESTController {
    private Core core;

    public RESTController(){
        core = new Core();
        DBConnectorList list = core.parseConfig();
        if(list != null){
            core.setConnectors(list);
        }
    }

    @GetMapping("/collections")
    public Collections Collections() {
        //SQLite c = new SQLite("inspireDB.sqlite", "Inspire");
        //c.renameFeature("tna_insp_designatedpoint", "statename", "TOBIAS");
        //c.renameTable("tna_insp_designatedpoint", "tabelele");
        //core.getConnectors().add(c);
        //core.writeConfig();
        //String s = c.checkConnection();
        //return new Collections(Arrays.asList(c.getAll()));
        return null;
    }


}
