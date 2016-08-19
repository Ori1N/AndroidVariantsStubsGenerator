package com.oridev.variantsstubsgenerator.sample;

import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;


/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub("flavor2")
public class Flavor1SpecificFunctionality {

    public static String getFlavor1Message() {
        return "this is flavor1";
    }

}
