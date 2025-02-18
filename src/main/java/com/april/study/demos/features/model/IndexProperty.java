package com.april.study.demos.features.model;

public class IndexProperty {
    Boolean index;
    String property;

    public IndexProperty(String property, Boolean index) {
        this.property = property;
        this.index = index;
    }

    public Boolean getIndex() {
        return index;
    }

    public void setIndex(Boolean index) {
        this.index = index;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
