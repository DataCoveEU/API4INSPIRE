/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class TableConfig {
    String alias;
    String table;
    String geoCol;
    String idCol;
    boolean exclude;
    HashMap<String, ColumnConfig> map;

    public TableConfig(String table, String alias) {
        this.alias = alias;
        this.table = table;
        this.exclude = false;
        map = new HashMap<>();
    }

    @JsonCreator
    public TableConfig(@JsonProperty("table") String table, @JsonProperty("alias") String alias,
                       @JsonProperty("map") HashMap<String, ColumnConfig> map, @JsonProperty("idCol") String idCol,
                       @JsonProperty("geoCol") String geoCol, @JsonProperty("exclude") boolean exclude) {
        this.alias = alias;
        this.table = table;
        this.map = map;
        this.geoCol = geoCol;
        this.idCol = idCol;
        this.exclude = exclude;
    }

    public boolean isExclude() {
        return exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public String getGeoCol() {
        return geoCol;
    }

    public void setGeoCol(String geoCol) {
        this.geoCol = geoCol;
    }

    public String getIdCol() {
        return idCol;
    }

    public void setIdCol(String idCol) {
        this.idCol = idCol;
    }

    public HashMap<String, ColumnConfig> getMap() {
        return map;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTable() {
        return table;
    }
}
