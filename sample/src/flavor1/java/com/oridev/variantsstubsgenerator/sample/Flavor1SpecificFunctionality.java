package com.oridev.variantsstubsgenerator.sample;

import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;


/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub("flavor2")
public class Flavor1SpecificFunctionality {

    private static final String TAG = Flavor1SpecificFunctionality.class.getSimpleName();

    public static void func1() {
        Log.d(TAG, "func1 called");
    }

}
