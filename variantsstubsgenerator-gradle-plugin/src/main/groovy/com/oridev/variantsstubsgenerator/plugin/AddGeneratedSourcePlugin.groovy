package com.oridev.variantsstubsgenerator.plugin;

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils
import org.gradle.api.Plugin
import org.gradle.api.Project


public class AddGeneratedSourcePlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {

        project.android {
            sourceSets {

                productFlavors.each { flavor ->

                    // for each flavor: read json file
                    def flavorName = flavor.name

                    def filesJson = new File("$project.buildDir/generated/source/apt/${flavorName}/debug/variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json")
                    if (filesJson.exists()) {
                        def filesPaths = new JsonSlurper().parse(filesJson)
                        filesPaths.each { path ->
                            println "adding generated source {$path} to flavor ${flavorName}"
                            "${flavorName}" {
                                java.srcDirs += new File(path)
                            }
                        }
                    }
                }

            }
        }
    }
}
