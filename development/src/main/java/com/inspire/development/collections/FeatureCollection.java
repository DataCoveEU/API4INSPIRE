package com.inspire.development.collections;

import java.util.ArrayList;

public class FeatureCollection extends mil.nga.sf.geojson.FeatureCollection {
    private String id;
    private ArrayList<Link> links;
    public FeatureCollection(String id){
        this.id = id;
        links = new ArrayList<>();
    }

    public String getId(){
        return id;
    }

    public ArrayList<Link> getLinks(){
        return links;
    }
}
