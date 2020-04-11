/*
 * The OGC API Simple provides enviromental data
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

import java.util.ArrayList;
import java.util.List;

/**
 * Returned to the /collections method
 */
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

    public void setCollections(List<FeatureCollection> c){
        this.collections = c;
    }
}
