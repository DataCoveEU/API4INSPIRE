package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class TableConfig {
    String alias;
    String table;
    String geoCol;
    String idCol;
    boolean execlude;
    HashMap<String, String> map;


    public boolean isExeclude() {
        return execlude;
    }

    public TableConfig(String table, String alias){
        this.alias = alias;
        this.table = table;
        this.execlude = false;
        map = new HashMap<>();
    }

    public String getGeoCol() {
        return geoCol;
    }

    public String getIdCol() {
        return idCol;
    }

    @JsonCreator
    public TableConfig(@JsonProperty("table") String table, @JsonProperty("alias")String alias, @JsonProperty("map")HashMap<String,String> map,@JsonProperty("idCol")String idCol,@JsonProperty("geoCol")String geoCol,@JsonProperty("execlude")boolean execlude){
        this.alias = alias;
        this.table = table;
        this.map = map;
        this.geoCol = geoCol;
        this.idCol = idCol;
        this.execlude = execlude;
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

    public void setGeoCol(String geoCol) {
        this.geoCol = geoCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public void setExeclude(boolean execlude){
        this.execlude = execlude;
    }
}
