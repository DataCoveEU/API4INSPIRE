/*
 * The OGC API Simple provides enviromental data
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inspire.development.collections.ImportantLinkList;
import com.inspire.development.database.DBConnector;

import java.net.URL;
import java.util.ArrayList;



public class Config {
    private DBConnectorList connectors;
    private String logPath;
    private String sqlitePath;
    private String configPath;
    private ImportantLinkList importantLinks;
    private String connectionPath;

    /**
     * Create a new config instance
     */
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
