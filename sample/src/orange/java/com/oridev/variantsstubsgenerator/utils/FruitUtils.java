package com.oridev.variantsstubsgenerator.utils;


import android.content.Context;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;
import com.oridev.variantsstubsgenerator.sample.BuildConfig;
import com.oridev.variantsstubsgenerator.sample.R;
import com.oridev.variantsstubsgenerator.sample.utils.AppConfig;


/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub(
        // flavorFrom is current flavor, meaning we can use BuildConfig.FLAVOR_{dimension}
        flavorFrom = BuildConfig.FLAVOR_dimenFruit,
        // flavorTo is the flavor we want stubs to be generated to.
        // When using complicated flavors system I prefer using custom defined constants
        flavorTo = AppConfig.DimenFruit.APPLE)
public class FruitUtils {

    public static String getPulpMessage(Context context) {
        return context.getString(R.string.message_orange_success);
    }

    public static int publicMethod(int x, float y) {
        return 0;
        // For each public method in annotated class, a stub method will be generated
    }

    private static void privateMethod(int x) {
        // For private methods stubs are not necessary
    }

}
