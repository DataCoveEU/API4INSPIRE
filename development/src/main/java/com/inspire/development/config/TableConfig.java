package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class TableConfig {
    String alias;
    String table;
    HashMap<String, String> map;


    public TableConfig(String table, String alias){
        this.alias = alias;
        this.table = table;
        map = new HashMap<>();
    }

    @JsonCreator
    public TableConfig(@JsonProperty("table") String table, @JsonProperty("alias")String alias, @JsonProperty("map")HashMap<String,String> map){
        this.alias = alias;
        this.table = table;
        this.map = map;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public String getAlias() {
        return alias;
    }

    public String getTable() {
        return table;
    }

    public void setAlias(String alias){
        this.alias = alias;
    }
}
