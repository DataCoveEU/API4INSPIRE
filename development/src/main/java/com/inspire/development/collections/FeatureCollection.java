package com.inspire.development.collections;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FeatureCollection extends mil.nga.sf.geojson.FeatureCollection {
    HashMap<String, HashMap<String, ArrayList<List<Double>>>> extent = new HashMap<>();
    private String id;
    private ArrayList<Link> links;

    public FeatureCollection(String id) {
        this.id = id;
        links = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    @JsonProperty
    public HashMap<String, HashMap<String, ArrayList<List<Double>>>> getExtent() {
        return extent;
    }

    public void setBB(List<Double> bb) {
        HashMap<String, ArrayList<List<Double>>> m = new HashMap<>();
        ArrayList<List<Double>> a = new ArrayList<>(Arrays.asList(bb));
        m.put("bbox", a);
        extent.put("spatial", m);
    }
}
