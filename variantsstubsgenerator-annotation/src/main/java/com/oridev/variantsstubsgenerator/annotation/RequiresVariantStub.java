package com.oridev.variantsstubsgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this class requires stubs in different flavor.
 * This annotation generates java files containing stubs of all the public methods.
 * The return value is default (null, 0, false).
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RequiresVariantStub {

    /**
     * The name of the current flavor.
     * \nIf not using flavor dimension then this should be {@code BuildConfig.FLAVOR }.
     * \nIf using flavor dimensions then this should be {@code BuildConfig.FLAVOR_{specific-flavor}}
     */
    String flavorFrom();

    /**
     * The name of the flavor we want to generate the stub class into.
     */
    String flavorTo();


    boolean throwException() default false;

}
