package com.oridev.variantsstubsgenerator.compiler;

import com.oridev.variantsstubsgenerator.Utils;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.tools.Diagnostic;

/**
 * Created by Ori on 03/09/2016.
 */
public class FileEntriesJsonManager {


    /* Json file path ---------------------- */

    private static final String ENTRIES_JSON_PATH = "/generated/assets/variantsStubsGenerator/meta/generated_files.json";
    private static String getJsonFilePath(String buildDir) {
        return buildDir + ENTRIES_JSON_PATH;
//        return ENTRIES_JSON_PATH + FileEntriesJsonManager.getJsonFileName(flavor);
    }


    /* Json Handling ---------------------- */

    private static JsonAdapter<List<GeneratedFileEntry>> mJsonAdapter = null;
    private static JsonAdapter<List<GeneratedFileEntry>> getJsonAdapter() {
        if (mJsonAdapter == null) {
            Moshi moshi = new Moshi.Builder().build();
            mJsonAdapter = moshi.adapter((Types.newParameterizedType(List.class, GeneratedFileEntry.class)));
        }
        return mJsonAdapter;
    }

    private static List<GeneratedFileEntry> jsonToObject(String json) {
        try {
            return getJsonAdapter().fromJson(json);
        } catch (IOException e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to parse json: " + e);
            return null;
        }
    }
    private static String objectToJson(List<GeneratedFileEntry> entries) {
        return getJsonAdapter().toJson(entries);
    }


    /* File actions handling */

    public static List<GeneratedFileEntry> readJsonFile(String buildDir) {
        final String jsonPath = getJsonFilePath(buildDir);

        File json = new File(jsonPath);
        if (!json.exists()) {
            return null;
        }

        Path path = json.toPath();
        try {
            String jsonContent = new String(Files.readAllBytes(path));
            return jsonToObject(jsonContent);

        } catch (IOException e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to open jsonEntries file: " + e);
            return null;
        }
    }

    public static void writeJsonFile(List<GeneratedFileEntry> entries, String buildDir) {
        final String jsonPath = getJsonFilePath(buildDir);

        Path path = Paths.get(jsonPath);
        Path dirPath = path.getParent();
        try {
            String content = objectToJson(entries);

            Files.createDirectories(dirPath);
            Files.write(path, content.getBytes());

        } catch (IOException e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to write info json file: " + e);
        }

    }




    /* Old code and drafts */

    //    public static final String JSON_PACKAGE = "variantsstubsgenerator.meta";
//    public static String getJsonFileName(String flavor) {
//        String JSON_FILES_NAME = "variant_generated_files";
//        //return JSON_FILES_NAME + ".json";
//        return JSON_FILES_NAME + "_" + flavor + ".json";
//    }

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
//            return jsonToObject(fileContent);
//
//        } catch (IOException e) {
//            Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to read info file: " + e);
//            return null;
//        }

//    public static void updateFileEntriesJson(List<String> generatedFiles) {
//
//        if (generatedFiles == null || generatedFiles.size() == 0) {
//            return;
//        }
//
//        // get all entries json
//        for (String flavor : generatedFiles.keySet()) {
//
//            GeneratedFileEntries entries = generatedFiles.get(flavor);
//
//            // write generated files details to json
//            String totalJson = FileEntriesJsonManager.objectToJson(entries.mEntries);
//
//            // write the json to the right flavor
////            try {
////
////                // add file to filer
////                String jsonFilePathStr = getJsonFilePath(flavor);
////                Utils.logMessage(Diagnostic.Kind.NOTE, "Writing info json to file " + jsonFilePathStr);
////
////                // write json to file
////                Path jsonFilePath = Paths.get(jsonFilePathStr);
////                Files.createFile(jsonFilePath);
////                Files.write(jsonFilePath, totalJson.getBytes());
////
////            } catch (IOException e) {
////                Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to create info file: " + e);
////            }
//
//            try {
//
//                // add file to filer
//                FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT,
//                        FileEntriesJsonManager.JSON_PACKAGE, FileEntriesJsonManager.getJsonFileName(flavor));
//
//                Utils.logMessage(Diagnostic.Kind.NOTE, "Writing info json to " + fileObj.getName());
//
//                // write json to file
//                Writer writer = fileObj.openWriter();
//                writer.write(totalJson);
//                writer.flush();
//                writer.close();
//
//                String originalPathStr = fileObj.getName();
//                String targetPathStr = originalPathStr.replace(flavorFrom, flavor);
//                Path targetPath = Paths.get(targetPathStr);
//
//                Utils.logMessage(Diagnostic.Kind.NOTE, "Moving info file to the right flavor " + targetPath.toString());
//
//                Files.createDirectories(targetPath);
//                Files.move(Paths.get(originalPathStr), targetPath, StandardCopyOption.REPLACE_EXISTING);
//
//            } catch (IOException e) {
//                Utils.logMessage(Diagnostic.Kind.WARNING, "Failed to create info file: " + e);
//            }
//
//        }
//
//    }


}
