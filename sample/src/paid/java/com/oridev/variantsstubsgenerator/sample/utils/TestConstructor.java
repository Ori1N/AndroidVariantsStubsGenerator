package com.oridev.variantsstubsgenerator.sample.utils;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 10/12/2016.
 */
@RequiresVariantStub(flavorTo = "free")
public class TestConstructor {

    protected TestConstructor(int param) {
        // some functionality...
    }

}
