/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inspire.development.collections.ImportantLinkList;
import java.net.URL;

public class Config {
    private DBConnectorList connectors;
    private String logPath;
    private String sqlitePath;
    private String configPath;
    private ImportantLinkList importantLinks;
    private String connectionPath;

    public Config(){
        URL url = this.getClass().getClassLoader().getResource("../");

        String path = url == null ? "./" : url.getPath();

        logPath = path + "logs";

        sqlitePath = path + "sqlite";

        connectionPath = path + "connections.json";

        configPath = System.getenv("CONFIG_OGCAPISIMPLE") != null ? System.getenv("CONFIG_OGCAPISIMPLE") : path + "config.json";

        connectors = new DBConnectorList();
        importantLinks = new ImportantLinkList();
    }


    public Config(@JsonProperty("logPath") String logPath, @JsonProperty("sqlitePath") String sqlitePath, @JsonProperty("connectors") DBConnectorList connectors, @JsonProperty("importantLinks") ImportantLinkList importantLinks,@JsonProperty("configPath") String configPath){
        this.logPath = logPath;
        this.sqlitePath = sqlitePath;
        this.configPath = configPath;
        this.connectors = connectors;
        this.importantLinks = importantLinks;
    }

    /**
     * Get a list of all important links
     * @return list
     */
    public ImportantLinkList getImportantLinks() {
        return importantLinks;
    }


    /**
     * Get a list of all connections
     * @return list
     */
    public DBConnectorList getConnectors() {
        return connectors;
    }

    public String getConnectionPath() {
        return connectionPath;
    }

    /**
     * Get current log path
     * @return log path
     */
    public String getLogPath() {
        if(System.getenv("LOG_OGCAPISIMPLE") != null){
            return System.getenv("LOG_OGCAPISIMPLE");
        }
        return logPath;
    }

    /**
     * Get path of sqlite folder
     * @return sqlite folder
     */
    public String getSqlitePath() {
        if(System.getenv("SQLITE_OGCAPISIMPLE") != null){
            return System.getenv("SQLITE_OGCAPISIMPLE");
        }
        return sqlitePath;
    }

    /**
     * Get Paging Limit
     * @return paging limit
     */
    @JsonIgnore
    public String getPagingLimit(){
        if(System.getenv("PAGING_LIMIT_OGCAPISIMPLE") != null){
            return System.getenv("PAGING_LIMIT_OGCAPISIMPLE");
        }
        return "10000";
    }

    /**
     * Get config path
     * @return config path
     */
    public String getConfigPath() {
        return configPath;
    }
}
