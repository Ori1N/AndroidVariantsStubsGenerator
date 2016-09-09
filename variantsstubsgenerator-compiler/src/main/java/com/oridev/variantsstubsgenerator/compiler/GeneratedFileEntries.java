package com.oridev.variantsstubsgenerator.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ori on 06/09/2016.
 */
public class GeneratedFileEntries {
    public final List<String> mEntries;
    public final String mFlavorFrom;

    public GeneratedFileEntries(String flavorFrom, String entry) {
        mFlavorFrom = flavorFrom;
        mEntries = new ArrayList<>();
        mEntries.add(entry);
    }

    public void addEntry(String entry) {
        mEntries.add(entry);
    }
}
