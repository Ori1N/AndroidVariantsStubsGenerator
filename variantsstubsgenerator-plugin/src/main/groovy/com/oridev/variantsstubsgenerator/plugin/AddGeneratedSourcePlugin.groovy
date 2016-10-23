package com.oridev.variantsstubsgenerator.plugin;

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project


public class AddGeneratedSourcePlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        def filesJson = new File(getJsonPath(project))
        if (filesJson.exists()) {

            def filesPaths = new JsonSlurper().parse(filesJson)
            logMessage("applying AddGeneratedSourcePlugin, (info json containing $filesPaths.size entries)..", true);
            filesPaths.each { generatedEntry ->
                def entryTarget = generatedEntry.flavor;

                if (["debug", "release"].contains(entryTarget)) {
                    // if flavorTo is a buildType
                    logMessage("handling entry with buildType target [$entryTarget], path [$generatedEntry.path]");

                    project.android {
                        sourceSets {
                            buildTypes.each { buildType ->
                                try {
                                    // if this generated file should be added to current flavor
                                    if (entryTarget.equals(buildType.name)) {
                                        logMessage("adding generated source [$generatedEntry.path] to buildType [$entryTarget]", true);
                                        // add generated file path to flavor source set
                                        "${entryTarget}" {
                                            java.srcDirs += new File(generatedEntry.path)
                                        }
                                    }

                                } catch (Exception e) {
                                    println(e);
                                }
                            }
                        }
                    }

                } else {
                    // if flavorTo is a flavor
                    logMessage("handling entry with flavor target [$entryTarget], path [$generatedEntry.path]");

                    project.android {
                        sourceSets {
                            productFlavors.each { flavor ->
                                try {
                                    // if this generated file should be added to current flavor
                                    if (entryTarget.equals(flavor.name)) {
                                        logMessage("adding generated source [$generatedEntry.path] to flavor [$entryTarget]", true);
                                        // add generated file path to flavor source set
                                        "${entryTarget}" {
                                            java.srcDirs += new File(generatedEntry.path)
                                        }
                                    }

                                } catch (Exception e) {
                                    println(e);
                                }
                            }
                        }
                    }

                }
            }
        }


//        project.android {
//            sourceSets {
//                //buildTypes.each { buildType
//                    productFlavors.each { flavor ->
//
//                        try {
//                            // for each flavor: read json file
//                            def flavorName = flavor.name
//
//                            def filesJson = new File(getJsonPath(project))
//                            if (filesJson.exists()) {
//                                def filesPaths = new JsonSlurper().parse(filesJson)
//                                filesPaths.each { generatedEntry ->
//                                    // if this generated file should be added to current flavor
//                                    if (generatedEntry.flavor.equals(flavorName)) {
//                                        println "adding generated source {$generatedEntry.path} to flavor ${generatedEntry.flavor}"
//                                        // add generated file path to flavor source set
//                                        "${flavorName}" {
//                                            java.srcDirs += new File(generatedEntry.path)
//                                        }
//                                    }
//                                }
//                            }
////
////                            def filesJsonRelease = new File(getJsonPath(project, "release", flavorName))
////                            if (filesJsonRelease.exists()) {
////                                def filesPaths = new JsonSlurper().parse(filesJsonRelease)
////                                filesPaths.each { path ->
////                                    println "adding generated source {$path} to flavor ${flavorName}"
////                                    "${flavorName}" {
////                                        java.srcDirs += new File(path)
////                                    }
////                                }
////                            }
//                        } catch (Exception e) {
//                            println(e);
//                        }
//                    }
//                //}
//
//            }
//        }
    }

    static String getJsonPath(Project project) {
        return "$project.buildDir/generated/assets/variantsStubsGenerator/meta/generated_files.json";

//        return "$project.buildDir/generated/source/apt/${flavorName}/${buildType}/" +
//                "variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json";

    }


    private static final boolean DEBUG = false;
    static void logMessage(String msg) {
        logMessage(msg, false);
    }
    static void logMessage(String msg, boolean showOnRelease) {
        if (DEBUG || showOnRelease) {
            println(msg);
        }
    }

}
