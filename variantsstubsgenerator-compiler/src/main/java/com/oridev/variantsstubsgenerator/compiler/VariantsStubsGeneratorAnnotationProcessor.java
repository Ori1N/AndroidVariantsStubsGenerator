package com.oridev.variantsstubsgenerator.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.exception.AttemptToUseStubException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;


@AutoService(Processor.class)
public class VariantsStubsGeneratorAnnotationProcessor extends AbstractProcessor {


    private ProcessingEnvironment environment;
    private Elements elementUtils;
    //private Types typesUtils;
    private Filer filer;

    //private boolean mFirstRound = true;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        environment = env;
        elementUtils = env.getElementUtils();
        //typesUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RequiresVariantStub.class.getCanonicalName());
        return types;
    }

    private static final String OPTION_VARIANT = "variantName";
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new LinkedHashSet<>();
        options.add(OPTION_VARIANT);
        return options;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Map<String, GeneratedFilesEntries> generatedFilesByFlavors = new HashMap<>();
        // fixme: temp solution! won't work for multiple flavored builds...
        String auxFlavorFrom = null;

        // generate stubs for annotated classes
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class);
        for (Element element : elements) {

            if (!SuperficialValidation.validateElement(element)) {
                continue;
            }

            JavaFile file = generateStubJavaFile((TypeElement) element);

            RequiresVariantStub annotation = element.getAnnotation(RequiresVariantStub.class);
            final String flavorFrom = annotation.flavorFrom();
            final String flavorTo = annotation.flavorTo();
            auxFlavorFrom = flavorFrom;

            try {
                if (!doesClassExist(file, flavorFrom, flavorTo)) {
                    writeSourceFile(file, flavorFrom, flavorTo);

                    logMessage(Diagnostic.Kind.NOTE, "Processing done.");

                    // add generated file to map
                    if (generatedFilesByFlavors.get(flavorTo) == null) {
                        generatedFilesByFlavors.put(flavorTo, new GeneratedFilesEntries(flavorFrom, new FileEntry(file.typeSpec.name, file.packageName)));
                    } else {
                        generatedFilesByFlavors.get(flavorTo).addEntry(new FileEntry(file.typeSpec.name, file.packageName));
                    }

                }
            } catch (Throwable e) {
                logMessage(Diagnostic.Kind.NOTE, "Processing failed: " + e);
            }
        }

        // write generated files to their target flavors
        if (!generatedFilesByFlavors.isEmpty()) {
            writeGeneratedFilesToJson(generatedFilesByFlavors);
        }

        // add generated files to this flavor
        addGeneratedFilesToFiler(auxFlavorFrom);

        return true;
    }


    // Stubs Generation ----------------

    private JavaFile generateStubJavaFile(TypeElement element) {
        logMessage(Diagnostic.Kind.NOTE, "Processing annotated class " + element.getSimpleName());

        // get annotation parameters
        RequiresVariantStub annotation = element.getAnnotation(RequiresVariantStub.class);
        final boolean throwException = annotation.throwException();

        // start building the java stubs file
        try {
            // Create the duplicate class
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(element.getSimpleName().toString());
            Modifier[] classModifiers = element.getModifiers().toArray(new Modifier[element.getModifiers().size()]);
            classBuilder.addModifiers(classModifiers);

            // for each public element inside the annotated class
            for (Element classInnerElement : element.getEnclosedElements()) {
                if (classInnerElement.getModifiers().contains(Modifier.PUBLIC)) {

                    // create stubs for methods
                    if (classInnerElement.getKind() == ElementKind.METHOD) {
                        ExecutableElement method = (ExecutableElement) classInnerElement;

                        TypeMirror methodReturnType = method.getReturnType();

                        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                                .addModifiers(method.getModifiers())
                                .returns(TypeName.get(methodReturnType));

                        // add parameters
                        for (VariableElement methodParam : method.getParameters()) {
                            TypeName methodParamType = TypeName.get(methodParam.asType());
                            String methodParamName = methodParam.getSimpleName().toString();
                            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(methodParamType, methodParamName);
                            methodBuilder.addParameter(paramBuilder.build());
                        }

                        if (throwException) {
                            // add throw statement
                            methodBuilder.addStatement("throw new $T($S, $S)", AttemptToUseStubException.class,
                                    annotation.flavorFrom(), annotation.flavorTo());

                        } else if (methodReturnType.getKind() != TypeKind.VOID) {
                            // add return statement if required
                            methodBuilder.addStatement("return " + getDefaultReturnTypeForTypeMirror(methodReturnType));
                        }

                        classBuilder.addMethod(methodBuilder.build());
                    }

                    // todo: handle non methods elements! */
                }
            }

            // write the new file
            String classPackage = element.getQualifiedName().toString();
            classPackage = classPackage.substring(0, classPackage.lastIndexOf('.')); // todo: handle inner classes!

            return JavaFile.builder(classPackage, classBuilder.build())
                    .addFileComment("Generated code from VariantsStubsGenerator library. Do not modify!")
                    .build();

        } catch (Exception e) {
            logMessage(Diagnostic.Kind.WARNING, "generateStubJavaFile failure: " + e);
            return null;
        }
    }

    private boolean doesClassExist(JavaFile file, String flavorFrom, String flavorTo) {
//        try {
//            FileObject fileObj = filer.getResource(StandardLocation.SOURCE_OUTPUT, file.packageName, file.typeSpec.name);
//            //return fileObj != null;
//        } catch (IOException e) {
//            logMessage(Diagnostic.Kind.WARNING, "doesClassExist failure: " + e);
//        }
        return false;
        // todo: implement!
    }

    private String writeSourceFile(JavaFile javaFile, String flavorFrom, String flavorTo) throws IOException {
        final String EXT = "_d_";

        // create draft file (copy of the file we want to create but in flavorFrom)
        String fileName = javaFile.packageName.isEmpty()
                ? javaFile.typeSpec.name
                : javaFile.packageName + "." + EXT + javaFile.typeSpec.name;
        List<Element> originatingElements = javaFile.typeSpec.originatingElements;
        JavaFileObject filerSourceFile = filer.createSourceFile(fileName,
                originatingElements.toArray(new Element[originatingElements.size()]));
        try (Writer writer = filerSourceFile.openWriter()) {
            javaFile.writeTo(writer);
        } catch (Exception e) {
            logMessage(Diagnostic.Kind.WARNING, "Failed to write draft file: " + e);
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) { }
            throw e;
        }

        // get the desired target path
        File generatedFile = new File(filerSourceFile.toUri());
        String targetPathStr = generatedFile.getPath()
                // set path to flavorTo
                .replace(flavorFrom, flavorTo)
                // remove extension from file name
                .replace("/" + EXT + javaFile.typeSpec.name, "/" + javaFile.typeSpec.name);
        File targetFile = new File(targetPathStr);
        File targetDirectory = targetFile.getParentFile();

        // copy the draft file to the real location (flavorTo)
        logMessage(Diagnostic.Kind.NOTE, "copying generated file to " + targetDirectory);
        if (!targetDirectory.exists()) {
            Files.createDirectories(targetDirectory.toPath());
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(targetFile);
            javaFile.writeTo(fileWriter);
            //Files.copy(generatedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING /*, StandardCopyOption.ATOMIC_MOVE */);
        } catch (Exception e) {
            logMessage(Diagnostic.Kind.WARNING, "Error copying file: " + e);
        } finally {
            if (fileWriter != null) {
                try { fileWriter.close(); }
                catch (IOException e) { }
            }
        }

        // change draft file in flavorFrom's class name to temp name
        Path path = Paths.get(filerSourceFile.toUri());
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("class " + javaFile.typeSpec.name, "class " + EXT + javaFile.typeSpec.name);
        Files.write(path, content.getBytes(charset));


        /* close filer (no compilation error but now gradle doesn't consider generated files */
//        if (filer instanceof Closeable) {
//            try {
//                ((Closeable) filer).close();
//            } catch (Exception e) {
//                logMessage(Diagnostic.Kind.WARNING, "Failed to close filer: " + e);
//            }
//        }

        return targetPathStr;

        /* delete file after moving (didn't help) */
//        try {
//            //Runtime.getRuntime().exec("rm ./.gradle/2.14.1/taskArtifacts/cache.properties.lock");
//
//            boolean deleted = filerSourceFile.delete();
//            //filer.getResource(StandardLocation.SOURCE_OUTPUT, javaFile.packageName, javaFile.typeSpec.name).delete();
//            if (!deleted /*|| !filerSourceFile.delete() */) {
//                logMessage(Diagnostic.Kind.NOTE, "Error deleting file");
//
//            }
//        } catch (Exception e) {
//            logMessage(Diagnostic.Kind.NOTE, "Failed to delete file: " + e);
//        }

    }


    private void writeGeneratedFilesToJson(Map<String, GeneratedFilesEntries> generatedFiles) {

        if (generatedFiles == null || generatedFiles.size() == 0) {
            return;
        }

        // get all entries json
        for (String flavor : generatedFiles.keySet()) {

            GeneratedFilesEntries entries = generatedFiles.get(flavor);

            String flavorFrom = entries.mFlavorFrom;

            // write generated files details to json
            String totalJson = getJsonAdapter().toJson(entries.mEntries);

            // write the json to the right flavor
            try {

                // add file to filer
                FileObject fileObj = filer.createResource(StandardLocation.SOURCE_OUTPUT, JSON_PACKAGE, getJsonFileName(flavor));

                logMessage(Diagnostic.Kind.NOTE, "Writing info json to file..");

                // write json to file
                Writer writer = fileObj.openWriter();
                writer.write(totalJson);
                writer.flush();
                writer.close();

                String originalPathStr = fileObj.getName();
                String targetPathStr = originalPathStr.replace(flavorFrom, flavor);
                Path targetPath = Paths.get(targetPathStr);

                logMessage(Diagnostic.Kind.NOTE, "Moving info file to the right flavor");

                Files.createDirectories(targetPath);
                Files.move(Paths.get(originalPathStr), targetPath, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                logMessage(Diagnostic.Kind.WARNING, "Failed to create info file: " + e);
            }
        }
    }

    private void addGeneratedFilesToFiler(String currentFlavor) {

//        String currentFlavor = processingEnv.getOptions().get(OPTION_VARIANT);

        if (currentFlavor == null) {
            return;
        }

        try {
            // read json content
            FileObject fileObj = filer.getResource(StandardLocation.SOURCE_OUTPUT, JSON_PACKAGE, getJsonFileName(currentFlavor));
            logMessage(Diagnostic.Kind.NOTE, "info file got from filer: " + fileObj.getName());

            Reader reader = fileObj.openReader(false);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder builder = new StringBuilder();
            String aux = "";

            while ((aux = bufferedReader.readLine()) != null) {
                builder.append(aux);
            }
            bufferedReader.close();
            reader.close();

            String fileContent = builder.toString();
            List<FileEntry> entries = getJsonAdapter().fromJson(fileContent);

            if (entries != null) {
                for (FileEntry entry : entries) {
                    logMessage(Diagnostic.Kind.NOTE, "Adding generated entry " + entry.mName + " to filer");
                    addSourceFilesToFiler(entry.mName, entry.mPackage);
                }
            }

//            final String fileName = "Flavor2SpecificFunctionality";
//            final String filePackage = "com.oridev.variantsstubsgenerator.sample";
//            addSourceFilesToFiler(fileName, filePackage);

        } catch (FileNotFoundException e) {
            logMessage(Diagnostic.Kind.NOTE, "info file not found: " + e);
        } catch (Exception e) {
            logMessage(Diagnostic.Kind.WARNING, "Failed to read info file: " + e);
        }

    }



    // Adding Generated Files To Filer -------------------------

    private static final String JSON_PACKAGE = "variantsstubsgenerator.meta";
    private String getJsonFileName(String flavor) {
        String JSON_FILES_NAME = "variant_generated_files";
        //return JSON_FILES_NAME + ".json";
        return JSON_FILES_NAME + "_" + flavor + ".json";
    }


    /* Json Handling */

    private JsonAdapter<List<FileEntry>> mJsonAdapter = null;
    private JsonAdapter<List<FileEntry>> getJsonAdapter() {
        if (mJsonAdapter == null) {
            Moshi moshi = new Moshi.Builder().build();
            mJsonAdapter = moshi.adapter((Types.newParameterizedType(List.class, FileEntry.class)));
        }
        return mJsonAdapter;
    }

    static private class FileEntry {
        public final String mName;
        public final String  mPackage;

        public FileEntry(String name, String _package) {
            mName = name;
            mPackage = _package;
        }
    }

    static private class GeneratedFilesEntries {
        public final List<FileEntry> mEntries;
        public final String mFlavorFrom;
        public GeneratedFilesEntries(String flavorFrom, FileEntry entry) {
            mFlavorFrom = flavorFrom;
            mEntries = new ArrayList<>();
            mEntries.add(entry);
        }

        public void addEntry(FileEntry entry) {
            mEntries.add(entry);
        }
    }


    /* Utilities --------------------------------------- */

    private void addSourceFilesToFiler(String fileName, String filePackage) {

        try {
            // add java file with the right name and package to filer
            String fileQualifiedName = filePackage + "." + fileName;
            JavaFileObject filerSourceFile = filer.createSourceFile(fileQualifiedName);

            // get file current content
            Path targetPath = Paths.get(filerSourceFile.toUri());
            String fileContent = new String(Files.readAllBytes(targetPath));

            // rewrite file content
            Writer writer = filerSourceFile.openWriter();
            writer.write(fileContent);
            writer.flush();
            writer.close();

            logMessage(Diagnostic.Kind.NOTE, "Added file to filer " + filerSourceFile.getName());

            //executeTestMethod();

        } catch (Exception e) {
            logMessage(Diagnostic.Kind.WARNING, "Failed to add file to filer: " + e);
        }
    }

    private Object getDefaultReturnTypeForTypeMirror(TypeMirror type) {
        TypeName typeName = TypeName.get(type);

        // if not primitive - the default should be null
        if (!typeName.isPrimitive()) {
            return null;
        }

        // if boolean
        if (typeName == TypeName.BOOLEAN) {
            return false;
        }
        // if other primitive type - this will do
        return 0;
    }

    private void logMessage(Diagnostic.Kind kind, String message) {

        try {
            if (message != null) {
                processingEnv.getMessager().printMessage(kind, message);
            } else {
                processingEnv.getMessager().printMessage(kind, "null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* Reflection */
//    private static final String M_DISPLAY_STATE = "displayState";
//    private static final String M_NEW_ROUND = "newRound";

//    private void executeFilerMethod(String methodName) {
//
//        Class<? extends Filer> type = filer.getClass();
//
//        Exception error = null;
//        Method method = null;
//        try {
//            method = type.getMethod(methodName);
//
//            try {
//                method.invoke(type);
//            } catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
//                error = e;
//            }
//
//        } catch (NoSuchMethodException e) {
//            error = e;
//        }
//
//        if (error != null) {
//            logMessage(Diagnostic.Kind.WARNING, "Invoking method failed: " + error);
//        }
//    }

    /* V2 - use of android apt example, working! :) */
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        // collect all the annotated classes
//        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Component.class);
//        Map<String, String> componentClasses = new HashMap<>();
//        for (Element element : elements) {
//            if (!SuperficialValidation.validateElement(element)) {
//                continue;
//            }
//            // now get the annotation value an the class name
//            String name = elementUtils.getPackageOf(element).getQualifiedName().toString() + "." + element.getSimpleName().toString();
//            final String[] values = element.getAnnotation(Component.class).value();
//            for (String value : values) {
//                componentClasses.put(value, name);
//            }
//        }
//
//        // ignore empty writes
//        if (componentClasses.isEmpty()) {
//            return false;
//        }
//
//
//        // Create the registry class
//        TypeSpec.Builder result = TypeSpec.classBuilder("ComponentRegistry");
//        result.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//
//        // Map<String, Class>
//        ClassName map = ClassName.get("java.util", "Map");
//        ClassName hashMap = ClassName.get("java.util", "HashMap");
//        final ParameterizedTypeName mapOfClasses = ParameterizedTypeName.get(map, TypeName.get(String.class), TypeName.get(Class.class));
//
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getRegisteredClasses")
//                .returns(mapOfClasses)
//                .addStatement("$T result = new $T<>()", mapOfClasses, hashMap)
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
//
//        // add all the statements for the annotated classes
//        for (String name : componentClasses.keySet()) {
//            methodBuilder = methodBuilder.addStatement("result.put(\"$N\", $N.class)", name, componentClasses.get(name));
//        }
//        methodBuilder = methodBuilder.addStatement("return result");
//        result.addMethod(methodBuilder.build());
//
//        // write the new file
//        try {
//            JavaFile.builder("de.manuelohlendorf.androidaptexample", result.build())
//                    .addFileComment("Generated code from annotation compiler. Do not modify!")
//                    .build().writeTo(filer);
//        } catch (IOException e) {
//            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Unable to write register %s",
//                    e.getMessage()), null);
//        }
//        return true;
//    }

    /* v1 (my first try, unsuccessful) */
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//
//        for (Element element : roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class)) {
//            TypeElement classElement = (TypeElement) element;
//            generateStubFile(classElement);
//        }
//        return false;
//    }
//
//    private void generateStubFile(TypeElement classElement) {
//
//        // create the class object
//        TypeSpec.Builder classSpecBuilder = TypeSpec.classBuilder(classElement.getSimpleName().toString());
//
//        for (Element element : classElement.getEnclosedElements()) {
//            //Method method = (Method) element;
//
//            if (element.getModifiers().contains(Modifier.PUBLIC)) {
//                MethodSpec methodSpec = MethodSpec.methodBuilder(element.getSimpleName().toString())
//                        .addModifiers(element.getModifiers())
//                        //.addParameters(element.)
//                        .returns(Void.class)
//                        .build();
//
//                classSpecBuilder.addMethod(methodSpec);
//            }
//        }
//
//        try { // write the file
//            JavaFile file = JavaFile.builder(classElement.getQualifiedName().toString(),
//                    classSpecBuilder.build()).build();
//            file.writeTo(processingEnv.getFiler());
//        } catch (IOException e) {
//            // Note: calling e.printStackTrace() will print IO errors
//            // that occur from the file already existing after its first run, this is normal
//        }
//
//    }

}
