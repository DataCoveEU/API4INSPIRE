package com.inspire.development.collections;

import java.util.List;

public class Collections {
    List<FeatureCollection> collections;
    List<Link> links;

    public Collections(List<FeatureCollection> collections) {
        this.collections = collections;
    }

    public List<FeatureCollection> getCollections() {
        return collections;
    }

    public List<Link> getLinks() {
        return links;
    }
}
