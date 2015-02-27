package com.a9ski.jsf.exporter;

public enum ColumnType {
	HEADER("header"),
	FOOTER("footer");
    
    private final String facet;
    
    ColumnType(String facet) {
        this.facet = facet;
    }

    public String facet() {
        return facet;
    }
    
    @Override
    public String toString() {
        return facet;
    }
}
