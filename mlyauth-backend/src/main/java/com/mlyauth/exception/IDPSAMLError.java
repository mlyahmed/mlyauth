package com.mlyauth.exception;

public class IDPSAMLError extends IDPException {

    private IDPSAMLError(Exception e) {
        super(e);
    }

    public static IDPSAMLError newInstance(Exception e) {
        return new IDPSAMLError(e);
    }


}
