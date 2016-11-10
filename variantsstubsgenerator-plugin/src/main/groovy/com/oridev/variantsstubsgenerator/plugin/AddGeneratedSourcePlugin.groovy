package com.oridev.variantsstubsgenerator.plugin

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project


public class AddGeneratedSourcePlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        //addSourceSetsForFlavors(project);
        generateFilesBeforeCompile(project);
    }


    /* add existing generated source directories to their flavors' sourceSets */
    /* (This is for AndroidStudio support, not required for build success */

    void addSourceSetsForFlavors(Project project) {

        // handle project buildTypes
        project.android.buildTypes.all { buildType ->
            addSourceSetsForFlavor(project, buildType)
        }

        // handle project flavors
        project.android.productFlavors.all { flavor ->
            addSourceSetsForFlavor(project, flavor);
        }
    }

    void addSourceSetsForFlavor(def project, def flavor) {
        String sourceSetPath = PathsUtils.getTargetSourceSetPath(project.buildDir.getPath(), flavor.name);
        if (new File(sourceSetPath).isDirectory()) {
            Utils.logMessage("Adding existing directory [$sourceSetPath] to sourceSet [$flavor.name]");
            addPathToSourceSet(project, flavor.name, sourceSetPath + "/");
        }
    }

    void addPathToSourceSet(Project project, String flavor, String path) {
        project.android {
            sourceSets {
                "${flavor}" {
                    java.srcDirs += path
                }
            }
        }
    }


    /* scan sourceSets and generate sources */

    void generateFilesBeforeCompile(def project) {
        project.android.applicationVariants.all { variant ->
            variant.javaCompile.doFirst {
                generateFilesForVariant(project, variant);
            }
        }
    }

    void generateFilesForVariant(def project, def variant) {
        // get the project source dir
        def rootSourceDir = new File(project.projectDir, "src");
        for (sourceDir in rootSourceDir.listFiles()) {

            if (shouldScanSourceDir(variant, sourceDir)) {
                // scan sourceSets that may contain generating code
                Utils.logMessage("Scanning sourceDir: [$sourceDir]")
                sourceDir.eachFileRecurse { file ->

                    if (shouldGenerateSourceFromSourceFile(file)) {
                        generateStubForCurrentVariant(project, variant, file);
                    }
                }
            }
        }
    }

    void generateStubForCurrentVariant(Project project, def variant, File sourceFile) {

        JavaStubGenerator generator = new JavaStubGenerator(sourceFile);
        String annotationFlavorTo = generator.getAnnotationFlavorTo();

        if (variant.name.toLowerCase().contains(annotationFlavorTo) ||
                variant.buildType.name.equals(annotationFlavorTo)) {
            String path = generator.generateStubSourceFile();

            Utils.logMessage("Adding generated source [$path]...");

            // add generated file to sourcePath
            variant.javaCompile.source path;
        }
    }


    boolean shouldScanSourceDir(def variant, def sourceDir) {

        final String MAIN = "main";
        final String TEST = "test";

        final String sourceDirName = sourceDir.name.toLowerCase();
        boolean scanSourceSet = true;

        // don't scan main and test sourceSets
        if (sourceDirName.equals(MAIN) || sourceDirName.contains(TEST)) {
            scanSourceSet = false;

            // don't scan current variant's buildType
        } else if (sourceDirName.contains(variant.buildType.name)) {
            scanSourceSet = false;
        } else {
            for (currentFlavor in variant.productFlavors) {
                // don't scan current variant's flavors
                if (sourceDirName.contains(currentFlavor.name)) {
                    scanSourceSet = false;
                }
            }
        }

        return scanSourceSet;
    }

    boolean shouldGenerateSourceFromSourceFile(File file) {

        // get file extension
        int extensionIndex = file.name.lastIndexOf('.');
        String fileExtension = (extensionIndex == -1) ?  null : file.name.substring(extensionIndex + 1);

        if ("java".equals(fileExtension)) {
            if (doesFileContainAnnotation(file)) {
                return true;
            }
        }

        return false;
    }

    private static final String ANNOTATION_STR = "@RequiresVariantStub";

    boolean doesFileContainAnnotation(File file) {
        def fileLines = file.readLines();
        for (line in fileLines) {
            if (line.contains(ANNOTATION_STR)) {
                return true;
            }
            if (line.contains("class ")) {
                return false;
            }
        }
        return false;
    }


    /* Drafts / Old / Unused -------------------------------------------------------------- */

    /* Add source files (from json) to gradle sourceSet before compile (working!!) */
//    void addPathsForVariants(Project project) {
//
//        project.android.applicationVariants.all { variant ->
//
//            variant.javaCompile.doFirst {
//
//                def filesJson = new File(getJsonPath(project))
//                if (filesJson.exists()) {
//                    def filesEntries = new JsonSlurper().parse(filesJson);
//
//                    def variantPaths = filesEntries
//                            .findAll { variant.name.toLowerCase().contains(it.flavor) }
//                            .collect { it.path };
//
//                    if (!variantPaths.isEmpty()) {
//
//                        Utils.logMessage("applying AddGeneratedSourcePlugin for variant $variant.name with $variantPaths.size entries)..");
//                        variantPaths.each { path ->
//                            Utils.logMessage("AddGeneratedSourcePlugin variant [$variant.name] adding path [$path]", true);
//                            variant.javaCompile.source path;
//                        }
//                    }
//                }
//
//            }
//
//        }
//    }

    /* Add source files (from json) to gradle sourceSet (working!) */
//    void addSourceFromJson(Project project) {
//
//        def filesJson = new File(getJsonPath(project))
//        if (filesJson.exists()) {
//
//            def filesPaths = new JsonSlurper().parse(filesJson)
//            logMessage("applying AddGeneratedSourcePlugin, (info json containing $filesPaths.size entries)..", true);
//            filesPaths.each { generatedEntry ->
//                def entryTarget = generatedEntry.flavor;
//
//                try {
//                    if (["debug", "release"].contains(entryTarget)) {
//                        // if flavorTo is a buildType
//                        logMessage("handling entry with buildType target [$entryTarget], path [$generatedEntry.path]");
//
//                        project.android.buildTypes.all { buildType ->
//                            // if this generated file should be added to current buildType
//                            if (entryTarget.equals(buildType.name)) {
//                                addSourceFileToSourceSet(project, entryTarget, generatedEntry.path)
//                            }
//                        }
//
//                    } else {
//                        // if flavorTo is a flavor
//                        logMessage("handling entry with flavor target [$entryTarget], path [$generatedEntry.path]");
//
//                        project.android.productFlavors.all { flavor ->
//                            // if this generated file should be added to current flavor
//                            if (entryTarget.equals(flavor.name)) {
//                                addSourceFileToSourceSet(project, entryTarget, generatedEntry.path);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    println(e);
//                }
//            }
//        }
//
//
////        project.android {
////            sourceSets {
////                //buildTypes.each { buildType
////                    productFlavors.each { flavor ->
////
////                        try {
////                            // for each flavor: read json file
////                            def flavorName = flavor.name
////
////                            def filesJson = new File(getJsonPath(project))
////                            if (filesJson.exists()) {
////                                def filesPaths = new JsonSlurper().parse(filesJson)
////                                filesPaths.each { generatedEntry ->
////                                    // if this generated file should be added to current flavor
////                                    if (generatedEntry.flavor.equals(flavorName)) {
////                                        println "adding generated source {$generatedEntry.path} to flavor ${generatedEntry.flavor}"
////                                        // add generated file path to flavor source set
////                                        "${flavorName}" {
////                                            java.srcDirs += new File(generatedEntry.path)
////                                        }
////                                    }
////                                }
////                            }
//////
//////                            def filesJsonRelease = new File(getJsonPath(project, "release", flavorName))
//////                            if (filesJsonRelease.exists()) {
//////                                def filesPaths = new JsonSlurper().parse(filesJsonRelease)
//////                                filesPaths.each { path ->
//////                                    println "adding generated source {$path} to flavor ${flavorName}"
//////                                    "${flavorName}" {
//////                                        java.srcDirs += new File(path)
//////                                    }
//////                                }
//////                            }
////                        } catch (Exception e) {
////                            println(e);
////                        }
////                    }
////                //}
////
////            }
////        }
//    }

//    static String getJsonPath(Project project) {
//        return "$project.buildDir/generated/assets/variantsStubsGenerator/meta/generated_files.json";
//
////        return "$project.buildDir/generated/source/apt/${flavorName}/${buildType}/" +
////                "variantsstubsgenerator/meta/variant_generated_files_${flavorName}.json";
//
//    }

}
