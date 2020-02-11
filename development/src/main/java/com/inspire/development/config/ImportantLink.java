package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportantLink {
    String link;
    String name;

    public ImportantLink(@JsonProperty("link") String link, @JsonProperty("name") String name){
        this.link = link;
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }
}
