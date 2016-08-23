package com.oridev.variantsstubsgenerator.compiler;

import com.google.auto.service.AutoService;
import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.exception.AttemptToUseStubException;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;


@AutoService(Processor.class)
public class VariantsStubsGeneratorAnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typesUtils;
    private Filer filer;

    private boolean mFirstRound = true;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        typesUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RequiresVariantStub.class.getCanonicalName());
        return types;
    }

//    @Override
//    public Set<String> getSupportedOptions() {
//        Set<String> options = new LinkedHashSet<>();
//        options.add("flavor");
//        return options;
//    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


//    /**
//     * For debugging!! yeah!!
//     */
//    public static void main(String[] args) {
//        try {
//            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//            JavaFileManager manager = compiler.getStandardFileManager(null, null, null);
//                //new DiagnosticCollector<JavaFileObject>(), Locale.getDefault(), Charset.defaultCharset());
//            //Path source = Files.get createTempDirectory("stackoverflow").resolve("Test.java");
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

//        if (!mFirstRound) {
//            logMessage(Diagnostic.Kind.NOTE, "process already called, skipping..");
//            return true;
//        }
//        mFirstRound = false;

        // collect all the annotated classes
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class);
        for (Element element : elements) {
            logMessage(Diagnostic.Kind.NOTE, "Processing annotated class " + element.getSimpleName());
            TypeElement classElement = (TypeElement) element;

            // get annotation parameters
            final String flavorFrom = element.getAnnotation(RequiresVariantStub.class).flavorFrom();
            final String flavorTo = element.getAnnotation(RequiresVariantStub.class).flavorTo();
            final boolean throwException = element.getAnnotation(RequiresVariantStub.class).throwException();

//            if (!SuperficialValidation.validateElement(element)) {
//                continue;
//            }

            // start building the java stubs file
            try {
                // Create the duplicate class
                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(classElement.getSimpleName().toString());
                Modifier[] classModifiers = classElement.getModifiers().toArray(new Modifier[classElement.getModifiers().size()]);
                classBuilder.addModifiers(classModifiers);

                // for each public element inside the annotated class
                for (Element classInnerElement : classElement.getEnclosedElements()) {
                    if (classInnerElement.getModifiers().contains(Modifier.PUBLIC)) {

                        // create stubs for methods
                        if (classInnerElement.getKind() == ElementKind.METHOD) {
                            ExecutableElement method = (ExecutableElement) classInnerElement;

                            TypeMirror methodReturnType = method.getReturnType();

                            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                                    .addModifiers(method.getModifiers())
                                    .returns(TypeName.get(methodReturnType));

                            if (throwException) {
                                // add throw statement
                                methodBuilder.addStatement("throw new $T($S, $S)", AttemptToUseStubException.class, flavorFrom, flavorTo);

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
                String classPackage = classElement.getQualifiedName().toString();
                classPackage = classPackage.substring(0, classPackage.lastIndexOf('.')); // todo: handle inner classes!

                JavaFile file = JavaFile.builder(classPackage, classBuilder.build())
                        .addFileComment("Generated code from VariantsStubsGenerator library. Do not modify!\n")
                        .build();

                writeSourceFile(file, flavorFrom, flavorTo);

                logMessage(Diagnostic.Kind.NOTE, "Processing done.");

            } catch (Throwable e) {
                logMessage(Diagnostic.Kind.NOTE, "Processing failed: " + e);
            }
        }

        logMessage(Diagnostic.Kind.NOTE, "All processing done.");
        return true;
    }

    private void writeSourceFile(JavaFile javaFile, String flavorFrom, String flavorTo) throws IOException {

        String fileName = javaFile.packageName.isEmpty()
                ? javaFile.typeSpec.name
                : javaFile.packageName + "._d_" + javaFile.typeSpec.name;
        List<Element> originatingElements = javaFile.typeSpec.originatingElements;
        JavaFileObject filerSourceFile = filer.createSourceFile(fileName,
                originatingElements.toArray(new Element[originatingElements.size()]));
        try (Writer writer = filerSourceFile.openWriter()) {
            javaFile.writeTo(writer);
        } catch (Exception e) {
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) {
            }
            throw e;
        }

        File generatedFile = new File(filerSourceFile.toUri());

        // move the file to location
        String targetPathStr = generatedFile.getPath()
                .replace(flavorFrom, flavorTo)
                .replace("/_d_" + javaFile.typeSpec.name, "/" + javaFile.typeSpec.name);
        File targetDirectory = new File(targetPathStr);
        logMessage(Diagnostic.Kind.NOTE, "moving generated file to " + targetDirectory);

        if (!targetDirectory.exists()) {
            Files.createDirectories(targetDirectory.toPath());
        }
        try {
            javaFile.writeTo(targetDirectory);
            //Files.copy(generatedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING /*, StandardCopyOption.ATOMIC_MOVE */);
            logMessage(Diagnostic.Kind.NOTE, "Move successful");
        } catch (Exception e) {
            logMessage(Diagnostic.Kind.NOTE, "Error moving file: " + e);
        }

        // change file in flavorFrom's class name to temp name
        Path path = Paths.get(filerSourceFile.toUri());
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replaceAll("class " + javaFile.typeSpec.name, "class " + "_d_" + javaFile.typeSpec.name);
        Files.write(path, content.getBytes(charset));



        /* close filer (no compilation error but now gradle doesn't consider generated files */
//        if (filer instanceof Closeable) {
//            filerSourceFile.delete();
//            try {
//                ((Closeable) filer).close();
//
//            } catch (Exception e) {
//                logMessage(Diagnostic.Kind.WARNING, "Failed to close filer: " + e);
//            }
//        }

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
