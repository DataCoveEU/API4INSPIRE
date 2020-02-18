package com.inspire.development.collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mil.nga.sf.geojson.Feature;

import java.util.ArrayList;
import java.util.List;

public class FeatureWithLinks extends Feature {
    private List<Link> links = new ArrayList<>();
    @JsonIgnore
    public String collection;

    public FeatureWithLinks(Feature f){
        setBbox(f.getBbox());
        setGeometry(f.getGeometry());
        setId(f.getId());
        setProperties(f.getProperties());
    }

    public List<Link> getLinks(){
        return links;
    }

}
