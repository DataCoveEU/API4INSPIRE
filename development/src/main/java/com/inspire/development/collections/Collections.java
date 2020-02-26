/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.collections;

import java.util.ArrayList;
import java.util.List;

public class Collections {
    List<FeatureCollection> collections;
    List<Link> links = new ArrayList<>();

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
