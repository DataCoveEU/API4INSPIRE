/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportantLink {
    private String link;
    private String name;

    /**
     * Create a new ImportantLink
     * @param link link to be used
     * @param name name the link should be showed
     */
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
