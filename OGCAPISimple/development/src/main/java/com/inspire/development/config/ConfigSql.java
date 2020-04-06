package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class ConfigSql {

    private HashMap<String, TableConfig> config;
    private HashMap<String, String> sqlString;

    /**
     * Create a new ConfigSql instance. Used for storing config and sqlString config in a file
     * @param config config
     * @param sqlString sqlString HashMap.
     */
    @JsonCreator
    public ConfigSql(@JsonProperty("config") HashMap<String, TableConfig> config, @JsonProperty("sqlString")HashMap<String, String> sqlString ){
        this.config = config;
        this.sqlString = sqlString;
    }

    public HashMap<String, TableConfig> getConfig() {
        return config;
    }

    public HashMap<String, String> getSqlString() {
        return sqlString;
    }
}
