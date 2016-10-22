package com.oridev.variantsstubsgenerator;

import com.oridev.variantsstubsgenerator.compiler.VariantsStubsGeneratorAnnotationProcessor;

import javax.tools.Diagnostic;

/**
 * Created by Ori on 03/09/2016.
 */
public class Utils {

    public static void logMessage(Diagnostic.Kind kind, String message) {
        logMessage(kind, message, false);
    }

    public static void logMessage(Diagnostic.Kind kind, String message, boolean releaseMode) {
        VariantsStubsGeneratorAnnotationProcessor.logMessage(kind, message, releaseMode);
    }

}
