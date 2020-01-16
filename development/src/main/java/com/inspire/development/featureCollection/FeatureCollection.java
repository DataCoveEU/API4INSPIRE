package com.inspire.development.featureCollection;

public class FeatureCollection extends mil.nga.sf.geojson.FeatureCollection {
    private String name;
    public FeatureCollection(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
