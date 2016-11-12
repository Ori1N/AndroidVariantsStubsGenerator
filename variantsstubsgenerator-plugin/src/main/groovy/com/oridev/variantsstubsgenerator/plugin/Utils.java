package com.oridev.variantsstubsgenerator.plugin;

/**
 * Created by Ori on 03/11/2016.
 */

public class Utils {


    private static final boolean DEBUG = true;
    static void logMessage(String msg) {
        logMessage(msg, false);
    }
    static void logMessage(String msg, boolean showOnRelease) {
        if (DEBUG || showOnRelease) {
            System.out.println(msg);
        }
    }

}
