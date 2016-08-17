package com.oridev.variantsstubsgenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Ori on 18/08/2016.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RequiresVariantStub {
    String value();
}
