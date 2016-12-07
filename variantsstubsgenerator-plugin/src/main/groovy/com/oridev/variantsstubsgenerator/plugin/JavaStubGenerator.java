package com.oridev.variantsstubsgenerator.plugin;


import com.oridev.variantsstubsgenerator.annotation.GeneratedVariantStub;
import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.AnnotationTargetSource;
import org.jboss.forge.roaster.model.source.Import;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.source.MethodHolderSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.TypeHolderSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Created by Ori on 03/11/2016.
 */

public class JavaStubGenerator {



    public String getAnnotationFlavorTo() {
        AnnotationSource annotation = (AnnotationSource) mSource.getAnnotation(RequiresVariantStub.class);
        if (annotation != null) {
            return annotation.getStringValue("flavorTo");
//        return mAnnotation.getStringValue("flavorTo");
        }
        return null;
    }

    public String generateStubSourceFile() {

        try {
            File targetFile = generateJavaStubFile();
            return targetFile.getPath();

        } catch (IOException e) {
            Utils.logMessage("IOException: " + e);
            return null;
        }
    }

    private File mOriginalFile = null;
    private JavaType<?> mSource = null;

    public JavaStubGenerator(File originalFile) {
        mOriginalFile = originalFile;
        initializeSource();
    }

    private void initializeSource() {
        try {
            Utils.logMessage("Parsing file " + mOriginalFile.getPath());

            // read file content
            byte[] encoded = Files.readAllBytes(Paths.get(mOriginalFile.getPath()));
            String originalCode = new String(encoded, Charset.defaultCharset());

            String formattedCode = formatSource(originalCode);

            mSource = Roaster.parse(formattedCode);

        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("JavaStubGenerator Invalid param originalFile [" + mOriginalFile + "]", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing file " + mOriginalFile.getName(), e);
        }

    }


    private File generateJavaStubFile() throws IOException {

        String targetPath = getTargetPath();
        File targetDir = new File(targetPath);
        if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
            throw new IOException("Failed to create directory " + targetPath);
        }

        JavaType<?> generatedSource = generateStubSource(mSource);

        File targetFile = new File(targetDir, generatedSource.getName() + ".java");

        String generatedContent = generatedSource.toString();

        //Utils.logMessage("*** Writing generated content:\n" + generatedContent);

        Files.write(Paths.get(targetFile.toURI()), generatedContent.getBytes());

        return targetFile;
    }

    private String getTargetPath() {
        String targetFlavor = getAnnotationFlavorTo();
        String sourcePackage = mSource.getPackage().replace('.', '/'); // .replaceAll("\\.", "/")

        String buildDir = getBuildDir(mOriginalFile.getPath(), sourcePackage);
        String targetSourceSet = PathsUtils.getTargetSourceSetPath(buildDir, targetFlavor);
        Utils.logMessage("*** targetSourceSet [" + targetSourceSet + "], sourcePackage [" + sourcePackage + "]");

        return targetSourceSet + "/" + sourcePackage;
    }

    private static String getBuildDir(String exampleSourcePath, String packagePath) {
        String sourceSetPath = exampleSourcePath.substring(0, exampleSourcePath.indexOf(packagePath) - 1);
        File sourceSetFile = new File(sourceSetPath);
        // get projectDir from {projectDir}/{src}/{flavor}/java
        File projectDir = sourceSetFile.getParentFile().getParentFile().getParentFile();
        return new File(projectDir, "build").getPath();
    }




    /* Java Source Generation ------------- */

    private static JavaType<?> generateStubSource(JavaType<?> originalSource) {

        JavaSource javaSource = (JavaSource) originalSource;

        handleLibraryAnnotations(javaSource);
        handleTypeSource(originalSource);
        removeUnusedImportsUsingRoaster(javaSource);

        return originalSource;
    }

//    private static void addExplanatoryJavadoc(JavaSource<?> source) {
//        source.getJavaDoc().setFullText("Generated coed by VariantsStubsGenerator library. Do not modify!");
//    }

    private static void handleLibraryAnnotations(AnnotationTargetSource source) {

        // remove annotation RequiresVariantStub annotation
        AnnotationSource annotationSource = source.getAnnotation(RequiresVariantStub.class);
        source.removeAnnotation(annotationSource);

        // add GeneratedVariantStubAnnotation
        //source.addAnnotation(GeneratedVariantStub.class);
    }

    private static void removeUnusedImportsUsingRoaster(JavaSource<?> source) {
        final String sourceContent = source.toString();

        for (Import importObj : source.getImports()) {
            String importClassName = importObj.getSimpleName();

            String contentAfterImport = sourceContent.substring(sourceContent.indexOf(importClassName) + importClassName.length());
            // if the source doesn't contain the class usage
            if (!contentAfterImport.contains(importClassName)) {
                source.removeImport(importObj);
            }

        }
    }

    private static void handleTypeSource(JavaType<?> source) {
        if (source.isClass()) {
            JavaClassSource classSource = (JavaClassSource) source;

            handleSourceMethods(classSource);
            handleSourceNestedTypes(classSource);

        } else if (source.isInterface()) {
            // interface cannot contain non-public elements, leave as is
            handleSourceNestedTypes((JavaInterfaceSource) source);

        } else if (source.isEnum()) {
            handleSourceMethods((JavaEnumSource) source);

        } else if (source.isAnnotation()) {
            // todo?
        }
    }


    private static <T extends JavaSource<T>> void handleSourceMethods(MethodHolderSource<T> source) {

        // handle source methods
        for (MethodSource<T> method : source.getMethods()) {

            if (!method.isPublic()) {
                if (source instanceof JavaEnumSource && method.isConstructor()) {
                    method.setBody("");
                } else {
                    // remove non public methods
                    source.removeMethod(method);
                }
            } else {
                Type<?> returnType = method.getReturnType();
                String methodBody;
                if (returnType.getName().equals("void")) {
                    methodBody = "";
                } else {
                    String returnValue = getReturnStringValue(returnType);
                    methodBody = "return " + returnValue + ";";
                }
                method.setBody(methodBody);
            }
        }



    }

    private static <T extends JavaSource<T>> void handleSourceNestedTypes(TypeHolderSource<T> source) {
        for (JavaSource<?> innerType : source.getNestedTypes()) {
            handleTypeSource(innerType);
        }
    }


    private static String getReturnStringValue(Type<?> returnType) {

        final String returnTypeName = returnType.getName();
        String returnTypeStr;

        if (returnType.isPrimitive()) {
            if (returnTypeName.equals("boolean")) {
                // for boolean returnType return false
                returnTypeStr = "false";
            } else {
                // for other primitive types return 0
                returnTypeStr = "0";
            }
        } else {
            // for non primitive types return null
            returnTypeStr = "null";
        }

        return returnTypeStr;
    }

    private static String formatSource(String source) {

        return "// Generated code by VariantsStubsGenerator library. Do not modify!\n" +
                removeComments(source);

        //String formattedGeneratedSource = Roaster.format(source);

//        FileContents sourceContent = new FileContents()
//        try {
//            DetailAST walker = TreeWalker.(sourceContent);
//            walker.
//        } catch (RecognitionException | TokenStreamException e) {
//            e.printStackTrace();
//        }
//        UnusedImportsCheck check = new UnusedImportsCheck() {
//
//        };

//        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addMethod(main)
//                .build();
//
//        JavaFile javaFile = JavaFile.Builder
//                .build();
//
//        javaFile.writeTo(System.out);
    }

    public static String removeComments(String code) {
        StringBuilder newCode = new StringBuilder();
        StringReader reader = null;
        try {
            reader = new StringReader(code);
            boolean inBlockComment = false;
            boolean inLineComment = false;
            boolean out = true;

            int prev = reader.read();
            int cur;
            for(cur = reader.read(); cur != -1; cur = reader.read()) {
                if(inBlockComment) {
                    if (prev == '*' && cur == '/') {
                        inBlockComment = false;
                        out = false;
                    }
                } else if (inLineComment) {
                    if (cur == '\r') { // start untested block
                        reader.mark(1);
                        int next = reader.read();
                        if (next != '\n') {
                            reader.reset();
                        }
                        inLineComment = false;
                        out = false; // end untested block
                    } else if (cur == '\n') {
                        inLineComment = false;
                        out = false;
                    }
                } else {
                    if ((prev == '/' && cur == '*') || prev == '*' && cur == '*') {
                        //if (prev == '/' && cur == '*') {
                        reader.mark(1); // start untested block
                        int next = reader.read();
                        if (next != '*') {
                            inBlockComment = true; // tested line (without rest of block)
                        }
                        reader.reset(); // end untested block
                    } else if (prev == '/' && cur == '/') {
                        inLineComment = true;
                    } else if (out){
                        newCode.append((char)prev);
                    } else {
                        out = true;
                    }
                }
                prev = cur;
            }
            if (prev != -1 && out && !inLineComment) {
                newCode.append((char)prev);
            }
        } catch (IOException e) {
            Utils.logMessage("JavaStubGenerator error removing source comments: " + e, true);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return newCode.toString();
    }




    /* Roaster aux methods ----------- */

//    private void setTypeHeader(JavaType<?> original, JavaType<?> generated) {
//
//    }
//
//    private void SetTypeModifiers(JavaType<?> original, JavaType<?> generated) {
//
//    }




    /* JavaPoet object generation -------------------------------- */
//    private JavaFile generateStubJavaFile(CompilationUnit element) {
//        TypeDeclaration type = getPublicTypeFromCompilationUnit(element);
//
//        final String qualifiedName = element.getPackage() + "." + element.getTypes().
//
//        // generate stub java-file-object
//        try {
//            TypeSpec.Builder typeSpecBuilder = generateStubTypeObject(type, element.getPackage().getPackageName());
//
//            typeSpecBuilder.addAnnotation(GeneratedVariantStub.class);
//
//            // write the new file
//            String typePackage = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
//
//            return JavaFile.builder(typePackage, typeSpecBuilder.build())
//                    .addFileComment("Generated code from annotated class " + qualifiedName + ", Do not modify!")
//                    .build();
//
//        } catch (Exception e) {
//            Utils.logMessage("generateStubJavaFile failure: " + e);
//            return null;
//        }
//    }
//
//    private TypeSpec.Builder generateStubTypeObject(TypeDeclaration element, String _package) {
//
//
//        final String simpleName = element.getName();
//        final String qualifiedName = _package + "." + simpleName;
//
//        TypeSpec.Builder builder;
//
//        // get element kind. since this is a TypeElement this must be type
//        ElementKind elementKind = getElementKindFromTypeDeclaration(element);
//        switch (elementKind) {
//
//            case CLASS:
//                builder = TypeSpec.classBuilder(simpleName);
//                break;
//
//            case INTERFACE:
//                builder = TypeSpec.interfaceBuilder(simpleName);
//                break;
//
//            case ENUM:
//                builder = TypeSpec.enumBuilder(simpleName);
//                break;
//
//            case ANNOTATION_TYPE:
//                throw new IllegalArgumentException("Stub generation for annotation types is still not supported... :(");
////                builder = TypeSpec.annotationBuilder(simpleName);
////                break;
//
//            default:
//                throw new EnumConstantNotPresentException(ElementKind.class, elementKind.name());
//
//        }
//
//            // add type modifiers (private, static, final...)
//            builder.addModifiers(getModifiersForType(element));
//
//            // add type parameters (generics)
////            builder.addTypeVariables(getTypeParametersForType(element));
//
//            // add type annotations
//            builder.addAnnotations(getAnnotationsForElement(element));
//
//            // for each public element inside the annotated type
//            for (Element innerElement : element.getEnclosedElements()) {
//                if (innerElement.getModifiers().contains(Modifier.PUBLIC)) {
//
//                    String innerElementName = innerElement.getSimpleName().toString();
//                    ElementKind innerElementKind = innerElement.getKind();
//
//                    // create inner types
//                    if (innerElementKind.isClass() || innerElementKind.isInterface()) {
//                        TypeSpec innerTypeSpec = generateStubTypeObject((TypeElement) innerElement, annotation).build();
//                        builder.addType(innerTypeSpec);
//
//                        // create stubs for methods
//                    } else if (innerElementKind == ElementKind.METHOD) {
//                        ExecutableElement method = (ExecutableElement) innerElement;
//                        if (shouldAddMethodToGeneratedClass(element, method)) {
//                            MethodSpec methodSpec = generateStubMethodObject(method, throwException, qualifiedName).build();
//                            builder.addMethod(methodSpec);
//                        }
//
//                    } else if (innerElementKind == ElementKind.ENUM_CONSTANT) {
//                        builder.addEnumConstant(innerElementName);
//                    }
//
//                    // todo: handle non methods elements! */
//
//                }
//
//            }
//
//        return builder;
//    }
//
//    private MethodSpec.Builder generateStubMethodObject(ExecutableElement method, boolean throwException, String typeQualifiedName) {
//        final String methodName = method.getSimpleName().toString();
//
//        StringBuilder infoStrBuilder = new StringBuilder(typeQualifiedName + "$" + methodName + "(");
//
//        TypeMirror methodReturnType = method.getReturnType();
//
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
//                .addModifiers(method.getModifiers())
//                .returns(TypeName.get(methodReturnType))
//                .addAnnotations(getAnnotationsForElement(method));
//
//
//        // add parameters
//        List<? extends VariableElement> methodParams = method.getParameters();
//        for (int i = 0; i < methodParams.size(); i++) {
//            VariableElement methodParam = methodParams.get(i);
//
//            TypeName methodParamType = TypeName.get(methodParam.asType());
//            String methodParamName = methodParam.getSimpleName().toString();
//            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(methodParamType, methodParamName);
//            methodBuilder.addParameter(paramBuilder.build());
//
//            infoStrBuilder.append(methodParamType);
//            if (i < methodParams.size() - 1) {
//                infoStrBuilder.append(", ");
//            }
//        }
//
//        infoStrBuilder.append(")");
//
//        if (throwException) {
//            String info = infoStrBuilder.toString();
//            // add throw statement
//            methodBuilder.addStatement("throw new $T($S)", AttemptToUseStubException.class, info);
//
//        } else if (methodReturnType.getKind() != TypeKind.VOID) {
//            // add return statement if necessary
//            methodBuilder.addStatement("return " + getDefaultReturnTypeForTypeMirror(methodReturnType));
//        }
//
//        return methodBuilder;
//    }
//
//
//    /* Object Generation Aux */
//
//    private ElementKind getElementKindFromTypeDeclaration(TypeDeclaration type) {
//
//        ElementKind kind = null;
//
//        if (type instanceof ClassOrInterfaceDeclaration) {
//            ClassOrInterfaceDeclaration classOrInterface = (ClassOrInterfaceDeclaration) type;
//
//            if (!classOrInterface.isInterface()) {
//                kind = ElementKind.INTERFACE;
//            } else {
//                kind = ElementKind.CLASS;
//            }
//
//        } else if (type instanceof EnumDeclaration) {
//            kind = ElementKind.ENUM;
//
//        } else if (type instanceof AnnotationDeclaration) {
//            kind = ElementKind.ANNOTATION_TYPE;
//        }
//
//        return kind;
//    }
//
//    private TypeDeclaration getPublicTypeFromCompilationUnit(CompilationUnit unit) {
//        for (TypeDeclaration type : unit.getTypes()) {
//            if (ModifierSet.isPublic(type.getModifiers())) {
//                // there can only be one public type inside java file
//                return type;
//            }
//        }
//        return null;
//    }
//
//    private Modifier[] getModifiersForType(TypeDeclaration element) {
//        List<Modifier> modifierList = new ArrayList<>();
//
//        // add modifiers
//        int modifiers = element.getModifiers();
//
//        if (ModifierSet.isPublic(modifiers)) {
//            modifierList.add(Modifier.PUBLIC);
//        }
//        if (ModifierSet.isAbstract(modifiers)) {
//            modifierList.add(Modifier.ABSTRACT);
//        }
//        if (ModifierSet.isFinal(modifiers)) {
//            modifierList.add(Modifier.FINAL);
//        }
//
//        // remove redundant modifiers
//        ElementKind elementKind = getElementKindFromTypeDeclaration(element);
//        if (elementKind.isInterface()) {
//            modifierList.remove(Modifier.ABSTRACT);
//        }
//        if (elementKind == ElementKind.ENUM) {
//            modifierList.remove(Modifier.FINAL);
//        }
//
//        return modifierList.toArray(new Modifier[modifierList.size()]);
//    }
//
////    private List<TypeVariableName> getTypeParametersForType(ClassOrInterfaceDeclaration element) {
////
////
////        List<? extends TypeParameter> typeParameters = element.getTypeParameters();
////
////        List<TypeVariableName> typeNames = new ArrayList<>();
////        for (TypeParameter typeParameter : typeParameters) {
////
////            // get generic inheritance data
////            List<ClassOrInterfaceType> typeParameterBoundsTypeMirror = typeParameter.getTypeBound();
////            TypeName[] typeParameterBoundsTypeName = new TypeName[typeParameterBoundsTypeMirror.size()];
////            for (int i = 0; i < typeParameterBoundsTypeMirror.size(); i++) {
////                Type type = typeParameterBoundsTypeMirror.get(i).getName();
////                typeParameterBoundsTypeName[i] = TypeName.get(type);
////            }
////
////            TypeVariableName typeVariable = TypeVariableName.get(
////                    typeParameter.getSimpleName().toString(), typeParameterBoundsTypeName);
////            typeNames.add(typeVariable);
////        }
////
////        return typeNames;
////    }
//
//    private List<AnnotationSpec> getAnnotationsForElement(TypeDeclaration element) {
//
//        List<? extends AnnotationExpr> annotationExprs = element.getAnnotations();
//        List<AnnotationSpec> annotationSpecs = new ArrayList<>(annotationExprs.size());
//
//        for (AnnotationExpr mirror : annotationExprs) {
//            mirror.getName().getName()
//
//            AnnotationSpec annotation = AnnotationSpec.get();
//
//            // exclude variantsStubsGenerator annotations...
//            if (getSupportedAnnotationTypes().contains(annotation.type.toString())) {
//                continue;
//            }
//
//            annotationSpecs.add(annotation);
//        }
//
//        return annotationSpecs;
//    }
//
//    private AnnotationSpec annotationMirrorToAnnotationSpec(AnnotationMirror annotationMirror) {
//        return AnnotationSpec.get(annotationMirror);
//    }
//
//    private boolean shouldAddMethodToGeneratedClass(TypeElement containingClass, ExecutableElement method) {
//
//        // handle enum base methods
//        if (containingClass.getKind() == ElementKind.ENUM) {
//            final String methodName = method.getSimpleName().toString();
//            if (methodName.equals("values") || methodName.equals("valueOf")) {
//                return false;
//            }
//        }
//
//        // as default return true
//        return true;
//    }
//
//    private Object getDefaultReturnTypeForTypeMirror(TypeMirror type) {
//        TypeName typeName = TypeName.get(type);
//
//        // if not primitive - the default should be null
//        if (!typeName.isPrimitive()) {
//            return null;
//        }
//
//        // if boolean
//        if (typeName == TypeName.BOOLEAN) {
//            return false;
//        }
//        // if other primitive type - this will do
//        return 0;
//    }

}
