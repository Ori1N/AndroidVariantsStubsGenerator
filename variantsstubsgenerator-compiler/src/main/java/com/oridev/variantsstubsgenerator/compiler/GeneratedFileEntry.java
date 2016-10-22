package com.oridev.variantsstubsgenerator.compiler;

/**
 * Created by Ori on 20/10/2016.
 */

public class GeneratedFileEntry {

    private String flavor;
    private String path;

    public GeneratedFileEntry(String flavorTo, String path) {
        this.flavor = flavorTo;
        this.path = path;
    }

    public String getFlavor() {
        return flavor;
    }
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj == null || !(obj instanceof GeneratedFileEntry)) {
            return false;
        }
        GeneratedFileEntry entry = (GeneratedFileEntry) obj;
        return flavor.equals(entry.flavor) && path.equals(entry.path);
    }

    @Override
    public int hashCode() {
        return flavor.hashCode() + path.hashCode();
    }
}
