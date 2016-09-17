package com.oridev.variantsstubsgenerator.sample;

import android.content.Context;
import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 18/08/2016.
 */
//@RequiresVariantStub(flavorFrom = "flavor2", flavorTo = "flavor1")
public class Flavor2SpecificFunctionality {

    public static String getFlavor2Message(Context context) {
        return context.getString(R.string.message_flavor2_success);
    }

}
