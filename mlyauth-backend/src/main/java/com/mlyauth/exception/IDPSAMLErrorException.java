package com.mlyauth.exception;

public class IDPSAMLErrorException extends IDPException {

    private IDPSAMLErrorException(Exception e) {
        super(e);
    }

    public static IDPSAMLErrorException newInstance(Exception e) {
        return new IDPSAMLErrorException(e);
    }


}
