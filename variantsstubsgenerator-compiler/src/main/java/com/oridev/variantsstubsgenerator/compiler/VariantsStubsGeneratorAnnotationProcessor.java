package com.oridev.variantsstubsgenerator.compiler;

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
import com.squareup.javapoet.TypeVariableName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.naming.OperationNotSupportedException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
public class VariantsStubsGeneratorAnnotationProcessor extends AbstractProcessor {

    private static final boolean DEBUG = false;

    private static ProcessingEnvironment environment;
    private Filer filer;

    //private boolean mFirstRound = true;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        environment = env;
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

        List<GeneratedFileEntry> generatedFiles = new ArrayList<>();

        // generate stubs for annotated classes
        Set<? extends Element> generatingElements = roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class);
        for (Element element : generatingElements) {

            TypeElement typeElement = (TypeElement) element;
            final String fileQualifiedName = typeElement.getQualifiedName().toString();

            JavaFile file = generateStubJavaFile(typeElement);
            if (file != null) {

                RequiresVariantStub annotation = element.getAnnotation(RequiresVariantStub.class);
                final String flavorTo = annotation.flavorTo();

                try {
                    String basePath = getSourceSetPath(flavorTo);
                    writeSourceFile(file, basePath);

                    String path = getPathForFile(file, flavorTo);
                    if (path != null) {
                        generatedFiles.add(new GeneratedFileEntry(flavorTo, path));
                    }

                } catch (Throwable e) {
                    Utils.logMessage(Diagnostic.Kind.NOTE, "Processing failed for file " + fileQualifiedName + ": " + e, true);
                }
            }
        }

        if (!generatedFiles.isEmpty()) {
            // write generated files paths to the info json
            writeFileEntriesJson(generatedFiles);
        }

        return true;
    }


    /* JavaFile object generation -------------------------------- */

    private JavaFile generateStubJavaFile(TypeElement element) {
        final String qualifiedName = element.getQualifiedName().toString();
        final RequiresVariantStub annotation = element.getAnnotation(RequiresVariantStub.class);

        // generate stub java-file-object
        try {
            TypeSpec typeSpec = generateStubTypeObject(element, annotation);

            // write the new file
            String typePackage = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));

            return JavaFile.builder(typePackage, typeSpec)
                    .addFileComment("Generated code from annotated class " + qualifiedName + ", Do not modify!")
                    .build();

        } catch (Exception e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "generateStubJavaFile failure: " + e);
            return null;
        }
    }

    private TypeSpec generateStubTypeObject(TypeElement element, RequiresVariantStub annotation) {

        final String simpleName = element.getSimpleName().toString();
        final String qualifiedName = element.getQualifiedName().toString();

        // get annotation parameters
        final boolean throwException = annotation.throwException();

        // create type builder
        TypeSpec.Builder builder;

        // get element kind. since this is a TypeElement this must be type
        ElementKind elementKind = element.getKind();
        switch (elementKind) {

            case CLASS:
                builder = TypeSpec.classBuilder(simpleName);
                break;

            case INTERFACE:
                builder = TypeSpec.interfaceBuilder(simpleName);
                break;

            case ENUM:
                builder = TypeSpec.enumBuilder(simpleName);
                break;

            case ANNOTATION_TYPE:
                throw new IllegalArgumentException("Stub generation for annotation types is still not supported... :(");
//                builder = TypeSpec.annotationBuilder(simpleName);
//                break;

            default:
                throw new EnumConstantNotPresentException(ElementKind.class, elementKind.name());

        }

        // add type modifiers (private, static, final...)
        builder.addModifiers(getModifiersForType(element));

        // add type parameters (generics)
        builder.addTypeVariables(getTypeParametersForType(element));

        // add type annotations


        // for each public element inside the annotated type
        for (Element innerElement : element.getEnclosedElements()) {
            if (innerElement.getModifiers().contains(Modifier.PUBLIC)) {

                String innerElementName = innerElement.getSimpleName().toString();
                ElementKind innerElementKind = innerElement.getKind();

                // create inner types
                if (innerElementKind.isClass() || innerElementKind.isInterface()) {
                    TypeSpec innerTypeSpec = generateStubTypeObject((TypeElement) innerElement, annotation);
                    builder.addType(innerTypeSpec);

                    // create stubs for methods
                } else if (innerElementKind == ElementKind.METHOD) {
                    MethodSpec methodSpec = generateStubMethodObject((ExecutableElement) innerElement, throwException, qualifiedName);
                    builder.addMethod(methodSpec);

                } else if (innerElementKind == ElementKind.ENUM_CONSTANT) {
                    builder.addEnumConstant(innerElementName);
                }

                // todo: handle non methods elements! */

            }

        }

        return builder.build();
    }

    private MethodSpec generateStubMethodObject(ExecutableElement method, boolean throwException, String typeQualifiedName) {
        final String methodName = method.getSimpleName().toString();

        StringBuilder infoStrBuilder = new StringBuilder(typeQualifiedName + "$" + methodName + "(");

        TypeMirror methodReturnType = method.getReturnType();

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(method.getModifiers())
                .returns(TypeName.get(methodReturnType));


        // add parameters
        List<? extends VariableElement> methodParams = method.getParameters();
        for (int i = 0; i < methodParams.size(); i++) {
            VariableElement methodParam = methodParams.get(i);

            TypeName methodParamType = TypeName.get(methodParam.asType());
            String methodParamName = methodParam.getSimpleName().toString();
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(methodParamType, methodParamName);
            methodBuilder.addParameter(paramBuilder.build());

            infoStrBuilder.append(methodParamType);
            if (i < methodParams.size() - 1) {
                infoStrBuilder.append(", ");
            }
        }

        infoStrBuilder.append(")");

        if (throwException) {
            String info = infoStrBuilder.toString();
            // add throw statement
            methodBuilder.addStatement("throw new $T($S)", AttemptToUseStubException.class, info);

        } else if (methodReturnType.getKind() != TypeKind.VOID) {
            // add return statement if required
            methodBuilder.addStatement("return " + getDefaultReturnTypeForTypeMirror(methodReturnType));
        }

        return methodBuilder.build();
    }

    /* Object Generation Aux */

    private Modifier[] getModifiersForType(TypeElement element) {
        List<Modifier> modifierList = new ArrayList<>(element.getModifiers());

        if (element.getKind().isInterface()) {
            modifierList.remove(Modifier.ABSTRACT);
        }

        return modifierList.toArray(new Modifier[modifierList.size()]);
    }

    private List<TypeVariableName> getTypeParametersForType(TypeElement element) {
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();

        List<TypeVariableName> typeNames = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeParameters) {

            // get generic inheritance data
            List<? extends TypeMirror> typeParameterBoundsTypeMirror = typeParameter.getBounds();
            TypeName[] typeParameterBoundsTypeName = new TypeName[typeParameterBoundsTypeMirror.size()];
            for (int i = 0; i < typeParameterBoundsTypeMirror.size(); i++) {
                typeParameterBoundsTypeName[i] = TypeName.get(typeParameterBoundsTypeMirror.get(i));
            }

            TypeVariableName typeVariable = TypeVariableName.get(
                    typeParameter.getSimpleName().toString(), typeParameterBoundsTypeName);
            typeNames.add(typeVariable);
        }

        return typeNames;
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


    /* Write file ----------------------------------------------- */

    private String mExamplePath = null;
    private static final String EXAMPLE_PACKAGE = "com.example";
    private static final String EXAMPLE_NAME = "_d_";
    private String getExamplePath() {
        if (mExamplePath == null) {

            String fileName = EXAMPLE_PACKAGE + "." + EXAMPLE_NAME;
            JavaFileObject filerSourceFile;
            try {
                filerSourceFile = filer.createSourceFile(fileName);
                mExamplePath = filerSourceFile.toUri().getPath();

            } catch (IOException e) {
                Utils.logMessage(Diagnostic.Kind.WARNING, "getExamplePath failed createSourceFile: " + e);

            } finally {
                // todo: release resource? (avoid following warning:
                // warning: Unclosed files for the types '[com.example._d_.java]'; these types will not undergo annotation processing
            }
        }

        return mExamplePath;
    }

    private static final String BUILD_RELATIVE_PATH = "generated/source/apt/";

    private String getBuildDir() {
        String examplePath = getExamplePath();
        return examplePath.substring(0, examplePath.indexOf(BUILD_RELATIVE_PATH));
    }

    private String getSourceSetPath(String flavorTo) {

        final String examplePath = getExamplePath();

        /* fix for multiple dimensions projects */
        String currentFlavor = examplePath.substring(examplePath.indexOf(BUILD_RELATIVE_PATH) + BUILD_RELATIVE_PATH.length());
        currentFlavor = currentFlavor.substring(0, currentFlavor.indexOf('/'));

        // get the desired target path
        String fullPath = examplePath
                // set path to flavorTo
                .replace(BUILD_RELATIVE_PATH + currentFlavor, BUILD_RELATIVE_PATH + flavorTo);

        // remove package and file from path
        return fullPath.substring(0, fullPath.indexOf(EXAMPLE_PACKAGE.replaceAll("\\.", "/")));
    }

    private String getPathForFile(JavaFile javaFile, String flavorTo) {

        final String examplePath = getExamplePath();

        /* fix for multiple dimensions projects */
        String currentFlavor = examplePath.substring(examplePath.indexOf(BUILD_RELATIVE_PATH) + BUILD_RELATIVE_PATH.length());
        currentFlavor = currentFlavor.substring(0, currentFlavor.indexOf('/'));

        // get the desired target path
        return examplePath
                // set path to flavorTo
                .replace(BUILD_RELATIVE_PATH + currentFlavor, BUILD_RELATIVE_PATH + flavorTo)
                // set file package
                .replace(EXAMPLE_PACKAGE.replaceAll("\\.", "/"), javaFile.packageName.replaceAll("\\.", "/"))
                // set file name
                .replace("/" + EXAMPLE_NAME, "/" + javaFile.typeSpec.name);
    }

    private void writeSourceFile(JavaFile javaFile, String path) {
        Utils.logMessage(Diagnostic.Kind.NOTE, "Writing file [" + javaFile.packageName + "." +
                javaFile.typeSpec.name + "] sourceSetPath [" + path + "]", true);

        try {
            javaFile.writeTo(new File(path));
        } catch (Exception e) {
            Utils.logMessage(Diagnostic.Kind.WARNING, "Error copying file " + javaFile.typeSpec.name + ": " + e);
        }
    }



    private void writeFileEntriesJson(List<GeneratedFileEntry> generatedFiles) {

        if (generatedFiles == null || generatedFiles.size() == 0) {
            return;
        }

        String buildDir = getBuildDir();
        FileEntriesJsonManager.updateFileEntriesJson(buildDir, generatedFiles);
    }


    /* Utilities --------------------------------------- */


    public static void logMessage(Diagnostic.Kind kind, String message, boolean showOnRelease) {

        if (showOnRelease || DEBUG) {
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


    /* Drafts / Old Code... */
    //    private String writeSourceFile(JavaFile javaFile, String flavorFrom, String flavorTo) throws IOException {
//        final String EXT = "_d_";
//
//        // create draft file (copy of the file we want to create but in flavorFrom)
//        String fileName = javaFile.packageName.isEmpty()
//                ? javaFile.typeSpec.name
//                : javaFile.packageName + "." + EXT + javaFile.typeSpec.name;
//        List<Element> originatingElements = javaFile.typeSpec.originatingElements;
//        JavaFileObject filerSourceFile = filer.createSourceFile(fileName,
//                originatingElements.toArray(new Element[originatingElements.size()]));
//        try (Writer writer = filerSourceFile.openWriter()) {
//            javaFile.writeTo(writer);
//        } catch (Exception e) {
//            logMessage(Diagnostic.Kind.WARNING, "Failed to write draft file: " + e);
//            try {
//                filerSourceFile.delete();
//            } catch (Exception ignored) { }
//            throw e;
//        }
//
//        // get the desired target path
//        File generatedFile = new File(filerSourceFile.toUri());
//        String targetPathStr = generatedFile.getPath()
//                // set path to flavorTo
//                .replace("generated/source/apt/" + flavorFrom, "generated/source/apt/" + flavorTo)
//                // remove extension from file name
//                .replace("/" + EXT + javaFile.typeSpec.name, "/" + javaFile.typeSpec.name);
//        File targetFile = new File(targetPathStr);
//        File targetDirectory = targetFile.getParentFile();
//
//        targetDirectory.mkdirs();
//        // folder validation.
////        if (!targetDirectory.isDirectory() && !targetDirectory.mkdir()) {
////            throw new IOException("Failed to create directory: " + targetDirectory);
////        }
//
//        // copy the draft file to the real location (flavorTo)
//        logMessage(Diagnostic.Kind.NOTE, "copying generated file to " + targetDirectory);
//        FileWriter fileWriter = null;
//        try {
//            fileWriter = new FileWriter(targetFile);
//            javaFile.writeTo(fileWriter);
//            //Files.copy(generatedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING /*, StandardCopyOption.ATOMIC_MOVE */);
//        } catch (Exception e) {
//            logMessage(Diagnostic.Kind.WARNING, "Error copying file: " + e);
//        } finally {
//            if (fileWriter != null) {
//                try { fileWriter.close(); }
//                catch (IOException e) { }
//            }
//        }
//
//        // change draft file in flavorFrom's class name to temp name
//        Path path = Paths.get(filerSourceFile.toUri());
//        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
//        content = content.replaceFirst("class " + javaFile.typeSpec.name, "class " + EXT + javaFile.typeSpec.name);
//        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
//
//
//        /* close filer (no compilation error but now gradle doesn't consider generated files */
////        if (filer instanceof Closeable) {
////            try {
////                ((Closeable) filer).close();
////            } catch (Exception e) {
////                logMessage(Diagnostic.Kind.WARNING, "Failed to close filer: " + e);
////            }
////        }
//
//        return targetPathStr;
//
//        /* delete file after moving (didn't help) */
////        try {
////            //Runtime.getRuntime().exec("rm ./.gradle/2.14.1/taskArtifacts/cache.properties.lock");
////
////            boolean deleted = filerSourceFile.delete();
////            //filer.getResource(StandardLocation.SOURCE_OUTPUT, javaFile.packageName, javaFile.typeSpec.name).delete();
////            if (!deleted /*|| !filerSourceFile.delete() */) {
////                logMessage(Diagnostic.Kind.NOTE, "Error deleting file");
////
////            }
////        } catch (Exception e) {
////            logMessage(Diagnostic.Kind.NOTE, "Failed to delete file: " + e);
////        }
//
//    }

}
