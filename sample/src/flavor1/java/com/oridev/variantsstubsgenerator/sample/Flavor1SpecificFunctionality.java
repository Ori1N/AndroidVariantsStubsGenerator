package com.oridev.variantsstubsgenerator.sample;


import android.content.Context;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;


/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub(flavorFrom = "flavor1", flavorTo = "flavor2")
public class Flavor1SpecificFunctionality {

    public static String getFlavor1Message(Context context) {
        return context.getString(R.string.message_flavor1_success);
    }

    private static void privateMethod(int x) {

    }

    public static int publicMethod(int x, float y) {
        return x  + (int) y;
    }

}
