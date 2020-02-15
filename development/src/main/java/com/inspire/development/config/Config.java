package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inspire.development.collections.ImportantLinkList;

import java.io.File;

public class Config {
    DBConnectorList connectors;
    String logPath;
    String sqlitePath;
    ImportantLinkList importantLinks;

    public Config(){
        logPath = "./logs";
        sqlitePath = "./sqlite";
        connectors = new DBConnectorList();
        importantLinks = new ImportantLinkList();
    }

    public ImportantLinkList getImportantLinks() {
        return importantLinks;
    }

    public Config(@JsonProperty("logPath") String logPath, @JsonProperty("sqlitePath") String sqlitePath, @JsonProperty("connectors") DBConnectorList connectors, @JsonProperty("importantLinks") ImportantLinkList importantLinks){
        this.logPath = logPath;
        this.sqlitePath = sqlitePath;
        this.connectors = connectors;
        this.importantLinks = importantLinks;
    }

    public DBConnectorList getConnectors() {
        return connectors;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getSqlitePath() {
        return sqlitePath;
    }

    public void setLogPath(String logPath) {
        System.setProperty("log4j.saveDirectory",logPath);
        this.logPath = logPath;
    }

    public void setSqlitePath(String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }
}
