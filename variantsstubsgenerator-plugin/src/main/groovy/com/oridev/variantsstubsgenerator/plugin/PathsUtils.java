package com.oridev.variantsstubsgenerator.plugin;

import java.io.File;

/**
 * Created by Ori on 10/11/2016.
 */

public class PathsUtils {

    /* Path manipulation */

    private static final String BUILD_RELATIVE_PATH = "generated/source/flavors/";

    public static String getTargetSourceSetPath(String buildDir, String flavorTo) {
        return buildDir + "/" + BUILD_RELATIVE_PATH + flavorTo + "/java";
    }


//    public static String getSourceFlavor(String sourcePath, String packagePath) {
//        String sourceSetPath = sourcePath.substring(0, sourcePath.indexOf(packagePath) - 1);
//        // get variant from {sourcePath}/{flavor}/java
//        return new File(sourceSetPath).getParentFile().getName();
//    }

}
