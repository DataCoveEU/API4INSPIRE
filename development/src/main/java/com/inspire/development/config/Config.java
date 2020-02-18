package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inspire.development.collections.ImportantLinkList;

import java.io.File;

public class Config {
    DBConnectorList connectors;
    String logPath;
    String sqlitePath;
    String configPath;
    ImportantLinkList importantLinks;

    public Config(){
        logPath = "./../ogcapisimple/sqlite";

        if(System.getProperty("log4j.saveDirectory") == null)
            System.setProperty("log4j.saveDirectory",logPath);

        sqlitePath = "./../ogcapisimple/sqlite";
        configPath = "./../ogcapisimple/config.json";
        connectors = new DBConnectorList();
        importantLinks = new ImportantLinkList();
    }

    public ImportantLinkList getImportantLinks() {
        return importantLinks;
    }

    public Config(@JsonProperty("logPath") String logPath, @JsonProperty("sqlitePath") String sqlitePath, @JsonProperty("connectors") DBConnectorList connectors, @JsonProperty("importantLinks") ImportantLinkList importantLinks,@JsonProperty("configPath") String configPath){
        this.logPath = logPath;

        if(System.getProperty("log4j.saveDirectory") == null)
            System.setProperty("log4j.saveDirectory",logPath);

        this.sqlitePath = sqlitePath;
        this.configPath = configPath;
        this.connectors = connectors;
        this.importantLinks = importantLinks;
    }

    public DBConnectorList getConnectors() {
        return connectors;
    }

    public String getLogPath() {
        if(System.getProperty("log4j.saveDirectory") != null){
            return System.getProperty("log4j.saveDirectory");
        }
        return logPath;
    }

    public String getSqlitePath() {
        if(System.getProperty("sqlite.directory") != null){
            return System.getProperty("sqlite.directory");
        }
        return sqlitePath;
    }

    public void setLogPath(String logPath) {
        System.setProperty("log4j.saveDirectory",logPath);
        this.logPath = logPath;
    }

    public void setSqlitePath(String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }

    @JsonIgnore
    public String getPagingLimit(){
        if(System.getProperty("paging.limit") != null){
            return System.getProperty("paging.limit");
        }
        return "10000";
    }

    public String getConfigPath() {
        return configPath;
    }

}
