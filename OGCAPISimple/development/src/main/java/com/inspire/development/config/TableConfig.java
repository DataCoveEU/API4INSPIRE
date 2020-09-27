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
package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

/**
 * Used for configuring a table
 */
public class TableConfig {
    private String alias;
    private String table;
    private String geoCol;
    private String idCol;
    private boolean exclude;
    private HashMap<String, ColumnConfig> map;

    /**
     * Create a new table config
     * @param table original table name
     * @param alias table alias to be used
     */
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

    public void setTable(String table){this.table = table;}
}
