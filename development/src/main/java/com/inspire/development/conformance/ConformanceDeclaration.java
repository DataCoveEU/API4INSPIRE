/*
 * Created on Wed Feb 26 2020
 *
 * @author Tobias Pressler
 *
 * Copyright (c) 2020 - Tobias Pressler
 */
package com.inspire.development.conformance;

public class ConformanceDeclaration {

    private String[] conformsTo;

    public ConformanceDeclaration(String[] conformsTo) {
        this.conformsTo = conformsTo;
    }

    public String[] getConformsTo() {
        return this.conformsTo;
    }
}
