package com.oridev.variantsstubsgenerator.compiler;

import com.google.auto.service.AutoService;
import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class VariantsStubsGeneratorAnnotationProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typesUtils;
    private Filer filer;

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

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        String message = getDebugMessage(processingEnv);
        //processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
        System.out.println(message);

        // collect all the annotated classes
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RequiresVariantStub.class);
        for (Element element : elements) {
            TypeElement classElement = (TypeElement) element;

//            if (!SuperficialValidation.validateElement(element)) {
//                continue;
//            }

            // start building the java stubs file

            try {

                // Create the duplicate class
                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(classElement.getSimpleName().toString());
                Modifier[] classModifiers = classElement.getModifiers().toArray(new Modifier[classElement.getModifiers().size()]);
                classBuilder.addModifiers(classModifiers);

                for (Element classInnerElement : classElement.getEnclosedElements()) {
                    if (classInnerElement.getModifiers().contains(Modifier.PUBLIC)) {

                        if (classInnerElement.getKind() == ElementKind.METHOD) {

                            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(classInnerElement.getSimpleName().toString())
                                    .returns(Void.TYPE)
                                    .addModifiers(classInnerElement.getModifiers());

                            classBuilder.addMethod(methodBuilder.build());
                        }

                        // todo: handle non methods elements!

                    }
                }


                // write the new file
                try {
                    // now get the annotation value an the class name
                    final String flavor = element.getAnnotation(RequiresVariantStub.class).value();
                    //String name = elementUtils.getPackageOf(element).getQualifiedName().toString() + "." + element.getSimpleName().toString();


                    String classPackage = classElement.getQualifiedName().toString();
                    classPackage = classPackage.substring(0, classPackage.lastIndexOf('.')); // todo: handle inner classes!

                    JavaFile file = JavaFile.builder(classPackage, classBuilder.build())
                            .addFileComment("Generated code from annotation compiler. Do not modify!")
                            .build();
                    file.writeTo(filer);

                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Unable to write register %s",
                            e.getMessage()), null);
                }
            } catch (NullPointerException e) {

            }

        }

        return true;
    }


    private String getDebugMessage(ProcessingEnvironment env) {
//        String text = "";
//        for (Map.Entry<String, String> entry : env.getOptions().entrySet()) {
//            text += entry.getKey() + ": " + entry.getValue() + "\n";
//        }
//        return text;
        return env.getOptions().get("release");
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
