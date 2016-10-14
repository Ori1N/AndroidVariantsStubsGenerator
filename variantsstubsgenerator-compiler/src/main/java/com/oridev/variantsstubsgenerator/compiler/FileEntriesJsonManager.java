package com.oridev.variantsstubsgenerator.compiler;

import com.oridev.variantsstubsgenerator.Utils;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Created by Ori on 03/09/2016.
 */
public class FileEntriesJsonManager {

    public static final String JSON_PACKAGE = "variantsstubsgenerator.meta";
    public static String getJsonFileName(String flavor) {
        String JSON_FILES_NAME = "variant_generated_files";
        //return JSON_FILES_NAME + ".json";
        return JSON_FILES_NAME + "_" + flavor + ".json";
    }

    private static JsonAdapter<List<String>> mJsonAdapter = null;
    private static JsonAdapter<List<String>> getJsonAdapter() {
        if (mJsonAdapter == null) {
            Moshi moshi = new Moshi.Builder().build();
            mJsonAdapter = moshi.adapter((Types.newParameterizedType(List.class, String.class)));
        }
        return mJsonAdapter;
    }

    private static String toJson(List<String> entries) {
        return getJsonAdapter().toJson(entries);
    }
    private static List<String> fromJson(String json) {
        try {
            return getJsonAdapter().fromJson(json);
        } catch (IOException e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to parse json: " + e);
            return null;
        }
    }


    private static final String ENTRIES_JSON_PATH = "./build/generated/res/variantsStubsGenerator/";
    public static String getJsonFilePath(String flavor) {
        return ENTRIES_JSON_PATH + FileEntriesJsonManager.getJsonFileName(flavor);
    }

//    public static List<String> readFileEntriesJson(Filer filer, String flavor) {
//
//        // read json content
//        try {
//            FileObject fileObj = filer.getResource(StandardLocation.SOURCE_OUTPUT,
//                    FileEntriesJsonManager.JSON_PACKAGE, FileEntriesJsonManager.getJsonFileName(flavor));
//            Utils.logMessage(Diagnostic.Kind.NOTE, "info file got from filer: " + fileObj.getName());
//
//            Reader reader = fileObj.openReader(false);
//            BufferedReader bufferedReader = new BufferedReader(reader);
//
//            StringBuilder builder = new StringBuilder();
//            String aux = "";
//
//            while ((aux = bufferedReader.readLine()) != null) {
//                builder.append(aux);
//            }
//            bufferedReader.close();
//            reader.close();
//
//            String fileContent = builder.toString();
//            return fromJson(fileContent);
//
//        } catch (IOException e) {
//            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to read info file: " + e);
//            return null;
//        }
//
////        File json = new File(getJsonFilePath(flavor));
////        if (!json.exists()) {
////            return null;
////        }
////
////        Path jsonPath = Paths.get(getJsonFilePath(flavor));
////        try {
////            String jsonContent = new String(Files.readAllBytes(jsonPath));
////            return fromJson(jsonContent);
////
////        } catch (IOException e) {
////            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to open jsonEntries file: " + e);
////            return null;
////        }
//    }

    public static void writeFileEntriesJson(Filer filer, Map<String, GeneratedFileEntries> generatedFiles) {

        if (generatedFiles == null || generatedFiles.size() == 0) {
            return;
        }

        // get all entries json
        for (String flavor : generatedFiles.keySet()) {

            GeneratedFileEntries entries = generatedFiles.get(flavor);

            String flavorFrom = entries.mFlavorFrom;

            // write generated files details to json
            String totalJson = FileEntriesJsonManager.toJson(entries.mEntries);

            // write the json to the right flavor
//            try {
//
//                // add file to filer
//                String jsonFilePathStr = getJsonFilePath(flavor);
//                Utils.logMessage(Diagnostic.Kind.NOTE, "Writing info json to file " + jsonFilePathStr);
//
//                // write json to file
//                Path jsonFilePath = Paths.get(jsonFilePathStr);
//                Files.createFile(jsonFilePath);
//                Files.write(jsonFilePath, totalJson.getBytes());
//
//            } catch (IOException e) {
//                Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to create info file: " + e);
//            }

            try {

                // add file to filer
                FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT,
                        FileEntriesJsonManager.JSON_PACKAGE, FileEntriesJsonManager.getJsonFileName(flavor));

                Utils.logMessage(Diagnostic.Kind.NOTE, "Writing info json to file..");

                // write json to file
                Writer writer = fileObj.openWriter();
                writer.write(totalJson);
                writer.flush();
                writer.close();

                String originalPathStr = fileObj.getName();
                String targetPathStr = originalPathStr.replace(flavorFrom, flavor);
                Path targetPath = Paths.get(targetPathStr);

                Utils.logMessage(Diagnostic.Kind.NOTE, "Moving info file to the right flavor");

                Files.createDirectories(targetPath);
                Files.move(Paths.get(originalPathStr), targetPath, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to create info file: " + e);
            }

        }


//        // get all entries json
//        for (String flavor : generatedFiles.keySet()) {
//
//            GeneratedFileEntries entries = generatedFiles.get(flavor);
//
//            String flavorFrom = entries.mFlavorFrom;
//
//
//            // write generated files details to json
//            String totalJson = FileEntriesJsonManager.toJson(entries.mEntries);
//
//            // write the json to the right flavor
//        }


    }


}
