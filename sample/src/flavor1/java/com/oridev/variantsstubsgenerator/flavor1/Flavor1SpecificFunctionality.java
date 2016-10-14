package com.oridev.variantsstubsgenerator.flavor1;


import android.content.Context;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.sample.BuildConfig;
import com.oridev.variantsstubsgenerator.sample.R;


/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub(flavorFrom = BuildConfig.FLAVOR, flavorTo = "flavor2")
public class Flavor1SpecificFunctionality {

    public static String getFlavor1Message(Context context) {
        return context.getString(R.string.message_flavor1_success);
    }

    private static void privateMethod(int x) {
        // For private methods stubs are not necessary
    }

    public static void publicMethod(int x, float y) {
        // For each public method in annotated class, a stub method will be generated
    }

}
