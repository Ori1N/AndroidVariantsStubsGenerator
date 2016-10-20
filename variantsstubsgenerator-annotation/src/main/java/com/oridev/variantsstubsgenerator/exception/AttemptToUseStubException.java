package com.oridev.variantsstubsgenerator.exception;


/**
 * Created by Ori on 20/08/2016.
 */
public class AttemptToUseStubException extends UnsupportedOperationException {

    public AttemptToUseStubException(String flavorTo) {
        super("Attempt to use flavor [" + flavorTo + "] stub!");
    }

}
