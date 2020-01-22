package com.inspire.development.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Collections {
    List<FeatureCollection> collections;
    public Collections(List<FeatureCollection> collections){
        this.collections = collections;
    }

    public List<FeatureCollection> getCollections() {
        return collections;
    }

}
