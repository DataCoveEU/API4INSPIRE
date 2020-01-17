package com.inspire.development.collections;

public class FeatureCollection extends mil.nga.sf.geojson.FeatureCollection {
    private String id;
    public FeatureCollection(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }
}
