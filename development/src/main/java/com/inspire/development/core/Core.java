package com.inspire.development.core;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.development.config.DBConnectorList;
import com.inspire.development.database.DBConnector;
import com.inspire.development.database.connector.SQLite;
import org.sqlite.core.DB;

import java.io.File;
import java.io.IOException;

public class Core {
    DBConnectorList connectors;

    public Core(){
        connectors = new DBConnectorList();
    }

    public static DBConnectorList parseConfig(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File("config/config.json"), DBConnectorList.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeConfig(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixIn(SQLite.class, DBConnector.class);
        try {
            objectMapper.writeValue(new File("config/config.json"), connectors);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DBConnectorList getConnectors(){
        return connectors;
    }


    public void setConnectors(DBConnectorList l){
        this.connectors = l;
    }

    public void addConnector(DBConnector d){
        this.connectors.add(d);
    }
}
