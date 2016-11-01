package com.oridev.variantsstubsgenerator.plugin;

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project


public class AddGeneratedSourcePlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        addPathsForVariants(project);
    }

    void addPathsForVariants(Project project) {

        project.android.applicationVariants.all { variant ->

            variant.javaCompile.doFirst {

                def filesJson = new File(getJsonPath(project))
                if (filesJson.exists()) {
                    def filesEntries = new JsonSlurper().parse(filesJson);

                    def variantPaths = filesEntries
                            .findAll { variant.name.toLowerCase().contains(it.flavor) }
                            .collect { it.path };

                    if (!variantPaths.isEmpty()) {

                        logMessage("applying AddGeneratedSourcePlugin for variant $variant.name with $variantPaths.size entries)..");
                        variantPaths.each { path ->
                            logMessage("AddGeneratedSourcePlugin variant [$variant.name] adding path [$path]", true);
                            variant.javaCompile.source path;
                        }
                    }
                }

            }

        }
    }


    /* working!! */
//    void addTargetsForVariants(Project project) {
//
//        def filesJson = new File(getJsonPath(project))
//        if (filesJson.exists()) {
//            def filesEntries = new JsonSlurper().parse(filesJson);
//
//            logMessage("applying AddGeneratedSourcePlugin, (info json containing $filesEntries.size entries)..", true);
//
//            project.android.applicationVariants.all { variant ->
//
//                addSourceFilesForVariant(project, variant, filesEntries);
//                //addSourceDirectoriesForVariant(project, variant, filesEntries);
//            }
//        }
//    }

//    void addSourceFilesForVariant(def variant) {
//
//        def variantPaths = filesEntries
//                .findAll { variant.name.toLowerCase().contains(it.flavor) }
//                .collect { it.path };
//
//        variantPaths.each { path ->
//
//            def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
//            javaCompile.doFirst {
//                logMessage("adding path [$path] to variant [$variant.name]", true);
//                javaCompile.source path;
//            }
//
//        }
//
//    }

//    void addSourceFilesForVariant(def variant) {
//
//        def variantPaths = filesEntries
//                .findAll { variant.name.toLowerCase().contains(it.flavor) }
//                .collect { it.path };
//
//        variantPaths.each { path ->
//
//            def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
//            javaCompile.doFirst {
//                logMessage("adding path [$path] to variant [$variant.name]", true);
//                javaCompile.source path;
//            }
//        }
//    }

    /* not working, but starting to get there... */
//    void addSourceDirectoriesForVariant(def project, def variant, def filesEntries) {
//
//        def variantTargets = filesEntries
//        // extract entries targets
//                .collect { it.flavor }
//        // remove duplicates
//                .unique()
//        // filter only targets relevant for this variant
//                .findAll { variant.name.toLowerCase().contains(it) };
//
//        variantTargets.each { target ->
//
//            def sourcePath = new File(getSourcePath(project, target, variant.buildType.name));
//
//            def javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
//            javaCompile.doFirst {
//                logMessage("adding path [$sourcePath] to variant [$variant.name]", true);
//
//                //javaCompile.source sourcePath;
//
//                variant.addJavaSourceFoldersToModel(sourcePath);
//                javaCompile.options.compilerArgs += [
//                        '-sourcepath', sourcePath,
//                        //'-processorpath', configurations.apt.getAsPath()
//                ]
//
//                logMessage("*** doFirst: javaCompile.compilerArgs: " + javaCompile.options.compilerArgs);
////                        javaCompile.source.each {
////                            logMessage("*** doFirst: javaCompile.source: " + it);
////                        }
//
//            }
//        }
//    }

//    static String getSourcePath(Project project, String target, String buildType) {
//        def sourcePathStr = "${project.buildDir}/generated/source/apt/";
//        if (["debug", "release"].contains(target)) {
//            sourcePathStr += "main/${target}"
//        } else {
//            sourcePathStr += "${target}/${buildType}"
//        }
//        return sourcePathStr;
//    }

    /* Working code!! */
    void addSourceFromJson(Project project) {

        def filesJson = new File(getJsonPath(project))
        if (filesJson.exists()) {

            def filesPaths = new JsonSlurper().parse(filesJson)
            logMessage("applying AddGeneratedSourcePlugin, (info json containing $filesPaths.size entries)..", true);
            filesPaths.each { generatedEntry ->
                def entryTarget = generatedEntry.flavor;

                try {
                    if (["debug", "release"].contains(entryTarget)) {
                        // if flavorTo is a buildType
                        logMessage("handling entry with buildType target [$entryTarget], path [$generatedEntry.path]");

                        project.android.buildTypes.all { buildType ->
                            // if this generated file should be added to current buildType
                            if (entryTarget.equals(buildType.name)) {
                                addSourceFileToSourceSet(project, entryTarget, generatedEntry.path)
                            }
                        }

                    } else {
                        // if flavorTo is a flavor
                        logMessage("handling entry with flavor target [$entryTarget], path [$generatedEntry.path]");

                        project.android.productFlavors.all { flavor ->
                            // if this generated file should be added to current flavor
                            if (entryTarget.equals(flavor.name)) {
                                addSourceFileToSourceSet(project, entryTarget, generatedEntry.path);
                            }
                        }
                    }
                } catch (Exception e) {
                    println(e);
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

    void addSourceFileToSourceSet(Project project, String target, String path) {
        logMessage("adding generated source [$path] to target [$target]", true);
        project.android {
            sourceSets {
                "${target}" {
                    java.srcDirs += path
                }
            }
        }
    }

    static String getJsonPath(Project project) {
        return "$project.buildDir/generated/assets/variantsStubsGenerator/meta/generated_files.json";

//        return "$project.buildDir/generated/source/apt/${flavorName}/${buildType}/" +
//                "variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json";

    }


    private static final boolean DEBUG = true;
    static void logMessage(String msg) {
        logMessage(msg, false);
    }
    static void logMessage(String msg, boolean showOnRelease) {
        if (DEBUG || showOnRelease) {
            println(msg);
        }
    }

}
