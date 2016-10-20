package com.oridev.variantsstubsgenerator.plugin;

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project


public class AddGeneratedSourcePlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        println("applying AddGeneratedSourcePlugin..");
        project.android {
            sourceSets {
                //buildTypes.each { buildType
                    productFlavors.each { flavor ->

                        try {
                            // for each flavor: read json file
                            def flavorName = flavor.name

                            def filesJson = new File(getJsonPath(project))
                            if (filesJson.exists()) {
                                def filesPaths = new JsonSlurper().parse(filesJson)
                                filesPaths.each { generatedEntry ->
                                    // if this generated file should be added to current flavor
                                    if (generatedEntry.flavor.equals(flavorName)) {
                                        println "adding generated source {$generatedEntry.path} to flavor ${generatedEntry.flavor}"
                                        // add generated file path to flavor source set
                                        "${flavorName}" {
                                            java.srcDirs += new File(generatedEntry.path)
                                        }
                                    }
                                }
                            }
//
//                            def filesJsonRelease = new File(getJsonPath(project, "release", flavorName))
//                            if (filesJsonRelease.exists()) {
//                                def filesPaths = new JsonSlurper().parse(filesJsonRelease)
//                                filesPaths.each { path ->
//                                    println "adding generated source {$path} to flavor ${flavorName}"
//                                    "${flavorName}" {
//                                        java.srcDirs += new File(path)
//                                    }
//                                }
//                            }
                        } catch (Exception e) {
                            println(e);
                        }
                    }
                //}

            }
        }
    }


//    def performTaskForFlavor(Project project, String buildType, ProductFlavor flavor) {
//        def flavorName = flavor.name
//
//        def filesJson = new File(getJsonPath(project, buildType, flavorName))
//        if (filesJson.exists()) {
//            def filesPaths = new JsonSlurper().parse(filesJson)
//            filesPaths.each { path ->
//                println "adding generated source {$path} to flavor ${flavorName}"
//                "${flavorName}" {
//                    java.srcDirs += new File(path)
//                }
//            }
//        }
//    }

    static String getJsonPath(Project project) {
        return "$project.buildDir/generated/assets/variantsStubsGenerator/meta/generated_files.json";

//        return "$project.buildDir/generated/source/apt/${flavorName}/${buildType}/" +
//                "variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json";

    }

}