package com.oridev.variantsstubsgenerator.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.oridev.variantsstubsgenerator.Utils;
import com.oridev.variantsstubsgenerator.annotation.GeneratedVariantStub;
import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.exception.AttemptToUseStubException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    private static final boolean DEBUG = true;

    private static ProcessingEnvironment environment;
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
        types.add(GeneratedVariantStub.class.getCanonicalName());
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

        Map<String, GeneratedFileEntries> generatedFilesByFlavors = new HashMap<>();
        // fixme: temp solution! won't work for multiple flavored builds...
        String auxFlavorFrom = null;

        // generate stubs for annotated classes
        Set<? extends Element> generatingElements = roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class);
        for (Element element : generatingElements) {

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
                    String path = writeSourceFile(file, flavorFrom, flavorTo);

                    // add generated file to map
                    if (generatedFilesByFlavors.get(flavorTo) == null) {
                        // for first file added to this flavor - create new GeneratedFilesEntries object
                        generatedFilesByFlavors.put(flavorTo, new GeneratedFileEntries(flavorFrom, path));
                    } else {
                        // if GeneratedFilesEntries object already exists for flavorTo - add entry to existing object
                        generatedFilesByFlavors.get(flavorTo).addEntry(path);
                    }

                }
            } catch (Throwable e) {
                logMessage(Diagnostic.Kind.NOTE, "Processing failed: " + e);
            }
        }

//        Set<? extends Element> generatedElements = roundEnv.getElementsAnnotatedWith(GeneratedVariantStub.class);
//        for (Element element : generatedElements) {
//            Utils.logMessage(Diagnostic.Kind.NOTE, "Generated class: " + element.getSimpleName());
//        }
//
//        for (Element element : roundEnv.getRootElements()) {
//            Utils.logMessage(Diagnostic.Kind.NOTE, "Root element class: " + element.getSimpleName());
//        }

        if (!generatedFilesByFlavors.isEmpty()) {
            // write generated files to their target flavors
            writeFileEntriesJson(generatedFilesByFlavors);
        }


        //logMessage(Diagnostic.Kind.NOTE, "Processing done.");

        return true;
    }


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

            classBuilder.addAnnotation(GeneratedVariantStub.class);

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
                .replace("generated/source/apt/" + flavorFrom, "generated/source/apt/" + flavorTo)
                // remove extension from file name
                .replace("/" + EXT + javaFile.typeSpec.name, "/" + javaFile.typeSpec.name);
        File targetFile = new File(targetPathStr);
        File targetDirectory = targetFile.getParentFile();

        targetDirectory.mkdirs();
        // folder validation.
//        if (!targetDirectory.isDirectory() && !targetDirectory.mkdir()) {
//            throw new IOException("Failed to create directory: " + targetDirectory);
//        }

        // copy the draft file to the real location (flavorTo)
        logMessage(Diagnostic.Kind.NOTE, "copying generated file to " + targetDirectory);
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
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        content = content.replaceFirst("class " + javaFile.typeSpec.name, "class " + EXT + javaFile.typeSpec.name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));


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

    private void writeFileEntriesJson(Map<String, GeneratedFileEntries> generatedFiles) {

        if (generatedFiles == null || generatedFiles.size() == 0) {
            return;
        }

        FileEntriesJsonManager.writeFileEntriesJson(filer, generatedFiles);
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



    /* Utilities --------------------------------------- */

    public static void logMessage(Diagnostic.Kind kind, String message) {

        if (DEBUG) {
            try {
                if (message != null) {
                    environment.getMessager().printMessage(kind, message);
                } else {
                    environment.getMessager().printMessage(kind, "null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
