//package com.oridev.variantsstubsgenerator.compiler;
//
//import com.oridev.variantsstubsgenerator.Utils;
//import com.oridev.variantsstubsgenerator.compiler.FileEntriesJsonManager;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.Reader;
//import java.io.Writer;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//import java.util.Map;
//
//import javax.annotation.processing.Filer;
//import javax.tools.Diagnostic;
//import javax.tools.FileObject;
//import javax.tools.JavaFileObject;
//import javax.tools.StandardLocation;
//
///**
// * Created by Ori on 03/09/2016.
// */
//public class AddGeneratedFilesTask {
//
//
//    public static void addGeneratedFilesToFiler(Filer filer, String currentFlavor) {
//
////        String currentFlavor = processingEnv.getOptions().get(OPTION_VARIANT);
//
//        if (currentFlavor == null) {
//            return;
//        }
//
//        try {
//            List<String> entries = FileEntriesJsonManager.readFileEntriesJson(filer, currentFlavor);
//
//            if (entries != null) {
//                for (String path : entries) {
//                    Utils.logMessage(Diagnostic.Kind.NOTE, "getting name and package from path: " + path);
//                    String fileName = getFileNameFromFilePath(path);
//                    String filePackage = getFilePackageFromFilePath(path, currentFlavor);
//                    Utils.logMessage(Diagnostic.Kind.NOTE, "Adding generated entry with name [" + fileName +
//                            "], package [" + filePackage + "] to filer");
//                    addSourceFileToFiler(filer, fileName, filePackage);
//                }
//            }
//
////            final String fileName = "Flavor2SpecificFunctionality";
////            final String filePackage = "com.oridev.variantsstubsgenerator.sample";
////            addSourceFileToFiler(fileName, filePackage);
//
//        } catch (Exception e) {
//            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to read info file: " + e);
//        }
//
//    }
//
//    private static String getFileNameFromFilePath(String filePath) {
//        // remove file location
//        String fileNameWithExt = filePath.substring(filePath.lastIndexOf("/") + 1);
//        // remove extension
//        return fileNameWithExt.substring(0, fileNameWithExt.lastIndexOf("."));
//    }
//
//    private static String getFilePackageFromFilePath(String filePath, String flavor) {
//        String fileName = getFileNameFromFilePath(filePath);
//
//        int flavorPathI = filePath.lastIndexOf("/" + flavor + "/");
//        String fileQualifiedPath = filePath.substring(flavorPathI + flavor.length() /* don't forget the `/`s! */ + 2);
//        return fileQualifiedPath
//                // remove buildType (debug / release) and remove file name
//                .substring(fileQualifiedPath.indexOf("/") + 1, fileQualifiedPath.lastIndexOf("/" + fileName))
//                .replaceAll("/", ".");
//    }
//
////    private static void addSourceFileToFiler(String filePath) {
//    private static void addSourceFileToFiler(Filer filer, String fileName, String filePackage) {
//
//        try {
//
//            // add java file with the right name and package to filer
//            String fileQualifiedName = filePackage + "." + fileName;
//            JavaFileObject filerSourceFile = filer.createSourceFile(fileQualifiedName);
//            String filePath = filerSourceFile.toUri().getPath();
//
//            // get file current content
//            Path targetPath = Paths.get(filePath);
//
////            byte[] fileContent = Files.readAllBytes(targetPath);
////            Files.write(targetPath, fileContent);
//
//            String fileContent = new String(Files.readAllBytes(targetPath));
//
//            // rewrite file content
//            Writer writer = filerSourceFile.openWriter();
////            Writer writer = new FileWriter(filePath);
//            writer.write(fileContent);
//            writer.flush();
//            writer.close();
//
//            Utils.logMessage(Diagnostic.Kind.NOTE, "Added file to filer " + filePath);
//
//            //executeTestMethod();
//
//        } catch (Exception e) {
//            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to add file to filer: " + e);
//        }
//    }
//
//}