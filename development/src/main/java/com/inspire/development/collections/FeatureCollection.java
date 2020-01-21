package com.inspire.development.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FeatureCollection extends mil.nga.sf.geojson.FeatureCollection {
    private String id;
    private ArrayList<Link> links;
    HashMap<String,HashMap<String, ArrayList<ArrayList<Double>>>> extent = new HashMap<>();

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

    public HashMap<String, HashMap<String, ArrayList<ArrayList<Double>>>> getExtent() {
        return extent;
    }

    public void setBB(ArrayList<Double> bb){
        HashMap<String, ArrayList<ArrayList<Double>>> m = new HashMap<>();
        ArrayList<ArrayList<Double>> a = new ArrayList<>(Arrays.asList(bb));
        m.put("bbox", a);
        extent.put("spatial", m);
    }
}
