package com.oridev.variantsstubsgenerator.sample;

import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub(flavorFrom = BuildConfig.FLAVOR, flavorTo = "flavor1", throwException = true)
public class Flavor2SpecificFunctionality {

    public static String getFlavor2MessageOrThrow() {
        return "this is flavor2 real method";
    }

}
