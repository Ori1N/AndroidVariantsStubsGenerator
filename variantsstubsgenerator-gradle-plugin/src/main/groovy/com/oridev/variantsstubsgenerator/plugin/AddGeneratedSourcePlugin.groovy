package com.oridev.variantsstubsgenerator.plugin;

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.model.ProductFlavor
import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils
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

                            def filesJsonDebug = new File(getJsonPath(project, "debug", flavorName))
                            if (filesJsonDebug.exists()) {
                                def filesPaths = new JsonSlurper().parse(filesJsonDebug)
                                filesPaths.each { path ->
                                    println "adding generated source {$path} to flavor ${flavorName}"
                                    "${flavorName}" {
                                        java.srcDirs += new File(path)
                                    }
                                }
                            }

                            def filesJsonRelease = new File(getJsonPath(project, "release", flavorName))
                            if (filesJsonRelease.exists()) {
                                def filesPaths = new JsonSlurper().parse(filesJsonRelease)
                                filesPaths.each { path ->
                                    println "adding generated source {$path} to flavor ${flavorName}"
                                    "${flavorName}" {
                                        java.srcDirs += new File(path)
                                    }
                                }
                            }
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

    static String getJsonPath(Project project, String buildType, String flavorName) {
        return "$project.buildDir/generated/source/apt/${flavorName}/${buildType}/" +
                "variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json";
    }

}
