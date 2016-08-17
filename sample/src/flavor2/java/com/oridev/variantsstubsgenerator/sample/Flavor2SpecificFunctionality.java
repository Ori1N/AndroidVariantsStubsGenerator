package com.oridev.variantsstubsgenerator.sample;

import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 18/08/2016.
 */
@RequiresVariantStub("flavor1")
public class FlavorSpecificFunctionality {

    private static final String TAG = FlavorSpecificFunctionality.class.getSimpleName();

    public static void func2() {
        Log.d(TAG, "func2 called");
    }

}
