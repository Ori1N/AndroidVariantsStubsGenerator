package com.oridev.variantsstubsgenerator.sample.utils;

import com.oridev.variantsstubsgenerator.annotation.RequiresVariantStub;

/**
 * Created by Ori on 07/12/2016.
 */
@RequiresVariantStub(flavorTo = "free")
enum TestEnum {
    VALUE_1(1), VALUE_2(2);

    TestEnum(int x) {

    }

}
