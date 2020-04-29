/*
 * The OGC API Simple provides environmental data
 * Created on Wed Feb 26 2020
 * @author Tobias Pressler
 * Copyright (c) 2020 - Tobias Pressler
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.

 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */
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
