package com.oridev.variantsstubsgenerator.sample.utils;

import android.util.Log;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 23/10/2016.
 */
@RequiresVariantStub(flavorTo = AppConfig.BuildTypes.RELEASE)
public class Logger {

    public static void logMessage(String tag, String message) {
        Log.d(tag, message);
    }

}
