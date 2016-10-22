package com.oridev.variantsstubsgenerator.exception;


/**
 * Created by Ori on 20/08/2016.
 */
public class AttemptToUseStubException extends UnsupportedOperationException {

    public AttemptToUseStubException(String info) {
        super("Attempt to use generated stub!\n" + info);
    }

}
