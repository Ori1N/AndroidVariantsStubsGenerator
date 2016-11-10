package com.oridev.variantsstubsgenerator.plugin;


import org.gradle.internal.impldep.com.google.common.collect.Sets;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;


/**
 * Created by Ori on 03/11/2016.
 */

public class JavaStubGenerator {



    public String getAnnotationFlavorTo() {
        return mAnnotation.getStringValue("flavorTo");
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

//    public static String generateStubSourceFile(File originalFile) {
//
//        try {
//            JavaStubGenerator generator = new JavaStubGenerator(originalFile);
//            File targetFile = generator.generateJavaStubFile();
//            return targetFile.getPath();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

// module
    private File mOriginalFile = null;
    private JavaType<?> mOriginalSource = null;
    private AnnotationSource<?> mAnnotation = null;

    public JavaStubGenerator(File originalFile) {
        mOriginalFile = originalFile;
        try {
            mOriginalSource = Roaster.parse(originalFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("JavaStubGenerator Invalid param originalFile [" + originalFile + "]", e);
        }
        mAnnotation = ((JavaClassSource) mOriginalSource).getAnnotation("RequiresVariantStub");
    }


    private File generateJavaStubFile() throws IOException {

        String targetPath = getTargetPath();
        File targetDir = new File(targetPath);
        if (!targetDir.isDirectory() && !targetDir.mkdirs()) {
            throw new IOException("Failed to create directory " + targetPath);
        }

        File targetFile = new File(targetDir, mOriginalFile.getName());

        String generatedContent = mOriginalSource.toString();
        Files.write(Paths.get(targetFile.toURI()), generatedContent.getBytes());

        return targetFile;
    }

    private String getTargetPath() {
        String targetFlavor = getAnnotationFlavorTo();
        String sourcePackage = mOriginalSource.getPackage().replace('.', '/'); // .replaceAll("\\.", "/")

        String buildDir = getBuildDir(mOriginalFile.getPath(), sourcePackage);
        String targetSourceSet = PathsUtils.getTargetSourceSetPath(buildDir, targetFlavor);
        Utils.logMessage("*** targetSourceSet [" + targetSourceSet + "], sourcePackage [" + sourcePackage + "]");

        return targetSourceSet + "/java/" + sourcePackage;
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

        JavaType<?> generatedSource = null;

        if (originalSource.isClass()) {
            generatedSource = buildStubClass((JavaClassSource) originalSource);

        } else if (originalSource.isInterface()) {
            generatedSource = buildStubInterface((JavaInterfaceSource) originalSource);

        } else if (originalSource.isEnum()) {
            // todo!
            Utils.logMessage("Enum types currently not supported... :(");

        } else if (originalSource.isAnnotation()) {
            // todo!
            Utils.logMessage("Annotation types currently not supported... :(");
        }

        return generatedSource;
    }


    private static JavaClassSource buildStubClass(JavaClassSource originalClass) {

        for (MethodSource<?> method : originalClass.getMethods()) {

            if (method.isPublic()) {
//                originalClass.removeMethod(method);
            }
        }

        return null;
    }
    private static JavaInterfaceSource buildStubInterface(JavaInterfaceSource originalSource) {
        // todo!
        return null;
    }




    /* Roaster aux methods ----------- */

//    private void setTypeHeader(JavaType<?> original, JavaType<?> generated) {
//
//    }
//
//    private void SetTypeModifiers(JavaType<?> original, JavaType<?> generated) {
//
//    }




    /* JavaFile object generation -------------------------------- */
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
